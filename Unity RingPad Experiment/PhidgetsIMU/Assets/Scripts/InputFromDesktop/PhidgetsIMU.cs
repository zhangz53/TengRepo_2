using UnityEngine;
using System.Collections;
using System;
using System.IO;
using System.Text;
using Phidgets;
using Phidgets.Events;
using System.Collections.Generic;


/* Grabs and stores IMU orientation and angles to be called on later.
 * 
 * To use, call the appropriate getter method (getQuaternions/getAngles).
 * GetHistoricAngles/GetHistoricQuats returns the angles/quaternions at a certain point in time,
 * used to calculate twists and other gestures involving motions.
 */


public class PhidgetsIMU : MonoBehaviour
{
    private struct RotationHistory
    {
        public double Milliseconds;
        public Quaternion Rotation;
        public float[] Angles;
    }

    public enum AlgorithmUsed { AHRSAlg, EulerAxisAlg, CompassGravAlg };
    public AlgorithmUsed Alg = AlgorithmUsed.AHRSAlg;

    static AHRS.MadgwickAHRS madgwick = new AHRS.MadgwickAHRS(1f / 256f, 0.1f);
    //public AHRSState mAHRSState;

    // IMU
    private Spatial spatial;

    private int compassBearingFilterSize = 10;
    private double compassBearing = 0;

    private double[] lastAngles = { 0, 0, 0 };
    private double[] gyroHeading = { 0, 0, 0 }; //degrees
    private List<double[]> compassBearingFilter = new List<double[]>();
    private float[] IMUangles;

    private DateTime timestampLastUpdate;
    private float gx, gy, gz;  //gyrovalue degrees/s
    private float ax, ay, az;  //accelerometer g
    private float mx, my, mz;  //magnet gauss
    private float q0, q1, q2, q3;

    private Quaternion orientation, baseline, invBaseline, newQuaternion;

    public double MillisecondsOfHistory = 500;
    private Queue<RotationHistory> History = new Queue<RotationHistory>();

    // Use this for initialization
    void Start()
    {
        spatial = new Spatial();

        spatial.open();
        spatial.waitForAttachment(1000);
        spatial.DataRate = 32;
        //spatial.zeroGyro();

        timestampLastUpdate = DateTime.MinValue;

        //These constants are geographic dependent --- do not change
        spatial.setCompassCorrectionParameters(0.64473, 0.04197, -0.01880, -0.01187, 1.51858, 1.51659, 1.61795, 0.00715, 0.00798, 0.00675, -0.01492, 0.00817, -0.01580);

        spatial.SpatialData += new SpatialDataEventHandler(spatial_SpatialData);

        //Euler(-180, 0, 0)
        orientation = Quaternion.identity;
        baseline = Quaternion.identity;
        invBaseline = Quaternion.identity;

        IMUangles = new float[3];
        for (int i = 0; i < 3; i++)
            IMUangles[i] = 0;
    }

    public float SamplePeriod = 0.03f;

    // Use for calculations using sensors
    public void spatial_SpatialData(object sender, SpatialDataEventArgs e)
    {
        // Negative logic
        if (spatial.accelerometerAxes.Count != 3 || spatial.gyroAxes.Count != 3 || spatial.compassAxes.Count != 3)
            return;

        if (timestampLastUpdate != DateTime.MinValue)
        {
            // Conversion into correct units
            gx = (float)spatial.gyroAxes[0].AngularRate * 0.0174532925f;
            gy = (float)spatial.gyroAxes[1].AngularRate * 0.0174532925f;
            gz = (float)spatial.gyroAxes[2].AngularRate * 0.0174532925f;

            float seconds = (float)(DateTime.Now - timestampLastUpdate).TotalSeconds;
            
            madgwick.SamplePeriod = (float)(seconds);
            if (Alg == AlgorithmUsed.AHRSAlg)
                AHRSUpdate(gx, gy, gz, ax, ay, az, mx, my, mz);
            else if (Alg == AlgorithmUsed.CompassGravAlg)
                CompassGrav(ax, ay, az);
            //CompassGrav(ax, ay, az);

        }

        timestampLastUpdate = DateTime.Now;

        ax = (float)e.spatialData[0].Acceleration[0];
        ay = (float)e.spatialData[0].Acceleration[1];
        az = (float)e.spatialData[0].Acceleration[2];

        mx = (float)spatial.compassAxes[0].MagneticField;
        my = (float)spatial.compassAxes[1].MagneticField;
        mz = (float)spatial.compassAxes[2].MagneticField;

        CalculateCompassBearing();
    }

