using UnityEngine;
using System.Collections;
using Phidgets;
using System.Collections.Generic;
using System;
using Phidgets.Events;

namespace RingPad.Scripts
{

    public class PhidgetsRotationProvider : MonoBehaviour
    {

        public bool phidgetsAttached = true;

        private Spatial spatial;

        public enum AlgorithmUsed { AHRS };
        public AlgorithmUsed Alg = AlgorithmUsed.AHRS;

        //static AHRS.MadgwickAHRS madgwick = new AHRS.MadgwickAHRS(1f / 256f, 0.1f);
        static AHRS.MadgwickAHRS madgwick = new AHRS.MadgwickAHRS(0.004f, 0.1f);
        //public AHRSState mAHRSState;

        private int compassBearingFilterSize = 10;
        private double compassBearing = 0;
        private double[] lastAngles = { 0, 0, 0 };
        private double[] gyroHeading = { 0, 0, 0 }; //degrees
        private List<double[]> compassBearingFilter = new List<double[]>();
        private float[] IMUangles;

        private DateTime timestampLastUpdate;
        //private float gx, gy, gz;  //gyrovalue degrees/s
        //private float ax, ay, az;  //accelerometer g
        //private float mx, my, mz;  //magnet gauss
        private double gx, gy, gz;  //gyrovalue degrees/s
        private double ax, ay, az;  //accelerometer g
        private double mx, my, mz;  //magnet gauss
        private float q0, q1, q2, q3;

        private Quaternion rotation;

        public GameObject Target;

        private List<Vector3> xValuesVel; private List<Vector3> yValuesVel;
        private List<Vector3> xValuesPos; private List<Vector3> yValuesPos;

        private List<Vector3> xValuesAcc; private List<Vector3> yValuesAcc;
        private List<Vector3> xValuesGyr; private List<Vector3> yValuesGyr;

        private int order = 1;

        private ButterWorthFilter.ButterWorth mButterHP;
        private ButterWorthFilter.ButterWorth mButterLP;
        private Matrix4x4 mMatrix;
        private Matrix4x4 rMatrix;
        private Vector3 mTcAcc;
        private Vector3 mLinVel;
        private Vector3 mLinVelPrev;
        private Vector3 mLinPos;
        private Vector3 mLinPosPrev;
        private Vector3 mLinAcc;
        private double mSamplePeriod;
        private double pX, pY, pZ;

        private Vector3 pos;

        // Use this for initialization
        void Awake()
        {
            if (phidgetsAttached)
            {
                if (SystemInfo.deviceType == DeviceType.Handheld)
                    return;

                spatial = new Spatial();
                spatial.open();
                spatial.waitForAttachment(1000);
                spatial.DataRate = 64;
                spatial.zeroGyro();

                //These constants are geographic dependent --- do not change
                spatial.setCompassCorrectionParameters(0.64473, 0.04197, -0.01880, -0.01187, 1.51858, 1.51659, 1.61795, 0.00715, 0.00798, 0.00675, -0.01492, 0.00817, -0.01580);
                spatial.SpatialData += new SpatialDataEventHandler(spatial_SpatialData);

                rotation = Quaternion.identity;
                timestampLastUpdate = DateTime.MinValue;

                IMUangles = new float[3];
                for (int i = 0; i < 3; i++)
                    IMUangles[i] = 0;
            }

            mMatrix = Matrix4x4.identity;
            rMatrix = Matrix4x4.identity;
            mTcAcc = Vector3.zero;
            mLinAcc = Vector3.zero;
            mLinVel = Vector3.zero;
            mLinVelPrev = Vector3.zero;
            mLinPos = Vector3.zero;
            mLinPosPrev = Vector3.zero;
            mSamplePeriod = 0.004;

            xValuesVel = new List<Vector3>(); yValuesVel = new List<Vector3>();
            xValuesPos = new List<Vector3>(); yValuesPos = new List<Vector3>();

            xValuesAcc = new List<Vector3>(); yValuesAcc = new List<Vector3>();
            xValuesGyr = new List<Vector3>(); yValuesGyr = new List<Vector3>();

            mButterHP = new ButterWorthFilter.ButterWorth(ButterWorthFilter.ButterWorth.BandType.high);
            mButterLP = new ButterWorthFilter.ButterWorth(ButterWorthFilter.ButterWorth.BandType.low);

            pos = Vector3.zero;
        }

