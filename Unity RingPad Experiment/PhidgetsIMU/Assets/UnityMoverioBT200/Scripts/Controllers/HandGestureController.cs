using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class HandGestureController : MoverioController
  {

    public GameObject FingerIndex;

    private GameObject EPSONcamera;
    private Camera RightCamera;

    ~HandGestureController()
    {
      Debug.Log("Destroying the HandGestureController");
    }

    // Use this for initialization
    void Awake()
    {
      EPSONcamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      RightCamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera/rightCam").gameObject.GetComponent<Camera>();
    }

    // Update is called once per frame
    void Update()
    {
      if (!RunLocal)
        return;

      if (FingerIndex == null)
        return;

      //Gather locally
      Vector3 position = FingerIndex.transform.position;

      //Call the update function locally
      MoveTo(position);

      //Call the update function remotely
      if (Network.isClient)
        networkView.RPC("MoveTo", RPCMode.Others, position);
    }

    [RPC]
    private void MoveTo(Vector3 position)
    {
      //We are moving to an index finger overlap, given that the previous approach suffered from considerable Heisemberg effect. 
      // -- moreover, this approach where selection happens on the TouchPad make is comparable to the other selection methods. 
      gameObject.transform.position = position;

      CheckHovers();
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
      Collider[] targets = GetAffectedTargets();
      foreach (Collider target in targets)
      {
        SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
        args.Device = ControllerType.HandGesture;
        args.IsConflict = targets.Length > 1;
        args.PointerPx = RightCamera.WorldToScreenPoint(gameObject.transform.position);
        args.PointerPos = gameObject.transform.position;
        args.PointerQuat = gameObject.transform.rotation;

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
        SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
        args.Device = ControllerType.HandGesture;
        args.IsConflict = false;
        args.PointerPx = RightCamera.WorldToScreenPoint(gameObject.transform.position);
        args.PointerPos = gameObject.transform.position;
        args.PointerQuat = gameObject.transform.rotation;

        targetObj.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
        hoveredTargets.Remove(targetObj);
      }

      List<GameObject> keys = new List<GameObject>(hoveredTargets.Keys);
      foreach (GameObject targetObj in keys)
        hoveredTargets[targetObj] = false;
    }

    void CheckSelections(MoverioTouchpadEventArgs mieArgs)
    {
      Collider[] targets = GetAffectedTargets();

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
      args.Device = ControllerType.HandGesture;
      args.IsConflict = targets.Length > 1;
      args.PointerPx = RightCamera.WorldToScreenPoint(gameObject.transform.position);
      args.PointerPos = gameObject.transform.position;
      args.PointerQuat = gameObject.transform.rotation;

      foreach (Collider target in targets)
        target.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);

      if (targets.Length == 0 || args.IsConflict)
      {
        SelectionEventArgs seArgs = new SelectionEventArgs(args);
        seArgs.Type = SelectionEventArgs.SelectionEventType.Selected;

        MessageBroker.BroadcastAll("OnSelected", seArgs);
      }
    }

    Collider[] GetAffectedTargets()
    {
      List<Collider> targets = new List<Collider>();
      Collider[] objects = Physics.OverlapSphere(gameObject.transform.position, GetComponent<SphereCollider>().radius / 100);
      for (int index = 0; index < objects.Length; index++)
      {
        if (objects[index].tag.CompareTo("Target") != 0)
          continue;
        targets.Add(objects[index]);
      }
      return targets.ToArray();
    }

    override public void OnRunLocal()
    {
      if (runLocal)
        MoverioInputProvider.Instance.TreatMovementAsTouch = true;
    }

  }

}