    private void AHRSUpdate(double gx, double gy, double gz, double ax, double ay, double az, double mx, double my, double mz)
    {
        madgwick.Update((float)gx, (float)gy, (float)gz, (float)ax, (float)ay, (float)az, (float)mx, (float)my, (float)mz);

        q0 = madgwick.Quaternion[0];
        q1 = madgwick.Quaternion[1];
        q2 = madgwick.Quaternion[2];
        q3 = madgwick.Quaternion[3];

        // Different quaternion signs for different orientation of IMU. 
        // Not sure if there is a way to keep it consistent. Look into it further.

        // This is for when the IMU is face up
        //orientation = new Quaternion(-q1, -q0, q2, q3);

        // Face down
        orientation = new Quaternion(q1, -q0, q2, -q3);

        // Test
        //orientation = new Quaternion(q2, -q1, q3, q0);

        // Test2
        //orientation = new Quaternion(q3, q2, q0, q1);

        // Test3
        //orientation = new Quaternion(-q1, q0, q2, -q3);

        //Saving rotation history
        double currentTimeMillis = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds;
        History.Enqueue(new RotationHistory() { Milliseconds = currentTimeMillis, Rotation = orientation, Angles = IMUangles});
        RotationHistory his = History.Peek();
        while (his.Milliseconds < (currentTimeMillis - MillisecondsOfHistory))
        {
            History.Dequeue();
            his = History.Peek();
        }
    }
    private double r00, r01, r02, r10, r11, r12, r20, r21, r22;
    private float delta;

    private void CompassGrav(double ax, double ay, double az)
    {
        float compass = getAngles()[1]; //*0.0174532925f;
        delta = compass;
        double a, b, c, g, k;

        g = Mathf.Sqrt((float)((ax * ax) + (ay * ay) + (az * az)));

        a = -ax / g;
        b = -ay / g;
        c = -az / g;

        k = (Mathf.Pow((float)(((a * Mathf.Cos(delta) - a * b)) * (a * Mathf.Cos(delta) - b * Mathf.Sin(delta))), 2) / (a * a * Mathf.Pow((float)(a * Mathf.Cos(delta) - b * Mathf.Sin(delta)), 2)))
            + (Mathf.Pow((float)((b * b - c * Mathf.Cos(delta)) * (a * Mathf.Cos(delta) - a * b)), 2))
            + (Mathf.Pow((float)((a * Mathf.Cos(delta) - a * b) * (a * Mathf.Cos(delta) - b * Mathf.Sin(delta))), 2));

        //k = ((((a * Mathf.Cos(compass) - a * b)) * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass))) * (((a * Mathf.Cos(compass) - a * b)) * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass))) / (a * a * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass)) * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass)))
        //    + (((b * b - c * Mathf.Cos(compass)) * (a * Mathf.Cos(compass) - a * b)) * ((b * b - c * Mathf.Cos(compass)) * (a * Mathf.Cos(compass) - a * b)))
        //    + (((a * Mathf.Cos(compass) - a * b) * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass))) * ((a * Mathf.Cos(compass) - a * b) * (a * Mathf.Cos(compass) - b * Mathf.Sin(compass)))));

        r00 = ((b * b - c * Mathf.Cos(delta)) / (a * Mathf.Cos(delta) - b * Mathf.Sin(delta))) * Mathf.Sqrt((float)k);
        r00 = (b * b - (c * Mathf.Cos(delta))) / (a * Mathf.Cos(delta) - (b * Mathf.Sin(delta))) * (Mathf.Sqrt((float)k));
        r01 = Mathf.Sin(delta);
        r02 = a;
        r10 = (a * c) / (a * Mathf.Cos(delta) - a * b) * (Mathf.Sqrt((float)k));
        r11 = Mathf.Cos(delta);
        r12 = b;
        r20 = Mathf.Sqrt((float)k);
        r21 = b;
        r22 = c;

        MatrixToQuaternion(r00, r01, r02, r10, r11, r12, r20, r21, r22);

        //q3 = Mathf.Sqrt((float)(1 + r11 + r22 + r33)/2);
        //q0 = (float)(r32 - r23) / (4 * q3);
        //q1 = (float)(r13 - r31) / (4 * q3);
        //q2 = (float)(r21 - r12) / (4 * q3);

        //orientation = new Quaternion(q0, q1, q2, q3);

    }
    private Quaternion compassGravQuat;
    private float temp;

    private void MatrixToQuaternion(double r00, double r01, double r02, double r10, double r11, double r12, double r20, double r21, double r22)
    {
        temp = (float)(1 + r00 + r11 + r22);
        q3 = Mathf.Sqrt(temp) / 2;
        q0 = (float)(r21 - r12) / (4 * q3);
        q1 = (float)(r02 - r20) / (4 * q3);
        q2 = (float)(r10 - r01) / (4 * q3);

        compassGravQuat = new Quaternion(q0, q1, q2, q3);
        if (!float.IsNaN(q0))
            orientation = compassGravQuat;
    }

