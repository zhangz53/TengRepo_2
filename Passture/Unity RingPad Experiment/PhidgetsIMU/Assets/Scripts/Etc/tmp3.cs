//// Edit: Taking IMU data from glasses AND external(phidgets), modifying NeckJoint transform in Body/World based selection

//using UnityEngine;
//using System.Collections;
//using System.Text.RegularExpressions;
//using System.Collections.Generic;
//using UnityMoverioBT200.Scripts.Util;
//using UnityMoverioBT200.Scripts.Providers;

//namespace UnityMoverioBT200.Scripts.Controllers
//{

//  public class RaycastController : MoverioController
//  {

//    /** Behaviour changes whether it's connected or standalone, and according to the reference frame
//     *  -- the change is controlled from the SceneUI script
//     * 							  ViewCentric		   BodyCentric		WorldCentric
//     *  Connected			LockCamera			 UnlockCamera		UnlockCamera
//     *  StandAlone		LockCamera			 UnlockCamera		UnlockCamera
//     */
//    public enum BehaviourType { LockCamera, UnlockCamera }
//    public BehaviourType Behaviour = BehaviourType.LockCamera;

//    private GameObject NeckJoint;

//    public enum RaycastType { RaySimple, RayAll, PointCursor, LockDragFinger, LockTwist, AutoTwist }

//    public enum TwistDirectionType { ClockWise, CounterClockWise }

//    /** These are the public properties set on the Unity editor **/
//    public GameObject InitialPointing;
//    public RaycastType RayType = RaycastType.RaySimple;
//    public TwistDirectionType TwistDirection = TwistDirectionType.ClockWise;

//    public float RayDistance = 100f;

//    public bool UseApex = false;
//    public float AppexDegrees = 2.0f;
//    public int AppexLevels = 2;
//    public int AppexPointsPerLevel = 10;

//    public int SelectionFeedbackMillis = 150; //milliseconds
//    public int MillisecondsOfHistoryPosition = 500;

//    public Color BaseColor = Color.gray;
//    public Color LockColor = Color.green;
//    public Color SelectionColor = Color.blue;

//    public float TwistSegment = 25.0f; //degrees
//    public float DragSegment = 50.0f; //pixels

//    public GameObject InitialSource;
//    public bool UseExternalTrackingForPointCursor = false;

//    public bool AllowChangeRayType = false;
//    public bool AllowChangeRaySource = false;

//    [Header("ALERT: Experimental, uses lots of resources, flickering")]
//    [Tooltip("ALERT: Experimental, uses lots of resources, flickering")]
//    public bool UseRayLights = false;
//    public float RayLightRange = 0.15f;
//    public float RayLightIntensity = 1.0f;
//    public float RayLightSeparation = 0.25f;
//    public float RayLightBrightnessBump = 50f / 255f;
//    public int RaySpeedMillisenconds = 100;
//    /** End **/

//    private Queue<PositionHistory> History = new Queue<PositionHistory>();

//    private GameObject EPSONcamera { get; set; }
//    private GameObject WandRotation { get; set; }
//    private GameObject Tip { get; set; }

//    private List<GameObject> RaycastSources = new List<GameObject>();
//    private List<GameObject> RaycastEndings = new List<GameObject>();

//    private Color RayColor;
//    private LineRenderer RayToTarget;
//    private List<Light> RayLights;

//    private GameObject hoveredTarget = null;
//    private Dictionary<GameObject, bool> hoveredTargets = new Dictionary<GameObject, bool>();
//    private List<GameObject> orderedHoveredTargets = new List<GameObject>();
//    private List<GameObject> unHoveredTargets = new List<GameObject>();

//    private bool isDissambiguating = false;
//    private bool isDisambiguationAborted = false;
//    private Quaternion disambiguationRotStart = Quaternion.identity;
//    private Quaternion invDisambiguationRotStart = Quaternion.identity;
//    private int disambiguationIndex = -1;

//    ~RaycastController()
//    {
//      Debug.Log("Destroying the RaycastController");
//    }

//    // Use this for initialization 
//    void Awake()
//    {
//      NeckJoint = transform.parent.parent.FindChild("HeadControl/NeckJoint").gameObject;

//      EPSONcamera = transform.parent.parent.FindChild("HeadControl/NeckJoint/Camera BT-200/Stereoscopic Camera").gameObject;
//      WandRotation = transform.FindChild("WandRotation").gameObject;
//      Tip = transform.FindChild("WandRotation/WandJoint/Tip").gameObject;

