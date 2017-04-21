// Edit: Taking IMU data from glasses AND external(phidgets)

using UnityEngine;
using System.Collections;
using System.Text.RegularExpressions;
using System.Threading;
using UnityMoverioBT200.Scripts.Util;
using System.Collections.Generic;

namespace UnityMoverioBT200.Scripts.Providers
{

  public class RotationProvider : MonoBehaviour
  {

    //the Unity method uses only the gyro.attitude and I have noticed considerable drift
    public enum AlgorithmIMU { Unity, MadgwickOnAndroid }; //other options: Android orientation, MadgwickOnUnity

    public enum SensorMode { Controller, Headset, External };

    private Regex validateData;
    private string dataFromJava = string.Empty;

    private CircularList<float> framerateFilter;
    private System.DateTime lastUpdate;
    private CircularList<Quaternion> orientationFilter;
    private CircularList<Quaternion> headOrientationFilter;
    private int CurrentFilterSize = 0;
    private bool useMagnetometer = false;

    private Quaternion actualRotation;
    private Quaternion baselineRotation;
    private Quaternion invBaselineRotation;
    private Quaternion lastRotation;

    private Quaternion headActualRotation;
    private Quaternion headBaselineRotation;
    private Quaternion headInvBaselineRotation;

    public Quaternion Rotation { get { return actualRotation; } }
    public Quaternion RotationPrev { get { return lastRotation; } }

    public Quaternion HeadRotation { get { return headActualRotation; } }

    public bool IsReceivingDataFromIMU { get; set; }

    public int FilterSize = 5;
    public AlgorithmIMU Algorithm;
    public int BaselineTouchMillis = 3000;
    public SensorMode SourceIMU = SensorMode.External; 
    public GameObject ExternalRotationSource;
    private int updateCount = 0;
    private bool sensorChangedSetBaseline = false;

    public float[] TwistTriggerThreshold = new float[] { 30f, 30f, 7.5f };
    public enum TwistTriggerTypes { ScrewRight, ScrewLeft, Up, Down };
    public struct TwistEvent { public double Milliseconds; public TwistTriggerTypes Type; public Quaternion initialRot; }

    public double MillisecondsOfHistory = 500;
    private Queue<RotationHistory> History = new Queue<RotationHistory>();
    private Queue<RotationHistory> HeadHistory = new Queue<RotationHistory>();

    public bool ShowGUI;

    private static RotationProvider instance;
    public static RotationProvider Instance
    {
      get
      {
        if (instance == null)
          instance = new RotationProvider();
        return instance;
      }
    }

    public RotationProvider()
    {
      instance = this;
      instance.ShowGUI = false;
      instance.IsReceivingDataFromIMU = false;
    }

    ~RotationProvider()
    {
      Debug.Log("Destroying the RotationProvider");
    }

    // Use this for initialization
    void Start()
    {
      validateData = new Regex("(-)*([0-9])+.([0-9])+");
      framerateFilter = new CircularList<float>(60);

      lastUpdate = System.DateTime.Now;
      actualRotation = Quaternion.identity;
      baselineRotation = Quaternion.identity;
      invBaselineRotation = Quaternion.identity;
      lastRotation = Quaternion.identity;

      headActualRotation = Quaternion.identity;
      headBaselineRotation = Quaternion.identity;
      headInvBaselineRotation = Quaternion.identity;

      //Icredibly enough the Unity orientation methods work OK in Japan
      Input.gyro.enabled = true;
      orientationFilter = new CircularList<Quaternion>(FilterSize);
      headOrientationFilter = new CircularList<Quaternion>(FilterSize);

      StartCoroutine(FPS());
      CommToAndroid.CallAndroidMethod("setGameObject", this.name);

      if (SystemInfo.deviceType == DeviceType.Handheld)
      {
        SensorMode currentSensor = (SensorMode)System.Enum.Parse(typeof(SensorMode), CommToAndroid.CallAndroidMethod<string>("getSensorMode"));
        if (currentSensor != SourceIMU)
          SetSourceIMU(SourceIMU);
      }
    }

    // Update is called once per frame
    void FixedUpdate()
    {
      lastRotation = actualRotation;
      if (SourceIMU == SensorMode.Controller || SourceIMU == SensorMode.Headset)
        actualRotation = invBaselineRotation * GetFilteredRotation(GetRotationFromAndroid());
      else if (SourceIMU == SensorMode.External)
      {
        actualRotation = invBaselineRotation * GetFilteredRotation(ExternalRotationSource.transform.localRotation);
        headActualRotation = headInvBaselineRotation * GetFilteredHeadRotation(GetRotationFromAndroid());
            
      }

      SaveHistory();
      UpdateCount();
      ProcessRotationalTriggers();
    }

