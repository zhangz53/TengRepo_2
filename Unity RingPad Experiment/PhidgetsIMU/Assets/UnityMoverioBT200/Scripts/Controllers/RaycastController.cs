using UnityEngine;
using System.Collections;
using System.Text.RegularExpressions;
using System.Collections.Generic;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class RaycastController : MoverioController
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


    public enum RaycastType { PointCursor, RayAll, RaySimple, LockDragFinger, LockTwist, AutoTwist }

    public enum TwistDirectionType { ClockWise, CounterClockWise }

    public enum OrderingCriteria { DistanceToWand, DistanceToRay }

    public enum RayState { Parked, Active, Disambiguating }

    private RayState _state = RayState.Parked;
    public RayState State { get { return _state; } }

    /** These are the public properties set on the Unity editor **/
    public GameObject InitialPointing;
    public RaycastType RayType = RaycastType.RaySimple;
    public TwistDirectionType TwistDirection = TwistDirectionType.ClockWise;
    public OrderingCriteria Ordering = OrderingCriteria.DistanceToWand;

    public float RayDistance = 100f;

    public bool UseApex = false;
    public float AppexDegrees = 2.0f;
    public int AppexLevels = 2;
    public int AppexPointsPerLevel = 10;
    public bool TwistAppertureControl = false;
    public float MinAppexDegrees = 0f;
    public float MaxAppexDegrees = 6f;
    public float AppexTwistAngle = 45f;

    public bool AutoPark = true;
    public int AutoParkMillis = 5000;
    public float AutoParkMaxAngleDegrees = 45f;
    public float AutoParkStillAngleDegrees = 0.02f;

    public int SelectionFeedbackMillis = 150; //milliseconds
    public int MillisecondsOfHistoryPosition = 500;

    public Color ParkedColor = new Color32(100, 0, 0, 255);
    public Color BaseColor = Color.gray;
    public Color LockColor = Color.green;
    public Color SelectionColor = Color.blue;

    public float TwistSegment = 25.0f; //degrees
    public float DragSegment = 50.0f; //pixels

    public bool TwistUpCancelLock = true;

    public GameObject InitialSource;
    public bool UseExternalTrackingForPointCursor = false;

    public bool AllowChangeRayType = false;
    public bool AllowChangeRaySource = false;

    public bool UseRayLights = false;
    public float RayLightRange = 0.05f;
    public float RayLightIntensity = 1.0f;
    public float RayLightBrightnessBump = 50f / 255f;

    public LockDragGuide DragGuide;
    /** End **/

    private Queue<PositionHistory> History = new Queue<PositionHistory>();

    private GameObject EPSONcamera { get; set; }
    private GameObject WandRotation { get; set; }
    private GameObject Tip { get; set; }

    private List<GameObject> RaycastSources = new List<GameObject>();
    private List<GameObject> RaycastEndings = new List<GameObject>();

    private Color RayColor;
    private LineRenderer RayToTarget;
    private LineRenderer RayToInfinite;
    private List<Light> RayLights;
    private GameObject[] SceneTargets;

    private GameObject hoveredTarget = null;
    private GameObject targetAtStartDisambiguation = null;
    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();
    private List<GameObject> hoveredTargetsByDistance = new List<GameObject>();
    private List<GameObject> hoveredTargetsByCriteria = new List<GameObject>();
    private List<GameObject> unHoveredTargets = new List<GameObject>();

    private bool isDisambiguationAborted = false;
    private Quaternion disambiguationRotStart = Quaternion.identity;
    private Quaternion invDisambiguationRotStart = Quaternion.identity;
    private int disambiguationIndex = -1;

    private MoverioTouchpadEventArgs startingArgs = null;
    private MoverioTouchpadEventArgs movingArgs = null;

    ~RaycastController()
    {
      Debug.Log("Destroying the RaycastController");
    }

    // Use this for initialization 
    void Awake()
    {
      NeckJoint = transform.parent.parent.FindChild("HeadControl/NeckJoint").gameObject;


      EPSONcamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
      WandRotation = transform.FindChild("WandRotation").gameObject;
      Tip = transform.FindChild("WandRotation/WandJoint/Tip").gameObject;

      PrepareRayGraphics();
      PrepareRaycast();
      PrepareRayLights();
      PrepareDragGuide();

      //This is the initial state
      SetRayState(RayState.Parked);

      MoveTo(transform.position, transform.rotation);
    }

    void Start()
    {
      NotifyRayTypeChanged();
      NotifyRaycastLocationChanged();
      SetDefaults();
    }

    public void SetDefaults()
    {
      SetDefaultRotation();
    }

    // Update is called once per frame
    void Update()
    {
      UpdateRayLights();

      if (!RunLocal)
        return;

      //Does corrections
      transform.localPosition = Vector3.zero;

      //Saves the wands current location
      SaveHistory();

      //Gathers interaction data locally 
      Quaternion orientation = RotationProvider.Instance.Rotation;
      Quaternion headOrientation = RotationProvider.Instance.HeadRotation;

      //Calls the execution method both locally
      MoveTo(transform.position, orientation);
      MoveHead(headOrientation);

      //Calls execution method remotely
      if (Network.isClient)
      {
        networkView.RPC("MoveTo", RPCMode.Others, transform.position, orientation, State.ToString());
        networkView.RPC("MoveHead", RPCMode.Others, headOrientation);
      }
    }

    [RPC]
    private void MoveTo(Vector3 targetPosition, Quaternion targetRotation, string remoteState = null)
    {
      if (!IsCurrentController)
        return;

      if (remoteState != null)
      {
        RayState newState = (RayState)System.Enum.Parse(typeof(RayState), remoteState);
        SetRayState(newState);
      }

      if (State == RayState.Parked)
      {
        targetRotation = Quaternion.Euler(0f, 0f, 0f);

        gameObject.transform.position = targetPosition;
        WandRotation.transform.localRotation = targetRotation;

        UpdateRayPositionAndLenght();
      }
      else if (State == RayState.Active)
      {
        if (transform.parent.name.Contains("External"))
          targetRotation = Quaternion.Euler(0f, 0f, 0f);

        gameObject.transform.position = targetPosition;
        WandRotation.transform.localRotation = targetRotation;

        SetApexWithTwistAngle(targetRotation);

        if (Network.isServer || !RunLocal)
        {
          UpdateRayPositionAndLenght();
        }
        else
        {
          //All the data processing occurs in the client
          //Checks the hovers which forces to update the main target and the ray positions
          CheckHovers();
          UpdateRayPositionAndLenght();
          Disambiguate();
          NotifyTargets();

          //Tests the conditions for Auto Park
          EvaluateAutoPark();
        }
      }
      else if (State == RayState.Disambiguating)
      {
        if (Network.isServer || !RunLocal)
          return;

        //If the user just put his finger down and still within the tap time, the ray should remain static
        if ((System.DateTime.Now - startingArgs.EventTime).TotalMilliseconds < MoverioInputProvider.Instance.TapTimeOut)
          return;

        Disambiguate();
        NotifyTargets();
      }

      //Finalizes painting the cone in the Editor
      foreach (GameObject ending in RaycastEndings)
        Debug.DrawRay(transform.position, ending.transform.position - transform.position, RayColor);
    }

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

    void OnTouchStarted(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      if (State == RayState.Parked)
        return;

      if (!LocksWithFinger)
        return;


      startingArgs = args;
      movingArgs = args;
      MoveTo(GetHistoricPosition(100), RotationProvider.Instance.GetHistoricValue(100));
      StartDisambiguation();
    }

    void OnTouchMoved(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      if (State == RayState.Parked)
        return;

      movingArgs = args;
    }

    void OnTouchEnded(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      movingArgs = args;

      if (State == RayState.Active && !isDisambiguationAborted)
      {
        CheckSelection(args, handleHeisembergEffect: true);
        StopDisambiguating(wasAborted: false);
      }
      if (State == RayState.Disambiguating && !isDisambiguationAborted)
      {
        CheckSelection(args, handleHeisembergEffect: true);
        StopDisambiguating(wasAborted: false);
      }
      else if (State == RayState.Parked)
      {
        UnParkRay();
      }

      isDisambiguationAborted = false;
    }

    void OnTap(MoverioTouchpadEventArgs args)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      if (RayType == RaycastType.PointCursor)
        return;

      if (!LocksWithFinger)
        return;

      if (State != RayState.Disambiguating)
        return;

      hoveredTarget = targetAtStartDisambiguation;
      CheckSelection(args, handleHeisembergEffect: false);
      StopDisambiguating(wasAborted: true);
    }

    void OnTwist(RotationProvider.TwistEvent twist)
    {
      if (!RunLocal || !IsCurrentController)
        return;

      if (RayType == RaycastType.PointCursor)
        return;

      if (State == RayState.Parked)
        return;

      if (State == RayState.Disambiguating)
      {
        if (twist.Type == RotationProvider.TwistTriggerTypes.Up && TwistUpCancelLock)
          StopDisambiguating(wasAborted: true, resetRayColor: true);
        return;
      }

      //Only does the following if State == Active
      if (RayType == RaycastType.AutoTwist)
      {
        if ((twist.Type == RotationProvider.TwistTriggerTypes.ScrewRight && TwistDirection == TwistDirectionType.CounterClockWise) ||
            (twist.Type == RotationProvider.TwistTriggerTypes.ScrewLeft && TwistDirection == TwistDirectionType.ClockWise))
        {
          //Returns the wand to the orientation where the twist started
          //IMPORTANT: We should also return to the initial position
          MoveTo(GetHistoricPosition(twist.Milliseconds), twist.initialRot);

          //Sets the wand in disambiguiation mode
          StartDisambiguation();
        }
      }
      else if (twist.Type == RotationProvider.TwistTriggerTypes.Up)
        ParkRay();

    }

    void OnGUI()
    {
      if (!ShowGUI || !RunLocal)
        return;

      float heightEM = 0;
      if (AllowChangeRayType)
      {
        System.Array extensionMethods = System.Enum.GetValues(typeof(RaycastType));
        heightEM = 25 + 25 * extensionMethods.Length + 25;
        GUILayout.BeginArea(new Rect(Screen.width - 100, 0, 100, heightEM));
        GUILayout.Label("Ray Type:", GUILayout.Width(Screen.width), GUILayout.Height(20));
        foreach (RaycastType type in extensionMethods)
        {
          bool isCurrent = RayType == type;
          if (GUILayout.Toggle(isCurrent,
                               type.ToString(),
                               GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
          {
            all_SynchRayType(type.ToString());
            if (Network.isClient || Network.isServer)
              networkView.RPC("all_SynchRayType", RPCMode.OthersBuffered, RayType.ToString());
          }
        }
        GUILayout.EndArea();
      }

      if (AllowChangeRaySource)
      {
        GUILayout.BeginArea(new Rect(Screen.width - 100, heightEM, 100, 25 + 25 * (RaycastSources.Count + 1) + 10));
        GUILayout.Label("Wand Location", GUILayout.Width(100), GUILayout.Height(25));
        for (int index = 0; index < RaycastSources.Count; index++)
        {
          bool isCurrent = transform.parent == RaycastSources[index].transform;
          if (GUILayout.Toggle(isCurrent,
                               RaycastSources[index].name,
                               GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
          {
            all_SynchWandPosition(index);
            if (Network.isClient || Network.isServer)
              networkView.RPC("all_SynchWandPosition", RPCMode.OthersBuffered, index);
          }
        }
        GUILayout.EndArea();
      }
    }

    void PrepareRayGraphics()
    {
      RayColor = BaseColor;

      RayToTarget = gameObject.AddComponent<LineRenderer>();
      RayToTarget.material = new Material(Shader.Find("Particles/Additive"));
      RayToTarget.SetColors(RayColor, RayColor);
      RayToTarget.SetWidth(0.0025f, 0.0025f);
      RayToTarget.SetVertexCount(2);

      Vector3 direction = (Tip.transform.position - gameObject.transform.position).normalized;
      RayToTarget.SetPosition(0, gameObject.transform.position);
      RayToTarget.SetPosition(1, gameObject.transform.position + direction);

      RayToInfinite = WandRotation.AddComponent<LineRenderer>();
      RayToInfinite.material = new Material(Shader.Find("Particles/Additive"));
      RayToInfinite.SetColors(ParkedColor, Color.black);
      RayToInfinite.SetWidth(0.0025f, 0.0025f);
      RayToInfinite.SetVertexCount(2);

      RayToInfinite.SetPosition(0, gameObject.transform.position + direction);
      RayToInfinite.SetPosition(1, gameObject.transform.position + 2 * direction);
    }

    private void PrepareRaycast()
    {
      //Gets the player's endpoints and raysources
      Transform[] children = gameObject.transform.parent.parent.GetComponentsInChildren<Transform>();
      foreach (Transform go in children)
      {
        if (go == null)
          continue;
        if (go.tag == "RaySource")
          RaycastSources.Add(go.gameObject);
        else if (go.tag == "RayEnding")
          RaycastEndings.Add(go.gameObject);
      }

      //Finds the external source
      GameObject[] all = GameObject.FindGameObjectsWithTag("RaySource");
      foreach (GameObject go in all)
      {
        if (go == null)
          continue;
        if (!go.name.Contains("External"))
          continue;

        RaycastSources.Add(go);
        break;
      }

      //Generates all other ray endings
      if (UseApex)
      {
        GameObject coneEndings = RaycastEndings[0];
        float distanceFromWand = (coneEndings.transform.position - transform.position).magnitude;

        float maxDistanceFromCenter = distanceFromWand * Mathf.Tan((AppexDegrees / 2.0f) * Mathf.Deg2Rad);
        float distanceFromCenterPerLevel = maxDistanceFromCenter / AppexLevels;

        for (int level = 0; level < AppexLevels; level++)
        {
          float distanceFromCenter = distanceFromCenterPerLevel * (level + 1);
          float angleBetweenPoints = 360.0f / AppexPointsPerLevel;

          for (int point = 0; point < AppexPointsPerLevel; point++)
          {
            float currentAngle = angleBetweenPoints * point * Mathf.Deg2Rad;
            Vector3 position = new Vector3(Mathf.Sin(currentAngle) * distanceFromCenter, 0.0f, Mathf.Cos(currentAngle) * distanceFromCenter);

            GameObject newConeEnding = new GameObject();
            newConeEnding.name = "Level-" + level + "-Point-" + point;
            newConeEnding.tag = "RayEnding";
            newConeEnding.transform.parent = coneEndings.transform;
            newConeEnding.transform.localRotation = Quaternion.Euler(0, 0, 0);
            newConeEnding.transform.localPosition = position;

            RaycastEndings.Add(newConeEnding);
          }
        }
      }

      MoveWandToRaySource(InitialSource);
      RotationProvider.Instance.SetBaselineRotation();
    }

    private void PrepareRayLights()
    {
      if (!UseRayLights)
        return;

      Color lightColor = new Color(BaseColor.r + RayLightBrightnessBump, BaseColor.g + RayLightBrightnessBump, BaseColor.b + RayLightBrightnessBump);

      //Pre-loads as many lights as targets in the scene
      SceneTargets = GameObject.FindGameObjectsWithTag("Target");
      int totalLights = SceneTargets == null ? 0 : SceneTargets.Length;

      RayLights = new List<Light>();
      for (int lightIndex = 0; lightIndex < totalLights; lightIndex++)
      {
        GameObject lightGameObject = new GameObject("RayLight-" + lightIndex);
        lightGameObject.transform.parent = WandRotation.transform;

        Light lightComp = lightGameObject.AddComponent<Light>();
        lightComp.color = lightColor;
        lightComp.type = LightType.Point;
        lightComp.range = lightIndex * RayLightRange;
        lightComp.intensity = RayLightIntensity;
        lightComp.transform.localPosition = new Vector3(0f, 0f, lightIndex);

        RayLights.Add(lightComp);
      }
    }

    private void PrepareDragGuide()
    {
      if (DragGuide == null)
        DragGuide = transform.GetComponentInChildren<LockDragGuide>();
      if (DragGuide != null)
        DragGuide.enabled = false;
    }

    private void UpdateRayLights()
    {
      if (!UseRayLights)
        return;

      Vector3 wandray = Tip.transform.position - transform.position;

      for (int index = 0; index < SceneTargets.Length; index++)
      {
        Vector3 targetray = SceneTargets[index].transform.position - transform.position;
        float distanceOnRay = Vector3.Dot(wandray.normalized, targetray.normalized);
        float distance = distanceOnRay * targetray.magnitude;

        RayLights[index].transform.localPosition = new Vector3(0f, 0f, distance);
        RayLights[index].range = distance * RayLightRange;
      }
    }

    private void SetApexWithTwistAngle(Quaternion rotation)
    {
      if (!UseApex || !TwistAppertureControl)
        return;

      //1- Calculates Twist input
      float inputAngle = rotation.eulerAngles.z;
      if (inputAngle > 180f)
        inputAngle = -1 * (360 - inputAngle);
      inputAngle = Mathf.Min(Mathf.Max(inputAngle, -AppexTwistAngle), AppexTwistAngle);

      //2- Calculates appex according to twist and within the limits (-AppexTwistAngle, MinAppexDegrees) (0, AppexDegrees) (AppexTwistAngle, MaxAppexDegrees)
      float appex = AppexDegrees;
      if (inputAngle <= 0)
        appex = inputAngle * (AppexDegrees - MinAppexDegrees) / AppexTwistAngle + AppexDegrees;
      else
        appex = inputAngle * (MaxAppexDegrees - AppexDegrees) / AppexTwistAngle + AppexDegrees;

      //3- updates the cone endings
      GameObject coneEndings = RaycastEndings[0];
      float distanceFromWand = (coneEndings.transform.position - transform.position).magnitude;
      float maxDistanceFromCenter = distanceFromWand * Mathf.Tan((appex / 2.0f) * Mathf.Deg2Rad);
      float distanceFromCenterPerLevel = maxDistanceFromCenter / AppexLevels;

      for (int level = 0; level < AppexLevels; level++)
      {
        float distanceFromCenter = distanceFromCenterPerLevel * (level + 1);
        float angleBetweenPoints = 360.0f / AppexPointsPerLevel;

        for (int point = 0; point < AppexPointsPerLevel; point++)
        {
          float currentAngle = angleBetweenPoints * point * Mathf.Deg2Rad;
          Vector3 position = new Vector3(Mathf.Sin(currentAngle) * distanceFromCenter, 0.0f, Mathf.Cos(currentAngle) * distanceFromCenter);
          RaycastEndings[1 + level * AppexPointsPerLevel + point].transform.localPosition = position;
        }
      }
    }

    void CheckHovers()
    {
      //Assumes nothing is hovered
      List<GameObject> hoveredTargetsLastFrame = new List<GameObject>(hoveredTargets.Keys);
      foreach (GameObject targetObj in hoveredTargetsLastFrame)
        hoveredTargets[targetObj] = false;

      //Gets the new hovers and checks against the ones from the previous frame
      List<GameObject> targets = GetAffectedTargets();
      foreach (GameObject target in targets)
      {
        if (hoveredTargets.ContainsKey(target))
          hoveredTargets[target] = true;
        else
          hoveredTargets.Add(target, true);
      }

      //Selects the objects from previous frames that are not hovered this time
      unHoveredTargets.Clear();
      foreach (GameObject targetObj in hoveredTargets.Keys)
      {
        if (!hoveredTargets[targetObj])
          unHoveredTargets.Add(targetObj);
      }

      //Removes the reference to the objects that were not hovered this frame
      foreach (GameObject targetObj in unHoveredTargets)
        hoveredTargets.Remove(targetObj);

      //Gets the ordered list of the hovered objects
      hoveredTargetsByDistance = GetHoveredTargetsByDistance(RayType == RaycastType.PointCursor ? Tip.transform.position : transform.position);
      hoveredTargetsByCriteria = GetHoveredTargetsByCriteria(RayType == RaycastType.PointCursor ? Tip.transform.position : transform.position, (Tip.transform.position - transform.position).normalized);
    }

    List<GameObject> GetAffectedTargets()
    {
      List<GameObject> targets = new List<GameObject>();
      if (RayType == RaycastType.PointCursor)
      {
        Collider[] objects = Physics.OverlapSphere(Tip.transform.position, CalculateLocalTipRadious());
        for (int index = 0; index < objects.Length; index++)
        {
          if (objects[index].tag.CompareTo("Target") != 0)
            continue;
          targets.Add(objects[index].gameObject);
        }
      }
      else //for all other RayTypes it returns all of the targets that meet the ray
      {
        foreach (GameObject ending in RaycastEndings)
        {
          RaycastHit[] hits = ExecuteRaycast(ending.transform.position);
          for (int index = 0; index < hits.Length; index++)
          {
            if (hits[index].collider.tag.CompareTo("Target") != 0)
              continue;

            if (!targets.Contains(hits[index].collider.gameObject))
              targets.Add(hits[index].collider.gameObject);
          }
        }
      }

      return targets;
    }

    private RaycastHit[] ExecuteRaycast(Vector3 ending)
    {
      Vector3 origin = gameObject.transform.position;
      Vector3 direction = (ending - origin).normalized;
      RaycastHit[] hits = Physics.RaycastAll(origin, direction, 100f);

      return hits;
    }

    private float CalculateLocalTipRadious()
    {
      float tipLocalRadius = (Tip.collider as SphereCollider).radius;
      float tipRadius = (Tip.collider as SphereCollider).transform.TransformPoint(new Vector3(tipLocalRadius, 0f)).x;
      tipRadius = Mathf.Abs(tipRadius) / 10.0f;
      return tipRadius;
    }

    private List<GameObject> GetHoveredTargetsByDistance(Vector3 origin)
    {
      List<GameObject> temp = new List<GameObject>(hoveredTargets.Keys.Count);
      foreach (GameObject key in hoveredTargets.Keys)
        temp.Add(key);
      temp.Sort((a, b) =>
      {
        float distanceA = (a.transform.position - origin).magnitude;
        float distanceB = (b.transform.position - origin).magnitude;

        return (int)((distanceA - distanceB) * 1000);
      });
      return temp;
    }

    private List<GameObject> GetHoveredTargetsByCriteria(Vector3 origin, Vector3 rayDirection)
    {
      if (Ordering == OrderingCriteria.DistanceToWand)
        return GetHoveredTargetsByDistance(origin);

      List<GameObject> temp = new List<GameObject>(hoveredTargets.Keys.Count);
      foreach (GameObject key in hoveredTargets.Keys)
        temp.Add(key);

      if (Ordering == OrderingCriteria.DistanceToRay)
      {
        temp.Sort((a, b) =>
        {
          if (a == b)
            return 0;

          float distanceA = 0f, distanceB = 0f;

          Vector3 rayToA = a.transform.position - transform.position;
          float distanceAOnRayNormalized = Vector3.Dot(rayDirection.normalized, rayToA.normalized);
          float distanceAOnRay = distanceAOnRayNormalized * rayToA.magnitude;
          distanceA = Mathf.Sqrt(Mathf.Pow(rayToA.magnitude, 2f) - Mathf.Pow(distanceAOnRay, 2f));

          Vector3 rayToB = b.transform.position - transform.position;
          float distanceBOnRayNormalized = Vector3.Dot(rayDirection.normalized, rayToB.normalized);
          float distanceBOnRay = distanceBOnRayNormalized * rayToB.magnitude;
          distanceB = Mathf.Sqrt(Mathf.Pow(rayToB.magnitude, 2f) - Mathf.Pow(distanceBOnRay, 2f));

          return (int)((distanceA - distanceB) * 1000);
        });
      }

      return temp;
    }

    private void Disambiguate()
    {
      switch (RayType)
      {
        case RaycastType.RaySimple:
          hoveredTarget = GetDefaultHoveredObject();
          break;
        case RaycastType.RayAll:
          hoveredTarget = GetLastHoveredObject();
          break;
        case RaycastType.PointCursor:
          hoveredTarget = GetFirstHoveredObject();
          break;
        case RaycastType.AutoTwist:
        case RaycastType.LockTwist:
          if (State == RayState.Active)
            hoveredTarget = GetDefaultHoveredObject();
          else if (State == RayState.Disambiguating)
            hoveredTarget = DissambiguateOnAxis(2, TwistSegment, TwistDirection == TwistDirectionType.CounterClockWise ? 1 : -1);
          break;
        case RaycastType.LockDragFinger:
          if (State == RayState.Active)
            hoveredTarget = GetDefaultHoveredObject();
          else if (State == RayState.Disambiguating)
            hoveredTarget = DisambiguateWithTouch(1, DragSegment);
          break;
      }
    }

    private GameObject GetLastHoveredObject()
    {
      if (hoveredTargetsByDistance.Count == 0)
        return null;

      return hoveredTargetsByDistance[hoveredTargetsByDistance.Count - 1];
    }

    private GameObject GetFirstHoveredObject()
    {
      if (hoveredTargetsByDistance.Count == 0)
        return null;

      return hoveredTargetsByDistance[0];
    }

    private GameObject GetDefaultHoveredObject()
    {
      if (hoveredTargetsByCriteria.Count == 0)
        return null;

      return hoveredTargetsByCriteria[0];
    }

    private GameObject DissambiguateOnAxis(int axisIndex, float segmentForAxis, int direction)
    {
      if (hoveredTargetsByDistance.Count == 0)
      {
        disambiguationIndex = -1;
        return null;
      }
      else
      {
        Quaternion diff = invDisambiguationRotStart * RotationProvider.Instance.Rotation;
        float rotationInAxis = diff.eulerAngles[axisIndex];
        if (rotationInAxis > 180f)
          rotationInAxis = -1 * (360 - rotationInAxis);

        disambiguationIndex = (int)(rotationInAxis / segmentForAxis) * (int)Mathf.Sign(direction);
        disambiguationIndex = Mathf.Min(hoveredTargetsByDistance.Count - 1, Mathf.Max(0, disambiguationIndex));
        return hoveredTargetsByDistance[disambiguationIndex];
      }
    }

    private GameObject DisambiguateWithTouch(int axisIndex, float segmentForAxis)
    {
      if (hoveredTargetsByDistance.Count == 0)
      {
        disambiguationIndex = -1;
        return null;
      }
      else
      {
        disambiguationIndex = (int)(movingArgs.DragFromOrigin.magnitude / segmentForAxis);// * Mathf.Sign(movingArgs.DragFromOrigin[axisIndex]));
        disambiguationIndex = Mathf.Min(hoveredTargetsByDistance.Count - 1, Mathf.Max(0, disambiguationIndex));
        return hoveredTargetsByDistance[disambiguationIndex];
      }
    }

    private void NotifyTargets()
    {
      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
      args.Device = ControllerType.Raycast;
      args.IsConflict = hoveredTargets.Keys.Count > 1;
      args.NrOfConflictedTargets = hoveredTargets.Count;
      args.IndexOfConflictSolution = disambiguationIndex;
      args.PointerPx = Input.mousePosition;
      args.PointerPos = gameObject.transform.position;
      args.PointerQuat = State == RayState.Disambiguating ? disambiguationRotStart : RotationProvider.Instance.Rotation;
      args.Tag = transform.parent.name;

      bool setConflictSolution;
      switch (RayType)
      {
        case RaycastType.RaySimple:
        case RaycastType.PointCursor:
        case RaycastType.LockTwist:
        case RaycastType.LockDragFinger:
        case RaycastType.AutoTwist:
          setConflictSolution = true;
          break;
        case RaycastType.RayAll:
        default:
          setConflictSolution = false;
          break;
      }

      foreach (GameObject target in hoveredTargets.Keys)
      {
        args.IsConflictSolution = setConflictSolution && target == hoveredTarget;
        target.SendMessage("Hovered", args, SendMessageOptions.DontRequireReceiver);
      }

      args.IsConflictSolution = false;
      foreach (GameObject target in unHoveredTargets)
        target.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
    }

    public void UpdateRayPositionAndLenght()
    {
      //Sets the positions of the rays
      if (RayType == RaycastType.PointCursor)
        return;

      Vector3 direction = (Tip.transform.position - gameObject.transform.position).normalized;
      float distance = RayDistance;
      if (hoveredTargetsByDistance.Count > 0)
        distance = (transform.position - hoveredTargetsByDistance[hoveredTargetsByDistance.Count - 1].transform.position).magnitude;

      RayToTarget.SetPosition(0, gameObject.transform.position);
      RayToTarget.SetPosition(1, gameObject.transform.position + direction * distance);

      RayToInfinite.SetPosition(0, gameObject.transform.position + direction * distance);
      RayToInfinite.SetPosition(1, gameObject.transform.position + direction * (RayDistance - distance));
    }

    void UpdateRayColor()
    {
      //Sets the color of the ray
      Color newColor = Color.magenta;
      switch (State)
      {
        case RayState.Parked:
          newColor = ParkedColor; break;
        case RayState.Disambiguating:
          newColor = LockColor; break;
        case RayState.Active:
        default:
          newColor = BaseColor; break;
      }

      all_SynchRayColor((Vector4)newColor);
      if (Network.isClient)
        networkView.RPC("all_SynchRayColor", RPCMode.Others, (Vector3)(Vector4)newColor);
    }

    IEnumerator SelectionFeedback()
    {
      all_SynchRayColor((Vector4)SelectionColor);
      if (Network.isClient)
        networkView.RPC("all_SynchRayColor", RPCMode.Others, (Vector3)(Vector4)SelectionColor);

      yield return new WaitForSeconds(SelectionFeedbackMillis / 1000.0f);

      UpdateRayColor();
    }

    [RPC]
    void all_SynchRayColor(Vector3 shaftColor)
    {
      RayColor = new Color(shaftColor.x, shaftColor.y, shaftColor.z);
      RayToTarget.SetColors(RayColor, RayColor);
    }

    private void CheckSelection(MoverioTouchpadEventArgs mieArgs, bool handleHeisembergEffect)
    {
      if (handleHeisembergEffect)
      {
        //There is an effect here related to the Heisemberg effect.
        // -- When the finger is removed, the user might change the position of the points (for TouchDrag) or
        //    even rotate the hand back (for LockTwist and AutoLock). 
        // -- Therefore the checking on selection should happen as the state of things of about 100ms or 50ms before
        movingArgs.Position = MoverioInputProvider.Instance.GetHistoricValue(100); //for TouchDrag
        MoveTo(GetHistoricPosition(100), RotationProvider.Instance.GetHistoricValue(100)); //for hand trembling
      }

      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
      args.Device = ControllerType.Raycast;
      args.IsConflict = hoveredTargets.Count > 1 ? true : false;
      args.NrOfConflictedTargets = hoveredTargets.Count;
      args.IndexOfConflictSolution = disambiguationIndex;
      args.PointerPx = Input.mousePosition;
      args.PointerPos = gameObject.transform.position;
      args.PointerQuat = State == RayState.Disambiguating ? disambiguationRotStart : RotationProvider.Instance.Rotation;
      args.Tag = transform.parent.name;

      StartCoroutine(SelectionFeedback());

      switch (RayType)
      {
        case RaycastType.RayAll:
          CheckSelectionOnAll(args);
          break;
        case RaycastType.RaySimple:
        case RaycastType.LockTwist:
        case RaycastType.LockDragFinger:
        case RaycastType.PointCursor:
        case RaycastType.AutoTwist:
        default:
          if (hoveredTarget != null)
            hoveredTarget.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
          else
            CheckSelectionOnAll(args);
          break;
      }
    }

    private void CheckSelectionOnAll(SelectionControllerEventArgs args)
    {
      List<GameObject> targets = GetAffectedTargets();
      args.IsConflict = targets.Count > 1;

      if (targets.Count == 0)
      {
        SelectionEventArgs seArgs = new SelectionEventArgs(args);
        seArgs.Type = SelectionEventArgs.SelectionEventType.Selected;
        MessageBroker.BroadcastAll("OnSelected", seArgs);
      }
      else
      {
        args.IsConflict = targets.Count > 1 ? true : false;
        foreach (GameObject target in targets)
          target.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
      }
    }

    [RPC]
    void all_SynchRayType(string type)
    {
      RayType = (RaycastType)System.Enum.Parse(typeof(RaycastType), type);
      NotifyRayTypeChanged();
    }

    void NotifyRayTypeChanged()
    {
      if (RayType == RaycastType.PointCursor)
      {
        HideRay();
        if (UseExternalTrackingForPointCursor)
        {
          MoveWandToRaySource(RaycastSources.Find(position => position.name.Contains("External")));
          NotifyRaycastLocationChanged();
        }
      }
      else if (transform.parent.name.Contains("External"))
      {
        MoveWandToRaySource(InitialSource);
        NotifyRaycastLocationChanged();
      }

      disambiguationIndex = -1;
      SetRayState(RayState.Parked);
      DetermineMovementAsClick();

      MessageBroker.BroadcastAll("OnRayTypeChanged", RayType);
    }

    private void HideRay()
    {
      if (RayToTarget == null)
        return;

      RayToTarget.SetPosition(0, gameObject.transform.position);
      RayToTarget.SetPosition(1, gameObject.transform.position);
    }

    private void MoveWandToRaySource(GameObject raysource)
    {
      transform.parent = raysource.transform;
      transform.localPosition = Vector3.zero;

      //Saves the wands current location
      SaveHistory();
    }

    private void DetermineMovementAsClick()
    {
      if (LocksWithFinger)
        MoverioInputProvider.Instance.TreatMovementAsTouch = false;
      else
        MoverioInputProvider.Instance.TreatMovementAsTouch = true;
    }

    private bool LocksWithFinger
    {
      get
      {
        switch (RayType)
        {
          case RaycastType.LockDragFinger:
          case RaycastType.LockTwist:
            return true;
          case RaycastType.RaySimple:
          case RaycastType.RayAll:
          case RaycastType.PointCursor:
          case RaycastType.AutoTwist:
          default:
            return false;
        }
      }
    }

    [RPC]
    void all_SynchWandPosition(int locationIndex)
    {
      MoveWandToRaySource(RaycastSources[locationIndex]);
      NotifyRaycastLocationChanged();
    }

    private void NotifyRaycastLocationChanged()
    {
      SetRayState(RayState.Parked);
      SetDefaultRotation();
      MessageBroker.BroadcastAll("OnRaycastLocationChanged", transform.parent.gameObject);
    }

    void OnRotationBaselineSet()
    {
      if (!RunLocal || !IsCurrentController)
        return;

      SetDefaultRotation();
    }

    void OnReferenceFrameUpdated(ReferenceFrame refFrame)
    {
      if (!RunLocal || !IsCurrentController)
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

    private void SetDefaultRotation(GameObject target = null)
    {
      // Orient the entire head
      NeckJoint.transform.localRotation = Quaternion.Euler(0f, 0f, 0f);
      Vector3 tForward = transform.TransformVector(Vector3.forward);

      //When the source is external the ray should simply point straight ahead.
      if (RayType == RaycastType.PointCursor && UseExternalTrackingForPointCursor)
        transform.localRotation = Quaternion.Euler(0.0f, 0.0f, 0.0f);
      else if (transform.parent.name.Contains("External"))
        transform.localRotation = Quaternion.Euler(0.0f, 0.0f, 0.0f);
      else
      {
        if (target == null)
          target = InitialPointing;
        this.transform.LookAt(target.transform.position);
      }
    }

    private void StartDisambiguation()
    {
      if (hoveredTarget == null)
      {
        Vector3 origin = gameObject.transform.position;
        Vector3 direction = (Tip.transform.position - origin).normalized;
        Vector3 end = origin + direction * 100.0f;
        Vector3 endInScreen = EPSONcamera.GetComponent<Camera>().WorldToViewportPoint(end);

        if (endInScreen.x < 0.0f || endInScreen.x > 1.0f)
          return;
        if (endInScreen.y < 0.0f || endInScreen.y > 1.0f)
          return;
      }
      else if (!hoveredTarget.renderer.isVisible) //it does not allow locking if the target is out of the user's view
        return;

      //The code above has to be re-engineered to for situations where the end of the ray (void or the Hit point on a target)
      // is within the frustrum of the camera.

      disambiguationIndex = 0; //always selects the closest first
      disambiguationRotStart = RotationProvider.Instance.Rotation;
      invDisambiguationRotStart = Quaternion.Inverse(disambiguationRotStart);

      targetAtStartDisambiguation = hoveredTarget;

      SetRayState(RayState.Disambiguating);
      UpdateDragGuide();

      MessageBroker.BroadcastAll("OnRaycastLock");
    }

    private void StopDisambiguating(bool wasAborted, bool resetRayColor = false)
    {
      movingArgs = null;
      isDisambiguationAborted = wasAborted;
      hoveredTargetsByDistance.Clear();
      targetAtStartDisambiguation = null;

      SetRayState(RayState.Active, resetRayColor);
      UpdateDragGuide();

      MessageBroker.BroadcastAll("OnRaycastUnlock");
    }

    private void UpdateDragGuide()
    {
      if (DragGuide == null)
        return;

      DragGuide.enabled = false;

      if (RayType != RaycastType.LockDragFinger)
        return;

      if (State != RayState.Disambiguating)
        return;

      DragGuide.enabled = true;

      //Calculates the rotation angle
      Vector2 center = new Vector2(Screen.width * 3 / 4, Screen.height / 2);
      Vector2 direction = movingArgs.Position - center;

      //If the cursor is already close the the center of the right eye, it ignores the differences the hosizontan axis
      if (Mathf.Abs(direction.x) < 200)
        direction.x = 0;

      float angle = Vector2.Angle(Vector2.right, direction);

      if (movingArgs.Position.y > center.y)
      {
        if (movingArgs.Position.x < center.x)
          angle = 180 + (180 - angle);
        else
          angle = -angle;
      }

      DragGuide.angle = angle + 180;
    }

    private struct PositionHistory
    {
      public double Milliseconds;
      public Vector3 Position;
    }

    private Vector3 GetHistoricPosition(double millisIntoThePast)
    {
      if (History.Count == 0)
        return Vector2.zero;

      PositionHistory[] history = History.ToArray();
      double limit = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds - millisIntoThePast;

      int counter = -1;
      PositionHistory pastValue = history[++counter];

      while (pastValue.Milliseconds < limit && counter < (history.Length - 2))
        pastValue = history[++counter];

      return history[counter].Position;
    }

    private void SaveHistory()
    {
      double currentTimeMillis = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds;
      History.Enqueue(new PositionHistory() { Milliseconds = currentTimeMillis, Position = transform.position });

      //Removes the elements in the history Queue that are older than the set window
      PositionHistory his = History.Peek();
      while (his.Milliseconds < (currentTimeMillis - MillisecondsOfHistoryPosition))
      {
        History.Dequeue();
        his = History.Peek();
      }
    }

    override public void OnRunLocal()
    {
      WandRotation.SetActive(true);

      if (runLocal)
        DetermineMovementAsClick();
      else
      {
        HideRay();
        if (!IsCurrentController)
          WandRotation.SetActive(false);
      }
    }

    private System.DateTime autoLockConditionsMet = System.DateTime.MaxValue;

    private void EvaluateAutoPark()
    {
      if (!AutoPark)
        return;

      Vector3 origin = gameObject.transform.position;
      Vector3 direction = (Tip.transform.position - origin).normalized;
      Ray wandRay = new Ray(origin, direction);

      float distance = 0f;
      bool rayHitsBackWall = false;
      Plane[] viewPlanes = GeometryUtility.CalculateFrustumPlanes(EPSONcamera.GetComponent<Camera>());
      foreach (Plane plane in viewPlanes)
      {
        //Checks against the camera depth plane
        float farClipPlane = EPSONcamera.GetComponent<Camera>().farClipPlane - 10;
        if (plane.distance < farClipPlane)
          continue;

        if (!plane.Raycast(wandRay, out distance))
          break;

        float maxDistance = plane.distance / Mathf.Cos(AutoParkMaxAngleDegrees * Mathf.Deg2Rad);
        if (distance <= maxDistance)
          rayHitsBackWall = true;

        break;
      }

      bool isWandMoving = true;
      if (SystemInfo.deviceType == DeviceType.Handheld)
      {
        Quaternion historicRot = RotationProvider.Instance.RotationPrev;
        Quaternion diff = Quaternion.Inverse(historicRot) * RotationProvider.Instance.Rotation;
        Vector3 diffNegPos = RotationProvider.RotAsPosNeg(diff);

        if (diffNegPos.magnitude <= AutoParkStillAngleDegrees)
          isWandMoving = false;
      }

      if (rayHitsBackWall && isWandMoving)
      {
        autoLockConditionsMet = System.DateTime.MaxValue;
      }
      else
      {
        if (autoLockConditionsMet == System.DateTime.MaxValue)
          autoLockConditionsMet = System.DateTime.Now;

        if ((System.DateTime.Now - autoLockConditionsMet).TotalMilliseconds < AutoParkMillis)
          return;

        ParkRay();

        Debug.Log("RaycastController parked: rayHitsBackWall: " + rayHitsBackWall + ", isWandMoving: " + isWandMoving);
      }
    }

    private void ParkRay()
    {
      SetRayState(RayState.Parked);
    }

    private void UnParkRay()
    {
      RotationProvider.Instance.SetBaselineRotation();
      SetRayState(RayState.Active);
    }

    private void SetRayState(RayState newState, bool updateRayColor = true)
    {
      _state = newState;

      if (RayType == RaycastType.PointCursor)
        _state = RayState.Active;

      switch (_state)
      {
        case RayState.Parked:

          //Unhovers everything and clears the cache
          unHoveredTargets.Clear();
          foreach (GameObject targetObj in hoveredTargets.Keys)
            unHoveredTargets.Add(targetObj);
          hoveredTargets.Clear();
          NotifyTargets();

          break;
        case RayState.Active:
          autoLockConditionsMet = System.DateTime.Now;
          break;
        case RayState.Disambiguating:
          break;
      }

      if (updateRayColor)
        UpdateRayColor();
    }

  }

}