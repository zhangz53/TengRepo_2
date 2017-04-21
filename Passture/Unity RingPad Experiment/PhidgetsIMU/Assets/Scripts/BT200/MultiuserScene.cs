using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;
using UnityMoverioBT200.Scripts.Controllers;
using UnityMoverioBT200.Scripts.Util;

namespace PointingMobileHMD.Scripts
{

  public abstract class MultiuserScene : MonoBehaviour
  {

    public bool ShowGUI;

    //The prefab for the target objects
    public GameObject Participant = null;
    public GameObject Viewer = null;

    public DistanceZones Zone = DistanceZones.Control;
    public ReferenceFrame RefFrame = ReferenceFrame.View;
    public TargetWidths TargetWidth = TargetWidths.W1;
    public TargetDistances TargetDistance = TargetDistances.A1;

    public GameObject WorldLocations;
    public GameObject BodyLocations;
    public GameObject ViewLocations;

    protected GameObject EPSONcamera;
    protected Camera LeftEyeCamera;
    protected Camera RightEyeCamera;

    protected ControllerSettings cSettings;

    public void Start()
    {
      if (Viewer != null)
        TurnOffUser(Viewer);

      if (Participant != null)
        TurnOnUser(Participant);
    }

    protected void TurnOffUser(GameObject user)
    {
      EPSONcamera = user.transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      EPSONcamera.SetActive(false);

      EPSONcamera = null;
      LeftEyeCamera = null;
      RightEyeCamera = null;

      cSettings = user.transform.GetComponentInChildren<ControllerSettings>();
      cSettings.IsActiveUser = false;
      cSettings.SetCurrentController(cSettings.ControllerType);
    }

    protected void TurnOnUser(GameObject user)
    {
      EPSONcamera = user.transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      EPSONcamera.SetActive(true);

      LeftEyeCamera = EPSONcamera.transform.FindChild("leftCam").GetComponent<Camera>();
      RightEyeCamera = EPSONcamera.transform.FindChild("rightCam").GetComponent<Camera>(); ;

      cSettings = user.transform.GetComponentInChildren<ControllerSettings>();
      cSettings.IsActiveUser = true;
      cSettings.SetCurrentController(cSettings.ControllerType);
    }

    void OnPlayerConnected(NetworkPlayer player)
    {
      if (Network.connections.Length > 0)
        cSettings.Controller.RunLocal = false;

      //the viewer connected, it loads the viewer objects and turns off the participant camera
      if (Network.connections.Length == 2)
        networkView.RPC("client_ActivateViewer", player);
    }

    [RPC]
    void client_ActivateViewer()
    {
      TurnOffUser(Participant);
      TurnOnUser(Viewer);
    }

    protected GameObject GetInteractionZoneGO()
    {
      GameObject refFrameGO = ViewLocations;
      if (RefFrame == ReferenceFrame.Body)
        refFrameGO = BodyLocations;
      else if (RefFrame == ReferenceFrame.World)
        refFrameGO = WorldLocations;

      GameObject zoneGO = refFrameGO.transform.FindChild("Control").gameObject;
      if (Zone == DistanceZones.Near)
        zoneGO = refFrameGO.transform.FindChild("Near").gameObject;
      else if (Zone == DistanceZones.Far)
        zoneGO = refFrameGO.transform.FindChild("Far").gameObject;
      return zoneGO;
    }


    protected Vector2 GetTargetParams()
    {
      float tWidth = 0.02f;
      float tDist = 0.12f;

      if(TargetWidth == TargetWidths.W1)
      {
        tWidth = 0.01f;
      }
      else if (TargetWidth == TargetWidths.W2)
      {
        tWidth = 0.02f;
      }
      else if (TargetWidth == TargetWidths.W3)
      {
        tWidth = 0.04f;
      }

      if (TargetDistance == TargetDistances.A1)
      {
        tDist = 0.04f;
      }
      else if (TargetDistance == TargetDistances.A2)
      {
        tDist = 0.08f;
      }
      else if (TargetDistance == TargetDistances.A3)
      {
        tDist = 0.16f;
      }
      else if (TargetDistance == TargetDistances.A4)
      {
        tDist = 0.32f;
      }


      return new Vector2(tWidth, tDist);
    }

  }

}
