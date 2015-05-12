//using UnityEngine;
//using System.Collections;
//using System;
//using System.Collections.Generic;
//using UnityMoverioBT200.Scripts;

////using Vicon2Unity.ViconConnector.Scripts;
//using System.IO;
//using UnityMoverioBT200.Scripts.Util;
//using UnityMoverioBT200.Scripts.Providers;
//using UnityMoverioBT200.Scripts.Controllers;

//namespace PointingMobileHMD.Scripts
//{

//  public class ExpLocationsVsSensor : MultiuserScene
//  {

//    public System.String BaseFolder;
//    public int ParticipantNro = 0;
//    private ExperimentControl experiment;

//    public float targetWidthValue = 0.02f;
//    public float targetDistanceValue = 0.12f;
//    public int NumberOfTrials = 32;
//    private bool[] doneZones = new bool[3];
//    private bool[] doneWidths = new bool[Enum.GetNames(typeof(TargetWidths)).Length];
//    private bool[] doneDistances = new bool[Enum.GetNames(typeof(TargetDistances)).Length];


//    //The prefab for the target objects
//    public GameObject TargetSet = null;

//    private GameObject OffScreenStart = null;
//    private GameObject OffScreenEnd = null;
//    private LineRenderer OffScreenRay;

//    ~ExpLocationsVsSensor()
//    {
//      Debug.Log("Destroying the ExpLocationsVsSensor");
//    }

//    new void Start()
//    {
//      base.Start();

//      PrepareOffScreenRay();

//      experiment = new ExperimentControl("RingSelection");
//      experiment.DepthPlane = TargetSet;
//      experiment.Camera = EPSONcamera.GetComponent<Camera>();
//      experiment.LeftEyeCamera = LeftEyeCamera;
//      experiment.Settings = cSettings;
//      experiment.BaseFolder = BaseFolder;
//      experiment.ParticipantNro = ParticipantNro;

//      all_UpdateReferenceFrame(Zone.ToString(), RefFrame.ToString(), TargetWidth.ToString(), TargetDistance.ToString());
//      IndexInitialTargets();
//      CreateInitialLayout();
//    }

//    void PrepareOffScreenRay()
//    {
//      OffScreenRay = gameObject.AddComponent<LineRenderer>();
//      OffScreenRay.material = new Material(Shader.Find("Particles/Additive"));
//      OffScreenRay.SetColors(Color.blue, Color.red);
//      OffScreenRay.SetWidth(0.001f, 0.003f);
//      OffScreenRay.SetVertexCount(2);
//      OffScreenRay.SetPosition(0, Vector3.zero);
//      OffScreenRay.SetPosition(1, Vector3.zero);

//      OffScreenStart = TargetSet;
//      OffScreenEnd = TargetSet;
//    }

//    void IndexInitialTargets()
//    {
//      if (TargetSet == null)
//        return;

//      UnityEngine.Random.seed = (int)(System.DateTime.Now.Ticks % 1000);

//      targets = new List<Target>();
//      targets.AddRange(TargetSet.GetComponentsInChildren<Target>());
//      targets.Sort(new TargetComparer());
//    }

//    private void CreateInitialLayout()
//    {
//      CreateLayout(targetWidthValue, targetDistanceValue);
//    }

//    // CALL METHOD TO CHANGE DURING RUNTIME
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

//    private void UpdateLayout(float tWidth, float tDistance)
//    {
//      if (targets != null)
//      {
//        float distanceFromZero = tDistance / 2.0f;
//        float angle = 360.0f / targets.Count;

//        for (int index = 0; index < targets.Count; index++)
//        {
//          targets[index].transform.localScale = new Vector3(tWidth, tWidth, tWidth);
//          float currentAngle = angle * index * Mathf.Deg2Rad;
//          Vector3 position = new Vector3(Mathf.Sin(currentAngle) * distanceFromZero, 0.0f, Mathf.Cos(currentAngle) * distanceFromZero);
//          targets[index].transform.localPosition = position;
//        }
//      }
//    }

