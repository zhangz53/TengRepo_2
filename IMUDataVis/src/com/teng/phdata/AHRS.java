package com.teng.phdata;

import com.teng.math.Quaternion;

public class AHRS {

	public Quaternion quaternion;
	public double SamplePeriod;
	public double Beta;
	
	public AHRS(double samplePeriod, double beta)
	{
		quaternion = new Quaternion();;
		SamplePeriod = samplePeriod;
		Beta = beta;
	}
	
	public void Update(double gx, double gy, double gz, double ax, double ay, double az, double mx, double my, double mz)
    {
        double q1 = quaternion.x, q2 = quaternion.y, q3 = quaternion.z, q4 = quaternion.w;   // short name local variable for readability
        double norm;
        double hx, hy, _2bx, _2bz;
        double s1, s2, s3, s4;
        double qDot1, qDot2, qDot3, qDot4;

        // Auxiliary variables to avoid repeated arithmetic
        double _2q1mx;
        double _2q1my;
        double _2q1mz;
        double _2q2mx;
        double _4bx;
        double _4bz;
        double _2q1 = 2f * q1;
        double _2q2 = 2f * q2;
        double _2q3 = 2f * q3;
        double _2q4 = 2f * q4;
        double _2q1q3 = 2f * q1 * q3;
        double _2q3q4 = 2f * q3 * q4;
        double q1q1 = q1 * q1;
        double q1q2 = q1 * q2;
        double q1q3 = q1 * q3;
        double q1q4 = q1 * q4;
        double q2q2 = q2 * q2;
        double q2q3 = q2 * q3;
        double q2q4 = q2 * q4;
        double q3q3 = q3 * q3;
        double q3q4 = q3 * q4;
        double q4q4 = q4 * q4;

        // Normalise accelerometer measurement
        norm = (double)Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0f) return; // handle NaN
        norm = 1 / norm;        // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Normalise magnetometer measurement
        norm = (double)Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0f) return; // handle NaN
        norm = 1 / norm;        // use reciprocal for division
        mx *= norm;
        my *= norm;
        mz *= norm;

        // Reference direction of Earth's magnetic field
        _2q1mx = 2f * q1 * mx;
        _2q1my = 2f * q1 * my;
        _2q1mz = 2f * q1 * mz;
        _2q2mx = 2f * q2 * mx;
        hx = mx * q1q1 - _2q1my * q4 + _2q1mz * q3 + mx * q2q2 + _2q2 * my * q3 + _2q2 * mz * q4 - mx * q3q3 - mx * q4q4;
        hy = _2q1mx * q4 + my * q1q1 - _2q1mz * q2 + _2q2mx * q3 - my * q2q2 + my * q3q3 + _2q3 * mz * q4 - my * q4q4;
        _2bx = (double)Math.sqrt(hx * hx + hy * hy);
        _2bz = -_2q1mx * q3 + _2q1my * q2 + mz * q1q1 + _2q2mx * q4 - mz * q2q2 + _2q3 * my * q4 - mz * q3q3 + mz * q4q4;
        _4bx = 2f * _2bx;
        _4bz = 2f * _2bz;

        // Gradient decent algorithm corrective step
        s1 = -_2q3 * (2f * q2q4 - _2q1q3 - ax) + _2q2 * (2f * q1q2 + _2q3q4 - ay) - _2bz * q3 * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (-_2bx * q4 + _2bz * q2) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q3 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s2 = _2q4 * (2f * q2q4 - _2q1q3 - ax) + _2q1 * (2f * q1q2 + _2q3q4 - ay) - 4f * q2 * (1 - 2f * q2q2 - 2f * q3q3 - az) + _2bz * q4 * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q3 + _2bz * q1) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + (_2bx * q4 - _4bz * q2) * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s3 = -_2q1 * (2f * q2q4 - _2q1q3 - ax) + _2q4 * (2f * q1q2 + _2q3q4 - ay) - 4f * q3 * (1 - 2f * q2q2 - 2f * q3q3 - az) + (-_4bx * q3 - _2bz * q1) * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q2 + _2bz * q4) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + (_2bx * q1 - _4bz * q3) * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s4 = _2q2 * (2f * q2q4 - _2q1q3 - ax) + _2q3 * (2f * q1q2 + _2q3q4 - ay) + (-_4bx * q4 + _2bz * q2) * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (-_2bx * q1 + _2bz * q3) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q2 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        norm = 1f / (double)Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);    // normalise step magnitude
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        // Compute rate of change of quaternion
        qDot1 = 0.5f * (-q2 * gx - q3 * gy - q4 * gz) - Beta * s1;
        qDot2 = 0.5f * (q1 * gx + q3 * gz - q4 * gy) - Beta * s2;
        qDot3 = 0.5f * (q1 * gy - q2 * gz + q4 * gx) - Beta * s3;
        qDot4 = 0.5f * (q1 * gz + q2 * gy - q3 * gx) - Beta * s4;

        // Integrate to yield quaternion
        q1 += qDot1 * SamplePeriod;
        q2 += qDot2 * SamplePeriod;
        q3 += qDot3 * SamplePeriod;
        q4 += qDot4 * SamplePeriod;
        norm = 1f / (double)Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);    // normalise quaternion
    
        quaternion.Set(q1 * norm, q2 * norm, q3 * norm, q4 * norm);
    }
	
	
	public void Update(double gx, double gy, double gz, double ax, double ay, double az)
    {
        double q1 = quaternion.x, q2 = quaternion.y, q3 =quaternion.z, q4 = quaternion.w;   // short name local variable for readability
        double norm;
        double s1, s2, s3, s4;
        double qDot1, qDot2, qDot3, qDot4;

        // Auxiliary variables to avoid repeated arithmetic
        double _2q1 = 2 * q1;
        double _2q2 = 2 * q2;
        double _2q3 = 2 * q3;
        double _2q4 = 2 * q4;
        double _4q1 = 4 * q1;
        double _4q2 = 4 * q2;
        double _4q3 = 4 * q3;
        double _8q2 = 8 * q2;
        double _8q3 = 8 * q3;
        double q1q1 = q1 * q1;
        double q2q2 = q2 * q2;
        double q3q3 = q3 * q3;
        double q4q4 = q4 * q4;

        // Normalise accelerometer measurement
        norm = (double)Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0) return; // handle NaN
        norm = 1 / norm;        // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Gradient decent algorithm corrective step
        s1 = _4q1 * q3q3 + _2q3 * ax + _4q1 * q2q2 - _2q2 * ay;
        s2 = _4q2 * q4q4 - _2q4 * ax + 4 * q1q1 * q2 - _2q1 * ay - _4q2 + _8q2 * q2q2 + _8q2 * q3q3 + _4q2 * az;
        s3 = 4 * q1q1 * q3 + _2q1 * ax + _4q3 * q4q4 - _2q4 * ay - _4q3 + _8q3 * q2q2 + _8q3 * q3q3 + _4q3 * az;
        s4 = 4 * q2q2 * q4 - _2q2 * ax + 4 * q3q3 * q4 - _2q3 * ay;
        norm = 1 / (double)Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);    // normalise step magnitude
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        // Compute rate of change of quaternion
        qDot1 = 0.5 * (-q2 * gx - q3 * gy - q4 * gz) - Beta * s1;
        qDot2 = 0.5 * (q1 * gx + q3 * gz - q4 * gy) - Beta * s2;
        qDot3 = 0.5 * (q1 * gy - q2 * gz + q4 * gx) - Beta * s3;
        qDot4 = 0.5 * (q1 * gz + q2 * gy - q3 * gx) - Beta * s4;

        // Integrate to yield quaternion
        q1 += qDot1 * SamplePeriod;
        q2 += qDot2 * SamplePeriod;
        q3 += qDot3 * SamplePeriod;
        q4 += qDot4 * SamplePeriod;
        norm = 1 / Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);    // normalise quaternion
        //quaternion.x = q1 * norm;
        //Quaternion[1] = q2 * norm;
        //Quaternion[2] = q3 * norm;
        ///Quaternion[3] = q4 * norm;
        
        quaternion.Set(q1 * norm, q2 * norm, q3 * norm, q4 * norm);
    }

}
