using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;
//using Vicon2Unity.ViconConnector.Scripts;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class HeadController : MoverioController
  {
    /** Behaviour changes whether it's connected or standalone, and according to the reference frame
     *  -- the change is controlled from the SceneUI script
     * 							ViewCentric		BodyCentric		WorldCentric
     *  Connected			Gyro				RaycastExt		RaycastExt
     *  StandAlone		Gyro				RaycastIMU		RaycastIMU
     */
    public enum BehaviourType { GyroMouse, RaycastIMU, RaycastExternal, Camera }
    public BehaviourType Behaviour = BehaviourType.RaycastIMU;

    public GameObject LookAtObject;
    public GUITexture HeadPointer;

    public Color RayColor = Color.blue;

    private GameObject NeckJoint;
    private GameObject EPSONcamera;
    private Camera RightCamera;

    private Ray SelectionRay;
    private LineRenderer SelectionRayRenderer;

    ~HeadController()
    {
      Debug.Log("Destroying the HeadController");
    }

    // Use this for initialization
    void Awake()
    {
      NeckJoint = transform.FindChild("NeckJoint").gameObject;
      EPSONcamera = transform.FindChild("NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      RightCamera = transform.FindChild("NeckJoint/Camera BT-200/Stereoscopic Camera/rightCam").gameObject.GetComponent<Camera>();

      PrepareSelectionRay();
    }

    void Start()
    {
      SetDefaults();
    }

    public void SetDefaults()
    {
      SetDefaultRotation();
      SetDefaultPointerPosition();
    }

    private void SetDefaultRotation()
    {
      //it orientates the entire head
      NeckJoint.transform.localRotation = Quaternion.Euler(0f, 0f, 0f);

      Vector3 tForward = transform.TransformVector(Vector3.forward);
      gameObject.transform.LookAt(transform.position + tForward);
    }

    private void SetDefaultPointerPosition()
    {
      Quaternion rotation = RotationProvider.Instance.Rotation;

      SetDefaultPointerPosition(rotation);

      if (Network.isClient)
        networkView.RPC("SetDefaultPointerPosition", RPCMode.Others, rotation);
    }

    private void SetDefaultPointerPosition(Quaternion rotation)
    {
      if (Behaviour == BehaviourType.GyroMouse)
        lastRotationInv = Quaternion.Inverse(rotation);

      //Sets it to 0,0 --> the middle of the display
      Vector2 position = new Vector2(0, 0);
      Rect boundedPointerLocation = HeadPointer.pixelInset;
      boundedPointerLocation.x = position.x - boundedPointerLocation.width / 2;
      boundedPointerLocation.y = position.y - boundedPointerLocation.height / 2;

      if (RightCamera != null && LookAtObject != null)
      {
        if (Behaviour == BehaviourType.RaycastIMU || Behaviour == BehaviourType.Camera)
          NeckJoint.transform.localRotation = RotationProvider.Instance.HeadRotation;

        //Sets it to the position which is the middle of the two displays according to the target distance
        float distanceToTargets = (LookAtObject.transform.position - transform.position).magnitude;

        Ray headDirection = new Ray(EPSONcamera.transform.position, EPSONcamera.transform.forward);
        Vector3 aimingPoint = headDirection.GetPoint(distanceToTargets);
        Vector3 screenPoint = RightCamera.WorldToScreenPoint(aimingPoint);
        Vector3 middlePoint = new Vector3(Screen.width * 3 / 4, Screen.height / 2);
        Vector3 diff = screenPoint - middlePoint;
        boundedPointerLocation.x += diff.x;
        boundedPointerLocation.y += diff.y;

        ApplyMinMaxBoundaries(ref boundedPointerLocation);
      }

      HeadPointer.pixelInset = boundedPointerLocation;
    }

    void PrepareSelectionRay()
    {
      SelectionRayRenderer = gameObject.AddComponent<LineRenderer>();
      SelectionRayRenderer.material = new Material(Shader.Find("Particles/Additive"));
      SelectionRayRenderer.SetColors(RayColor, Color.red);
      SelectionRayRenderer.SetWidth(0.005f, 0.005f);
      SelectionRayRenderer.SetVertexCount(2);

      DrawSelectionRay(Vector3.zero, Vector3.zero);
    }

    // Update is called once per frame
    void Update()
    {
      if (!RunLocal)
        return;

      //fixes the location of the crosshair image
      HeadPointer.transform.position = new Vector3(0.5f, 0.5f, 0.0f);

      //Gathers data locally
      Quaternion headRotation = RotationProvider.Instance.HeadRotation;
      Quaternion extRotation = RotationProvider.Instance.Rotation;

      //Calls the update locally
      //Test calling separate functions for each rotation. Result: No flicker..
      MoveTo(extRotation);
      MoveHead(headRotation);

      //Calls the update remotely
      if (Network.isClient)
      {
        networkView.RPC("MoveTo", RPCMode.Others, extRotation);
        networkView.RPC("MoveHead", RPCMode.Others, headRotation);
      }
    }

    //[RPC]
    private void MoveTo(Quaternion rotation)
    {
      if (!IsCurrentController)
        return;

      switch (Behaviour)
      {
        case BehaviourType.GyroMouse:
          ComputeGyroPointer(rotation);
          break;
        case BehaviourType.RaycastIMU:
          ComputeGyroPointer(rotation);
          //NeckJoint.transform.localRotation = rotation;
          break;
        case BehaviourType.RaycastExternal:
          //The neckjoint orientation is set by the optical tracker script (Vicon or OptiTrack)
          break;
        case BehaviourType.Camera:
          ComputeGyroPointer(rotation);
          //NeckJoint.transform.localRotation = rotation;
          //The neckjoint orientation is set by the optical tracker script (Vicon or OptiTrack)
          break;

      }

      //Cursor on screen occasionally hovers different targets. Keeps this from happening.
      if (Network.isClient)
      {
        CheckHovers();
        DrawSelectionRay();
      }
    }

    [RPC]
    private void MoveHead(Quaternion rotation)
    {
      //if (!IsCurrentController)
      //  return;

      switch (Behaviour)
      {
        case BehaviourType.GyroMouse:
          //ComputeGyroPointer(rotation);
          break;
        case BehaviourType.RaycastIMU:
          NeckJoint.transform.localRotation = rotation;
          break;
        case BehaviourType.RaycastExternal:
          //The neckjoint orientation is set by the optical tracker script (Vicon or OptiTrack)
          break;
        case BehaviourType.Camera:
          NeckJoint.transform.localRotation = rotation;
          //The neckjoint orientation is set by the optical tracker script (Vicon or OptiTrack)
          break;

      }
    }

    //readings below this number will be considered zero
    public float IMUNoiseFactor = 0.00125f;
    //the following are the multipliers for width and height
    public float MultiplierX = 540f * 2.0f;
    public float MultiplierY = 540f * 2.0f;

    private Quaternion lastRotationInv;

    void ComputeGyroPointer(Quaternion rotation)
    {
      Quaternion rotationDiff = lastRotationInv * rotation;
      lastRotationInv = Quaternion.Inverse(rotation);

      Vector3 rotPosNeg = RotationProvider.RotAsPosNeg(rotationDiff);

      Rect pointerLocation = HeadPointer.pixelInset;
      pointerLocation.x += MultiplierX * CDFunction(rotPosNeg.y);
      pointerLocation.y += MultiplierY * CDFunction(rotPosNeg.x * -1);
      ApplyMinMaxBoundaries(ref pointerLocation);

      HeadPointer.pixelInset = pointerLocation;
    }

    private void ApplyMinMaxBoundaries(ref Rect pointer)
    {
      Vector2 minValues = new Vector2(-1 * (Screen.width / 4 + HeadPointer.pixelInset.width / 2),
                                       -1 * (Screen.height / 2 + HeadPointer.pixelInset.height / 2));
      Vector2 maxValues = new Vector2(Screen.width / 4 - HeadPointer.pixelInset.width / 2,
                                       Screen.height / 2 - HeadPointer.pixelInset.height / 2);

      pointer.x = Mathf.Max(minValues.x, Mathf.Min(maxValues.x, pointer.x));
      pointer.y = Mathf.Max(minValues.y, Mathf.Min(maxValues.y, pointer.y));
    }

    float CDFunction(float angleDiff)
    {
      float sign = Mathf.Sign(angleDiff);
      float val = Mathf.Abs(angleDiff);
      float cdCorrectedVal = Mathf.Atan(val * Mathf.Deg2Rad - IMUNoiseFactor);

      return sign * Mathf.Max(0f, cdCorrectedVal);
    }

    void OnRotationBaselineSet()
    {
      if (!RunLocal || !IsCurrentController)
        return;

      SetDefaultPointerPosition();
    }

    void OnReferenceFrameUpdated(ReferenceFrame refFrame)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      SetDefaultPointerPosition();

      /** Behaviour changes whether it's connected or standalone, and according to the reference frame
       *  -- the change is controlled from the SceneUI script
       * 							ViewCentric		BodyCentric		WorldCentric
       *  Connected			Gyro				RaycastExt		RaycastExt
       *  StandAlone		Gyro				RaycastIMU		RaycastIMU
       */
      if (refFrame == ReferenceFrame.View)
        Behaviour = BehaviourType.GyroMouse;
      //else if (Network.isClient)
      //  Behaviour = BehaviourType.Camera;
      else
        Behaviour = BehaviourType.Camera;
    }

    void OnTouchStarted(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal)
        return;

      CheckSelections(args);
    }

    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();

    void CheckHovers()
    {
      Collider target = GetFirstAffectedTarget();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
      args.Device = ControllerType.Head;
      args.IsConflict = false;
      args.PointerPx = GetScreenPoint();
      args.PointerPos = EPSONcamera.transform.position;
      args.PointerQuat = EPSONcamera.transform.rotation;

      if (target != null)
      {
        if (hoveredTargets.ContainsKey(target.gameObject))
          hoveredTargets[target.gameObject] = true;
        else
          hoveredTargets.Add(target.gameObject, true);
        target.SendMessage("Hovered", args, SendMessageOptions.DontRequireReceiver);
      }

      List<GameObject> notHovered = new List<GameObject>();
      foreach (GameObject targetObj in hoveredTargets.Keys)
      {
        if (!hoveredTargets[targetObj])
          notHovered.Add(targetObj);
      }

      foreach (GameObject targetObj in notHovered)
      {
        args = new SelectionControllerEventArgs(null);
        args.Device = ControllerType.Head;
        args.IsConflict = false;
        args.PointerPx = GetScreenPoint();
        args.PointerPos = EPSONcamera.transform.position;
        args.PointerQuat = EPSONcamera.transform.rotation;

        targetObj.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
        hoveredTargets.Remove(targetObj);
      }

      List<GameObject> keys = new List<GameObject>(hoveredTargets.Keys);
      foreach (GameObject targetObj in keys)
        hoveredTargets[targetObj] = false;
    }

    void CheckSelections(MoverioTouchpadEventArgs mieArgs)
    {
      Collider target = GetFirstAffectedTarget();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
      args.Device = ControllerType.Head;
      args.IsConflict = false;
      args.PointerPx = GetScreenPoint();
      args.PointerPos = EPSONcamera.transform.position;
      args.PointerQuat = EPSONcamera.transform.rotation;

      if (target != null)
        target.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
      else
      {
        SelectionEventArgs seArgs = new SelectionEventArgs(args);
        seArgs.Type = SelectionEventArgs.SelectionEventType.Selected;

        MessageBroker.BroadcastAll("OnSelected", seArgs);
      }
    }

    Collider GetFirstAffectedTarget()
    {
      Vector3 origin = GetScreenPoint();
      SelectionRay = RightCamera.ScreenPointToRay(origin);

      RaycastHit hit;
      Physics.Raycast(SelectionRay, out hit);
      if (hit.collider != null && hit.collider.tag.CompareTo("Target") == 0)
        return hit.collider;
      return null;
    }

    private Vector3 GetScreenPoint()
    {
      Vector3 origin = new Vector3(Screen.width / 2 + Screen.width / 4, Screen.height / 2);
      origin.x = origin.x + HeadPointer.pixelInset.center.x;
      origin.y = origin.y + HeadPointer.pixelInset.center.y;
      return origin;
    }

    void DrawSelectionRay()
    {
      if (!RunLocal || SystemInfo.deviceType == DeviceType.Desktop)
      {
        var origin = SelectionRay.origin;
        var direction = SelectionRay.direction;
        var endPoint = origin + direction * 100f;

        RaycastHit hit;
        if (Physics.Raycast(origin, direction, out hit))
          endPoint = hit.point;

        DrawSelectionRay(origin, endPoint);
      }
    }

    void DrawSelectionRay(Vector3 origin, Vector3 endPoint)
    {
      SelectionRayRenderer.SetPosition(0, origin);
      SelectionRayRenderer.SetPosition(1, endPoint);
    }

    override public void OnRunLocal()
    {
      HeadPointer.enabled = runLocal;

      if (runLocal)
        MoverioInputProvider.Instance.TreatMovementAsTouch = true;
      else
        DrawSelectionRay(Vector3.zero, Vector3.zero);
    }

  }

}