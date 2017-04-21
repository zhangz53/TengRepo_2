/*
 * Does not change camera orientation using the Glasses IMU, but will change using
 * the external rotation. Implementation of Raycast/GyroCursor with Glasses IMU
 * head orientation functionality works though. ????
 */

using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{
  public class HeadRotationController : MonoBehaviour
  {
    /** Behaviour changes whether it's connected or standalone, and according to the reference frame
    *  -- the change is controlled from the SceneUI script
    * 							  ViewCentric		   BodyCentric		WorldCentric
    *  Connected			LockCamera			 UnlockCamera		UnlockCamera
    *  StandAlone		LockCamera			 UnlockCamera		UnlockCamera
    */
    public enum BehaviourType { LockCamera, UnlockCamera }
    public BehaviourType Behaviour = BehaviourType.LockCamera;

    
    private GameObject NeckJoint;
    private GameObject EPSONcamera;
    private Camera RightCamera;

    ~HeadRotationController()
    {
      Debug.Log("Destroying the HeadRotationController");
    }

    // Use this for initialization
    void Awake()
    { 
      NeckJoint = transform.FindChild("NeckJoint").gameObject;
      EPSONcamera = transform.FindChild("NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
    }

    void Start()
    {
      SetDefaults();
    }

    public void SetDefaults()
    {
      SetDefaultRotation();
    }

    private void SetDefaultRotation()
    {
      // Orient the entire head
      NeckJoint.transform.localRotation = Quaternion.Euler(0f, 0f, 0f);

      Vector3 tForward = transform.TransformVector(Vector3.forward);
      //gameObject.transform.LookAt(transform.position + tForward);
    }


    public Quaternion headRotation;
    void Update()
    {
      //Gathers data locally
      Quaternion rotation = RotationProvider.Instance.HeadRotation;
      headRotation = RotationProvider.Instance.HeadRotation;

      //print(headRotation.x + " " + headRotation.y + " " + headRotation.z + " " + headRotation.w);


      //Calls update locally
      //MoveTheHead(rotation);

      ////Calls update remotely
      //if (Network.isClient)
      //{
      //  networkView.RPC("MoveTheHead", RPCMode.Others, rotation);
      //}
    }

    [RPC]
    private void MoveTheHead(Quaternion rotation)
    {
      switch (Behaviour)
      {
        case BehaviourType.LockCamera:
          break;
        case BehaviourType.UnlockCamera:
          NeckJoint.transform.localRotation = rotation;
          break;
      }
      //NeckJoint.transform.localRotation = rotation;

    }

    void OnReferenceFrameUpdated(ReferenceFrame refFrame)
    {

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
  }
}
