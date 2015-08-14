package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import processing.core.PApplet;

import com.teng.filter.ButterWorth;
import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class SerialDataAcc {
	CommPort commPort;
	private static String outputString = new String();
	public static Quaternion quat;
	public static Vector3 acc;
	public static double stamp = 0;  //in seconds
	public static double prevStamp = 0;
	public static double curStamp = 0;
	public static boolean isFirstFrame = true;
	
	public static Vector3 pos;      //space pos
	public static Vector3 velocity;	//space speed
	public static Vector3 linAcc;	//space acc
	public static Matrix4 mMatrix;	//space rotation matrix
	public static Vector3 filter_velocity;	//space speed
	public static Vector3 filter_pos;	//space acc
	public static Vector3 xAxis;
	
	//
	public static int outwardsCount = 0;
	public static double prevDistance = 0;
	public static int inwardsCount = 0;
	public static boolean isMovingOutwards = false;
	
	public static int logSize = 65*2; //250hz for 5 secs
	public static ArrayList<Vector3> accLog;
	public static ArrayList<Vector3> velLog;
	public static ArrayList<Vector3> posLog;
	
	
	public static ButterWorth mButterHp;

	public static SerialDataAcc instance;
	public static SerialDataAcc getSharedInstance()
	{
		if(instance == null)
		{
			instance = new SerialDataAcc();
		}
		return instance;
	}
	
	public SerialDataAcc()
	{
		super();
		quat = new Quaternion();
		acc = new Vector3();
		pos = new Vector3(); pos.Set(Vector3.Zero);
		velocity = new Vector3(); velocity.Set(Vector3.Zero);
		linAcc = new Vector3(); linAcc.Set(Vector3.Zero);
		mMatrix = new Matrix4();
		xAxis = new Vector3(1.0, 0, 0);
		
		filter_velocity = new Vector3(); filter_velocity.Set(Vector3.Zero);
		filter_pos = new Vector3(); filter_pos.Set(Vector3.Zero);
		
		accLog = new ArrayList<Vector3>();
		velLog = new ArrayList<Vector3>();
		posLog = new ArrayList<Vector3>();
		
		mButterHp = new ButterWorth(ButterWorth.BandType.high);
		//create data set for velocity and pos
		mButterHp.createDataSet();
		mButterHp.createDataSet();
		
		instance = this;
	}
	
	void connect (String portName) throws Exception
	{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();	
                (new Thread(new SerialReader(in))).start();
                
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
	}
	
	class CloseThread extends Thread
	{
		public void run()
		{
			commPort.close();
		}
	}
	
	void disConnect()
	{
		 if(commPort != null)
		 {
			 try {
				commPort.getInputStream().close();
				new CloseThread().start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}
	
	public static class SerialReader implements Runnable 
	{
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                	//read single byte
                    for(int itrl = 0; itrl < len; itrl++ )
                    {
                    	String inputString = new String(buffer, itrl, 1);  //1 or 2?
                    	outputString += inputString;
                    	
                    	if(inputString.equals("\n"))
                    	{
                    		//System.out.print(outputString);
                    		//System.out.println(outputString.length());  //should equal to 73
                    		if(outputString.length() == 91 && outputString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                				if(outPutStringArr.length == 11)
                				{
                					//set acc
                					acc.Set(decodeFloat(outPutStringArr[3]) / 100.0, 
                							decodeFloat(outPutStringArr[4]) / 100.0, 
                							decodeFloat(outPutStringArr[5]) / 100.0);
                					
                					//set quat
                					Quaternion tempQuat = new Quaternion();
                					tempQuat.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat.Nor();
                					quat.Set(tempQuat);
                					
                					curStamp = System.currentTimeMillis() / 1000.0;
                					if(isFirstFrame)
                					{
                						prevStamp = curStamp;
                						isFirstFrame = false;
                					}else
                					{
                						stamp = curStamp - prevStamp;
                						//do the calculations
                    					getWorldPos(acc, quat, stamp);
                    					
                    					prevStamp = curStamp;
                					}
                						
                					
                				}
                    		}
                    		
                    		outputString = new String();
                    	}
                    }
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
	}
	
	//decode hex string to double
	static float decodeFloat(String inString)
	{
		byte[] inData = new byte[4];
		
		if(inString.length() == 8)
		{
			inData[0] = (byte) Integer.parseInt(inString.substring(0, 2), 16);
			inData[1] = (byte) Integer.parseInt(inString.substring(2, 4), 16);
			inData[2] = (byte) Integer.parseInt(inString.substring(4, 6), 16);
			inData[3] = (byte) Integer.parseInt(inString.substring(6, 8), 16);
		}
		
		int intbits = (inData[3] << 24) | ((inData[2] & 0xff) << 16) | ((inData[1] & 0xff) << 8) | (inData[0] & 0xff);
		return Float.intBitsToFloat(intbits);
	}
	
	static void getWorldPos(Vector3 ac, Quaternion qu, double duration)  //no filters at the moment, need to implement good filters, todo task
	{
		//add filters?
		
		/*
		mMatrix.Set(qu);
		linAcc.Set(ac);
		linAcc.Mul(mMatrix.inv());
		
		velocity.Add(linAcc.scl(stamp));
		//apply high pass filter
		filter_velocity.Set(mButterHp.applyButterWorth(1, 1, velocity));
		
		pos.Add(filter_velocity.scl(stamp));
		filter_pos.Set(mButterHp.applyButterWorth(2, 1, pos));
		*/
		
		//get around x
		double aroundXRad_Acc = qu.getAngleAroundRad(xAxis);
		double alongMovementAcc = ac.y * Math.cos(aroundXRad_Acc) + ac.z * Math.sin(aroundXRad_Acc);
		double orthoMovementAcc = -ac.y * Math.sin(aroundXRad_Acc) + ac.z * Math.cos(aroundXRad_Acc);
	
		linAcc.Set(0.0, alongMovementAcc, 0.0);
		velocity.Add(linAcc.scl(duration));
		filter_velocity.Set(mButterHp.applyButterWorth(1, 1, velocity));
		
		//System.out.println(filter_velocity.y);
		
		
		pos.Add(filter_velocity.scl(duration));
		filter_pos.Set(mButterHp.applyButterWorth(2, 1, pos));
		
		//heuristic: forwards gestures, that keeps moving away from (0,0,0)
		testMoveOutwards(filter_pos, 20);
		
		//log data
		Vector3 temp1 = new Vector3();
		temp1.Set(linAcc);
		accLog.add(temp1);
		Vector3 temp2 = new Vector3();
		temp2.Set(filter_velocity);
		velLog.add(temp2);
		Vector3 temp3 = new Vector3();
		temp3.Set(filter_pos);
		posLog.add(temp3);
		
		if(accLog.size() > logSize)
		{
			accLog.remove(0);
		}
		
		if(velLog.size() > logSize)
		{
			velLog.remove(0);
		}

		if(posLog.size() > logSize)
		{
			posLog.remove(0);
		}
	}
	
	static void testMoveOutwards(Vector3 position, int validFrameNumber)
	{
		double distanceToZero2 = position.len2();
		
		if(distanceToZero2 < prevDistance)
		{
			inwardsCount++;
			
			if(inwardsCount == validFrameNumber)
			{
				inwardsCount = 0;
				
				//print the value
				//System.out.println("distance: " + Math.sqrt(distanceToZero2));
			}
		}else
		{
			inwardsCount = 0;
		}
		
		prevDistance = distanceToZero2;
	}
	
	static void reset()
	{
		
	}
	
}

public class TrajectoryTracing extends PApplet{

	public SerialDataAcc mSerialDataAcc;
	
	boolean ready4Interpolate = false;
	
	public double widthSeg;
	public double heightSeg;
	public double heightThreshold;
	public int windowWidth;
	public int windowHeight;
	
	public void setup()
	{
		//initialize COM port, zigbee to be COM11
		mSerialDataAcc = new SerialDataAcc();
		try {
			mSerialDataAcc.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		windowWidth = 1500;
		windowHeight = 1200;  //split into two
		widthSeg = windowWidth / mSerialDataAcc.logSize;
		heightThreshold = 0.5;  //+ - 10
		heightSeg = (windowHeight/2) / (2 * heightThreshold);
		
		size(windowWidth, windowHeight, P3D);
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
		
		
		//size(1000, 1000);
		//background(250);
	}
	
	public void draw()
	{
		
		background(250);
		
		//coordinates
		/*
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
		}*/
		
		//cube
		{
			pushMatrix();				
			fill(200, 10, 50);
			lights();
			translate(width/2 - (float)mSerialDataAcc.filter_pos.y*1000.0f, height*3/5 -  (float)mSerialDataAcc.filter_pos.z*1000.0f  ,  (float)mSerialDataAcc.filter_pos.x*1000.0f);
			
			//translate(width/2 , height*3/5 , 0f);
			
			//apply rotation matrix
			
			applyMatrix((float) mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M00], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M01], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M02], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M03],
					(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M10], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M11], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M12], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M13],
					(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M20], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M21], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M22], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M23],
					(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M30], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M31], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M32], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M33]);
			
			
			noStroke();
			box(100);
			popMatrix();
		}
		
		
		//signal
		{
			pushMatrix();	
			ArrayList<Vector3> signalList = mSerialDataAcc.posLog;
			int acc1Size = signalList.size();  //since it's not in the same thread, size could be larger than the logsize
			if(acc1Size > mSerialDataAcc.logSize)
			{
				acc1Size = mSerialDataAcc.logSize;
			}
			
			if(acc1Size > 10)
			{
				for(int itra = 0; itra < (acc1Size-1); itra ++)
				{
					stroke(255, 0, 0);  //x
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra).x * heightSeg), 
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra + 1).x * heightSeg));
					
					stroke(0, 255, 0);  //y
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra).y *10 * heightSeg),
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra + 1).y *10 * heightSeg));
					
					stroke(0, 0, 255);  //z
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra).z * heightSeg), 
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + signalList.get(itra + 1).z * heightSeg));
				}
			}
			popMatrix();
		}
		
		/*
		background(250);
		pushMatrix();
		
		fill(100, 50, 50, 150);
		noStroke();
		ellipse(500 + (float)mSerialDataAcc.filter_pos.x*500.0f, 500 + (float)mSerialDataAcc.filter_pos.y*500.0f, 200, 200);
		
		popMatrix();
		*/
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialDataAcc.disConnect();
			exit();
		}
		
		if(key == 'r')
		{
			mSerialDataAcc.velocity.Set(Vector3.Zero);
			mSerialDataAcc.pos.Set(Vector3.Zero);
			mSerialDataAcc.filter_velocity.Set(Vector3.Zero);
			mSerialDataAcc.filter_pos.Set(Vector3.Zero);
			
		}
		
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.imuv4.TrajectoryTracing"});
	}
	
}