        void Update()
        {
            // Update ray rotation
            if (phidgetsAttached)
            {
                if (SystemInfo.deviceType == DeviceType.Handheld)
                    return;

                Target.transform.localRotation = rotation;
                Target.transform.localPosition = pos;
            }
            // Use arrow keys when phidgets imu is not attached
            else
            {
                float dirX, dirY;
                dirX = dirY = 0;

                if (Input.GetKey(KeyCode.LeftArrow))
                    dirY = -0.5f;
                if (Input.GetKey(KeyCode.RightArrow))
                    dirY = 0.5f;
                if (Input.GetKey(KeyCode.DownArrow))
                    dirX = 0.5f;
                if (Input.GetKey(KeyCode.UpArrow))
                    dirX = -0.5f;

                Target.transform.Rotate(dirX, dirY, 0, Space.Self);
            }
        }

        public float SamplePeriod = 0.03f;

        // Use for calculations using sensors
        private void spatial_SpatialData(object sender, SpatialDataEventArgs e)
        {
            if (phidgetsAttached)
            {
                if (spatial.accelerometerAxes.Count != 3 || spatial.gyroAxes.Count != 3 || spatial.compassAxes.Count != 3)
                    return;

                if (timestampLastUpdate != DateTime.MinValue)
                {
                  //ax = (float)e.spatialData[0].Acceleration[0];
                  //ay = (float)e.spatialData[0].Acceleration[1];
                  //az = (float)e.spatialData[0].Acceleration[2];
                  ax = e.spatialData[0].Acceleration[0];
                  ay = e.spatialData[0].Acceleration[1];
                  az = e.spatialData[0].Acceleration[2];
                  Vector3 acc = new Vector3((float)ax, (float)ay, (float)az);
                  //Debug.Log("BEFORE FILTER: " + ax + " " + ay + " " + az );

                  // Conversion into correct units
                  //gx = (float)spatial.gyroAxes[0].AngularRate * 0.0174532925f;
                  //gy = (float)spatial.gyroAxes[1].AngularRate * 0.0174532925f;
                  //gz = (float)spatial.gyroAxes[2].AngularRate * 0.0174532925f;

                  //mx = (float)spatial.compassAxes[0].MagneticField;
                  //my = (float)spatial.compassAxes[1].MagneticField;
                  //mz = (float)spatial.compassAxes[2].MagneticField;
                  mx = spatial.compassAxes[0].MagneticField;
                  my = spatial.compassAxes[1].MagneticField;
                  mz = spatial.compassAxes[2].MagneticField;

                  gx = spatial.gyroAxes[0].AngularRate * 0.0174532925f;
                  gy = spatial.gyroAxes[1].AngularRate * 0.0174532925f;
                  gz = spatial.gyroAxes[2].AngularRate * 0.0174532925f;
                  Vector3 gyr = new Vector3((float)gx, (float)gy, (float)gz);  //new is very very important

                  float seconds = (float)(DateTime.Now - timestampLastUpdate).TotalSeconds;
                  //madgwick.SamplePeriod = (float)(seconds);
                  madgwick.SamplePeriod = (float)mSamplePeriod;

                  accelBeforeFilter.WriteLine("" + ax + "," + ay + "," + az);

                  // Filter Acc data
                  xValuesAcc.Add(acc);
                  if (xValuesAcc.Count > (order + 1))
                  {
                    xValuesAcc.RemoveAt(0);
                  }

                  if (xValuesAcc.Count == (order + 1))
                  {
                    // filter
                    Vector3 yValue = mButterLP.applyButterWorth(xValuesAcc, yValuesAcc);
                    yValuesAcc.Add(yValue);
                  }
                  else
                  {
                    yValuesAcc.Add(acc);
                  }

                  if (yValuesAcc.Count > order)
                  {
                    yValuesAcc.RemoveAt(0);
                  }


                  //Filter Gyro Data
                  xValuesGyr.Add(gyr);
                  if (xValuesGyr.Count > (order + 1))
                  {
                    xValuesGyr.RemoveAt(0);
                  }

                  if (xValuesGyr.Count == (order + 1))
                  {
                    // filter
                    Vector3 yValue = mButterLP.applyButterWorth(xValuesGyr, yValuesGyr);
                    yValuesGyr.Add(yValue);
                  }
                  else
                  {
                    yValuesGyr.Add(gyr);
                  }

                  if (yValuesGyr.Count > order)
                  {
                    yValuesGyr.RemoveAt(0);
                  }

                  int sz = order;
                  int lastIndex = yValuesGyr.Count - 1;

                  //Debug.Log(yValuesGyr[lastIndex].x + ", " + yValuesGyr[lastIndex].y + ", " +yValuesGyr[lastIndex].z + ", ");

                  gx = yValuesGyr[lastIndex].x;
                  gy = yValuesGyr[lastIndex].y;
                  gz = yValuesGyr[lastIndex].z;


                  lastIndex = yValuesAcc.Count - 1;
                  ax = yValuesAcc[lastIndex].x;
                  ay = yValuesAcc[lastIndex].y;
                  az = yValuesAcc[lastIndex].z;

                  accelAfterFilter.WriteLine("" + ax + "," + ay + "," + az);

                  AHRSUpdate(gx, gy, gz, ax, ay, az);

                  Quaternion mappedQuat;
                  mappedQuat = new Quaternion(-madgwick.Quaternion[1], madgwick.Quaternion[2], madgwick.Quaternion[3], madgwick.Quaternion[0]);
                  //mMatrix.SetTRS(Vector3.zero, mappedQuat, new Vector3(1, 1, 1));

                  mMatrix = QuaternionToMatrix(mappedQuat);
                  rMatrix = mMatrix;
                  Matrix4x4 aMatrix = mMatrix.inverse;

                  //print((mMatrix * aMatrix).ToString());


                  //print(madgwick.Quaternion[0] + " " + madgwick.Quaternion[1] + " " + madgwick.Quaternion[2] + " " + madgwick.Quaternion[3]);
                  //print(mMatrix.ToString());
                  ////print(mMatrix[0, 0] + ", " + mMatrix[0, 1] + ", " + mMatrix[0, 2] + ", " + mMatrix[0, 3] + ", ");
                  ////print(mMatrix[1, 0] + ", " + mMatrix[1, 1] + ", " + mMatrix[1, 2] + ", " + mMatrix[1, 3] + ", ");
                  ////print(mMatrix[1, 0] + ", " + mMatrix[2, 1] + ", " + mMatrix[2, 2] + ", " + mMatrix[2, 3] + ", ");
                  ////print(mMatrix[1, 0] + ", " + mMatrix[3, 1] + ", " + mMatrix[3, 2] + ", " + mMatrix[3, 3] + ", ");

                  /* Not getting the correct acceleration in x. Multiplying acc vector with inverse of rmatrix does not give correct acc in x */
                  //accelerometer in Earth frame
                  mTcAcc = new Vector3((float)yValuesAcc[lastIndex].x, (float)yValuesAcc[lastIndex].y, (float)yValuesAcc[lastIndex].z);
                  //print("before: " + mTcAcc.x + ", " + mTcAcc.y + ", " + mTcAcc.z + ", ");

                  //mTcAcc = aMatrix.MultiplyPoint(mTcAcc);
                  mTcAcc = VecMulMatrix(mTcAcc, aMatrix);
                  //print("mTcAcc: " + mTcAcc.x + ", " + mTcAcc.y + ", " +  mTcAcc.z + ", ");

                  ////linear acceleration in earth frame
                  mTcAcc -= new Vector3(0f, 0f, 1f);
                  //print("mTcAcc: " + mTcAcc.x + ", " + mTcAcc.y + ", " + mTcAcc.z + ", ");


                  mLinAcc = (mTcAcc * (9.81f));
                  //print("after: " + mLinAcc.x + ", " + mLinAcc.y + ", " + mLinAcc.z + ", ");

                  ///* Linear velocity */
                  mLinVel = mLinVelPrev + mLinAcc * (float)mSamplePeriod;//(float)(seconds); //
                  mLinVelPrev = mLinVel;

                  ////print("before : velx  " + mLinVel.x + "," + "vely " + mLinVel.y + "," + "velz  " + mLinVel.z + ",");

                  velocityBeforeFilter.WriteLine(mLinVel.x + "," + mLinVel.y + "," + mLinVel.z);

                  ////high pass filter linear velocity to remove drift
                  ////push the lasted to the end and delete the first
                  Vector3 temp = new Vector3();
                  temp = mLinVel;

                  xValuesVel.Add(temp);

                  if (xValuesVel.Count > (order + 1))
                  {
                    xValuesVel.RemoveAt(0);
                  }

                  if (xValuesVel.Count == (order + 1))
                  {
                    //filter
                    Vector3 yValue = mButterHP.applyButterWorth(xValuesVel, yValuesVel);
                    yValuesVel.Add(yValue);
                  }
                  else
                  {
                    yValuesVel.Add(mLinVel);
                  }

                  if (yValuesVel.Count > order)
                  {
                    yValuesVel.RemoveAt(0);
                  }

                  lastIndex = yValuesVel.Count - 1;

                  velocityAfterFilter.WriteLine("" + yValuesVel[lastIndex].x + "," + yValuesVel[lastIndex].y + "," + yValuesVel[lastIndex].z + ",");

                  ///* Linear position */
                  mLinPos = (mLinPosPrev + (yValuesVel[yValuesVel.Count - 1] * (float)(mSamplePeriod)));//(float)(seconds)));//
                  mLinPosPrev = (mLinPos);

                  //high pass filter linear position to remove drift
                  Vector3 temppos = new Vector3();
                  temppos = (mLinPos);

                  xValuesPos.Add(temppos);
                  if (xValuesPos.Count > (order + 1))
                  {
                    xValuesPos.RemoveAt(0);
                  }

                  if (xValuesPos.Count == (order + 1))
                  {
                    //filter
                    Vector3 yValue = mButterHP.applyButterWorth(xValuesPos, yValuesPos);
                    yValuesPos.Add(yValue);
                  }
                  else
                  {
                    yValuesPos.Add(mLinPos);
                  }

                  if (yValuesPos.Count > order)
                  {
                    yValuesPos.RemoveAt(0);
                  }

                  pX = yValuesPos[yValuesPos.Count - 1].x;
                  pY = yValuesPos[yValuesPos.Count - 1].y;
                  pZ = yValuesPos[yValuesPos.Count - 1].z;
                }

                /*
                 * 
                 * Positional calculation still bugged.
                 * 
                 */

                timestampLastUpdate = DateTime.Now;


                CalculateCompassBearing();
            }
        }