//      PrepareRayGraphics();
//      PrepareRaycast();
//      PrepareRayLights();

//      MoveTo(transform.position, transform.rotation);
//    }

//    void Start()
//    {
//      NotifyRayTypeChanged();
//      NotifyRaycastLocationChanged();
//      SetDefaults();
//    }

//    public void SetDefaults()
//    {
//      SetDefaultRotation();
//    }

//    void PrepareRayGraphics()
//    {
//      RayColor = BaseColor;

//      RayToTarget = gameObject.AddComponent<LineRenderer>();
//      RayToTarget.material = new Material(Shader.Find("Particles/Additive"));
//      RayToTarget.SetColors(RayColor, RayColor);
//      RayToTarget.SetWidth(0.0025f, 0.0025f);
//      RayToTarget.SetVertexCount(2);

//      Vector3 direction = (Tip.transform.position - gameObject.transform.position).normalized;
//      RayToTarget.SetPosition(0, gameObject.transform.position);
//      RayToTarget.SetPosition(1, gameObject.transform.position + direction);
//    }

//    private void PrepareRaycast()
//    {
//      //Gets the player's endpoints and raysources
//      Transform[] children = gameObject.transform.parent.parent.GetComponentsInChildren<Transform>();
//      foreach (Transform go in children)
//      {
//        if (go == null)
//          continue;
//        if (go.tag == "RaySource")
//          RaycastSources.Add(go.gameObject);
//        else if (go.tag == "RayEnding")
//          RaycastEndings.Add(go.gameObject);
//      }

//      //Finds the external source
//      GameObject[] all = GameObject.FindGameObjectsWithTag("RaySource");
//      foreach (GameObject go in all)
//      {
//        if (go == null)
//          continue;
//        if (!go.name.Contains("External"))
//          continue;

//        RaycastSources.Add(go);
//        break;
//      }

//      //Generates all other ray endings
//      if (UseApex)
//      {
//        GameObject coneEndings = RaycastEndings[0];
//        float distanceFromWand = (coneEndings.transform.position - transform.position).magnitude;

//        float maxDistanceFromCenter = distanceFromWand * Mathf.Tan((AppexDegrees / 2.0f) * Mathf.Deg2Rad);
//        float distanceFromCenterPerLevel = maxDistanceFromCenter / AppexLevels;

//        for (int level = 0; level < AppexLevels; level++)
//        {
//          float distanceFromCenter = distanceFromCenterPerLevel * (level + 1);
//          float angleBetweenPoints = 360.0f / AppexPointsPerLevel;

//          for (int point = 0; point < AppexPointsPerLevel; point++)
//          {
//            float currentAngle = angleBetweenPoints * point * Mathf.Deg2Rad;
//            Vector3 position = new Vector3(Mathf.Sin(currentAngle) * distanceFromCenter, 0.0f, Mathf.Cos(currentAngle) * distanceFromCenter);

//            GameObject newConeEnding = new GameObject();
//            newConeEnding.name = "Level-" + level + "-Point-" + point;
//            newConeEnding.tag = "RayEnding";
//            newConeEnding.transform.parent = coneEndings.transform;
//            newConeEnding.transform.localRotation = Quaternion.Euler(0, 0, 0);
//            newConeEnding.transform.localPosition = position;

//            RaycastEndings.Add(newConeEnding);
//          }
//        }
//      }

//      MoveWandToRaySource(InitialSource);
//      SetDefaultRotation();
//      RotationProvider.Instance.SetBaselineRotation();
//    }

//    private void PrepareRayLights()
//    {
//      if (!UseRayLights)
//        return;

//      Color lightColor = new Color(BaseColor.r + RayLightBrightnessBump, BaseColor.g + RayLightBrightnessBump, BaseColor.b + RayLightBrightnessBump);
//      int totalLights = (int)(RayDistance / RayLightSeparation);

//      RayLights = new List<Light>();
//      for (int lightIndex = 0; lightIndex < totalLights; lightIndex++)
//      {
//        GameObject lightGameObject = new GameObject("RayLight-" + lightIndex);
//        lightGameObject.transform.parent = WandRotation.transform;

//        Light lightComp = lightGameObject.AddComponent<Light>();
//        lightComp.color = lightColor;
//        lightComp.type = LightType.Point;
//        lightComp.range = RayLightRange;
//        lightComp.intensity = RayLightIntensity;
//        lightComp.transform.localPosition = new Vector3(0f, 0f, RayLightSeparation * lightIndex);

