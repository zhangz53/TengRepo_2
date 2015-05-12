using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using UnityEngine;
using UnityMoverioBT200.Scripts.Controllers;
using UnityMoverioBT200.Scripts.Util;

namespace PointingMobileHMD.Scripts
{

  public class ExperimentControl
  {
    public bool Started { get; private set; }

    private StreamWriter experimentFile;

    public System.String BaseFolder;
    public int ParticipantNro = 0;

    public TrialInformation Trial { get; set; }

    public ControllerSettings Settings { get; set; }

    public Camera Camera { get; set; }

    public Camera LeftEyeCamera { get; set; }

    public GameObject DepthPlane { get; set; }

    public string ExperimentName { get; set; }

    public ExperimentControl(string name)
    {
      Started = false;
      ExperimentName = name;
      Trial = new TrialInformation();
    }

    public void CreateNewTrial(SelectionEventArgs args, GameObject target, int trialNr, DistanceZones zone = DistanceZones.Control, ReferenceFrame refFrame = ReferenceFrame.World)
    {
      Started = true;

      //If it receives the trial number as a parameter it uses that number, else it increases from the previous trial
      Vector3 lastTargetPos = Trial.Target == null ? Vector3.zero : Trial.LastTargetPosition;

      Trial = new TrialInformation();
      Trial.RefFrame = refFrame;
      Trial.Zone = zone;
      Trial.Controller = Settings.ControllerType;
      Trial.TrialNro = trialNr;
      Trial.TrialWidth = target.transform.localScale.x;
      Trial.TrialDistance = (lastTargetPos - target.transform.position).magnitude;
      Trial.Target = target;
      Trial.LastTargetPosition = lastTargetPos;
      Trial.StartTime = System.DateTime.Now;

      if (args != null)
      {
        Trial.ControllerInitialPx = args.ControllerEvent.PointerPx;
        Trial.ControllerInitialPos = args.ControllerEvent.PointerPos;
        Trial.ControllerInitialQuat = args.ControllerEvent.PointerQuat;
      }

      Trial.CameraInitialPos = Camera.transform.position;
      Trial.CameraInitialQuat = Camera.transform.rotation;

      if (Settings.IsCursorBasedController) //Touch, Gyro, Head (gyro)
      {
        Vector2 origin = (Vector2)LeftEyeCamera.WorldToScreenPoint(lastTargetPos);
        Vector2 end = (Vector2)LeftEyeCamera.WorldToScreenPoint(target.transform.position);
        Trial.InitialDistanceToTarget = (end - origin).magnitude;
        Trial.InitialDistanceToTargetPxAdjusted = CalculateAdjustedDistance(origin, end, new Vector3(2f, 1.0f, 1.0f));
      }
      else if (Settings.ControllerType == ControllerType.HandGesture) //Hand
      {
        Trial.InitialDistanceToTarget = (Trial.ControllerInitialPos - target.transform.position).magnitude;
      }
      else //IsRotationBasedController -- Raycast, Head
      {
        Trial.InitialDistanceToTarget = Vector3.Angle(Trial.ControllerInitialPos - lastTargetPos, Trial.ControllerInitialPos - target.transform.position);
      }
    }

