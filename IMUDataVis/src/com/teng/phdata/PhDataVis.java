package com.teng.phdata;

import java.util.ArrayList;

import processing.core.PApplet;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.SpatialPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.SpatialDataEvent;
import com.phidgets.event.SpatialDataListener;
import com.teng.filter.ButterWorth;
import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

public class PhDataVis extends PApplet{
	
	public SpatialPhidget spatial;
	
	private AHRS mAHRS;
	private double[] matrix;
	private Matrix4 mMatrix;
	private Matrix4 rMatrix;
	private Vector3 mTcAcc;
	private Vector3 mLinVel;
	private Vector3 mLinVelPrev;
	private Vector3 mLinPos;
	private Vector3 mLinPosPrev;
	
	private Vector3 mLinAcc;
	private double mSamplePeriod;
	
	private ArrayList<Vector3> xValuesVel; private ArrayList<Vector3> yValuesVel;
	private ArrayList<Vector3> xValuesPos; private ArrayList<Vector3> yValuesPos;
	
	private ArrayList<Vector3> xValuesAcc; private ArrayList<Vector3> yValuesAcc;
	private ArrayList<Vector3> xValuesGyr; private ArrayList<Vector3> yValuesGyr;
	
	private int order = 1;
	private ButterWorth mButterHP;
	private ButterWorth mButterLP;
	
	private double pX, pY, pZ;
	
	public double vX, vY, vZ;
	
	DataStorage dataStorage;
	