//        RayLights.Add(lightComp);
//      }
//    }

//    // Update is called once per frame
//    void Update()
//    {
//      UpdateRayLights();

//      if (!RunLocal)
//        return;

//      //Does corrections
//      transform.localPosition = Vector3.zero;

//      //Saves the wands current location
//      SaveHistory();

//      //Gathers interaction data locally 
//      Quaternion orientation = isDissambiguating ? WandRotation.transform.localRotation : RotationProvider.Instance.Rotation;
//      Quaternion headOrientation = RotationProvider.Instance.HeadRotation;

//      //Calls the update method locally
//      MoveTo(transform.position, orientation);
//      MoveHead(headOrientation);

//      //Calls execution method remotely
//      if (Network.isClient)
//      {
//        networkView.RPC("MoveTo", RPCMode.Others, transform.position, orientation, isDissambiguating.ToString());
//        networkView.RPC("MoveHead", RPCMode.Others, headOrientation);
//      }
//    }

//    private float ligthDisplacement = 0f;

//    private void UpdateRayLights()
//    {
//      if (!UseRayLights)
//        return;

//      float raySpeed = RaySpeedMillisenconds / 1000f;
//      ligthDisplacement += RayLightSeparation * Time.deltaTime / raySpeed;
//      ligthDisplacement = ligthDisplacement % RayLightSeparation;

//      for (int lightIndex = 0; lightIndex < RayLights.Count; lightIndex++)
//        RayLights[lightIndex].transform.localPosition = new Vector3(0f, 0f, RayLightSeparation * lightIndex + ligthDisplacement);
//    }

//    [RPC]
//    private void MoveTo(Vector3 targetPosition, Quaternion targetRotation, string remoteIsDisambiguating = null)
//    {
//      if (!IsCurrentController)
//        return;

//      gameObject.transform.position = targetPosition;
//      WandRotation.transform.localRotation = targetRotation;

//      if (remoteIsDisambiguating != null)
//        isDissambiguating = bool.Parse(remoteIsDisambiguating);

//      //Checks the hovers which forces to update the main target and the ray positions
//      CheckHovers();
//      Disambiguate();
//      NotifyTargets();
//      UpdateRayPositionAndLenght();

//      foreach (GameObject ending in RaycastEndings)
//        Debug.DrawRay(transform.position, ending.transform.position - transform.position, RayColor);
//    }

//    // Use to move head orientation when camera is unlocked
//    [RPC]
//    private void MoveHead(Quaternion rotation)
//    {
//      if (!IsCurrentController)
//        return;

//      switch (Behaviour)
//      {
//        case BehaviourType.LockCamera:
//          break;
//        case BehaviourType.UnlockCamera:
//          NeckJoint.transform.localRotation = rotation;
//          break;
//      }
//    }

//    void CheckHovers()
//    {
//      if (isDissambiguating)
//        return;

//      //Assumes nothing is hovered
//      List<GameObject> hoveredTargetsLastFrame = new List<GameObject>(hoveredTargets.Keys);
//      foreach (GameObject targetObj in hoveredTargetsLastFrame)
//        hoveredTargets[targetObj] = false;

//      //Gets the new hovers and checks against the ones from the previous frame
//      List<GameObject> targets = GetAffectedTargets();
//      foreach (GameObject target in targets)
//      {
//        if (hoveredTargets.ContainsKey(target))
//          hoveredTargets[target] = true;
//        else
//          hoveredTargets.Add(target, true);
//      }

//      //Selects the objects from previous frames that are not hovered this time
//      unHoveredTargets.Clear();
//      foreach (GameObject targetObj in hoveredTargets.Keys)
//      {
//        if (!hoveredTargets[targetObj])
//          unHoveredTargets.Add(targetObj);
//      }

//      //Removes the reference to the objects that were not hovered this frame
//      foreach (GameObject targetObj in unHoveredTargets)
//        hoveredTargets.Remove(targetObj);

//      //Gets the ordered list of the hovered objects
//      orderedHoveredTargets = GetOrderedHoveredTargets(RayType == RaycastType.PointCursor ? Tip.transform.position : transform.position);
//    }