    private Quaternion GetRotationFromAndroid()
    {
      Quaternion rotFromAndroid = Quaternion.Euler(0.0f, 0.0f, 0.0f);

      if (SystemInfo.deviceType != DeviceType.Handheld)
        return rotFromAndroid;

      if (dataFromJava == string.Empty)
        return rotFromAndroid;

      MatchCollection matches = validateData.Matches(dataFromJava);
      if (matches == null || matches.Count != 4)
      {
        Debug.Log("Wrong data received from Java: " + dataFromJava);
        return rotFromAndroid;
      }

      if (SourceIMU == SensorMode.Controller)
      {
        if (Algorithm == AlgorithmIMU.MadgwickOnAndroid)
        {
          rotFromAndroid.y = float.Parse(matches[0].Value);
          rotFromAndroid.z = float.Parse(matches[1].Value);
          rotFromAndroid.x = float.Parse(matches[2].Value) * -1;
          rotFromAndroid.w = float.Parse(matches[3].Value);
        }
        else if (Algorithm == AlgorithmIMU.Unity)
        {
          rotFromAndroid.x = Input.gyro.attitude.x * -1;
          rotFromAndroid.z = Input.gyro.attitude.y * -1;
          rotFromAndroid.y = Input.gyro.attitude.z * -1;
          rotFromAndroid.w = Input.gyro.attitude.w;
        }
      }
      else if (SourceIMU == SensorMode.Headset || SourceIMU == SensorMode.External)
      {
        if (Algorithm == AlgorithmIMU.MadgwickOnAndroid)
        {
          //this code is not correctly mapped
          rotFromAndroid.x = float.Parse(matches[0].Value);
          rotFromAndroid.y = float.Parse(matches[1].Value);
          rotFromAndroid.z = float.Parse(matches[2].Value);
          rotFromAndroid.w = float.Parse(matches[3].Value);
        }
        else if (Algorithm == AlgorithmIMU.Unity)
        {
          rotFromAndroid.x = Input.gyro.attitude.x * -1;
          rotFromAndroid.y = Input.gyro.attitude.y * -1;
          rotFromAndroid.z = Input.gyro.attitude.z;
          rotFromAndroid.w = Input.gyro.attitude.w;
        }
      }

      return rotFromAndroid;
    }

    private void SaveHistory()
    {
      double currentTimeMillis = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds;
      History.Enqueue(new RotationHistory() { Milliseconds = currentTimeMillis, Rotation = actualRotation });

      //Removes the elements in the history Queue that are older than the set window
      RotationHistory his = History.Peek();
      while (his.Milliseconds < (currentTimeMillis - MillisecondsOfHistory))
      {
        History.Dequeue();
        his = History.Peek();
      }
    }

    void UpdateCount()
    {
      updateCount++;
      if (!sensorChangedSetBaseline && updateCount >= 60)
      {
        SetBaselineRotation();
        sensorChangedSetBaseline = true;
      }
    }

    void OnDoubleTap(MoverioTouchpadEventArgs args)
    {
      SetBaselineRotation();
    }

    public double TwistThresholdMillis = 125;

    private void ProcessRotationalTriggers()
    {
      Quaternion historicRot = GetHistoricValue(TwistThresholdMillis);
      Quaternion diff = Quaternion.Inverse(historicRot) * Rotation;
      Vector3 diffNegPos = RotationProvider.RotAsPosNeg(diff);

      //Checks for twist trigger to the right
      if (diffNegPos.z > TwistTriggerThreshold[2])
        MessageBroker.BroadcastAll("OnTwist", new TwistEvent() { Type = TwistTriggerTypes.ScrewRight, initialRot = historicRot, Milliseconds = TwistThresholdMillis });
      else if(diffNegPos.z < -TwistTriggerThreshold[2])
        MessageBroker.BroadcastAll("OnTwist", new TwistEvent() { Type = TwistTriggerTypes.ScrewLeft, initialRot = historicRot, Milliseconds = TwistThresholdMillis });

      if (diffNegPos.x < -TwistTriggerThreshold[0])
        MessageBroker.BroadcastAll("OnTwist", new TwistEvent() { Type = TwistTriggerTypes.Up, initialRot = historicRot, Milliseconds = TwistThresholdMillis });
      else if (diffNegPos.x > TwistTriggerThreshold[0])
        MessageBroker.BroadcastAll("OnTwist", new TwistEvent() { Type = TwistTriggerTypes.Down, initialRot = historicRot, Milliseconds = TwistThresholdMillis });

      // ----- IMPORTANT -------------
      //Remove the next elements in the Queue otherwise it will continue triggering the same event
    }