        private void AHRSUpdate(double gx, double gy, double gz, double ax, double ay, double az, double mx, double my, double mz)
        {
            madgwick.Update((float)gx, (float)gy, (float)gz, (float)ax, (float)ay, (float)az, (float)mx, (float)my, (float)mz);

            // Assuming madgwick quaternion returns in [x,y,z,w] for [0,1,2,3]
            q0 = madgwick.Quaternion[0];
            q1 = madgwick.Quaternion[1];
            q2 = madgwick.Quaternion[2];
            q3 = madgwick.Quaternion[3];

            // Different quaternion signs for different orientation of IMU. 
            // Not sure if there is a way to keep it consistent. 

            // Use this for when the IMU is face up
            rotation = new Quaternion(-q1, -q0, q2, q3);

            // Use this when IMU is upside down
            //rotation = new Quaternion(q1, -q0, q2, -q3);

            // Test
            //orientation = new Quaternion(q2, -q1, q3, q0);

            // Test2
            //orientation = new Quaternion(q3, q2, q0, q1);

            // Test3
            //orientation = new Quaternion(-q1, q0, q2, -q3);


        }

        private void AHRSUpdate(double gx, double gy, double gz, double ax, double ay, double az)
        {
            madgwick.Update((float)gx, (float)gy, (float)gz, (float)ax, (float)ay, (float)az);

            // Assuming madgwick quaternion returns in [x,y,z,w] for [0,1,2,3]
            q0 = madgwick.Quaternion[0];
            q1 = madgwick.Quaternion[1];
            q2 = madgwick.Quaternion[2];
            q3 = madgwick.Quaternion[3];

            // Different quaternion signs for different orientation of IMU. 
            // Not sure if there is a way to keep it consistent. 

            // Use this for when the IMU is face up
            //rotation = new Quaternion(-q1, -q0, q2, q3);

            // Use this when IMU is upside down
            //rotation = new Quaternion(q1, -q0, q2, -q3);

            // Test
            //orientation = new Quaternion(q2, -q1, q3, q0);

            // Test2
            //orientation = new Quaternion(q3, q2, q0, q1);

            // Test3
            //orientation = new Quaternion(-q1, q0, q2, -q3);

            // Test4 madgwick is in [w,pitch,roll,yaw]
            rotation = new Quaternion(q2, q3, q1, q0);
        }