//    List<GameObject> GetAffectedTargets()
//    {
//      List<GameObject> targets = new List<GameObject>();
//      if (RayType == RaycastType.PointCursor)
//      {
//        Collider[] objects = Physics.OverlapSphere(Tip.transform.position, CalculateLocalTipRadious());
//        for (int index = 0; index < objects.Length; index++)
//        {
//          if (objects[index].tag.CompareTo("Target") != 0)
//            continue;
//          targets.Add(objects[index].gameObject);
//        }
//      }
//      else //for all other RayTypes it returns all of the targets that meet the ray
//      {
//        foreach (GameObject ending in RaycastEndings)
//        {
//          RaycastHit[] hits = ExecuteRaycast(ending.transform.position);
//          for (int index = 0; index < hits.Length; index++)
//          {
//            if (hits[index].collider.tag.CompareTo("Target") != 0)
//              continue;

//            if (!targets.Contains(hits[index].collider.gameObject))
//              targets.Add(hits[index].collider.gameObject);
//          }
//        }
//      }

//      return targets;
//    }

//    private RaycastHit[] ExecuteRaycast(Vector3 ending)
//    {
//      Vector3 origin = gameObject.transform.position;
//      Vector3 direction = (ending - origin).normalized;
//      RaycastHit[] hits = Physics.RaycastAll(origin, direction, 100f);

//      return hits;
//    }

//    private float CalculateLocalTipRadious()
//    {
//      float tipLocalRadius = (Tip.collider as SphereCollider).radius;
//      float tipRadius = (Tip.collider as SphereCollider).transform.TransformPoint(new Vector3(tipLocalRadius, 0f)).x;
//      tipRadius = Mathf.Abs(tipRadius) / 10.0f;
//      return tipRadius;
//    }

//    private List<GameObject> GetOrderedHoveredTargets(Vector3 referenceObjectPos)
//    {
//      List<GameObject> temp = new List<GameObject>(hoveredTargets.Keys.Count);
//      foreach (GameObject key in hoveredTargets.Keys)
//        temp.Add(key);
//      temp.Sort((a, b) =>
//      {
//        float distanceA = (a.transform.position - referenceObjectPos).magnitude;
//        float distanceB = (b.transform.position - referenceObjectPos).magnitude;

//        return (int)((distanceA - distanceB) * 1000);
//      });
//      return temp;
//    }

//    private void Disambiguate()
//    {
//      switch (RayType)
//      {
//        case RaycastType.RaySimple:
//          hoveredTarget = GetFirstHoveredObject();
//          break;
//        case RaycastType.RayAll:
//          hoveredTarget = GetLastHoveredObject();
//          break;
//        case RaycastType.PointCursor:
//          hoveredTarget = GetFirstHoveredObject();
//          break;
//        case RaycastType.AutoTwist:
//        case RaycastType.LockTwist:
//          hoveredTarget = GetFirstHoveredObject();
//          if (isDissambiguating)
//            hoveredTarget = DissambiguateOnAxis(2, TwistSegment, TwistDirection == TwistDirectionType.CounterClockWise ? 1 : -1);
//          break;
//        case RaycastType.LockDragFinger:
//          hoveredTarget = GetFirstHoveredObject();
//          if (isDissambiguating)
//            hoveredTarget = DisambiguateWithTouch(1, DragSegment);
//          break;
//      }
//    }

//    private GameObject GetLastHoveredObject()
//    {
//      if (orderedHoveredTargets.Count == 0)
//        return null;

//      return orderedHoveredTargets[orderedHoveredTargets.Count - 1];
//    }

//    private GameObject GetFirstHoveredObject()
//    {
//      if (orderedHoveredTargets.Count == 0)
//        return null;

//      return orderedHoveredTargets[0];
//    }

//    private GameObject DissambiguateOnAxis(int axisIndex, float segmentForAxis, int direction)
//    {
//      if (orderedHoveredTargets.Count == 0)
//      {
//        disambiguationIndex = -1;
//        return null;
//      }
//      else
//      {
//        Quaternion diff = invDisambiguationRotStart * RotationProvider.Instance.Rotation;
//        float rotationInAxis = diff.eulerAngles[axisIndex];
//        if (rotationInAxis > 180f)
//          rotationInAxis = -1 * (360 - rotationInAxis);

//        disambiguationIndex = (int)(rotationInAxis / segmentForAxis) * (int)Mathf.Sign(direction);
//        disambiguationIndex = Mathf.Min(orderedHoveredTargets.Count - 1, Mathf.Max(0, disambiguationIndex));
//        return orderedHoveredTargets[disambiguationIndex];
//      }
//    }

