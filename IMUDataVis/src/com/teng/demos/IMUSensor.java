package com.teng.demos;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;

import com.teng.imuv4.PredictSVM;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class SensorSerial{
	CommPort commPort;
	private static String outputString = new String();
	
	public static Vector3 acc;
	public static Quaternion quat;
	public static ArrayList<Vector3> dataset_acc;
	public static ArrayList<Quaternion> dataset_quat;
	
	public static Vector3 xAxis;
	public static Vector3 yAxis;
	public static Vector3 zAxis;
	
	private static int sampleNumber = 64;
	private static int movingWindowSize = 32;  //decide frequency to examine the windowed data
	private static int movingCount = 0;
	
	public static ArrayList<Integer> tagIndex;
	
	public static int visLogSize = 100*5;  //5 secs
	public static ArrayList<Vector3> visAccLog;
	public static ArrayList<Vector3> angLog;
	
	public static PredictSVM predictSVM;
	public static double predictValue;
	public static double prevValue;
	
	public static SensorSerial instance;
	public static SensorSerial getSharedInstance()
	{
		if(instance == null)
		{
			instance = new SensorSerial();
		}
		
		return instance;
	}
	
	public SensorSerial()
	{
		acc = new Vector3();
		quat = new Quaternion();
		dataset_acc = new ArrayList<Vector3>();
		dataset_quat = new ArrayList<Quaternion>();
		
		visAccLog = new ArrayList<Vector3>();
		angLog = new ArrayList<Vector3>();
		tagIndex = new ArrayList<Integer>();
		
		xAxis = new Vector3(1.0, 0.0, 0.0);
		yAxis = new Vector3(0.0, 1.0, 0.0);
		zAxis = new Vector3(0.0, 0.0, 1.0);
		
		predictSVM = new PredictSVM("C:\\Users\\Teng\\Desktop\\dataset\\911demos\\raw\\wrist\\features\\rbf_model_pilot.model", "C:\\Users\\Teng\\Desktop\\dataset\\911demos\\raw\\wrist\\features\\range");
	
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
                    		//System.out.println(quatString.length());
                    		if(outputString.length() == 73 && outputString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                    			
                    			if(outPutStringArr.length == 9)
                				{
                    				acc.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				Vector3 tempAcc = new Vector3();
                    				tempAcc.Set(acc);
                    				visAccLog.add(tempAcc);
                    				
                    				//control the size
                    				if(visAccLog.size() > visLogSize)
                    				{
                    					visAccLog.remove(0);
                    				}
                    				
                    				Quaternion tempQuat = new Quaternion();                					
                					tempQuat.Set(decodeFloat(outPutStringArr[4]),  	//x 
                							decodeFloat(outPutStringArr[5]),    	//y
                							decodeFloat(outPutStringArr[6]), 		//z
                							decodeFloat(outPutStringArr[3]));		//w
                					
                					tempQuat.Nor();
                					quat.Set(tempQuat);
                					
            						double aroundXRad = tempQuat.getAngleAroundRad(xAxis);
            						double aroundYRad = tempQuat.getAngleAroundRad(yAxis);
            						double aroundZRad = tempQuat.getAngleAroundRad(zAxis);
                    				
            						double fixedX = tempAcc.x * Math.cos(aroundYRad) + tempAcc.z * Math.sin(aroundYRad);
            						double fixedZ = -tempAcc.x * Math.sin(aroundYRad) + tempAcc.z  * Math.cos(aroundYRad);
            						double fixedY = tempAcc.y;
            						
            						dataset_acc.add(new Vector3(fixedX, fixedY, fixedZ));
            						angLog.add(new Vector3(aroundXRad, aroundYRad, aroundZRad));
            						
            						if(angLog.size() > sampleNumber)
                    				{
            							angLog.remove(0);
            							dataset_acc.remove(0);
            							
            							movingCount++;
            							if(movingCount == movingWindowSize)
            							{
            								predictValue = predictSVM.predictSwipeChoppingBoard(dataset_acc, angLog);
            								
            								System.out.println(" " + predictValue);
            								
            								if(predictValue != prevValue && prevValue == 1.0)
            								{
            									//event trigger
            									tagIndex.add(0);
            								}
            								
            								prevValue = predictValue;
            								movingCount = 0;
            							}
            							
                    				}
            						
            						//tag index
            						for(int itrt = 0; itrt < tagIndex.size(); itrt++)
            						{
            							int tempIndex = tagIndex.get(itrt);
            							
            							if((tempIndex + 1) == visLogSize)
            							{
            								tagIndex.remove(itrt);
            							}else
            							{
            								tagIndex.set(itrt, tempIndex+1);
            							}
            							
            							
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
	private static float decodeFloat(String inString)
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
}



//drawings
public class IMUSensor extends PApplet{
	
	//acc receive
	public SensorSerial mSerial;
	
	//bt sender
	//DataTransmitterBT bt;
	
	
	String command1 = String.valueOf(1) + "," + String.valueOf(1) + "\n";
	String command2 = String.valueOf(2) + "," + String.valueOf(2) + "\n";
	String command3 = String.valueOf(3) + "," + String.valueOf(3) + "\n";
	String command4 = String.valueOf(4) + "," + String.valueOf(4) + "\n";
	
	
	public double widthSeg;
	public double heightSeg;
	public double heightThreshold;
	public int windowWidth;
	public int windowHeight;
	
	public void setup()
	{
		mSerial = new SensorSerial();
		try {
			mSerial.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//bt = new DataTransmitterBT();
		//try {
		//	bt.connect("COM3");
		//} catch (Exception e) {
			 //TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		windowWidth = 1500;
		windowHeight = 1200;  //split into two
		widthSeg = windowWidth / mSerial.visLogSize;
		heightThreshold = 10;  //+ - 10
		heightSeg = windowHeight / (2 * heightThreshold);
		
		size(windowWidth, windowHeight);
		background(250);
	}
	
	public void draw()
	{
		background(250);
		
		int accSize = mSerial.visAccLog.size();  //since it's not in the same thread, size could be larger than the logsize
		if(accSize > mSerial.visLogSize)
		{
			accSize = mSerial.visLogSize;
		}
		
		//draw tag background
		{
			pushMatrix();
			noStroke();
			fill(230);
			
			for(int itrt = 0; itrt<mSerial.tagIndex.size(); itrt++)
			{
				
				float leftX = (float)(windowWidth - (mSerial.tagIndex.get(itrt) + 64) * widthSeg);
				float leftY = 0.0f;
				float rectWidth = (float)(64.0 * widthSeg);
				float rectHeight = windowHeight;
				rect(leftX, leftY, rectWidth, rectHeight);
			}
			
			popMatrix();
		}
		
		
		//draw visAccLog
		{
			pushMatrix();
			
			
			if(accSize > 10)
			{
				for(int itra = 0; itra < (accSize-1); itra ++)
				{
					stroke(255, 0, 0);  //x
					line((float)(windowWidth - accSize * widthSeg + itra * widthSeg), 
							(float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra).x * heightSeg),
							 (float)(windowWidth - accSize * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra + 1).x * heightSeg));
					
					stroke(0, 255, 0);  //y
					line((float)(windowWidth - accSize * widthSeg + itra * widthSeg), 
							(float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra).y * heightSeg),
							 (float)(windowWidth - accSize * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra + 1).y * heightSeg));
					
					stroke(0, 0, 255);  //z
					line((float)(windowWidth - accSize * widthSeg + itra * widthSeg), 
							(float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra).z * heightSeg),
							 (float)(windowWidth - accSize * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight) - (float)(windowHeight * 1 / 2 + mSerial.visAccLog.get(itra + 1).z * heightSeg));
				}
			}
			
			popMatrix();
		}
	}
	
	
	public void keyPressed()
	{
		if(key == 'q'){
			mSerial.disConnect();
			//bt.disConnect();
			exit();
		}
		
		if(key == 'n'){
			//bt.sendData(command1);
		}
		
		if(key == 't'){
			mSerial.tagIndex.add(0);
		}
	}
	
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.demos.IMUSensor"});
		
		/*
		DataTransmitterBT bt = new DataTransmitterBT();
		try {
			bt.connect("COM3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String outputSample = String.valueOf(1) + "," + String.valueOf(1) + "\n";
		System.out.println("test");
		
		Scanner input = new Scanner(System.in);
		String order;
		
		while(input.hasNext())
		{
			order = input.next();
			System.out.println(order);
			
			char[] d = order.toCharArray();
			if(d[0] == 'q')
			{
				bt.disConnect();
				System.exit(0);
			}else if(d[0] == 'n')
			{
				bt.sendData(outputSample);
			}
			
			
		}*/
		
	}
}
