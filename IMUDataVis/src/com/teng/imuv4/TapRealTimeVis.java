package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PImage;

import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class kNNSample{
	
	public int label;
	public Quaternion quat;
	
	public kNNSample(int _label, Quaternion _quat)
	{
		label = _label;
		quat = new Quaternion(_quat.x, _quat.y, _quat.z, _quat.w);
	}
}

class DataSerial {
	CommPort commPort;
	private static String quatString = new String();
	public static Quaternion quat3;
	public static Vector3 acc1;
	public static Vector3 acc2;
	public static ArrayList<Vector3> dataset_acc1;
	public static ArrayList<Vector3> dataset_acc2;
	public static double typeValue = 0.0;
	public static boolean isRecording = false;
	public static boolean dataTrained = false;
	
	//knn sample
	public static ArrayList<kNNSample> kNNSamples;  
	public static int predictionFingerSegment;
	public static int paramK = 10;
	public static ArrayList<Integer> dataset_quat_predictions; 
	
	public static int sampleNum = 30;  //has to be mapped with the number chosen in PreProcessing
	private static int movingWindowSize = 30;  //decide frequency to examine the windowed data
	private static int movingCount = 0;
	
	public static int gestureState = 0;
	public static boolean stateUpdating = false;
	//svm function
	public static PredictSVM predictSVM;
	
	public static DataSerial instance;
	public static DataSerial getSharedInstance()
	{
		if(instance == null)
		{
			instance = new DataSerial();
		}
		return instance;
	}
	
	public DataSerial()
	{
		quat3 = new Quaternion();  //imu 1+2
		acc1 = new Vector3();
		acc2 = new Vector3();
		dataset_acc1 = new ArrayList<Vector3>();
		dataset_acc2 = new ArrayList<Vector3>();
		
		kNNSamples = new ArrayList<kNNSample>();
		dataset_quat_predictions = new ArrayList<Integer>();
		
		//load knnsamples
		loadkNNSamples(kNNSamples);
		predictSVM = new PredictSVM();
		
		instance = this;
	}
	