//    void Update()
//    {
//      //Makes the targetset always look at the camera
//      TargetSet.transform.LookAt(Participant.transform.position);

//      //Redraws the offscreen ray in the correct positions after the targets have moved in space (View, Body)
//      OffScreenRay.SetPosition(0, OffScreenStart.transform.position);
//      OffScreenRay.SetPosition(1, OffScreenEnd.transform.position);
//    }

//    void OnGUI()
//    {
//      if (!ShowGUI && GUILayout.Button("Show Controls", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        ShowGUI = true;
//      }
//      else if (ShowGUI && GUILayout.Button("Hide Controls", GUILayout.Width(100), GUILayout.Height(30)))
//      {
//        ShowGUI = false;
//      }

//      CommToAndroid.Instance.ShowGUI = ShowGUI;
//      NetworkProvider.Instance.ShowGUI = ShowGUI;
//      RotationProvider.Instance.ShowGUI = ShowGUI;

//      cSettings.RaycastController.ShowGUI = ShowGUI;

//      if (ShowGUI)
//      {
//        if (GUILayout.Button("Start", GUILayout.Width(100), GUILayout.Height(30)))
//        {
//          //starts the experiment on the desktop (server) or when it's standalone
//          if (!NetworkProvider.Instance.isConnected || Network.isServer)
//            server_StartExperiment();
//          else
//            networkView.RPC("server_StartExperiment", RPCMode.Server);
//        }

//        // GUI for changing reference frames(view/body/world)
//        GUILayout.BeginArea(new Rect(0, 65, 50, 150));
//        System.Array refFrames = System.Enum.GetValues(typeof(ReferenceFrame));
//        GUILayout.Label("RefFrame:", GUILayout.Width(100), GUILayout.Height(20));
//        foreach (ReferenceFrame refFrame in refFrames)
//        {
//          bool isCurrent = refFrame == RefFrame;
//          if (GUILayout.Toggle(isCurrent,
//                              refFrame.ToString(),
//                              GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//          {
//            //calls both the remote and the local method
//            all_UpdateReferenceFrame(Zone.ToString(), refFrame.ToString(), TargetWidth.ToString(), TargetDistance.ToString());
//            if (Network.isClient || Network.isServer)
//              networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, Zone.ToString(), refFrame.ToString(), TargetWidth.ToString(), TargetDistance.ToString());
//          }
//        }
//        GUILayout.EndArea();

//        // GUI for changing parameters(width/amplitude)
//        GUILayout.BeginArea(new Rect(60, 65, 50, 200));
//        System.Array targetWidths = System.Enum.GetValues(typeof(TargetWidths));
//        GUILayout.Label("TargetWidth:", GUILayout.Width(100), GUILayout.Height(20));
//        foreach (TargetWidths targetWidth in targetWidths)
//        {
//          bool isCurrent = targetWidth == TargetWidth;
//          if (GUILayout.Toggle(isCurrent,
//                              targetWidth.ToString(),
//                              GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//          {
//            //calls both the remote and the local method
//            all_UpdateReferenceFrame(Zone.ToString(), RefFrame.ToString(), targetWidth.ToString(), TargetDistance.ToString());
//            if (Network.isClient || Network.isServer)
//              networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, Zone.ToString(), RefFrame.ToString(), targetWidth.ToString(), TargetDistance.ToString());
//          }
//        }
//        GUILayout.EndArea();

//        GUILayout.BeginArea(new Rect(120, 65, 50, 200));
//        System.Array targetDistances = System.Enum.GetValues(typeof(TargetDistances));
//        GUILayout.Label("TargetDistance:", GUILayout.Width(100), GUILayout.Height(20));
//        foreach (TargetDistances targetDistance in targetDistances)
//        {
//          bool isCurrent = targetDistance == TargetDistance;
//          if (GUILayout.Toggle(isCurrent,
//                              targetDistance.ToString(),
//                              GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//          {
//            //calls both the remote and the local method
//            all_UpdateReferenceFrame(Zone.ToString(), RefFrame.ToString(), TargetWidth.ToString(), targetDistance.ToString());
//            if (Network.isClient || Network.isServer)
//              networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, Zone.ToString(), RefFrame.ToString(), TargetWidth.ToString(), targetDistance.ToString());
//          }
//        }
//        GUILayout.EndArea();