    void OnGUI()
    {
      if (!ShowGUI)
        return; 

      GUILayout.Space(310);
      if (GUILayout.Button("Set Baseline", GUILayout.Width(100), GUILayout.Height(30)))
      {
        if (SystemInfo.deviceType == DeviceType.Handheld)
          SetBaselineRotation();

        if (Network.isClient || Network.isServer)
          networkView.RPC("SynchSetBaselineRotation", RPCMode.OthersBuffered);
      }

      GUILayout.BeginArea(new Rect(0, 520, 200, 20));
      string topMessage = string.Format("gFPS: {1:0.00} dFPS: {0:0.00}", GetDataUpdateRate(), FramesPerSec);
      GUILayout.Label(topMessage, GUILayout.Width(Screen.width), GUILayout.Height(20));
      GUILayout.EndArea();

      //if (GUILayout.Toggle(useMagnetometer, "Magt", GUILayout.Width(100), GUILayout.Height(25)))
      //{
      //  if (SystemInfo.deviceType == DeviceType.Handheld)
      //    CommToAndroid.CallAndroidMethod("toggleMagnetometer");
      //  useMagnetometer = !useMagnetometer;

      //  if (Network.isClient || Network.isServer)
      //    networkView.RPC("SynchUseMagt", RPCMode.OthersBuffered, useMagnetometer.ToString());
      //}

      //SensorMode otherIMU = SourceIMU == SensorMode.Headset ? SensorMode.Controller : SensorMode.Headset;
      //if (GUILayout.Button("Use " + otherIMU.ToString(),
      //                     GUILayout.Width(100), GUILayout.Height(30)))
      //{
      //  SourceIMU = otherIMU;
      //  if (SystemInfo.deviceType == DeviceType.Handheld)
      //    CommToAndroid.CallAndroidMethod("setSensorMode", otherIMU.ToString());

      //  if (Network.isClient || Network.isServer)
      //    networkView.RPC("SynchSensorMode", RPCMode.OthersBuffered, otherIMU.ToString());
      //}

      //System.Array algorithmTypes = System.Enum.GetValues(typeof(AlgorithmIMU));
      //GUILayout.Label("Algorithm:", GUILayout.Width(100), GUILayout.Height(20));
      //foreach (AlgorithmIMU method in algorithmTypes)
      //{
      //  bool isCurrent = Algorithm == method;
      //  if (GUILayout.Toggle(isCurrent,
      //                       method.ToString(),
      //                       GUILayout.Width(100), GUILayout.Height(25)) && !isCurrent)  //-- this last piece is VERY important
      //  {
      //    Algorithm = method;

      //    if (Network.isClient || Network.isServer)
      //      networkView.RPC("SynchAlgorithm", RPCMode.OthersBuffered, Algorithm.ToString());
      //  }
      //}
    }

    [RPC]
    void SynchSetBaselineRotation()
    {
      if (SystemInfo.deviceType != DeviceType.Handheld)
        return;

      SetBaselineRotation();
    }

    [RPC]
    void SynchUseMagt(string magt)
    {
      useMagnetometer = bool.Parse(magt);

      if (SystemInfo.deviceType != DeviceType.Handheld)
        return;

      CommToAndroid.CallAndroidMethod("toggleMagnetometer");
    }

    [RPC]
    void SynchSensorMode(string sensorMode)
    {
      SourceIMU = (SensorMode)System.Enum.Parse(typeof(SensorMode), sensorMode); ;

      if (SystemInfo.deviceType != DeviceType.Handheld)
        return;

      if (SourceIMU != SensorMode.External)
        CommToAndroid.CallAndroidMethod("setSensorMode", SourceIMU.ToString());
    }

    [RPC]
    void SynchAlgorithm(string algorithm)
    {
      Algorithm = (AlgorithmIMU)System.Enum.Parse(typeof(AlgorithmIMU), algorithm);
    }

    /* **********************************************************************
     * PROPERTIES
     * *********************************************************************/
    public float FramesPerSec { get; protected set; }
    public float FrecuencyToCheckFPS = 0.5f;

    /*
     * EVENT: FPS
     */
    private IEnumerator FPS()
    {
      for (; ; )
      {
        // Capture frame-per-second
        int lastFrameCount = Time.frameCount;
        float lastTime = Time.realtimeSinceStartup;
        yield return new WaitForSeconds(FrecuencyToCheckFPS);
        float timeSpan = Time.realtimeSinceStartup - lastTime;
        int frameCount = Time.frameCount - lastFrameCount;

        // Display it
        FramesPerSec = frameCount / timeSpan;
      }
    }

    // This method is called from the Android code by means of a:
    //  UnityPlayer.UnitySendMessage("Object Name", "Message", "Parameters");
    //  Please note that "Message" is the name of the method.
    void Message(string data)
    {
      dataFromJava = data;

      System.DateTime updateTime = System.DateTime.Now;
      double milliseconds = (updateTime - lastUpdate).TotalMilliseconds;
      double fRate = 1000.0d / milliseconds;
      lastUpdate = updateTime;

      if (milliseconds < 10f) //removes the very short bursts that are corrupting the real value
        return;

      framerateFilter.Value = (float)fRate;
      framerateFilter.Next();
      IsReceivingDataFromIMU = true;
    }

