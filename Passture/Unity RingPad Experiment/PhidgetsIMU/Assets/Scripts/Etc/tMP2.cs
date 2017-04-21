//using UnityEngine;
//using System.Collections;
//using System.Collections.Generic;
//using UnityMoverioBT200.Scripts.Util;
//using UnityMoverioBT200.Scripts.Providers;

//namespace UnityMoverioBT200.Scripts.Controllers
//{

//  public class RingPadController : MoverioController
//  {

//    public GUITexture RingPadPointer;
//    public Color RayColor = Color.cyan;

//    private GameObject EPSONcamera;
//    private Camera LeftCamera;
//    private Camera RightCamera;

//    private Ray SelectionRay;
//    private LineRenderer SelectionRayRenderer;

//    ~RingPadController()
//    {
//      Debug.Log("Destroying the RingPadController");
//    }

//    void Awake()
//    {
//      if (RingPadPointer == null)
//        RingPadPointer = gameObject.GetComponent<GUITexture>();

//      EPSONcamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
//      RightCamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera/rightCam").gameObject.GetComponent<Camera>();

//      PrepareSelectionRay();
//    }

//    // Sensitivity multiplier
//    public float moveFactor = 1.0f;

//    public Vector2 moveVector;

//    // Update is called once per frame
//    void Update()
//    {
//      if (!RunLocal)
//        return;

//      //fixes the location of the crosshair image
//      RingPadPointer.transform.position = new Vector3(0.5f, 0.5f, 0.0f);

//      //Gathers interaction data locally? Might be taking from moverio touchpad mouse axes?
//      float x, y;
//      x = Input.GetAxis("Mouse X");
//      y = Input.GetAxis("Mouse Y");

//      moveVector = new Vector2(x * moveFactor, y * moveFactor);

//      //Calls update method locally
//      MoveTo(moveVector);

//      //Calls update method remotely
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("MoveTo", RPCMode.Others, moveVector);
//    }

//    [RPC]
//    void SyncMouseInput(Vector3 mousePosition, int mouseDownVal, float x, float y)
//    {
//      moveVector = new Vector2(x * moveFactor, y * moveFactor);

//      MoveTo(moveVector);
//      //ProcessInputFromNetworkMouse(mousePosition, mouseDown);
//      //gameObject.GetComponent<MoverioInputProvider>().ProcessInputFromMouseEvents(mousePosition, mouseDown);
//    }

//    [RPC]
//    private void MoveTo(Vector2 aVector)
//    {
//      if (!IsCurrentController)
//        return;

//      Vector2 cursorPosition = CalculateCursorPosition(aVector);

//      Rect pointer = RingPadPointer.pixelInset;
//      pointer.x = cursorPosition.x;
//      pointer.y = cursorPosition.y;
//      RingPadPointer.pixelInset = pointer;

//      CheckHovers();
//      DrawSelectionRay();
//    }

//    private Vector2 CalculateCursorPosition(Vector2 aVector)
//    {

//      Rect pointerLocation = RingPadPointer.pixelInset;
//      pointerLocation.x += moveFactor * aVector.x;
//      pointerLocation.y += moveFactor * aVector.y;

//      //limits
//      Vector2 minValues = new Vector2(-1 * (Screen.width / 4 + RingPadPointer.pixelInset.width / 2),
//                                      -1 * (Screen.height / 2 + RingPadPointer.pixelInset.height / 2));
//      Vector2 maxValues = new Vector2(Screen.width / 4 - RingPadPointer.pixelInset.width / 2,
//                                      Screen.height / 2 - RingPadPointer.pixelInset.height / 2);

//      Rect boundedPointerLocation = pointerLocation;
//      boundedPointerLocation.x = Mathf.Max(minValues.x, Mathf.Min(maxValues.x, pointerLocation.x));
//      boundedPointerLocation.y = Mathf.Max(minValues.y, Mathf.Min(maxValues.y, pointerLocation.y));

//      return boundedPointerLocation.position;
//    }


//    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();

//    void CheckHovers()
//    {
//      Collider target = GetFirstAffectedTarget();

//      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
//      args.Device = ControllerType.GyroMouse;
//      args.IsConflict = false;
//      args.PointerPx = GetScreenPoint();
//      args.PointerPos = EPSONcamera.transform.position;
//      args.PointerQuat = EPSONcamera.transform.rotation;

//      if (target != null)
//      {
//        if (hoveredTargets.ContainsKey(target.gameObject))
//          hoveredTargets[target.gameObject] = true;
//        else
//          hoveredTargets.Add(target.gameObject, true);
//        target.SendMessage("Hovered", args, SendMessageOptions.DontRequireReceiver);
//      }