//      }

//      GUILayout.BeginArea(new Rect(Screen.width - 100, Screen.height - 100, 100, 100));
//      GUILayout.Label("Trial " + (experiment.Trial.TrialNro + 1) + "/" + NumberOfTrials, GUILayout.Width(100), GUILayout.Height(20));
//      GUILayout.EndArea();
//    }

//    //[RPC]
//    //void all_UpdateReferenceFrame(string zone, string referenceFrame)
//    //{
//    //  Zone = (DistanceZones)System.Enum.Parse(typeof(DistanceZones), zone);
//    //  RefFrame = (ReferenceFrame)System.Enum.Parse(typeof(ReferenceFrame), referenceFrame);

//    //  if (TargetSet == null)
//    //    return;
//    //  GameObject zoneGO = GetInteractionZoneGO();
//    //  TargetSet.transform.SetParent(zoneGO.transform, true);
//    //  TargetSet.transform.localPosition = Vector3.zero;
//    //  TargetSet.transform.localScale = Vector3.one;
//    //  TargetSet.transform.LookAt(Participant.transform.position);

//    //  MessageBroker.BroadcastAll("OnReferenceFrameUpdated", RefFrame);
//    //}

//    [RPC]
//    void all_UpdateReferenceFrame(string zone, string referenceFrame, string targetWidth, string targetDist)
//    {
//      Zone = (DistanceZones)System.Enum.Parse(typeof(DistanceZones), zone);
//      RefFrame = (ReferenceFrame)System.Enum.Parse(typeof(ReferenceFrame), referenceFrame);
//      TargetWidth = (TargetWidths)System.Enum.Parse(typeof(TargetWidths), targetWidth);
//      TargetDistance = (TargetDistances)System.Enum.Parse(typeof(TargetDistances), targetDist);

//      if (TargetSet == null)
//        return;

//      Vector2 tParams = GetTargetParams();
//      targetWidthValue = tParams.x;
//      targetDistanceValue = tParams.y;

//      UpdateLayout(targetWidthValue, targetDistanceValue);

//      GameObject zoneGO = GetInteractionZoneGO();
//      TargetSet.transform.SetParent(zoneGO.transform, true);
//      TargetSet.transform.localPosition = Vector3.zero;
//      TargetSet.transform.localScale = Vector3.one;
//      TargetSet.transform.LookAt(Participant.transform.position);

//      MessageBroker.BroadcastAll("OnReferenceFrameUpdated", RefFrame);
//    }

//    private SelectionEventArgs selectionArgs = null;

//    private int currentTarget = -1;
//    private List<Target> targets;
//    private GameObject lastTarget;

//    [RPC]
//    private void server_StartExperiment()
//    {
//      //Unhighlights everything
//      for (int index = 0; index < targets.Count; index++)
//        targets[index].Highlighted = false;

//      //resets the zones to false
//      for (int zone = 0; zone < doneZones.Length; zone++)
//        doneZones[zone] = false;

//      //reset widths to false
//      for (int width = 0; width < doneWidths.Length; width++)
//        doneWidths[width] = false;

//      //reset distances to false
//      for (int dist = 0; dist < doneDistances.Length; dist++)
//      {
//        doneDistances[dist] = false;

//        // Do not do distances that will put targets out of view in view based.
//        if (RefFrame == ReferenceFrame.View && dist > 1)
//          doneDistances[dist] = true;

//      }

//      //determines the initial zone (fixed to control for purposes of ring experiment)
//      DistanceZones newZone = DistanceZones.Control;
//      //determines initial width
//      TargetWidths newWidth = (TargetWidths)UnityEngine.Random.Range(0, doneWidths.Length);
//      //determines initial distance
//      TargetDistances newDistance = (TargetDistances)UnityEngine.Random.Range(0, doneDistances.Length);
//      while (doneDistances[(int)newDistance])
//        newDistance = (TargetDistances)UnityEngine.Random.Range(0, doneDistances.Length);

