//using UnityEngine;
//using System.Collections;
//using System.Collections.Generic;
//using UnityMoverioBT200.Scripts;

////using Vicon2Unity.ViconConnector.Scripts;
//using System.IO;
//using UnityMoverioBT200.Scripts.Util;
//using UnityMoverioBT200.Scripts.Providers;
//using UnityMoverioBT200.Scripts.Controllers;

//namespace UnityMoverioBT200.Scripts.Scenes
//{

//  public class DemoDisambiguation : MonoBehaviour
//  {

//    //The prefab for the target objects
//    public GameObject Target = null;

//    public GameObject Depth1;
//    public GameObject Depth2;
//    public GameObject Depth3;
//    private GameObject DepthPlane;

//    public GameObject participant;

//    private ControllerSettings cSettings;
//    private GameObject BT200camera;
//    private Camera LeftEyeCamera;

//    private Vector3 OffScreenStart = Vector3.zero;
//    private Vector3 OffScreenEnd = Vector3.zero;
//    private LineRenderer OffScreenRay;

//    ~DemoDisambiguation()
//    {
//      Debug.Log("Destroying the DemoDisambiguation");
//    }

//    void Start()
//    {
//      DepthPlane = Depth1;
//      TurnOnUser(participant);

//      CommToAndroid.Instance.ShowGUI = true;
//      NetworkProvider.Instance.ShowGUI = true;
//      RotationProvider.Instance.ShowGUI = true;
//      cSettings.RaycastController.ShowGUI = true;

//      PrepareOffScreenRay();
//      Target.transform.position = Vector3.zero;
//      Random.seed = (int)(System.DateTime.Now.Ticks % 1000);
//    }

//    private void TurnOnUser(GameObject user)
//    {
//      BT200camera = user.transform.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
//      LeftEyeCamera = BT200camera.transform.FindChild("leftCam").GetComponent<Camera>();
//      cSettings = user.transform.GetComponentInChildren<ControllerSettings>();

//      BT200camera.SetActive(true);
//      cSettings.SetCurrentController(cSettings.StartController);
//    }

//    void OnPlayerConnected(NetworkPlayer player)
//    {
//      if (Network.connections.Length > 0)
//        cSettings.Controller.RunLocal = false;
//    }

//    void PrepareOffScreenRay()
//    {
//      OffScreenRay = gameObject.AddComponent<LineRenderer>();
//      OffScreenRay.material = new Material(Shader.Find("Particles/Additive"));
//      OffScreenRay.SetColors(Color.red, Color.blue);
//      OffScreenRay.SetWidth(0.001f, 0.003f);
//      OffScreenRay.SetVertexCount(2);
//      OffScreenRay.SetPosition(0, Vector3.zero);
//      OffScreenRay.SetPosition(1, Vector3.zero);

//      OffScreenStart = Target.transform.position;
//      OffScreenEnd = Target.transform.position;

//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);
//    }

//    void OnGUI()
//    {
//      if (GUILayout.Button("Back", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        Application.LoadLevel(0);
//        DestroyObject(this);
//      }

//      GUILayout.BeginArea(new Rect(0, 35, 100, 250));
//      if (GUILayout.Button("Start", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        //starts the experiment on the desktop (server) or when it's standalone
//        if (!NetworkProvider.Instance.isConnected || Network.isServer)
//          server_StartExperiment();
//        else
//          networkView.RPC("server_StartExperiment", RPCMode.Server);
//      }

//      bool isCurrent = DepthPlane == Depth1;
//      if (GUILayout.Toggle(isCurrent, Depth1.name, GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//      {
//        all_SynchDepth(1);

//        if (Network.isClient || Network.isServer)
//          networkView.RPC("all_SynchDepth", RPCMode.OthersBuffered, 1);
//      }

//      isCurrent = DepthPlane == Depth2;
//      if (GUILayout.Toggle(isCurrent, Depth2.name, GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//      {
//        all_SynchDepth(2);

//        if (Network.isClient || Network.isServer)
//          networkView.RPC("all_SynchDepth", RPCMode.OthersBuffered, 2);
//      }

//      isCurrent = DepthPlane == Depth3;
//      if (GUILayout.Toggle(isCurrent, Depth3.name, GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//      {
//        all_SynchDepth(3);

//        if (Network.isClient || Network.isServer)
//          networkView.RPC("all_SynchDepth", RPCMode.OthersBuffered, 3);
//      }
//      GUILayout.EndArea();
//    }

//    [RPC]
//    private void server_StartExperiment()
//    {
//      //Creates the trial tracking objects
//      Target.transform.localPosition = new Vector3(Random.Range(-0.125f, 0.125f), Random.Range(-0.125f, 0.125f), DepthPlane.transform.localPosition.z);

//      //creates the offscreen targeting objects
//      OffScreenStart = Depth1.transform.position;
//      OffScreenEnd = Target.transform.position;

//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);
//      if (Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, OffScreenStart, OffScreenEnd);
//    }

//    public void OnSelected(SelectionEventArgs args) // client or standalone
//    {
//      Debug.Log("OnSelected: " + (args.Target == null ? "void" : args.Target.name) + " Device: " + args.ControllerEvent.Device);

//      //saves the selection args object
//      //-- this is needed because in client mode it will receive an ack that a new target was created
//      if (args.Target == Target)
//      {
//        if (Network.isClient)
//          networkView.RPC("server_CreateNewTarget", RPCMode.Server);
//        else
//          server_CreateNewTarget();
//      }
//    }

//    [RPC]
//    private void server_CreateNewTarget()
//    {
//      //Keeps record of the current position
//      OffScreenStart = Target.transform.position;
//      //Finds a new random position for the target
//      Target.transform.localPosition = new Vector3(Random.Range(-0.125f, 0.125f), Random.Range(-0.125f, 0.125f), DepthPlane.transform.localPosition.z);
//      //Sets the ray new position to the end
//      OffScreenEnd = Target.transform.position;

//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);

//      if (Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, OffScreenStart, OffScreenEnd);
//    }


//    [RPC]
//    void all_SynchOffScreenTargets(Vector3 startPos, Vector3 endPos)
//    {
//      OffScreenStart = startPos;
//      OffScreenEnd = endPos;

//      OffScreenRay.SetPosition(0, OffScreenStart);
//      OffScreenRay.SetPosition(1, OffScreenEnd);
//    }

//    [RPC]
//    void all_SynchDepth(int depthLevel)
//    {
//      switch (depthLevel)
//      {
//        case 1:
//          DepthPlane = Depth1;
//          break;
//        case 2:
//          DepthPlane = Depth2;
//          break;
//        case 3:
//          DepthPlane = Depth3;
//          break;
//      }
//    }

//  }

//}