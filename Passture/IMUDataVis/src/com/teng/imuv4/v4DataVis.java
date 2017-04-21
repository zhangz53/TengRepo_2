package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;

import com.teng.imuv4.v4DataRecorderDouble.CloseThread;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

//works with two IMUs
//for tap on segments
//class to grab data from serial port
class SerialData {
	CommPort commPort;
	private static String quatString = new String();
	public static Quaternion quat1;
	public static Quaternion quat2;
	public static Quaternion quat3;
	public static double typeValue = 0.0;
	public static boolean isRecording = false;
	public static boolean dataTrained = false;
	
	//knn sample
	public static ArrayList<Quaternion> kNNSamples;  
	public static int sampleCount = 0;
	public static int predictionFingerSegment;
	
	public static DataStorage dataStorage;
	public static int sampleLabel = 0;
	
	public static int rType = 1;   //0- record one data per time  1- keep recording
	
	public static SerialData instance;
	public static SerialData getSharedInstance()
	{
		if(instance == null)
		{
			instance = new SerialData();
		}
		return instance;
	}
	
	public SerialData()
	{
		super();
		quat1 = new Quaternion();  //imu 1
		quat2 = new Quaternion();  //imu 2
		quat3 = new Quaternion();  //imu 1+2
		dataStorage = DataStorage.getInstance();
		kNNSamples = new ArrayList<Quaternion>();
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
                    	quatString += inputString;
                    	
                    	if(inputString.equals("\n"))
                    	{
                    		//System.out.print(quatString);
                    		//System.out.println(quatString.length());  //should equal to 37
                    		if(quatString.length() == 91 && quatString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = quatString.split(",");
                				if(outPutStringArr.length == 11)
                				{
                					Quaternion tempQuat = new Quaternion();
                					
                					/*
                					tempQuat.Set(decodeFloat(outPutStringArr[1]),  	//x 
                							decodeFloat(outPutStringArr[2]),    	//y
                							decodeFloat(outPutStringArr[3]), 		//z
                							decodeFloat(outPutStringArr[0]));		//w
                					
                					tempQuat.Nor();
                					quat1.Set(tempQuat);
                					
                					tempQuat.Set(decodeFloat(outPutStringArr[5]),  	//x 
                							decodeFloat(outPutStringArr[6]),    	//y
                							decodeFloat(outPutStringArr[7]), 		//z
                							decodeFloat(outPutStringArr[4]));		//w
                					
                					tempQuat.Nor();
                					quat2.Set(tempQuat);
                					
                					*/
                					tempQuat.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat.Nor();
                					quat3.Set(tempQuat);
                					
                					if(isRecording && dataTrained == false)
                					{               						
                						/*
                						DataStorage.AddSampleF(typeValue, 
                								quat1.w, quat1.x, quat1.y, quat1.z, 
                								quat2.w, quat2.x, quat2.y, quat2.z, 
                								quat3.w, quat3.x, quat3.y, quat3.z);
                						*/
                						//record each type with one sample for 1NN
                						/*
                						if(typeValue > kNNSamples.size())
                						{
                							kNNSamples.add(new Quaternion(quat3));
                							
                							//needed for data recording
                							
                							if(kNNSamples.size() == 14)
                							{
                								//save the parameters to a file
                								for(Quaternion quat : kNNSamples)
                								{
                									sampleLabel++;
                									if(sampleLabel == 15)
                									{
                										sampleLabel = 1;
                									}
                									DataStorage.AddSampleF(sampleLabel, quat.x, quat.y, quat.z, quat.w, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                								}
                								
                								//dataStorage.savef();
                								
                							}
                						}*/
                						
                						if(rType == 0)
                						{
                							if(typeValue > sampleCount)
                    						{
                    							Quaternion quat = new Quaternion(quat3);
                    							sampleLabel++;
            									if(sampleLabel == 15)
            									{
            										sampleLabel = 1;
            									}
                    							DataStorage.AddSampleF(sampleLabel, quat.x, quat.y, quat.z, quat.w, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                    							sampleCount++;
                    						}
                						}else if(rType == 1)
                						{
                							Quaternion quat = new Quaternion(quat3);
                							DataStorage.AddSampleF(sampleLabel, quat.x, quat.y, quat.z, quat.w, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                						}
                						
                						
                					}else if(dataTrained)
                					{
                						//start recognition
                						if(kNNSamples.size() == 14)
                						{
                							predictionFingerSegment = predictFingerSeg(quat3, kNNSamples);
                						}
                						
                					}
                				}
                    			
                    		}
                    		
                    		quatString = new String();
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
	
	private static int predictFingerSeg(Quaternion test, ArrayList<Quaternion> samples)
	{
		int predictResult = 0;
		double dis = 1.0;
		for(int itrs = 0; itrs<samples.size(); itrs++)
		{
			double tempDis = 1 - test.dot(samples.get(itrs));
			if(tempDis < dis)
			{
				dis = tempDis;
				predictResult = itrs + 1;
			}
		}
		
		//need to set the threshold
		return predictResult;
		
	}
	
}


public class v4DataVis extends PApplet{

	public SerialData mSerialData;
	
	Quaternion firstQuat = new Quaternion();
	Vector3 firstEuler = new Vector3();
	Quaternion secondQuat  = new Quaternion();
	Vector3 secondEuler = new Vector3();
	Quaternion thirdQuat  = new Quaternion();
	Vector3 thirdEuler = new Vector3();
	
	Vector3 firstVec = new Vector3(0, 0, 0);
	Vector3 secondVec = new Vector3(100, 0, 0);
	Vector3 curEuler = new Vector3();
	
	private ArrayList<PImage> imgs;
	private int imgIndex = 0;
	
	boolean ready4Interpolate = false;
	
	public void setup()
	{
		//initialize COM port, zigbee to be COM11
		mSerialData = new SerialData();
		try {
			mSerialData.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		size(1000, 1000, P3D);
		background(250);
		camera(500.0f, 200.0f, 1000.0f, 500.0f, 500.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		
		imgs = new ArrayList<PImage>();
		PImage img0 = loadImage("handmodel.png");
		PImage img1 = loadImage("indexone.png");
		PImage img2 = loadImage("indextwo.png");
		PImage img3 = loadImage("indexthree.png");
		PImage img4 = loadImage("middleone.png");
		PImage img5 = loadImage("middletwo.png");
		PImage img6 = loadImage("middlethree.png");
		PImage img7 = loadImage("ringone.png");
		PImage img8 = loadImage("ringtwo.png");
		PImage img9 = loadImage("ringthree.png");
		PImage img10 = loadImage("littleone.png");
		PImage img11 = loadImage("littletwo.png");
		PImage img12 = loadImage("littlethree.png");
		PImage img13 = loadImage("thumbone.png");
		PImage img14 = loadImage("thumbtwo.png");
		imgs.add(img0); 
		imgs.add(img1); imgs.add(img2); imgs.add(img3);
		imgs.add(img4); imgs.add(img5); imgs.add(img6);
		imgs.add(img7); imgs.add(img8); imgs.add(img9);
		imgs.add(img13); imgs.add(img14);  //minor changes here
		imgs.add(img10); imgs.add(img11); imgs.add(img12);
		
		
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
		}
		*/
		//draw hand/fingers
		{
			pushMatrix();				
			lights();
			noStroke();
			noFill();
			translate(width/2, height*3/5,  0);
			
			beginShape();
			PImage curImage;
			
			if(mSerialData.dataTrained)
			{
				curImage = imgs.get(mSerialData.predictionFingerSegment);
			}else
			{
				if(mSerialData.isRecording)
				{
					curImage = imgs.get(imgIndex);
				}else
				{
					curImage = imgs.get(0);
				}
			}
			
			texture(curImage);
			vertex(-298, -438, 50, 0, 0);
			vertex(298, -438, 50, curImage.width, 0);
			vertex(298, 0, 50, curImage.width, curImage.height);
			vertex(-298, 0, 50, 0, curImage.height);
			endShape();
			
			popMatrix();
		}
		
		
		//draw status
		{
			pushMatrix();
			textSize(32);
			fill(0, 102, 153);
			
			if(mSerialData.dataTrained)
			{
				text("Predicting ...", 10, 120);
			}else
			{
				if(mSerialData.isRecording)
				{
					text("Recording ...", 10, 120);
				}else
				{
					text("Not Recording", 10, 120);
				}
			}
			
			popMatrix();
		}
		
		//
		if(ready4Interpolate)
		{
			/*
			curEuler.Set(mSerialData.quat.getPitchRad(), mSerialData.quat.getYawRad(), mSerialData.quat.getRollRad());
			double xInter = (curEuler.x - firstEuler.x) / (secondEuler.x - firstEuler.x);
			double yInter = (curEuler.y - firstEuler.y) / (secondEuler.y - firstEuler.y);
			double zInter = (curEuler.z - firstEuler.z) / (secondEuler.z - firstEuler.z);
			
			double firstInter = Math.sqrt(xInter * xInter + yInter * yInter + zInter * zInter )/3;
			
			//xInter = (curEuler.x - secondEuler.x) / (thirdEuler.x - secondEuler.x);
			//yInter = (curEuler.y - secondEuler.y) / (thirdEuler.y - secondEuler.y);
			//zInter = (curEuler.z - secondEuler.z) / (thirdEuler.z - secondEuler.z);
			
			//double secondInter = Math.sqrt(xInter * xInter + yInter * yInter + zInter * zInter )/3;
			
			pushMatrix();
			fill(100, 50, 75);
			lights();
			translate(width/2  + 300.0f * (float)firstInter , height*3/5 , 0);  //- 100.f * (float)secondInter
			noStroke();
			sphere(30); 
			popMatrix();
			*/
			
			Quaternion tempQuat =   (firstQuat.conjugate()).mul(mSerialData.quat1) ;
			
			Vector3 tempVec = new Vector3();
			tempVec.Set(tempQuat.getPitchRad(), tempQuat.getYawRad(), tempQuat.getRollRad());
			//xInter = (curEuler.x - secondEuler.x) / (thirdEuler.x - secondEuler.x);
			//yInter = (curEuler.y - secondEuler.y) / (thirdEuler.y - secondEuler.y);
			//zInter = (curEuler.z - secondEuler.z) / (thirdEuler.z - secondEuler.z);
			
			//double secondInter = Math.sqrt(xInter * xInter + yInter * yInter + zInter * zInter )/3;
			
			pushMatrix();
			fill(100, 50, 75);
			lights();
			translate(width/2  + 300.0f * (float)tempVec.x, height*3/5 - 300.0f * (float)tempVec.z, 300.f * (float)tempVec.y);  //- 100.f * (float)secondInter
			noStroke();
			sphere(30); 
			popMatrix();
		}

		
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialData.disConnect();
			mSerialData.dataStorage.savef();
			exit();
		}
		
		if(key == 'd') {  //d for data record
			if(mSerialData.dataTrained == false){
				if(mSerialData.isRecording == false){
					
					if(mSerialData.rType == 0){
						mSerialData.typeValue++;
						imgIndex++;
						if(imgIndex == 15)
							imgIndex = 1;
						
						mSerialData.isRecording = true;
					}else if(mSerialData.rType == 1)
					{
						imgIndex++;
						if(imgIndex == 12)  // set the range 1-11, exclude the little finger
							imgIndex = 1;
						
						mSerialData.sampleLabel++;
						if(mSerialData.sampleLabel == 12)
						{
							mSerialData.sampleLabel = 1;
						}
						mSerialData.isRecording = true;
					}
				}else
				{
					mSerialData.isRecording = false;
					
					//if(imgIndex == 14)
					//{
						////mSerialData.dataStorage.savef();
						//mSerialData.dataTrained = true;	
					//}
				}	
			}
			
		}
		
		
		/*
		if(key == 'f'){
			firstQuat.Set(mSerialData.quat);
			firstEuler.Set(firstQuat.getPitchRad(), firstQuat.getYawRad(), firstQuat.getRollRad());
			ready4Interpolate = true;
		}
		
		if(key == 's'){
			secondQuat.Set(mSerialData.quat);
			secondEuler.Set(secondQuat.getPitchRad(), secondQuat.getYawRad(), secondQuat.getRollRad());
			
		}
		
		if(key == 't'){
			thirdQuat.Set(mSerialData.quat);
			thirdEuler.Set(thirdQuat.getPitchRad(), thirdQuat.getYawRad(), thirdQuat.getRollRad());
			
		}
		*/
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.imuv4.v4DataVis"});
	}
	
}