//      all_UpdateReferenceFrame(newZone.ToString(), RefFrame.ToString(), newWidth.ToString(), newDistance.ToString());
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, newZone.ToString(), RefFrame.ToString(), newWidth.ToString(), newDistance.ToString());
//      doneZones[(int)newZone] = true;
//      doneWidths[(int)newWidth] = true;
//      doneDistances[(int)newDistance] = true;

//      //Creates the trial tracking objects
//      int nextTarget = UnityEngine.Random.Range(0, targets.Count - 1);
//      all_SetTargetAndTrial(nextTarget, 0);
//      //if (Network.isServer)
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("all_SetTargetAndTrial", RPCMode.Others, nextTarget, experiment.Trial.TrialNro);

//      lastTarget = TargetSet;

//      //creates the offscreen targeting objects
//      GameObject osStart = GetInteractionZoneGO();
//      GameObject osEnd = targets[currentTarget].gameObject;
//      all_SynchOffScreenTargets(osStart.name, osEnd.name);
//      //if (Network.isServer)
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, osStart.name, osEnd.name);
//    }

//    [RPC]
//    private void all_SetTargetAndTrial(int nextTarget, int trial)
//    {
//      if (currentTarget >= 0)
//        targets[currentTarget].Highlighted = false;

//      currentTarget = nextTarget;

//      if (currentTarget >= 0 && currentTarget < targets.Count)
//        targets[currentTarget].Highlighted = true;

//      if (trial >= 0)
//        experiment.CreateNewTrial(selectionArgs, targets[currentTarget].gameObject, trial, Zone, RefFrame);
//      else
//        experiment.Stop();
//    }

//    [RPC]
//    void all_SynchOffScreenTargets(string startName, string endName)
//    {
//      OffScreenStart = GameObject.Find(startName);
//      OffScreenEnd = GameObject.Find(endName);
//    }

//    public void OnHovered(SelectionEventArgs args)
//    {
//      if (!experiment.Started)
//        return;

//      experiment.ProcessOnHover(args);
//    }

//    public void OnSelected(SelectionEventArgs args)
//    {
//      Debug.Log("OnSelected: " + (args.Target == null ? "void" : args.Target.name) + " Device: " + args.ControllerEvent.Device);
//      if (!experiment.Started)
//        return;


//      experiment.Trial.Triggers++;
//      if (experiment.Trial.Hovers > 0)
//        experiment.Trial.TriggersAfterArriving++;

//      //saves the selection args object
//      //-- this is needed because in client mode it will receive an ack that a new target was created
//      selectionArgs = args;

//      if (args.Target == experiment.Trial.Target)
//      {
//        //fills up the trialInfo object on whatever side (standalone/client) where the object was selected
//        experiment.CaptureTrialInfo(args);
//        Debug.Log("test");

//        if (Network.isClient)
//        {
//          //reports the event to the server
//          networkView.RPC("server_TargetSelectedInClient", RPCMode.Server, experiment.Trial.ToString());
//        }
//        else
//        {
//          //Prints it locally
//          Debug.Log("LOCAL: " + experiment.Trial.ToString());

//          //Creates new target
//          server_CreateNewTarget();
//        }
//      }

//    }

//    [RPC]
//    void server_TargetSelectedInClient(string logLine)
//    {
//      //Shows it in the Unity console
//      Debug.Log(logLine);
//      //Saves it in the log file
//      experiment.WriteToFile(logLine);

//      //Creates new target
//      server_CreateNewTarget();
//    }

//    private bool reset = false;
//    private void server_CreateNewTarget()
//    {
//      lastTarget = targets[currentTarget].gameObject;

//      int nextTarget = currentTarget + 8;
//      if (experiment.Trial.TrialNro % 2 == 1)
//        nextTarget++;
//      nextTarget = nextTarget % targets.Count;