//      List<GameObject> notHovered = new List<GameObject>();
//      foreach (GameObject targetObj in hoveredTargets.Keys)
//      {
//        if (!hoveredTargets[targetObj])
//          notHovered.Add(targetObj);
//      }

//      foreach (GameObject targetObj in notHovered)
//      {
//        args = new SelectionControllerEventArgs(null);
//        args.Device = ControllerType.GyroMouse;
//        args.IsConflict = false;
//        args.PointerPx = GetScreenPoint();
//        args.PointerPos = EPSONcamera.transform.position;
//        args.PointerQuat = EPSONcamera.transform.rotation;

//        targetObj.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
//        hoveredTargets.Remove(targetObj);
//      }

//      List<GameObject> keys = new List<GameObject>(hoveredTargets.Keys);
//      foreach (GameObject targetObj in keys)
//        hoveredTargets[targetObj] = false;
//    }

//    void DrawSelectionRay()
//    {
//      if (!RunLocal || SystemInfo.deviceType == DeviceType.Desktop)
//      {
//        var origin = SelectionRay.origin;
//        var direction = SelectionRay.direction;
//        var endPoint = origin + direction * 100f;

//        RaycastHit hit;
//        if (Physics.Raycast(origin, direction, out hit))
//          endPoint = hit.point;

//        DrawSelectionRay(origin, endPoint);
//      }
//    }

//    void DrawSelectionRay(Vector3 origin, Vector3 endPoint)
//    {
//      SelectionRayRenderer.SetPosition(0, origin);
//      SelectionRayRenderer.SetPosition(1, endPoint);
//    }

//    void OnTouchStarted(MoverioTouchpadEventArgs args)
//    {
//      if (!RunLocal)
//        return;

//      CheckSelections(args);
//    }

//    void CheckSelections(MoverioTouchpadEventArgs mieArgs)
//    {
//      Collider target = GetFirstAffectedTarget();

//      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
//      args.Device = ControllerType.GyroMouse;
//      args.IsConflict = false;
//      args.PointerPx = GetScreenPoint();
//      args.PointerPos = EPSONcamera.transform.position;
//      args.PointerQuat = EPSONcamera.transform.rotation;

//      if (target != null)
//        target.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
//      else
//      {
//        SelectionEventArgs seArgs = new SelectionEventArgs(args);
//        seArgs.Type = SelectionEventArgs.SelectionEventType.Selected;

//        MessageBroker.BroadcastAll("OnSelected", seArgs);
//      }
//    }

//    void OnRotationBaselineSet()
//    {
//      if (!RunLocal)
//        return;

//      Vector2 moveVector = new Vector2(RingPadPointer.transform.position.x, RingPadPointer.transform.position.y);

//      SetDefaultPointerPosition(moveVector);

//      if (Network.isClient)
//        networkView.RPC("SetDefaultPointerPosition", RPCMode.Others, moveVector);
//    }

//    [RPC]
//    public void SetDefaultPointerPosition(Vector2 aVector)
//    {

//      Rect pointer = RingPadPointer.pixelInset;
//      pointer.x = -1 * pointer.width / 2;
//      pointer.y = -1 * pointer.height / 2;

//      RingPadPointer.pixelInset = pointer;
//    }

//    Collider GetFirstAffectedTarget()
//    {
//      Vector3 origin = GetScreenPoint();

//      SelectionRay = RightCamera.ScreenPointToRay(origin);

//      RaycastHit hit;
//      Physics.Raycast(SelectionRay, out hit);

//      if (hit.collider != null && hit.collider.tag.CompareTo("Target") == 0)
//        return hit.collider;
//      return null;
//    }

//    private Vector3 GetScreenPoint()
//    {
//      Vector3 origin = new Vector3(Screen.width / 2 + Screen.width / 4, Screen.height / 2);
//      origin.x = origin.x + RingPadPointer.pixelInset.center.x;
//      origin.y = origin.y + RingPadPointer.pixelInset.center.y;
//      return origin;
//    }

//    void PrepareSelectionRay()
//    {
//      SelectionRayRenderer = gameObject.AddComponent<LineRenderer>();
//      SelectionRayRenderer.material = new Material(Shader.Find("Particles/Additive"));
//      SelectionRayRenderer.SetColors(RayColor, Color.red);
//      SelectionRayRenderer.SetWidth(0.005f, 0.005f);
//      SelectionRayRenderer.SetVertexCount(2);

//      DrawSelectionRay(Vector3.zero, Vector3.zero);
//    }

//    override public void OnRunLocal()
//    {
//      RingPadPointer.enabled = runLocal;

//      if (runLocal)
//        MoverioInputProvider.Instance.TreatMovementAsTouch = true;
//      else
//        DrawSelectionRay(Vector3.zero, Vector3.zero);
//    }

//  }

//}