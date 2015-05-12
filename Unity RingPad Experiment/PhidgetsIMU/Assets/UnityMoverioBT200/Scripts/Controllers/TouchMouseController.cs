using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class TouchMouseController : MoverioController
  {

    private GameObject EPSONcamera;
    private Camera LeftCamera;
    private Camera RightCamera;

    private Ray SelectionRay;
    private LineRenderer SelectionRayRenderer;

    private Vector3 mousePosition;

    public Color RayColor = Color.cyan;

    ~TouchMouseController()
    {
      Debug.Log("Destroying the TouchMouseController");
    }

    void Awake()
    {
      EPSONcamera = transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      RightCamera = transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera/rightCam").gameObject.GetComponent<Camera>();
      LeftCamera = transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera/leftCam").gameObject.GetComponent<Camera>();

      PrepareSelectionRay();
    }

    void Update()
    {
      if (!RunLocal)
        return;

      //Gathers data locally
      Vector3 localMP = Input.mousePosition;

      //Calls execution method locally
      MoveTo(localMP);

      //Calls execution method remotely
      if (Network.isClient)
        networkView.RPC("MoveTo", RPCMode.Others, localMP);
    }

    [RPC]
    private void MoveTo(Vector3 position)
    {
      if (!IsCurrentController)
        return;

      mousePosition = position;

      CheckHovers();
      DrawSelectionRay();
    }

    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();

    void CheckHovers()
    {
      Collider target = GetFirstAffectedTarget();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
      args.Device = ControllerType.TouchPad;
      args.IsConflict = false;
      args.PointerPx = mousePosition;
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
        args.Device = ControllerType.TouchPad;
        args.IsConflict = false;
        args.PointerPx = mousePosition;
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
      args.Device = ControllerType.TouchPad;
      args.IsConflict = false;
      args.PointerPx = mousePosition;
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
      Camera cam = LeftCamera;
      Vector3 position = mousePosition;
      if (position.x > (Screen.width / 2))
        cam = RightCamera;

      SelectionRay = cam.ScreenPointToRay(position);
      RaycastHit hit;
      Physics.Raycast(SelectionRay, out hit);

      if (hit.collider != null && hit.collider.tag.CompareTo("Target") == 0)
        return hit.collider;
      return null;
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

    override public void OnRunLocal()
    {
      if (runLocal)
        MoverioInputProvider.Instance.TreatMovementAsTouch = false;
      else
        DrawSelectionRay(Vector3.zero, Vector3.zero);
    }

  }

}