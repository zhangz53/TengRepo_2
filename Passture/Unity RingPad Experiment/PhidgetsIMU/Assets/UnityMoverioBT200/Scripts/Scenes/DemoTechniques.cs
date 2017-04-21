//using UnityEngine;
//using System.Collections;
//using System.Collections.Generic;
//using UnityMoverioBT200.Scripts;
//using System.IO;
//using UnityMoverioBT200.Scripts.Util;
//using UnityMoverioBT200.Scripts.Providers;
//using UnityMoverioBT200.Scripts.Controllers;
//using PointingMobileHMD.Scripts;

//namespace UnityMoverioBT200.Scripts.Scenes
//{

//  public class DemoTechniques : MonoBehaviour
//  {

//    //The prefab for the target objects
//    public GameObject TargetSet = null;

//    public GameObject WorldLocations;
//    public GameObject BodyLocations;
//    public GameObject ViewLocations;

//    private DistanceZones Zone = DistanceZones.Control;
//    private ReferenceFrame RefFrame = ReferenceFrame.View;

//    private GameObject EPSONcamera;

//    private Vector3 OffScreenStart = Vector3.zero;
//    private Vector3 OffScreenEnd = Vector3.zero;
//    private LineRenderer OffScreenRay;

//    private enum EpsonControllerType { TouchPad, GyroMouse, Raycast, HeadGyro, Head }
//    private EpsonControllerType controller = EpsonControllerType.Raycast;

//    private ControllerSettings cSettings;

//    ~DemoTechniques()
//    {
//      Debug.Log("Destroying the EpsonTechniques");
//    }

//    void Start()
//    {
//      EPSONcamera = GameObject.Find("Stereoscopic Camera");
//      cSettings = GameObject.FindObjectOfType<ControllerSettings>();

//      RotationProvider.Instance.ShowGUI = true;
//      CommToAndroid.Instance.ShowGUI = true;
//      NetworkProvider.Instance.ShowGUI = true;

//      PrepareOffScreenRay();
//      UpdateReferenceFrame();
//      IndexInitialTargets();
//      CreateLayout(0.01f, 0.085f);
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

//      OffScreenStart = TargetSet.transform.position;
//      OffScreenEnd = TargetSet.transform.position;
//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);
//    }

//    void Update()
//    {
//      //Makes the targetset always look at the camera
//      TargetSet.transform.LookAt(EPSONcamera.transform.position);
//    }

//    void OnGUI()
//    {
//      if (GUILayout.Button("Back", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        Application.LoadLevel(0);
//        DestroyObject(this);
//      }

//      if (GUILayout.Button("Start", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        //starts the experiment on the desktop (server) or when it's standalone
//        if (!NetworkProvider.Instance.isConnected || Network.isServer)
//          server_StartExperiment();
//        else
//          networkView.RPC("server_StartExperiment", RPCMode.Server);
//      }

//      System.Array controllerTypes = System.Enum.GetValues(typeof(EpsonControllerType));
//      int cHeight = 25 + 25 * controllerTypes.Length + 110;
//      GUILayout.BeginArea(new Rect(0, 65, 100, cHeight));
//      foreach (EpsonControllerType cont in controllerTypes)
//      {
//        bool isCurrent = controller == cont;
//        if (GUILayout.Toggle(isCurrent,
//                             cont.ToString(),
//                             GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//        {
//          all_SynchController(cont.ToString());

//          if (Network.isClient || Network.isServer)
//            networkView.RPC("all_SynchController", RPCMode.OthersBuffered, cont.ToString());
//        }
//      }
//      GUILayout.EndArea();

//    }

//    void IndexInitialTargets()
//    {
//      if (TargetSet == null)
//        return;

//      Random.seed = (int)(System.DateTime.Now.Ticks % 1000);

//      targets = new List<Target>();
//      targets.AddRange(TargetSet.GetComponentsInChildren<Target>());
//      targets.Sort(new TargetComparer());
//    }

//    private void UpdateReferenceFrame()
//    {
//      if (TargetSet == null)
//        return;

//      GameObject zoneGO = GetInteractionZoneGO();

//      TargetSet.transform.SetParent(zoneGO.transform, true);
//      TargetSet.transform.localPosition = Vector3.zero;
//      TargetSet.transform.LookAt(EPSONcamera.transform.position);

//      MessageBroker.BroadcastAll("OnReferenceFrameUpdated", RefFrame);
//    }

//    private GameObject GetInteractionZoneGO()
//    {
//      GameObject refFrameGO = ViewLocations;
//      if (RefFrame == ReferenceFrame.Body)
//        refFrameGO = BodyLocations;
//      else if (RefFrame == ReferenceFrame.World)
//        refFrameGO = WorldLocations;

//      GameObject zoneGO = refFrameGO.transform.FindChild("Control").gameObject;
//      if (Zone == DistanceZones.Near)
//        zoneGO = refFrameGO.transform.FindChild("Near").gameObject;
//      else if (Zone == DistanceZones.Far)
//        zoneGO = refFrameGO.transform.FindChild("Far").gameObject;
//      return zoneGO;
//    }