	public void setup()
	{
		//processing setup
		size(1000, 1000, P3D);
		background(250);
		camera(500.0f, 200.0f, 1000.0f, 500.0f, 500.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		pushMatrix();
		translate(width/2, height*3/5, 0);
		noFill();
		stroke(100, 0, 200);
		for(int itrz = 0; itrz < 500; itrz+=100)
		{
			line(-350, 0, itrz, 350, 0, itrz);
		}
		
		stroke(100, 200, 0);
		for(int itry = 0; itry > -500; itry-=100)
		{
			line(-350, itry, 0, 350, itry, 0);
		}
		popMatrix();
		
		//dataStorage = DataStorage.getInstance();
		
		//ahrs init
		mAHRS = new AHRS(0.004f, 0.1f);
		matrix = new double[16];
		mMatrix = new Matrix4();
		rMatrix = new Matrix4();
		mTcAcc = new Vector3();
		mLinVel = new Vector3();
		mLinVelPrev = new Vector3(); mLinVelPrev.Set(0, 0, 0);
		mLinPos = new Vector3();
		mLinPosPrev = new Vector3(); mLinPosPrev.Set(0,0,0);
		
		mLinAcc = new Vector3();
		mSamplePeriod = 0.004f;
		
		xValuesVel = new ArrayList<Vector3>();  //size 2
		yValuesVel = new ArrayList<Vector3>();  //size 1
		xValuesPos = new ArrayList<Vector3>();	//size 2
		yValuesPos = new ArrayList<Vector3>();  //size 1
		
		xValuesAcc = new ArrayList<Vector3>();  //size 2
		yValuesAcc = new ArrayList<Vector3>();  //size 1
		xValuesGyr = new ArrayList<Vector3>();  //size 2
		yValuesGyr = new ArrayList<Vector3>(); //size 1
		
		mButterHP = new ButterWorth(ButterWorth.BandType.high);
		mButterLP = new ButterWorth(ButterWorth.BandType.low);
		
		//phidget setup
		System.out.println(Phidget.getLibraryVersion());
		try {
			spatial = new SpatialPhidget();
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		spatial.addAttachListener(new AttachListener() {
			public void attached(AttachEvent ae){
				System.out.println("attachment of " + ae);
				try
				{
					((SpatialPhidget)ae.getSource()).setDataRate(4); //set data rate to 496ms
				}
				catch (PhidgetException pe)
				{
					System.out.println("Problem setting data rate!");
				}
			}
		});
		spatial.addDetachListener(new DetachListener() {
			public void detached(DetachEvent ae) {
				System.out.println("detachment of " + ae);
			}
		});
		spatial.addErrorListener(new ErrorListener() {
			public void error(ErrorEvent ee) {
				System.out.println("error event for " + ee);
			}
		});
		spatial.addSpatialDataListener(new SpatialDataListener() {
			public void data(SpatialDataEvent sde) {
				//System.out.println(sde);
				
				int i,j;
				for(j=0;j<sde.getData().length;j++)
				{
					if(sde.getData()[j].getAcceleration().length>0 && sde.getData()[j].getAngularRate().length>0 && sde.getData()[j].getMagneticField().length>0)
					{
						double ax = sde.getData()[j].getAcceleration()[0];
						double ay = sde.getData()[j].getAcceleration()[1];
						double az = sde.getData()[j].getAcceleration()[2];
						Vector3 acc = new Vector3(ax, ay, az);
						
						double gx = sde.getData()[j].getAngularRate()[0]* 0.0174532925;
						double gy = sde.getData()[j].getAngularRate()[1]* 0.0174532925;
						double gz = sde.getData()[j].getAngularRate()[2]* 0.0174532925;
						Vector3 gyr = new Vector3( gx, gy, gz);  //new is very very important
						
						double mx = sde.getData()[j].getMagneticField()[0];
						double my = sde.getData()[j].getMagneticField()[1];
						double mz = sde.getData()[j].getMagneticField()[2];
						
						//filter the acc data
						xValuesAcc.add(acc);
						if(xValuesAcc.size() > (order + 1))
						{
							xValuesAcc.remove(0);
						}
					
						if(xValuesAcc.size() == (order + 1))
						{
							//filter
							Vector3 yValue = mButterLP.applyButterWorth(xValuesAcc, yValuesAcc);
							yValuesAcc.add(yValue);
						}else
						{
							yValuesAcc.add(acc);
						}
						
						if(yValuesAcc.size() > order)
						{
							yValuesAcc.remove(0);
						}
						
						//filter the gyr data
						xValuesGyr.add(gyr);
						if(xValuesGyr.size() > (order + 1))
						{
							xValuesGyr.remove(0);
						}
					
						if(xValuesGyr.size() == (order + 1))
						{
							//filter
							Vector3 yValue = mButterLP.applyButterWorth(xValuesGyr, yValuesGyr);
							yValuesGyr.add(yValue);
						}else
						{
							yValuesGyr.add(gyr);
						}
						
						if(yValuesGyr.size() > order)
						{
							yValuesGyr.remove(0);
						}
						
						int sz = order;
						//DataStorage.AddSampleF(gyr.x, gyr.y, gyr.z, acc.x, acc.y, acc.z, yValuesGyr.get(sz-1).x, yValuesGyr.get(sz-1).y, yValuesGyr.get(sz-1).z, yValuesAcc.get(sz-1).x,  yValuesAcc.get(sz-1).y,  yValuesAcc.get(sz-1).z);
						//System.out.println("gyro: " + gx + ", " + gy + ", " +  gz + ", ");
						
						
						//device orientation
						//mAHRS.Update((double)gx, (double)gy, (double)gz, (double)ax, (double)ay, (double)az, (double)mx, (double)my, (double)mz);
						int lastIndex = yValuesGyr.size() - 1;
						gx = yValuesGyr.get(lastIndex).x;
						gy = yValuesGyr.get(lastIndex).y;
						gz = yValuesGyr.get(lastIndex).z;
						
						lastIndex = yValuesAcc.size() - 1;
						ax = yValuesAcc.get(lastIndex).x;
						ay = yValuesAcc.get(lastIndex).y;
						az = yValuesAcc.get(lastIndex).z;
						
						mAHRS.Update(gx, gy, gz, ax, ay, az);   //problem here, coordinates mis-mapped with the acc/gyro coordinates
						
						//System.out.println(mAHRS.quaternion.x + ", " + mAHRS.quaternion.y + ", " + mAHRS.quaternion.z + ", " + mAHRS.quaternion.w + ", ");
						//System.out.println();
						Quaternion mappedQuat = new Quaternion();
						mappedQuat.Set(-mAHRS.quaternion.z, mAHRS.quaternion.y, mAHRS.quaternion.x, mAHRS.quaternion.w);
						//System.out.println(mappedQuat.x + ", " + mappedQuat.y + ", " + mappedQuat.z + ", " + mappedQuat.w + ", ");
						
						mMatrix.Set(mappedQuat);
						//mMatrix.tra();  //transpose
						rMatrix.Set(mMatrix);
						//test
						//System.out.println( mMatrix.val[mMatrix.M00] + ", " +  mMatrix.val[mMatrix.M01] + ", " + mMatrix.val[mMatrix.M02] + ", " +  mMatrix.val[mMatrix.M03] + ", ");
						//System.out.println( mMatrix.val[mMatrix.M10] + ", " +  mMatrix.val[mMatrix.M11] + ", " + mMatrix.val[mMatrix.M12] + ", " +  mMatrix.val[mMatrix.M13] + ", ");
						//System.out.println( mMatrix.val[mMatrix.M20] + ", " +  mMatrix.val[mMatrix.M21] + ", " + mMatrix.val[mMatrix.M22] + ", " +  mMatrix.val[mMatrix.M23] + ", ");
						//System.out.println( mMatrix.val[mMatrix.M30] + ", " +  mMatrix.val[mMatrix.M31] + ", " + mMatrix.val[mMatrix.M32] + ", " +  mMatrix.val[mMatrix.M33] + ", ");
						
						//System.out.println();
						
						//accelerometer in Earth frame
						mTcAcc.Set(yValuesAcc.get(lastIndex));
						
						//System.out.println("before: " + mTcAcc.x + ", " + mTcAcc.y + ", " +  mTcAcc.z + ", ");
						
						
						//test
						//Vector3 geoVec = new Vector3();
						//geoVec.Set(0f, 0f, 1f);
						//geoVec.Mul(rMatrix);
						
						//System.out.println("after: " + geoVec.x + ", " + geoVec.y + ", " +  geoVec.z + ", ");
						
						mTcAcc.Mul(rMatrix.Inv());
						
						//System.out.println("after: " + mTcAcc.x + ", " + mTcAcc.y + ", " +  mTcAcc.z + ", ");
						
						//linear acceleration in earth frame
						mTcAcc.Sub(0f, 0f, 1f);
						mLinAcc.Set( mTcAcc.scl(9.81f));
						
						//System.out.println("after: " + mLinAcc.x + ", " + mLinAcc.y + ", " +  mLinAcc.z + ", ");
						//System.out.println();
						
						
						//linear velocity  //problem
						mLinVel.Set( mLinVelPrev.add(mLinAcc.scl(mSamplePeriod)));   
						mLinVelPrev.Set(mLinVel.x, mLinVel.y, mLinVel.z);
						
						
						//System.out.println("before : velx  " + mLinVel.x + ","  + "vely " + mLinVel.y + "," + "velz  " + mLinVel.z + ",");
						
						//high pass filter linear velocity to remove drift
						//push the lasted to the end and delete the first
						Vector3 temp = new Vector3();
						temp.Set(mLinVel);
						
						xValuesVel.add(temp);
						
						if(xValuesVel.size() > (order + 1))
						{
							xValuesVel.remove(0);		
						}
					
						if(xValuesVel.size() == (order + 1))
						{
							//filter
							Vector3 yValue = mButterHP.applyButterWorth(xValuesVel, yValuesVel);
							yValuesVel.add(yValue);
						}else
						{
							yValuesVel.add(mLinVel);
						}
						
						if(yValuesVel.size() > order)
						{
							yValuesVel.remove(0);
						}
						//here, the size of xs is order+1, the size of ys is order
						
						vX = yValuesVel.get(yValuesVel.size() - 1).x;
						vY = yValuesVel.get(yValuesVel.size() - 1).y;
						vZ = yValuesVel.get(yValuesVel.size() - 1).z;
						
						if(xValuesVel.size() == ( order+1))
							DataStorage.AddSampleF(1.0, xValuesVel.get(order).x, xValuesVel.get(order).y, xValuesVel.get(order).z, vX, vY, vZ, 0f, 0f, 0f, 0f, 0f, 0f);
						else
							DataStorage.AddSampleF(1.0, xValuesVel.get(order-1).x, xValuesVel.get(order-1).y, xValuesVel.get(order-1).z, vX, vY, vZ, 0f, 0f, 0f, 0f, 0f, 0f);
						
						
						
						//linear position
						mLinPos.Set(mLinPosPrev.add(yValuesVel.get(yValuesVel.size() - 1).scl(mSamplePeriod)));
						mLinPosPrev.Set(mLinPos);
						
						//high pass filter linear position to remove drift
						Vector3 temppos = new Vector3();
						temppos.Set(mLinPos);
						
						xValuesPos.add(temppos);
						if(xValuesPos.size() > (order + 1))
						{
							xValuesPos.remove(0);
						}
					
						if(xValuesPos.size() == (order + 1))
						{
							//filter
							Vector3 yValue = mButterHP.applyButterWorth(xValuesPos, yValuesPos);
							yValuesPos.add(yValue);
						}else
						{
							yValuesPos.add(mLinPos);
						}
						
						if(yValuesPos.size() > order)
						{
							yValuesPos.remove(0);
						}
						
						//result of pos
						pX =  yValuesPos.get(yValuesPos.size() - 1).x;
						pY =  yValuesPos.get(yValuesPos.size() - 1).y;
						pZ =  yValuesPos.get(yValuesPos.size() - 1).z;
						//System.out.println("x  " + pX + ","  + "y  " + pY + "," + "z  " + pZ + ","); 
						
					}
					
				}
			}
		});

		try {
			spatial.openAny();
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("waiting for Spatial attachment...");
		try {
			spatial.waitForAttachment();
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			System.out.println("Serial: " + spatial.getSerialNumber());
			System.out.println("Accel Axes: " + spatial.getAccelerationAxisCount());
			System.out.println("Gyro Axes: " + spatial.getGyroAxisCount());
			System.out.println("Compass Axes: " + spatial.getCompassAxisCount());
		}catch (PhidgetException e){
			e.printStackTrace();
		}
		
	}
	
	public void draw(){
		background(250);
		//draw coordinates
		{
			pushMatrix();
			translate(width/2, height*3/5, 0);
			noFill();
			stroke(100, 0, 200);
			for(int itrz = 0; itrz < 500; itrz+=100)
			{
				line(-350, 0, itrz, 350, 0, itrz);
			}
			
			stroke(100, 200, 0);
			for(int itry = 0; itry > -500; itry-=100)
			{
				line(-350, itry, 0, 350, itry, 0);
			}
			popMatrix();
		}
		
		//draw cube
		{
			pushMatrix();				
			fill(200, 10, 50);
			lights();
			//translate(width/2 + (float)pX*1000.0f, height*3/5 + (float)pY*1000.0f, (float)pZ*1000.0f);
			translate(width/2 - (float)pY*1000.0f, height*3/5 -  (float)pZ*1000.0f  ,  (float)pX*1000.0f);
			
			//translate(width/2 , height*3/5 , 0f);
			
			//apply rotation matrix
			//applyMatrix((float) mMatrix.val[mMatrix.M00], (float)mMatrix.val[mMatrix.M01], (float)mMatrix.val[mMatrix.M02], (float)mMatrix.val[mMatrix.M03],
				//	(float)mMatrix.val[mMatrix.M10], (float)mMatrix.val[mMatrix.M11], (float)mMatrix.val[mMatrix.M12], (float)mMatrix.val[mMatrix.M13],
					//(float)mMatrqix.val[mMatrix.M20], (float)mMatrix.val[mMatrix.M21], (float)mMatrix.val[mMatrix.M22], (float)mMatrix.val[mMatrix.M23],
					//(float)mMatrix.val[mMatrix.M30], (float)mMatrix.val[mMatrix.M31], (float)mMatrix.val[mMatrix.M32], (float)mMatrix.val[mMatrix.M33]);
			noStroke();
			box(10);
			
			//velocity lines
			stroke(125);
			line(0, 0, 0, -(float)vY * 200.0f, 0, 0);
			line(0, 0, 0, 0, -(float)vZ * 200.0f, 0);
			line(0, 0, 0, 0, 0, (float)vX * 200.0f);
			
			popMatrix();
		}
		
	}
	
	public void keyPressed(){
		if(key == 'q'){
			try {
				spatial.close();
			} catch (PhidgetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dataStorage.savef();
			
			spatial = null;
			exit();
		}
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.phdata.PhDataVis"});
	}
}