    private float GetDataUpdateRate()
    {
      float accumDFR, count;
      accumDFR = 0.0f;
      count = framerateFilter.Count;
      for (int index = 0; index < count; index++)
        accumDFR += framerateFilter[index];

      return accumDFR / count;
    }

    private Quaternion GetFilteredRotation(Quaternion rotation)
    {
      if (orientationFilter == null)
        return Quaternion.Euler(0.0f, 0.0f, 0.0f);

      if (rotation != Quaternion.identity)
      {
        orientationFilter.Value = rotation;
        orientationFilter.Next();
      }

      if (orientationFilter.Count == 0)
        return Quaternion.Euler(0.0f, 0.0f, 0.0f);

      float accumX, accumY, accumZ, accumW, count;
      accumX = accumY = accumZ = accumW = 0.0f;
      count = orientationFilter.Count;
      for (int index = 0; index < count; index++)
      {
        Quaternion quat = orientationFilter[index];
        accumX += quat.x;
        accumY += quat.y;
        accumZ += quat.z;
        accumW += quat.w;
      }

      Quaternion filtered = new Quaternion(accumX / count, accumY / count, accumZ / count, accumW / count);
      return filtered;
    }

    // Same as above but uses headOrientationFilter list instead of original orientation filter list
    private Quaternion GetFilteredHeadRotation(Quaternion rotation)
    {
      if (headOrientationFilter == null)
        return Quaternion.Euler(0.0f, 0.0f, 0.0f);

      if (rotation != Quaternion.identity)
      {
        headOrientationFilter.Value = rotation;
        headOrientationFilter.Next();
      }

      if (headOrientationFilter.Count == 0)
        return Quaternion.Euler(0.0f, 0.0f, 0.0f);

      float accumX, accumY, accumZ, accumW, count;
      accumX = accumY = accumZ = accumW = 0.0f;
      count = headOrientationFilter.Count;
      for (int index = 0; index < count; index++)
      {
        Quaternion quat = headOrientationFilter[index];
        accumX += quat.x;
        accumY += quat.y;
        accumZ += quat.z;
        accumW += quat.w;
      }

      Quaternion filtered = new Quaternion(accumX / count, accumY / count, accumZ / count, accumW / count);
      return filtered;
    }

    public void SetBaselineRotation()
    {
      baselineRotation = GetFilteredRotation(Quaternion.identity);
      invBaselineRotation = Quaternion.Inverse(baselineRotation);
      actualRotation = invBaselineRotation * baselineRotation;

      headBaselineRotation = GetFilteredHeadRotation(Quaternion.identity);
      headInvBaselineRotation = Quaternion.Inverse(headBaselineRotation);
      headActualRotation = headInvBaselineRotation * headBaselineRotation;

      MessageBroker.BroadcastAll("OnRotationBaselineSet");
    }

    public static Vector3 RotAsPosNeg(Quaternion rotationT)
    {
      Vector3 diffRotVec3 = new Vector3();

      diffRotVec3.x = rotationT.eulerAngles.x;
      if (diffRotVec3.x > 180f)
        diffRotVec3.x = -1 * (360 - diffRotVec3.x);
      diffRotVec3.y = rotationT.eulerAngles.y;
      if (diffRotVec3.y > 180f)
        diffRotVec3.y = -1 * (360 - diffRotVec3.y);
      diffRotVec3.z = rotationT.eulerAngles.z;
      if (diffRotVec3.z > 180f)
        diffRotVec3.z = -1 * (360 - diffRotVec3.z);

      return diffRotVec3;
    }

    public static string Vector3ToString(Vector3 point)
    {
      return string.Format("[{0:0.000}, {1:0.000}, {2:0.000}]", point.x, point.y, point.z);
    }

    public void SetSourceIMU(SensorMode imu)
    {
      SourceIMU = imu;
      if (SourceIMU != SensorMode.External)
        CommToAndroid.CallAndroidMethod("setSensorMode", SourceIMU.ToString());

      updateCount = 0;
      sensorChangedSetBaseline = false;
    }

    private struct RotationHistory
    {
      public double Milliseconds;
      public Quaternion Rotation;
    }

    public Quaternion GetHistoricValue(double millisIntoThePast)
    {
      if (History.Count == 0)
        return Quaternion.identity;

      RotationHistory[] history = History.ToArray();
      double limit = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds - millisIntoThePast;

      int counter = -1;
      RotationHistory pastValue = history[++counter];

      while (pastValue.Milliseconds < limit && counter < (history.Length - 2))
        pastValue = history[++counter];

      return history[counter].Rotation;
    }

  }

}