        // Matrix to Quaternion when necessary.
        private Quaternion MatrixToQuaternion(double r00, double r01, double r02, double r10, double r11, double r12, double r20, double r21, double r22)
        {
            float temp = (float)(1 + r00 + r11 + r22);
            q3 = Mathf.Sqrt(temp) / 2;
            q0 = (float)(r21 - r12) / (4 * q3);
            q1 = (float)(r02 - r20) / (4 * q3);
            q2 = (float)(r10 - r01) / (4 * q3);

            return float.IsNaN(q0) ? Quaternion.Euler(0f, 0f, 0f) : new Quaternion(q0, q1, q2, q3);
        }

        private Matrix4x4 QuaternionToMatrix(Quaternion q)
        {
            Matrix4x4 retMatrix = Matrix4x4.zero;
            double m00, m01, m02, m10, m11, m12, m20, m21, m22;
            Vector3 Row0, Row1, Row2, Row3;
            double sqw = q.w * q.w;
            double sqx = q.x * q.x;
            double sqy = q.y * q.y;
            double sqz = q.z * q.z;

            // invs (inverse square length) is only required if quaternion is not already normalised
            double invs = 1 / (sqx + sqy + sqz + sqw);
            m00 = (sqx - sqy - sqz + sqw) * invs; // since sqw + sqx + sqy + sqz =1/invs*invs
            m11 = (-sqx + sqy - sqz + sqw) * invs;
            m22 = (-sqx - sqy + sqz + sqw) * invs;

            double tmp1 = q.x * q.y;
            double tmp2 = q.z * q.w;
            m10 = 2.0 * (tmp1 + tmp2) * invs;
            m01 = 2.0 * (tmp1 - tmp2) * invs;

            tmp1 = q.x * q.z;
            tmp2 = q.y * q.w;
            m20 = 2.0 * (tmp1 - tmp2) * invs;
            m02 = 2.0 * (tmp1 + tmp2) * invs;
            tmp1 = q.y * q.z;
            tmp2 = q.x * q.w;
            m21 = 2.0 * (tmp1 + tmp2) * invs;
            m12 = 2.0 * (tmp1 - tmp2) * invs;

            Row0 = new Vector4((float)m00, (float)m01, (float)m02, 0);
            Row1 = new Vector4((float)m10, (float)m11, (float)m12, 0);
            Row2 = new Vector4((float)m20, (float)m21, (float)m22, 0);
            Row3 = new Vector4(0, 0, 0, 1);

            retMatrix.SetRow(0, Row0);
            retMatrix.SetRow(1, Row1);
            retMatrix.SetRow(2, Row2);
            retMatrix.SetRow(3, Row3);

            return retMatrix;
        }