//    private GameObject DisambiguateWithTouch(int axisIndex, float segmentForAxis)
//    {
//      if (orderedHoveredTargets.Count == 0)
//      {
//        disambiguationIndex = -1;
//        return null;
//      }
//      else
//      {
//        disambiguationIndex = (int)(movingArgs.DragFromOrigin.magnitude / segmentForAxis);// * Mathf.Sign(movingArgs.DragFromOrigin[axisIndex]));
//        disambiguationIndex = Mathf.Min(orderedHoveredTargets.Count - 1, Mathf.Max(0, disambiguationIndex));
//        return orderedHoveredTargets[disambiguationIndex];
//      }
//    }

//    private void NotifyTargets()
//    {
//      SelectionControllerEventArgs args = new SelectionControllerEventArgs(null);
//      args.Device = ControllerType.Raycast;
//      args.IsConflict = hoveredTargets.Keys.Count > 1;
//      args.NrOfConflictedTargets = hoveredTargets.Count;
//      args.IndexOfConflictSolution = disambiguationIndex;
//      args.PointerPx = Input.mousePosition;
//      args.PointerPos = gameObject.transform.position;
//      args.PointerQuat = isDissambiguating ? disambiguationRotStart : RotationProvider.Instance.Rotation;
//      args.Tag = transform.parent.name;

//      if (RayType == RaycastType.RaySimple)
//      {
//        args.IsConflictSolution = true;
//        if (hoveredTarget != null)
//          hoveredTarget.SendMessage("Hovered", args, SendMessageOptions.DontRequireReceiver);

//        args.IsConflictSolution = false;
//        foreach (GameObject target in hoveredTargets.Keys)
//        {
//          if (target == hoveredTarget)
//            continue;
//          target.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
//        }

//        foreach (GameObject target in unHoveredTargets)
//          target.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
//      }
//      else
//      {
//        bool setConflictSolution;
//        switch (RayType)
//        {
//          case RaycastType.PointCursor:
//          case RaycastType.LockTwist:
//          case RaycastType.LockDragFinger:
//          case RaycastType.AutoTwist:
//            setConflictSolution = true;
//            break;
//          case RaycastType.RayAll:
//          default:
//            setConflictSolution = false;
//            break;
//        }

//        foreach (GameObject target in hoveredTargets.Keys)
//        {
//          args.IsConflictSolution = setConflictSolution && target == hoveredTarget;
//          target.SendMessage("Hovered", args, SendMessageOptions.DontRequireReceiver);
//        }

//        args.IsConflictSolution = false;
//        foreach (GameObject target in unHoveredTargets)
//          target.SendMessage("NotHovered", args, SendMessageOptions.DontRequireReceiver);
//      }
//    }

//    public void UpdateRayPositionAndLenght()
//    {
//      //Sets the positions of the rays
//      if (isDissambiguating || RayType == RaycastType.PointCursor)
//        return;

//      Vector3 direction = (Tip.transform.position - gameObject.transform.position).normalized;
//      float distance = RayDistance;
//      if (orderedHoveredTargets.Count > 0)
//        distance = (transform.position - orderedHoveredTargets[orderedHoveredTargets.Count - 1].transform.position).magnitude;

//      RayToTarget.SetPosition(0, gameObject.transform.position);
//      RayToTarget.SetPosition(1, gameObject.transform.position + direction * distance);

//      //Turns off all lights
//      for (float position = 0f; position < distance; position += RayLightSeparation)
//      { }
//    }

//    void UpdateRayColor()
//    {
//      //Sets the color of the ray
//      RayColor = isDissambiguating ? LockColor : BaseColor;
//      RayToTarget.SetColors(RayColor, RayColor);

//      //Synchronizes color
//      if (Network.isClient)
//        networkView.RPC("SynchRayColor", RPCMode.Others, new Vector3(RayColor.r, RayColor.g, RayColor.b));
//    }

//    IEnumerator SelectionFeedback()
//    {
//      RayColor = SelectionColor;
//      RayToTarget.SetColors(RayColor, RayColor);
//      if (Network.isClient)
//        networkView.RPC("SynchRayColor", RPCMode.Others, new Vector3(RayColor.r, RayColor.g, RayColor.b));

//      yield return new WaitForSeconds(SelectionFeedbackMillis / 1000.0f);

//      UpdateRayColor();
//    }

//    [RPC]
//    void SynchRayColor(Vector3 shaftColor)
//    {
//      RayColor = new Color(shaftColor.x, shaftColor.y, shaftColor.z);
//      RayToTarget.SetColors(RayColor, RayColor);
//    }