    public void CaptureTrialInfo(SelectionEventArgs args)
    {
      if (!Started)
        return;

      float timeEllapsed = (float)(args.Time - Trial.StartTime).TotalMilliseconds;
      Trial.TotalMs = timeEllapsed;
      Trial.SelectionMs = timeEllapsed - Trial.LastArrivalMs;
      Trial.NrOfConflicted = args.ControllerEvent.NrOfConflictedTargets;
      Trial.TargetOrder = args.ControllerEvent.IndexOfConflictSolution;
      Trial.RayDisambiguationMethod = Settings.RaycastController.RayType.ToString();
      Trial.RayLocation = args.ControllerEvent.Tag;
      Trial.TargetDepth = DepthPlane.name;

      Trial.ControllerFinalPx = args.ControllerEvent.PointerPx;
      Trial.ControllerFinalPos = args.ControllerEvent.PointerPos;
      Trial.ControllerFinalQuat = args.ControllerEvent.PointerQuat;

      Trial.CameraFinalPos = Camera.transform.position;
      Trial.CameraFinalQuat = Camera.transform.rotation;

      if (Settings.IsCursorBasedController) //Touch, Gyro, Head (gyro)
      {
        //Distance Executed by the Controller
        Trial.ControllerExecutedDistance = (Trial.ControllerFinalPx - Trial.ControllerInitialPx).magnitude;
        Trial.ControllerExecutedDistancePxAdjusted = CalculateAdjustedDistance(Trial.ControllerInitialPx, Trial.ControllerFinalPx, new Vector3(2f, 1.0f, 1.0f));

        //Distance to Target
        Vector2 origin = (Vector2)LeftEyeCamera.WorldToScreenPoint(Trial.LastTargetPosition);
        Vector2 end = (Vector2)LeftEyeCamera.WorldToScreenPoint(Trial.Target.transform.position);
        Trial.FinalDistanceToTarget = (end - origin).magnitude;
        Trial.FinalDistanceToTargetPxAdjusted = CalculateAdjustedDistance(origin, end, new Vector3(2f, 1.0f, 1.0f));
      }
      else if (Settings.ControllerType == ControllerType.HandGesture) //Hand
      {
        //Given that this is a hand-moevd pointer, this value MUST be close to zero
        Trial.FinalDistanceToTarget = (Trial.ControllerFinalPos - Trial.Target.transform.position).magnitude;
      }
      else //IsRotationBasedController -- Raycast, Head
      {
        //Distance Executed by the Controller
        Trial.ControllerExecutedDistance = Quaternion.Angle(Trial.ControllerFinalQuat, Trial.ControllerInitialQuat);

        //Distance to Target
        Trial.FinalDistanceToTarget = Vector3.Angle(Trial.ControllerFinalPos - Trial.LastTargetPosition, Trial.ControllerFinalPos - Trial.Target.transform.position);
      }
    }

    private float CalculateAdjustedDistance(Vector3 origin, Vector3 end, Vector3 multiplier)
    {
      Vector3 diff = (end - origin);
      diff = new Vector3(diff.x * multiplier.x, diff.y * multiplier.y, diff.z * multiplier.z);
      return diff.magnitude;
    }

    private void OpenFile()
    {
      int count = 0;
      string fileName = BaseFolder + @"\" + ExperimentName + "-" + ParticipantNro + ".log";
      while (File.Exists(fileName))
        fileName = BaseFolder + @"\" + ExperimentName + "-" + ParticipantNro + "-" + (++count) + ".log";

      experimentFile = File.CreateText(fileName);
    }

    public void WriteToFile(string logLine)
    {
      if (!Started)
        return;
      if (experimentFile == null)
        OpenFile();
      if (experimentFile == null)
        return;

      experimentFile.WriteLine(logLine);
      experimentFile.Flush();
    }

    public void CloseFile()
    {
      if (!Started)
        return;
      if (experimentFile == null)
        return;

      experimentFile.Flush();
      experimentFile.Close();
    }

    public void ProcessOnHover(SelectionEventArgs args)
    {
      if (!Started)
        return;
      if (Trial.Target != args.Target)
        return;

      Trial.Hovers++;

      float timeEllapsed = (float)(args.Time - Trial.StartTime).TotalMilliseconds;
      Trial.FirstArrivalMs = Mathf.Min(Trial.FirstArrivalMs, timeEllapsed);
      Trial.LastArrivalMs = Mathf.Max(Trial.FirstArrivalMs, timeEllapsed);
    }

    public void OnRaycastLock()
    {
      if (!Started)
        return;

      Trial.RaycastLocks++;
    }

    public void Stop()
    {
      Started = false;
    }

  }

}