//    private int currentTarget = -1;
//    private List<Target> targets;
//    private GameObject lastTarget;

//    [RPC]
//    private void server_StartExperiment()
//    {
//      //Unhighlights everything
//      for (int index = 0; index < targets.Count; index++)
//        targets[index].Highlighted = false;

//      //Creates the trial tracking objects
//      int nextTarget = Random.Range(0, targets.Count);

//      all_SynchTarget(nextTarget);
//      if (Network.isServer)
//        networkView.RPC("all_SynchTarget", RPCMode.Others, nextTarget);

//      lastTarget = TargetSet;

//      //creates the offscreen targeting objects
//      OffScreenStart = GetInteractionZoneGO().transform.position;
//      OffScreenEnd = targets[currentTarget].gameObject.transform.position;

//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);
//      if (Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, OffScreenStart, OffScreenEnd);

//      //Hgihtlights the first target
//      targets[currentTarget].Highlighted = true;
//    }

//    [RPC]
//    private void all_SynchTarget(int nextTarget)
//    {
//      if (currentTarget >= 0)
//        targets[currentTarget].Highlighted = false;

//      currentTarget = nextTarget;
//      targets[currentTarget].Highlighted = true;
//    }

//    private void CreateLayout(float tWidth, float tDistance)
//    {
//      float distanceFromZero = tDistance / 2.0f;
//      float angle = 360.0f / targets.Count;

//      for (int index = 0; index < targets.Count; index++)
//      {
//        targets[index].transform.localScale = new Vector3(tWidth, tWidth, tWidth);
//        float currentAngle = angle * index * Mathf.Deg2Rad;
//        Vector3 position = new Vector3(Mathf.Sin(currentAngle) * distanceFromZero, 0.0f, Mathf.Cos(currentAngle) * distanceFromZero);
//        targets[index].transform.localPosition = position;
//      }
//    }


//    public void OnSelected(SelectionEventArgs args)
//    {
//      Debug.Log("OnSelected: " + (args.Target == null ? "void" : args.Target.name) + " Device: " + args.ControllerEvent.Device);

//      if (currentTarget < 0 || currentTarget >= targets.Count)
//        return;

//      if (args.Target != targets[currentTarget].gameObject)
//        return;

//      if (Network.isClient)
//        networkView.RPC("server_MoveToNewTarget", RPCMode.Server);
//      else
//        server_MoveToNewTarget();
//    }

//    [RPC]
//    private void server_MoveToNewTarget()
//    {
//      //Turns the current highlighting off and saves a reference to it
//      targets[currentTarget].Highlighted = false;
//      lastTarget = targets[currentTarget].gameObject;

//      //Sets the two at the same location in case it's the last selection of the experiment
//      OffScreenStart = targets[currentTarget].gameObject.transform.position;

//      //Calculates the next target
//      int nextTarget = (currentTarget + 8 + (offset++ % 2)) % 16;

//      all_SynchTarget(nextTarget);
//      if (Network.isServer)
//        networkView.RPC("all_SynchTarget", RPCMode.Others, nextTarget);

//      //Moves the ending point of the ray to the new selection target
//      OffScreenEnd = targets[currentTarget].gameObject.transform.position;

//      all_SynchOffScreenTargets(OffScreenStart, OffScreenEnd);
//      if (Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, OffScreenStart, OffScreenEnd);
//    }

//    int offset = 0;

//    [RPC]
//    void all_SynchOffScreenTargets(Vector3 startPos, Vector3 endPos)
//    {
//      OffScreenStart = startPos;
//      OffScreenEnd = endPos;

//      OffScreenRay.SetPosition(0, OffScreenStart);
//      OffScreenRay.SetPosition(1, OffScreenEnd);
//    }

//    [RPC]
//    void all_SynchController(string epsonControllerName)
//    {
//      controller = (EpsonControllerType)System.Enum.Parse(typeof(EpsonControllerType), epsonControllerName);

//      //calls both the remote and the local method
//      switch (controller)
//      {
//        case EpsonControllerType.TouchPad:
//          cSettings.SetCurrentController(ControllerType.TouchPad);
//          break;
//        case EpsonControllerType.GyroMouse:
//          cSettings.SetCurrentController(ControllerType.GyroMouse);
//          break;
//        case EpsonControllerType.Raycast:
//          cSettings.SetCurrentController(ControllerType.Raycast);
//          break;
//        case EpsonControllerType.HeadGyro:
//          RefFrame = ReferenceFrame.View;
//          cSettings.SetCurrentController(ControllerType.Head);
//          UpdateReferenceFrame();
//          break;
//        case EpsonControllerType.Head:
//          RefFrame = ReferenceFrame.World;
//          cSettings.SetCurrentController(ControllerType.Head);
//          UpdateReferenceFrame();
//          break;
//      }
//    }

//  }

//}