//    void OnTouchStarted(MoverioTouchpadEventArgs args)
//    {
//      if (!RunLocal)
//        return;

//      switch (RayType)
//      {
//        case RaycastType.LockTwist:
//        case RaycastType.LockDragFinger:
//          movingArgs = args;
//          MoveTo(GetHistoricPosition(100), RotationProvider.Instance.GetHistoricValue(100));
//          StartDisambiguation();
//          break;
//        case RaycastType.RaySimple:
//        case RaycastType.RayAll:
//        case RaycastType.PointCursor:
//        case RaycastType.AutoTwist: //does nothing, all selections happen on touchended
//        default:
//          break;
//      }
//    }

//    private MoverioTouchpadEventArgs movingArgs = null;

//    void OnTouchMoved(MoverioTouchpadEventArgs args)
//    {
//      if (!RunLocal)
//        return;

//      movingArgs = args;
//    }

//    void OnTouchEnded(MoverioTouchpadEventArgs args)
//    {
//      if (!RunLocal)
//        return;

//      movingArgs = args;
//      if (!isDisambiguationAborted)
//        CheckSelection(args);

//      StopDisambiguating();
//    }

//    void OnTwist(RotationProvider.TwistEvent twist)
//    {
//      if (!RunLocal)
//        return;

//      if (isDissambiguating)
//      {
//        if (twist.Type == RotationProvider.TwistTriggerTypes.Up)
//          StopDisambiguating(true);
//      }
//      else if (RayType == RaycastType.AutoTwist)
//      {
//        if ((twist.Type == RotationProvider.TwistTriggerTypes.ScrewRight && TwistDirection == TwistDirectionType.CounterClockWise) ||
//            (twist.Type == RotationProvider.TwistTriggerTypes.ScrewLeft && TwistDirection == TwistDirectionType.ClockWise))
//        {
//          //Returns the wand to the orientation where the twist started
//          //IMPORTANT: We should also return to the initial position
//          MoveTo(GetHistoricPosition(twist.Milliseconds), twist.initialRot);

//          //Sets the wand in disambiguiation mode
//          StartDisambiguation();
//        }
//      }
//    }

//    void CheckSelection(MoverioTouchpadEventArgs mieArgs)
//    {
//      //There is an effect here related to the Heisemberg effect.
//      // -- When the finger is removed, the user might change the position of the points (for TouchDrag) or
//      //    even rotate the hand back (for LockTwist and AutoLock). 
//      // -- Therefore the checking on selection should happen as the state of things of about 100ms or 50ms before
//      movingArgs.Position = MoverioInputProvider.Instance.GetHistoricValue(100); //for TouchDrag
//      MoveTo(GetHistoricPosition(100), RotationProvider.Instance.GetHistoricValue(100)); //for hand trembling

//      SelectionControllerEventArgs args = new SelectionControllerEventArgs(mieArgs);
//      args.Device = ControllerType.Raycast;
//      args.IsConflict = hoveredTargets.Count > 1 ? true : false;
//      args.NrOfConflictedTargets = hoveredTargets.Count;
//      args.IndexOfConflictSolution = disambiguationIndex;
//      args.PointerPx = Input.mousePosition;
//      args.PointerPos = gameObject.transform.position;
//      args.PointerQuat = isDissambiguating ? disambiguationRotStart : RotationProvider.Instance.Rotation;
//      args.Tag = transform.parent.name;

//      StartCoroutine(SelectionFeedback());

//      switch (RayType)
//      {
//        case RaycastType.RayAll:
//          CheckSelectionOnAll(args);
//          break;
//        case RaycastType.RaySimple:
//        case RaycastType.LockTwist:
//        case RaycastType.LockDragFinger:
//        case RaycastType.PointCursor:
//        case RaycastType.AutoTwist:
//        default:
//          if (hoveredTarget != null)
//            hoveredTarget.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
//          else
//            CheckSelectionOnAll(args);
//          break;
//      }
//    }

//    private void CheckSelectionOnAll(SelectionControllerEventArgs args)
//    {
//      List<GameObject> targets = GetAffectedTargets();
//      args.IsConflict = targets.Count > 1;

//      if (targets.Count == 0)
//      {
//        SelectionEventArgs seArgs = new SelectionEventArgs(args);
//        seArgs.Type = SelectionEventArgs.SelectionEventType.Selected;