//      bool doneAllDists = true;
//      bool doneAllWidths = true;


//      foreach (bool done in doneDistances)
//      {
//        if (!done)
//          doneAllDists = false;
//      }

//      foreach (bool done in doneWidths)
//      {
//        if (!done)
//          doneAllWidths = false;
//      }


//      experiment.Trial.TrialNro++;
//      if (experiment.Trial.TrialNro == NumberOfTrials)
//      {
//        if (doneAllWidths && doneAllDists)//&& doneZones[1] && doneZones[2]) //all zones are done, experiment finishes
//        {
//          nextTarget = -1;

//          all_SetTargetAndTrial(-1, -1);
//          //if (Network.isServer)
//          if (Network.isClient || Network.isServer)
//            networkView.RPC("all_SetTargetAndTrial", RPCMode.Others, -1, -1);

//          GameObject osStart1 = TargetSet;
//          GameObject osEnd1 = TargetSet;
//          all_SynchOffScreenTargets(osStart1.name, osEnd1.name);
//          //if (Network.isServer)
//          if (Network.isClient || Network.isServer)
//            networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, osStart1.name, osEnd1.name);

//          return;
//        }
//        else
//        {
//          // Change width
//          if (doneAllDists && !doneAllWidths)
//          {
//            TargetWidths newWidth = (TargetWidths)UnityEngine.Random.Range(0, doneWidths.Length);
//            while (doneWidths[(int)newWidth])
//              newWidth = (TargetWidths)UnityEngine.Random.Range(0, doneWidths.Length);

//            all_UpdateReferenceFrame(Zone.ToString(), RefFrame.ToString(), newWidth.ToString(), TargetDistance.ToString());
//            if (Network.isClient || Network.isServer)
//              networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, Zone.ToString(), RefFrame.ToString(), newWidth.ToString(), TargetDistance.ToString());
//            doneWidths[(int)newWidth] = true;

//            for (int dist = 0; dist < doneDistances.Length; dist++)
//            {
//              doneDistances[dist] = false;

//              // Do not do distances that will put targets out of view in view based.
//              if (RefFrame == ReferenceFrame.View && dist > 1)
//                doneDistances[dist] = true;
//            }
//          }
//          // Change distance keep width
//          else
//          {
//            TargetDistances newDistance = (TargetDistances)UnityEngine.Random.Range(0, doneDistances.Length);
//            while (doneDistances[(int)newDistance])
//              newDistance = (TargetDistances)UnityEngine.Random.Range(0, doneDistances.Length);

//            all_UpdateReferenceFrame(Zone.ToString(), RefFrame.ToString(), TargetWidth.ToString(), newDistance.ToString());
//            if (Network.isClient || Network.isServer)
//              networkView.RPC("all_UpdateReferenceFrame", RPCMode.OthersBuffered, Zone.ToString(), RefFrame.ToString(), TargetWidth.ToString(), newDistance.ToString());
//            doneDistances[(int)newDistance] = true;
//          }
//        }

//        nextTarget = UnityEngine.Random.Range(0, targets.Count - 1);
//        experiment.Trial.TrialNro = 0;

//      }

//      all_SetTargetAndTrial(nextTarget, experiment.Trial.TrialNro);
//      //if (Network.isServer)
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("all_SetTargetAndTrial", RPCMode.Others, nextTarget, experiment.Trial.TrialNro);

//      //creates the offscreen targeting objects
//      GameObject osStart2 = lastTarget;
//      GameObject osEnd2 = targets[currentTarget].gameObject;
//      all_SynchOffScreenTargets(osStart2.name, osEnd2.name);
//      //if (Network.isServer)
//      if (Network.isClient || Network.isServer)
//        networkView.RPC("all_SynchOffScreenTargets", RPCMode.Others, osStart2.name, osEnd2.name);
//    }

//    void OnApplicationQuit()
//    {
//      experiment.CloseFile();
//    }

//  }

//}