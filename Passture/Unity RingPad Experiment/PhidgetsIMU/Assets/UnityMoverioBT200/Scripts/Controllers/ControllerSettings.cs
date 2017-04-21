//Edited Controller.
//Added RingPad to ControllerType
//Added ability to change controller type using 1-4

using UnityEngine;
using System.Collections;
using System;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Controllers;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public enum ControllerType { Raycast, TouchPad, GyroMouse, HandGesture, Head} //, RingPad }

  public class ControllerSettings : MonoBehaviour
  {
    public bool lockCursor = false;

    public bool IsActiveUser { get; set; }

    public ControllerType StartController = ControllerType.Raycast;

    private ControllerType controllerType = ControllerType.Raycast;
    public ControllerType ControllerType { get { return controllerType; } }

    public MoverioController Controller
    {
      get
      {
        switch (controllerType)
        {
          case Controllers.ControllerType.Raycast:
            return RaycastController;
          case Controllers.ControllerType.TouchPad:
            return TouchMouseController;
          case Controllers.ControllerType.GyroMouse:
            return GyroMouseController;
          case Controllers.ControllerType.Head:
            return HeadController;
          case Controllers.ControllerType.HandGesture:
            return HandGestureController;
         
          
          //case Controllers.ControllerType.RingPad:
          //  return RingPadController;
          
          default:
            return null;
        }
      }
    }

    public bool ShowGUI;

    public RaycastController RaycastController;
    public TouchMouseController TouchMouseController;
    public GyroMouseController GyroMouseController;
    public HandGestureController HandGestureController;
    public HeadController HeadController;
    
    
    //public RingPadController RingPadController;

    ~ControllerSettings()
    {
      Debug.Log("Destroying the ControllerSettings");
    }

    // Use this for initialization
    void Awake()
    {
      if (RaycastController == null)
        RaycastController = transform.GetComponentInChildren<RaycastController>();
      if (TouchMouseController == null)
        TouchMouseController = transform.GetComponent<TouchMouseController>();
      if (GyroMouseController == null)
        GyroMouseController = transform.GetComponentInChildren<GyroMouseController>();
      if (HandGestureController == null)
        HandGestureController = transform.GetComponentInChildren<HandGestureController>();
      if (HeadController == null)
        HeadController = transform.GetComponentInChildren<HeadController>();


      //if (RingPadController == null)
      //  RingPadController = transform.GetComponentInChildren<RingPadController>();

      RaycastController.Settings = this;
      TouchMouseController.Settings = this;
      GyroMouseController.Settings = this;
      HandGestureController.Settings = this;
      HeadController.Settings = this;


       //RingPadController.Settings = this;

      MessageBroker.LoadBaseObjects();
    }

    void Start()
    {
      SetCurrentController(StartController);
    }

    // Adding ability to change cursor types with keyboard.
    void FixedUpdate()
    {
      ControllerType controller = ControllerType.Raycast;
      bool changed = false;
      if (Input.GetKeyDown(KeyCode.Alpha1))
      {
        controller = ControllerType.Raycast;
        changed = true;
      }
      if (Input.GetKeyDown(KeyCode.Alpha2))
      {
        controller = ControllerType.GyroMouse;
        changed = true;
      }
      if (Input.GetKeyDown(KeyCode.Alpha3))
      {
        controller = ControllerType.TouchPad;
        changed = true;
      }
      if (Input.GetKeyDown(KeyCode.Alpha4))
      {
        controller = ControllerType.Head;
        changed = true;
      }


      //if (Input.GetKeyDown(KeyCode.Alpha4))
      //{
      //  controller = ControllerType.RingPad;
      //  changed = true;
      //  //Screen.lockCursor = lockCursor;
      //}


      // Keeps from setting a new controller every update which causes flickering.
      if (changed)
      {
        SetCurrentController(controller);
        if (Network.isClient || Network.isServer)
          networkView.RPC("SynchCurrentController", RPCMode.OthersBuffered, controller.ToString());
      }
    }

    void OnGUI()
    {
      if (!ShowGUI || !IsActiveUser)
        return;

      System.Array controllerTypes = System.Enum.GetValues(typeof(ControllerType));
      int cHeight = 25 + 25 * controllerTypes.Length + 110;
      GUILayout.BeginArea(new Rect(0, 60, 100, cHeight));
      foreach (ControllerType controller in controllerTypes)
      {
        bool isCurrent = ControllerType == controller;
        if (GUILayout.Toggle(isCurrent,
                             controller.ToString(),
                             GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
        {
          //calls both the remote and the local method
          SetCurrentController(controller);

          if (Network.isClient || Network.isServer)
            networkView.RPC("SynchCurrentController", RPCMode.OthersBuffered, controller.ToString());
        }
      }
      GUILayout.EndArea();
    }

    [RPC]
    void SynchCurrentController(String newControllerS)
    {
      ControllerType newController = (ControllerType)System.Enum.Parse(typeof(ControllerType), newControllerS);
      SetCurrentController(newController);
    }

    public void SetCurrentController(ControllerType newController)
    {
      controllerType = newController;

      RaycastController.RunLocal = false;
      TouchMouseController.RunLocal = false;
      GyroMouseController.RunLocal = false;
      HandGestureController.RunLocal = false;
      HeadController.RunLocal = false;


      //RingPadController.RunLocal = false;

      switch (ControllerType)
      {
        case ControllerType.Raycast:
          //RotationProvider.Instance.SetSourceIMU(RotationProvider.SensorMode.Controller);
          RaycastController.RunLocal = IsActiveUser && !Network.isServer;
          break;
        case ControllerType.GyroMouse:
          //RotationProvider.Instance.SetSourceIMU(RotationProvider.SensorMode.Controller);
          GyroMouseController.RunLocal = IsActiveUser && !Network.isServer;
          break;
        case ControllerType.Head:
          // RotationProvider.Instance.SetSourceIMU(RotationProvider.SensorMode.Headset);
          HeadController.RunLocal = IsActiveUser && !Network.isServer;
          break;
        case ControllerType.HandGesture:
          HandGestureController.RunLocal = IsActiveUser && !Network.isServer;
          break;
        case ControllerType.TouchPad:
          TouchMouseController.RunLocal = IsActiveUser && !Network.isServer;
          break;


        //case ControllerType.RingPad:
        //  RingPadController.RunLocal = IsActiveUser && !Network.isServer;
        //  break;

        default:
          break;
      }

      HeadController.SetDefaults();
      MoverioInputProvider.Instance.LoadScripts();
    }

    public bool IsCursorBasedController //As opposed to rotation-based controller as in raycasting or head (OnBody, OnWorld)
    {
      get
      {
        switch (ControllerType)
        {
          case ControllerType.GyroMouse:
          case ControllerType.TouchPad:

          //case ControllerType.RingPad:
            return true;

          case ControllerType.Raycast:
          case ControllerType.HandGesture:
            return false;
          case ControllerType.Head:
            {
              if (HeadController.Behaviour == HeadController.BehaviourType.GyroMouse)
                return true;
              return false;
            }
          default:
            return false;
        }
      }
    }

  }

}