//        MessageBroker.BroadcastAll("OnSelected", seArgs);
//      }
//      else
//      {
//        args.IsConflict = targets.Count > 1 ? true : false;
//        foreach (GameObject target in targets)
//          target.SendMessage("Selected", args, SendMessageOptions.DontRequireReceiver);
//      }
//    }

//    void OnGUI()
//    {
//      if (!ShowGUI || !RunLocal)
//        return;

//      float heightEM = 0;
//      if (AllowChangeRayType)
//      {
//        System.Array extensionMethods = System.Enum.GetValues(typeof(RaycastType));
//        heightEM = 25 + 25 * extensionMethods.Length + 25;
//        GUILayout.BeginArea(new Rect(Screen.width - 100, 0, 100, heightEM));
//        GUILayout.Label("Ray Type:", GUILayout.Width(Screen.width), GUILayout.Height(20));
//        foreach (RaycastType type in extensionMethods)
//        {
//          bool isCurrent = RayType == type;
//          if (GUILayout.Toggle(isCurrent,
//                               type.ToString(),
//                               GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//          {
//            RayType = type;
//            NotifyRayTypeChanged();

//            if (Network.isClient || Network.isServer)
//              networkView.RPC("SynchRayType", RPCMode.OthersBuffered, RayType.ToString());
//          }
//        }
//        GUILayout.EndArea();
//      }

//      if (AllowChangeRaySource)
//      {
//        GUILayout.BeginArea(new Rect(Screen.width - 100, heightEM, 100, 25 + 25 * (RaycastSources.Count + 1) + 10));
//        GUILayout.Label("Wand Location", GUILayout.Width(100), GUILayout.Height(25));
//        for (int index = 0; index < RaycastSources.Count; index++)
//        {
//          bool isCurrent = transform.parent == RaycastSources[index].transform;
//          if (GUILayout.Toggle(isCurrent,
//                               RaycastSources[index].name,
//                               GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
//          {
//            MoveWandToRaySource(RaycastSources[index]);
//            NotifyRaycastLocationChanged();

//            if (Network.isClient || Network.isServer)
//              networkView.RPC("SynchWandPosition", RPCMode.OthersBuffered, index);
//          }
//        }
//        GUILayout.EndArea();
//      }
//    }

//    private void MoveWandToRaySource(GameObject raysource)
//    {
//      transform.parent = raysource.transform;
//      transform.localPosition = Vector3.zero;

//      //Saves the wands current location
//      SaveHistory();
//    }

//    [RPC]
//    void SynchRayType(string type)
//    {
//      RayType = (RaycastType)System.Enum.Parse(typeof(RaycastType), type);
//      NotifyRayTypeChanged();
//    }

//    void NotifyRayTypeChanged()
//    {
//      disambiguationIndex = -1;

//      if (RayType == RaycastType.PointCursor)
//      {
//        HideRay();

//        if (UseExternalTrackingForPointCursor)
//        {
//          MoveWandToRaySource(RaycastSources.Find(position => position.name.Contains("External")));
//          NotifyRaycastLocationChanged();
//        }
//      }
//      else if (transform.parent.name.Contains("External"))
//      {
//        MoveWandToRaySource(InitialSource);
//        NotifyRaycastLocationChanged();
//      }

//      DetermineMovementAsClick();

//      MessageBroker.BroadcastAll("OnRayTypeChanged", RayType);
//    }

//    private void HideRay()
//    {
//      if (RayToTarget == null)
//        return;

//      RayToTarget.SetPosition(0, gameObject.transform.position);
//      RayToTarget.SetPosition(1, gameObject.transform.position);
//    }

//    private void DetermineMovementAsClick()
//    {
//      switch (RayType)
//      {
//        case RaycastType.RaySimple:
//        case RaycastType.RayAll:
//        case RaycastType.PointCursor:
//        case RaycastType.AutoTwist:
//          MoverioInputProvider.Instance.TreatMovementAsTouch = true;
//          break;
//        case RaycastType.LockDragFinger:
//        case RaycastType.LockTwist:
//        default:
//          MoverioInputProvider.Instance.TreatMovementAsTouch = false;
//          break;
//      }
//    }

//    [RPC]
//    void SynchWandPosition(int locationIndex)
//    {
//      MoveWandToRaySource(RaycastSources[locationIndex]);
//      NotifyRaycastLocationChanged();
//    }

//    void NotifyRaycastLocationChanged()
//    {
//      SetDefaultRotation();
//      MessageBroker.BroadcastAll("OnRaycastLocationChanged", transform.parent.gameObject);
//    }