	private void loadkNNSamples(ArrayList<kNNSample> list)
	{
		String line = "";
		String splitBy = ",";
		BufferedReader br = null;
		String dataFile = "C:\\Users\\Teng\\Desktop\\dataset\\526\\knnsamples.csv";
		try {
			br = new BufferedReader(new FileReader(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				
				if(values.length == 13)
				{
					int kNNLabel = Integer.parseInt(values[0]);
					Quaternion kNNQuat = new Quaternion(Double.parseDouble(values[1]), Double.parseDouble(values[2]),Double.parseDouble(values[3]),Double.parseDouble(values[4]));  //x y z w
					list.add(new kNNSample(kNNLabel, kNNQuat));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
                    		//System.out.println(quatString.length());
                    		if(quatString.length() == 91 && quatString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = quatString.split(",");
                    			
                    			if(outPutStringArr.length == 11)
                				{
                    				acc1.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				acc2.Set(decodeFloat(outPutStringArr[3])/100.0,
                    						decodeFloat(outPutStringArr[4])/100.0, 
                    						decodeFloat(outPutStringArr[5])/100.0);
                					
                					Quaternion tempQuat = new Quaternion();                					
                					tempQuat.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat.Nor();
                					quat3.Set(tempQuat);
                					
                					//start recognition
                					if(kNNSamples.size() == 140)  //current 10 times
                					{
                						predictionFingerSegment = predictFingerSeg(quat3, kNNSamples, paramK);
                						System.out.println("seg: " + predictionFingerSegment);
                					}	
                					
                					if(stateUpdating)
                					{
                						gestureState = predictionFingerSegment;
                					}
                					
                					//save for test data sample
                    				dataset_acc1.add(new Vector3(acc1));
                    				dataset_acc2.add(new Vector3(acc2));
                    				dataset_quat_predictions.add(predictionFingerSegment);
                    				
                    				if(dataset_acc1.size() > sampleNum)
                    				{
                    					dataset_acc1.remove(0);
                    					dataset_acc2.remove(0);
                    					dataset_quat_predictions.remove(0);
                    					
                    					movingCount++;
                    					
                    					if(movingCount == movingWindowSize)
                    					{
                    						//predict
                    						if(dataset_acc1.size() == sampleNum && dataset_acc2.size() == sampleNum && dataset_quat_predictions.size() == sampleNum)
                    						{
                    							double predictValue = predictSVM.predictWithDefaultModel(dataset_acc1, dataset_acc2);
                    							//System.out.println("predict: " + predictValue);  //-1 or 1
                    							//heuristics here
                    							int fingerSegZeros = Collections.frequency(dataset_quat_predictions, 0);

                    							//heuristic 1: if no tap detected, then no
                    							if(fingerSegZeros == sampleNum)
                								{
                									//false positive
                									stateUpdating = false;
                									gestureState = 0;
                								}
                    							
                								//some tap detected
                    							if(predictValue == 1){
            										//three situations
            										//1st, bump and stay
                    								if(fingerSegZeros != sampleNum){
                    									if(dataset_quat_predictions.get(sampleNum -1) > 0  //this is inaccurate
                												&& dataset_quat_predictions.get(sampleNum -2) > 0)
                										{
                											stateUpdating = true;
                										}else//2nd, bump and flip
                										{
                											 
                											stateUpdating = false;
                											gestureState = getElementHighFrequency(dataset_quat_predictions, 0); //frequency, exlude 0
                										}
                    								}
                    								
            										//3rd, false positives
            										
            									}else  //-1  no bump
            									{
            										//three situations
            										//1st, no action at all
            										
            										
            										//2nd, already bumped and swiping
            										
            										
            										//3rd, just close but no bump
            										
            										
            									}
                								       							
                    							                    							
                    							movingCount = 0;
                    						}
                    						
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
	
	private static int predictFingerSeg(Quaternion test, ArrayList<kNNSample> samples, int k)
	{
		int predictResult = 0;
		double dis_threshold = 0.01;  // here to set the threshold
		ArrayList<Integer> topTenMinLabel = new ArrayList<Integer>();
		ArrayList<Double> topTenMinValues = new ArrayList<Double>();
		for(int itrt = 0; itrt < k; itrt++)
		{
			topTenMinLabel.add(0);
			topTenMinValues.add(dis_threshold);
		}
		
		for(kNNSample sample : samples)
		{
			double tempDis = 1.0 - test.dot(sample.quat);
			
			for(int itr = 0; itr < k; itr++)
			{
				if(tempDis < topTenMinValues.get(itr))
				{
					//push back the rest
					topTenMinValues.add(itr, tempDis);
					int szv = topTenMinValues.size();
					topTenMinValues.remove(szv - 1);
					topTenMinLabel.add(itr, sample.label);
					int szl = topTenMinLabel.size();
					topTenMinLabel.remove(szl - 1);
					//out of the for loop
					break;
				}
			}
			
		}
		
		//System.out.println(" " + topTenMinValues.get(0) + ", " + topTenMinValues.get(1) + ", " + topTenMinValues.get(2) + ", " + topTenMinValues.get(3) + ", " + topTenMinValues.get(4) + ", " + topTenMinValues.get(5) + ", " + topTenMinValues.get(6) + ", " + topTenMinValues.get(7) + ", " + topTenMinValues.get(8) + ", " + topTenMinValues.get(9));
		
		//get the labels with highest frequency
		/*
		int maxoccur = 0;
		for(int itrl = 0; itrl < 15; itrl++)
		{
			int occur = Collections.frequency(topTenMinLabel, itrl);
			
			if(occur > maxoccur)
			{
				predictResult = itrl;
				maxoccur = occur;
			}
		}*/
		
		predictResult = getElementHighFrequency(topTenMinLabel);
		
		//need to set the threshold
		return predictResult;
		
	}
	
	private static int getElementHighFrequency(ArrayList<Integer> list)
	{
		int maxoccur = 0;
		int result = 0;
		for(int itrl = 0; itrl < 15; itrl++)
		{
			int occur = Collections.frequency(list, itrl);
			
			if(occur > maxoccur)
			{
				result = itrl;
				maxoccur = occur;
			}
		}
		
		return result;
	}
	
	private static int getElementHighFrequency(ArrayList<Integer> list, int exludeNum)
	{
		int maxoccur = 0;
		int result = 0;
		for(int itrl = 1; itrl < 15; itrl++)
		{
			int occur = Collections.frequency(list, itrl);
			
			if(occur > maxoccur)
			{
				result = itrl;
				maxoccur = occur;
			}
		}
		
		return result;
	}
}

public class TapRealTimeVis extends PApplet{
	
	public DataSerial mSerialData;
	
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
		mSerialData = new DataSerial();
		
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
		imgs.add(img10); imgs.add(img11); imgs.add(img12);
		imgs.add(img13); imgs.add(img14);
		
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
		
		//hand pics
		{
			pushMatrix();				
			lights();
			noStroke();
			noFill();
			translate(width/2, height*3/5,  0);
			
			beginShape();
			PImage curImage;
			curImage = imgs.get(mSerialData.gestureState);
			
			texture(curImage);
			vertex(-298, -438, 50, 0, 0);
			vertex(298, -438, 50, curImage.width, 0);
			vertex(298, 0, 50, curImage.width, curImage.height);
			vertex(-298, 0, 50, 0, curImage.height);
			endShape();
			
			popMatrix();
		}
		
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialData.disConnect();
			exit();
		}
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.imuv4.TapRealTimeVis"});
	}
}