        private Vector3 VecMulMatrix(Vector3 vector, Matrix4x4 matrix)
        {
            Vector3 retVal = Vector3.zero;
            float x, y, z;

            //x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03]
            x = (float)(vector.x * matrix[0, 0] + vector.y * matrix[0, 1] + vector.z * matrix[0, 2] + matrix[0, 3]);
            //x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13]
            y = (float)(vector.x * matrix[1, 0] + vector.y * matrix[1, 1] + vector.z * matrix[1, 2] + matrix[1, 3]);
            //x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]
            z = (float)(vector.x * matrix[2, 0] + vector.y * matrix[2, 1] + vector.z * matrix[2, 2] + matrix[2, 3]);

            retVal = new Vector3(x, y, z);

            return retVal;
        }

        // Calculates IMU angles in X Y Z
        // X is (-90, 90) angles[0]
        // Y is (-180, 180) angles[1]
        // Z is (-180, 180) angles[2]
        private void CalculateCompassBearing()
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
                for (int i = 0; i < angles.Length; i++)
                    IMUangles[i] = (float)angles[i];
            }
            catch { }
        }

        private float[] getAngles()
        {
            return IMUangles;
        }

        public void OnApplicationQuit()
        {
            if (phidgetsAttached)
            {
                if (SystemInfo.deviceType == DeviceType.Handheld)
                    return;

                spatial.close();
            }
        }


    }

}