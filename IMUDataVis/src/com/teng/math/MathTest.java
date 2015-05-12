package com.teng.math;

public class MathTest {
	public static final void main(String args[]){
		
		System.out.println(Math.sin(Math.PI/2));
		
		Matrix4 inputMatrix = new Matrix4();
		
		Quaternion quat = new Quaternion();
		Vector3 rotAxis = new Vector3(7.0f, 5.9f, 3.4f);
		rotAxis.Nor();
		double rotAngle =  (Math.PI / 6);
		quat.Set(rotAxis.x *  Math.sin(rotAngle/2), rotAxis.y *  Math.sin(rotAngle/2), rotAxis.z *  Math.sin(rotAngle/2), Math.cos(rotAngle/2));	
		    
		Quaternion quat2 = new Quaternion();
		quat2.Set(rotAxis, rotAngle * MathUtils.radiansToDegrees);
		
		inputMatrix.Set(quat);
		System.out.println(inputMatrix.toString());
		
		inputMatrix.Set(quat2);
		System.out.println(inputMatrix.toString());
		
		Matrix4 inputMatrix2 = new Matrix4();
		inputMatrix2.Set(inputMatrix);
		inputMatrix2.Inv();
		
		System.out.println(inputMatrix2.toString());
		
		//Matrix4 output = inputMatrix.Mul(inputMatrix2);
		//System.out.println((inputMatrix.Mul(inputMatrix2)).toString());
	}
}