//    void OnRotationBaselineSet()
//    {
//      if (!RunLocal)
//        return;

//      SetDefaultRotation();
//    }

//    void OnReferenceFrameUpdated(ReferenceFrame refFrame)
//    {
//      if (!RunLocal)
//        return;

//      SetDefaultRotation();

//      /** Behaviour changes whether it's connected or standalone, and according to the reference frame
//     *  -- the change is controlled from the SceneUI script
//     * 							  ViewCentric		   BodyCentric		WorldCentric
//     *  Connected			LockCamera			 UnlockCamera		UnlockCamera
//     *  StandAlone		LockCamera			 UnlockCamera		UnlockCamera
//     */

//      if (refFrame == ReferenceFrame.View)
//        Behaviour = BehaviourType.LockCamera;
//      else
//        Behaviour = BehaviourType.UnlockCamera;
//    }

//    private void SetDefaultRotation(GameObject target = null)
//    {
//      //When the source is external the ray should simply point straight ahead.

//      // Orient the entire head
//      NeckJoint.transform.localRotation = Quaternion.Euler(0f, 0f, 0f);

//      Vector3 tForward = transform.TransformVector(Vector3.forward);

//      //if (RayType == RaycastType.PointCursor && UseExternalTrackingForPointCursor)
//      //{
//      //  this.transform.rotation = Quaternion.Euler(0.0f, 0.0f, 0.0f);
//      //}
//      //else
//      //{
//      if (target == null)
//        target = InitialPointing;
//      this.transform.LookAt(target.transform.position);
//      //}
//    }

//    private void StartDisambiguation()
//    {
//      if (hoveredTarget == null)
//      {
//        Vector3 origin = gameObject.transform.position;
//        Vector3 direction = (Tip.transform.position - origin).normalized;
//        Vector3 end = origin + direction * 100.0f;
//        Vector3 endInScreen = EPSONcamera.GetComponent<Camera>().WorldToViewportPoint(end);

//        if (endInScreen.x < 0.0f || endInScreen.x > 1.0f)
//          return;
//        if (endInScreen.y < 0.0f || endInScreen.y > 1.0f)
//          return;
//      }
//      else if (!hoveredTarget.renderer.isVisible)
//        return;

//      //The code above has to be re-engineered to for situations where the end of the ray (void or the Hit point on a target)
//      // is within the frustrum of the camera.

//      disambiguationIndex = 0; //always selects the closest first
//      disambiguationRotStart = RotationProvider.Instance.Rotation;
//      invDisambiguationRotStart = Quaternion.Inverse(disambiguationRotStart);

//      isDissambiguating = true;
//      UpdateRayColor();

//      MessageBroker.BroadcastAll("OnRaycastLock");
//    }

//    private void StopDisambiguating(bool abortedDisambiguation = false)
//    {
//      movingArgs = null;
//      isDisambiguationAborted = abortedDisambiguation;
//      orderedHoveredTargets.Clear();

//      isDissambiguating = false;
//      if (abortedDisambiguation)
//        UpdateRayColor();

//      MessageBroker.BroadcastAll("OnRaycastUnlock");
//    }

//    private struct PositionHistory
//    {
//      public double Milliseconds;
//      public Vector3 Position;
//    }

//    private Vector3 GetHistoricPosition(double millisIntoThePast)
//    {
//      if (History.Count == 0)
//        return Vector2.zero;

//      PositionHistory[] history = History.ToArray();
//      double limit = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds - millisIntoThePast;

//      int counter = -1;
//      PositionHistory pastValue = history[++counter];

//      while (pastValue.Milliseconds < limit && counter < (history.Length - 2))
//        pastValue = history[++counter];

//      return history[counter].Position;
//    }

//    private void SaveHistory()
//    {
//      double currentTimeMillis = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds;
//      History.Enqueue(new PositionHistory() { Milliseconds = currentTimeMillis, Position = transform.position });

//      //Removes the elements in the history Queue that are older than the set window
//      PositionHistory his = History.Peek();
//      while (his.Milliseconds < (currentTimeMillis - MillisecondsOfHistoryPosition))
//      {
//        History.Dequeue();
//        his = History.Peek();
//      }
//    }

//    override public void OnRunLocal()
//    {
//      WandRotation.SetActive(true);

//      if (runLocal)
//        DetermineMovementAsClick();
//      else
//      {
//        HideRay();
//        if (!IsCurrentController)
//          WandRotation.SetActive(false);
//      }
//    }

//  }

//}