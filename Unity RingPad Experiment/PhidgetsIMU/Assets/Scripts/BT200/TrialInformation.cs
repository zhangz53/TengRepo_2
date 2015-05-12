using UnityEngine;
using System.Collections;
using UnityMoverioBT200.Scripts;
using UnityMoverioBT200.Scripts.Util;
using UnityMoverioBT200.Scripts.Controllers;

namespace PointingMobileHMD.Scripts
{
  public class TrialInformation
  {

    public ReferenceFrame RefFrame = ReferenceFrame.View;
    public DistanceZones Zone = DistanceZones.Control;

    public System.DateTime TimeStamp = System.DateTime.MinValue;
    public int TrialNro = -1;
    public ControllerType Controller = ControllerType.Raycast;
    public float TrialWidth = -1;
    public float TrialDistance = -1;
    public GameObject Target = null;
    public Vector3 LastTargetPosition = Vector3.zero;

    public System.DateTime StartTime = System.DateTime.MinValue;

    public string RayLocation = string.Empty;
    public string RayDisambiguationMethod = string.Empty;
    public string TargetDepth = string.Empty;
    public int NrOfConflicted = -1;
    public int TargetOrder = -1;

    public float TotalMs = float.MaxValue; //time since departure till the object is selected
    public float FirstArrivalMs = float.MaxValue; //time from departure till the first arrival
    public float LastArrivalMs = float.MaxValue; //time from departure till the last arrival
    public float SelectionMs = float.MaxValue; //time from the last arrival till the object is selected
    public int Hovers = 0; //the number of times the cursor entered into the target object
    public int Triggers = 0; //the number of times the selection button was pressed (includes missed selections)
    public int TriggersAfterArriving = 0; //the number of times the selection button was pressed (includes missed selections) after the first arrival
    public int RaycastLocks = 0;

    public Vector3 ControllerInitialPx = Vector3.zero;
    public Vector3 ControllerInitialPos = Vector3.zero;
    public Quaternion ControllerInitialQuat = Quaternion.identity;

    public Vector3 ControllerFinalPx = Vector3.zero;
    public Vector3 ControllerFinalPos = Vector3.zero;
    public Quaternion ControllerFinalQuat = Quaternion.identity;

    public Vector3 CameraInitialPos = Vector3.zero;
    public Quaternion CameraInitialQuat = Quaternion.identity;
    public Vector3 CameraFinalPos = Vector3.zero;
    public Quaternion CameraFinalQuat = Quaternion.identity;

    public float ControllerExecutedDistance = -1; //This is the distance in pixels travelled by the touchpointer, headpointer and gyromouse
    public float ControllerExecutedDistancePxAdjusted = -1; //Same as above, adjusted for side-by-side efect

    public float InitialDistanceToTarget = -1;
    public float InitialDistanceToTargetPxAdjusted = -1;
    public float FinalDistanceToTarget = -1;
    public float FinalDistanceToTargetPxAdjusted = -1;

    /**
     * This field refers to comparison between the actual pointer travel distance versus the distance it was support to travel.
     * A value greater than 1 means the user ended up moving more than expected.
     * A value smaller than 1 means the user performed some kind of adjustment so that cursor movement distance is lower.
     */
    public float MotorEfficiencyRate { get { return ControllerExecutedDistance / InitialDistanceToTarget; } }

    /**
     * This field refers to the change of the distance between the targets, at the begginign and at the end of the selection process.
     *  For the location based techniques (cursor) it refers to the change in the distance the cursor would have to travel.
     *  For the rotation based techniques (raycast, head) it refers to the chenge in the angle the controller would have to travel
     * A value greater than 1 means the user moved in a way that the distance between the two targets increased.
     *  However this movement might have included traslation and rotation of the camera in space, in a way that would have brought the selection tool closer to the target.
     * A value smaller than 1 means the distance between the targets was smaller, which can be achieved by moving away from both targets.
     */
    public float TargetAdjustmentRate { get { return FinalDistanceToTarget / InitialDistanceToTarget; } }

    /**
     * This field refers to the spatial difference between the initial and final controller positions
     * A value closer to zero means the controller did NOT translate, but it doesn't rule out rotation.
     */
    public float ControllerLocationChange { get { return (ControllerFinalPos - ControllerInitialPos).magnitude; } }

    /**
     * This field refers to the angle difference between the initial and final controller orientation.
     * A value closer to zero means the controller did NOT rotate, but it doesn't rule out translation.
     */
    public float ControllerRotationChange { get { return Quaternion.Angle(ControllerFinalQuat, ControllerInitialQuat); } }

    /**
     * This field refers to the spatial difference between the initial and final camera positions
     * A value closer to zero means the camera did NOT translate, but it doesn't rule out rotation.
     */
    public float CameraLocationChange { get { return (CameraFinalPos - CameraInitialPos).magnitude; } }

    /**
     * This field refers to the angle difference between the initial and final camera orientation.
     * A value closer to zero means the camera did NOT rotate, but it doesn't rule out translation.
     */
    public float CameraRotationChange { get { return Quaternion.Angle(CameraFinalQuat, CameraInitialQuat); } }

    public override string ToString()
    {
      return string.Format(
        "{0};{1};{2};{3};{4};{5};{6};" + 
        "{7:0.000};{8:0.000};{9};{10};{11};{12};" + 
        "{13:0.000};{14:0.000};{15:0.000};{16:0.000};{17};{18};{19};" + 
        "{20:0.000};{21:0.000};{22:0.000};" +
        "{23:0.000};{24:0.000};{25:0.000};" +
        "{26:0.000};{27:0.000};{28:0.000};{29:0.000};{30:0.000};{31:0.000};",
        StartTime.ToString("MM/dd/yyyy hh:mm:ss.fff tt"), RefFrame, Zone, TrialNro, Controller, RayLocation, RayDisambiguationMethod,
        TrialWidth, TrialDistance, Target.name, NrOfConflicted, TargetOrder, TargetDepth,
        TotalMs, FirstArrivalMs, LastArrivalMs, SelectionMs, Hovers, Triggers, TriggersAfterArriving,
        InitialDistanceToTarget, FinalDistanceToTarget, ControllerExecutedDistance, 
        InitialDistanceToTargetPxAdjusted, FinalDistanceToTargetPxAdjusted, ControllerExecutedDistancePxAdjusted,
        MotorEfficiencyRate, TargetAdjustmentRate, ControllerLocationChange, ControllerRotationChange, CameraLocationChange, CameraRotationChange);
    }

  }

}