    void CalculateCompassBearing()
    {
        double[] gravity = {
                spatial.accelerometerAxes[0].Acceleration,
                spatial.accelerometerAxes[1].Acceleration,
                spatial.accelerometerAxes[2].Acceleration};

        double[] magField = {
                spatial.compassAxes[0].MagneticField, 
                spatial.compassAxes[1].MagneticField, 
                spatial.compassAxes[2].MagneticField};

        //Roll Angle - about axis 0
        //  tan(roll angle) = gy/gz
        //  Use Atan2 so we have an output os (-180 - 180) degrees
        double rollAngle = Math.Atan2(gravity[1], gravity[2]);

        //Pitch Angle - about axis 1
        //  tan(pitch angle) = -gx / ((gy * sin(roll angle)) + (gz * cos(roll angle)))
        //  Pitch angle range is (-90 - 90) degrees
        double pitchAngle = Math.Atan(-gravity[0] / (gravity[1] * Math.Sin(rollAngle) + gravity[2] * Math.Cos(rollAngle)));

        //Yaw Angle - about axis 2
        //  tan(yaw angle) = (mz * sin(roll) – my * cos(roll)) / 
        //                   (mx * cos(pitch) + my * sin(pitch) * sin(roll) + mz * sin(pitch) * cos(roll))
        //  Use Atan2 to get our range in (-180 - 180)
        //
        //  Yaw angle == 0 degrees when axis 0 is pointing at magnetic north
        double yawAngle = Math.Atan2(magField[2] * Math.Sin(rollAngle) - magField[1] * Math.Cos(rollAngle),
            magField[0] * Math.Cos(pitchAngle) + magField[1] * Math.Sin(pitchAngle) * Math.Sin(rollAngle) + magField[2] * Math.Sin(pitchAngle) * Math.Cos(rollAngle));

        double[] angles = { rollAngle, pitchAngle, yawAngle };

        //we low-pass filter the angle data so that it looks nicer on-screen
        try
        {
            //make sure the filter buffer doesn't have values passing the -180<->180 mark
            //Only for Roll and Yaw - Pitch will never have a sudden switch like that
            for (int i = 0; i < 3; i += 2)
            {
                if (Math.Abs(angles[i] - lastAngles[i]) > 3)
                    foreach (double[] stuff in compassBearingFilter)
                        if (angles[i] > lastAngles[i])
                            stuff[i] += 360 * Math.PI / 180.0;
                        else
                            stuff[i] -= 360 * Math.PI / 180.0;
            }

            lastAngles = (double[])angles.Clone();

            compassBearingFilter.Add((double[])angles.Clone());
            if (compassBearingFilter.Count > compassBearingFilterSize)
                compassBearingFilter.RemoveAt(0);

            yawAngle = pitchAngle = rollAngle = 0;
            foreach (double[] stuff in compassBearingFilter)
            {
                rollAngle += stuff[0];
                pitchAngle += stuff[1];
                yawAngle += stuff[2];
            }
            yawAngle /= compassBearingFilter.Count;
            pitchAngle /= compassBearingFilter.Count;
            rollAngle /= compassBearingFilter.Count;



            //Convert radians to degrees for display
            compassBearing = yawAngle * (180.0 / Math.PI);
            pitchAngle *= (180.0 / Math.PI);
            rollAngle *= (180.0 / Math.PI);

            //Debug.Log(" pitch: " + pitchAngle);

            angles[0] = pitchAngle;
            angles[2] = rollAngle;
            angles[1] = compassBearing;

            IMUangles = new float[3];

            for (int i = 0; i < angles.Length; i++ )
                IMUangles[i] = (float)angles[i];
        }
        catch { }
    }

    public void OnApplicationQuit()
    {
        spatial.close();
    }

    private void zeroGyroButton_Click()
    {
        spatial.zeroGyro();
        gyroHeading[0] = 0;
        gyroHeading[1] = 0;
        gyroHeading[2] = 0;
        StartCoroutine(WaitForGyro(1.5f));
    }

    IEnumerator WaitForGyro(float waitTime)
    {
        yield return new WaitForSeconds(waitTime);
    }

    // Update is called once per frame
    void Update()
    {
        // Reset baseline
        if(Input.GetKeyDown("z"))
        {
            SetBaseline();
        }
        if (Input.GetKeyDown("x"))
        {
            spatial.zeroGyro();
            SetBaseline();
        }
    }

    public void SetBaseline()
    {
        baseline = orientation;
        invBaseline = Quaternion.Inverse(baseline);
        newQuaternion = invBaseline * baseline;
    }

    public float[] getAngles()
    {
        return IMUangles;
    }

    public float[] getFloatAngles()
    {

        float[] IMUfloats = new float[IMUangles.Length];
        for (int i = 0; i < IMUangles.Length; ++i)
        {
            IMUfloats[i] = (float)IMUangles[i];
        }
        return IMUfloats;
    }

    public Quaternion getQuaternion()
    {
        return invBaseline * orientation;
    }

    public Quaternion[] getQuaternions()
    {
        Quaternion[] quats = new Quaternion[3];
        quats[0] = baseline;
        quats[1] = invBaseline;
        quats[2] = orientation;
        return quats;
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

    public float[] GetHistoricAngles(double millisIntoThePast)
    {
        float[] empty = { 0, 0, 0 };


        if (History.Count == 0)
            return empty;

        RotationHistory[] history = History.ToArray();
        double limit = System.TimeSpan.FromTicks(System.DateTime.Now.Ticks).TotalMilliseconds - millisIntoThePast;

        int counter = -1;
        RotationHistory pastValue = history[++counter];

        while (pastValue.Milliseconds < limit && counter < (history.Length - 2))
            pastValue = history[++counter];

        return history[counter].Angles;
    }

    public Vector3 RotAsPosNeg(Quaternion rotationT)
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
}
