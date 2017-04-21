using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class GyroMouseController : MoverioController
  {
    /** Behaviour changes whether it's connected or standalone, and according to the reference frame
     *  -- the change is controlled from the SceneUI script
     * 							  ViewCentric		   BodyCentric		WorldCentric
     *  Connected			LockCamera			 UnlockCamera		UnlockCamera
     *  StandAlone		LockCamera			 UnlockCamera		UnlockCamera
     */
    public enum BehaviourType { LockCamera, UnlockCamera }
    public BehaviourType Behaviour = BehaviourType.LockCamera;

    public GUITexture GyroPointer;
    public Color RayColor = Color.yellow;

    private Quaternion lastRotationInv;
    
    private GameObject NeckJoint;
    private GameObject EPSONcamera;
    private Camera RightCamera;
    private Ray SelectionRay;
    private LineRenderer SelectionRayRenderer;

    ~GyroMouseController()
    {
      Debug.Log("Destroying the GyroMouseController");
    }

    void Awake()
    {
      if (GyroPointer == null)
        GyroPointer = gameObject.GetComponent<GUITexture>();

      NeckJoint = transform.parent.parent.FindChild("HeadControl/NeckJoint").gameObject;
      EPSONcamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      RightCamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera/rightCam").gameObject.GetComponent<Camera>();

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

    //readings below this number will be considered zero
    public float IMUNoiseFactor = 0.00125f;
    //the following are the multipliers for width and height
    public float MultiplierX = 540f * 2.0f;
    public float MultiplierY = 540f * 2.0f;

    // Update is called once per frame
    void Update()
    {
      if (!RunLocal)
        return;

      //fixes the location of the crosshair image
      GyroPointer.transform.position = new Vector3(0.5f, 0.5f, 0.0f);

      //Gathers interaction data locally 
      Quaternion rotation = RotationProvider.Instance.Rotation;
      Quaternion headRotation = RotationProvider.Instance.HeadRotation;

      //Calls update method locally
      MoveTo(rotation);
      MoveHead(headRotation);

      //Calls update method remotely
      if (Network.isClient)
      {
        networkView.RPC("MoveTo", RPCMode.Others, rotation);
        networkView.RPC("MoveHead", RPCMode.Others, headRotation);
      }
    }

    [RPC]
    private void MoveTo(Quaternion rotation)
    {
      if (!IsCurrentController)
        return;

      Vector3 cursorPosition = CalculateGyroCursorPosition(rotation);

      Rect pointer = GyroPointer.pixelInset;
      pointer.x = cursorPosition.x;
      pointer.y = cursorPosition.y;
      GyroPointer.pixelInset = pointer;

      if (Network.isClient)
      {
        CheckHovers();
        DrawSelectionRay();
      }
    }

    // Use to move head orientation when camera is unlocked
    [RPC]
    private void MoveHead(Quaternion rotation)
    {
      if (!IsCurrentController)
        return;

      switch (Behaviour)
      {
        case BehaviourType.LockCamera:
          break;
        case BehaviourType.UnlockCamera:
          NeckJoint.transform.localRotation = rotation;
          break;
      }
    }

    private Vector3 CalculateGyroCursorPosition(Quaternion rotation)
    {
      Quaternion rotationDiff = lastRotationInv * rotation;
      lastRotationInv = Quaternion.Inverse(rotation);

      Vector3 rotPosNeg = RotationProvider.RotAsPosNeg(rotationDiff);

      Rect pointerLocation = GyroPointer.pixelInset;
      pointerLocation.x += MultiplierX * CDFunction(rotPosNeg.y);
      pointerLocation.y += MultiplierY * CDFunction(rotPosNeg.x * -1);

      //limits
      Vector2 minValues = new Vector2(-1 * (Screen.width / 4 + GyroPointer.pixelInset.width / 2),
                                      -1 * (Screen.height / 2 + GyroPointer.pixelInset.height / 2));
      Vector2 maxValues = new Vector2(Screen.width / 4 - GyroPointer.pixelInset.width / 2,
                                      Screen.height / 2 - GyroPointer.pixelInset.height / 2);

      Rect boundedPointerLocation = pointerLocation;
      boundedPointerLocation.x = Mathf.Max(minValues.x, Mathf.Min(maxValues.x, pointerLocation.x));
      boundedPointerLocation.y = Mathf.Max(minValues.y, Mathf.Min(maxValues.y, pointerLocation.y));

      return boundedPointerLocation.position;
    }

    float CDFunction(float angleDiff)
    {
      float sign = Mathf.Sign(angleDiff);
      float val = Mathf.Abs(angleDiff);
      float cdCorrectedVal = Mathf.Atan(val * Mathf.Deg2Rad - IMUNoiseFactor);

      return sign * Mathf.Max(0f, cdCorrectedVal);
    }

    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();

    void CheckHovers()
    {
      Collider target = GetFirstAffectedTarget();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
      args.Device = ControllerType.GyroMouse;
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
        args.Device = ControllerType.GyroMouse;
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

    void OnTouchStarted(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal)
        return;

      CheckSelections(args);
    }

    void CheckSelections(MoverioTouchpadEventArgs mieArgs)
    {
      Collider target = GetFirstAffectedTarget();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
      args.Device = ControllerType.GyroMouse;
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

    void OnRotationBaselineSet()
    {
      if (!RunLocal)
        return;

      Quaternion rotation = RotationProvider.Instance.Rotation;

      //SetDefaultRotation();
      SetDefaultPointerPosition(rotation);

      if (Network.isClient)
        networkView.RPC("SetDefaultPointerPosition", RPCMode.Others, rotation);
    }

    [RPC]
    public void SetDefaultPointerPosition(Quaternion rotation)
    {
      lastRotationInv = Quaternion.Inverse(rotation);

      Rect pointer = GyroPointer.pixelInset;
      pointer.x = -1 * pointer.width / 2;
      pointer.y = -1 * pointer.height / 2;

      GyroPointer.pixelInset = pointer;
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
      origin.x = origin.x + GyroPointer.pixelInset.center.x;
      origin.y = origin.y + GyroPointer.pixelInset.center.y;
      return origin;
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

    void OnReferenceFrameUpdated(ReferenceFrame refFrame)
    {
      if (!RunLocal)
        return;

      SetDefaultRotation();

      /** Behaviour changes whether it's connected or standalone, and according to the reference frame
     *  -- the change is controlled from the SceneUI script
     * 							  ViewCentric		   BodyCentric		WorldCentric
     *  Connected			LockCamera			 UnlockCamera		UnlockCamera
     *  StandAlone		LockCamera			 UnlockCamera		UnlockCamera
     */

      if (refFrame == ReferenceFrame.View)
        Behaviour = BehaviourType.LockCamera;
      else
        Behaviour = BehaviourType.UnlockCamera;
    }

    override public void OnRunLocal()
    {
      GyroPointer.enabled = runLocal;

      if (runLocal)
        MoverioInputProvider.Instance.TreatMovementAsTouch = true;
      else
        DrawSelectionRay(Vector3.zero, Vector3.zero);
    }

  }

}

  