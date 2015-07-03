/**
 * 1st step: directional swipe, show arrows
 * 2nd step: type words, use swipe to select, replace mouse actions
 * 3rd step: web browser, previous page and next page
 */

package com.teng.demos;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import com.teng.imuv4.PredictSVM;
import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class DataSerialKeyPad {
	CommPort commPort;
	private static String quatString = new String();
	public static Quaternion quat3;
	public static Vector3 acc1;
	public static Vector3 acc2;
	public static ArrayList<Vector3> dataset_acc1;
	public static ArrayList<Vector3> dataset_acc2;
	public static ArrayList<Quaternion> dataset_quat3;
	
	public static double typeValue = 0.0;
	public static boolean isRecording = false;
	public static boolean dataTrained = false;
	
	public static double predictionFingerSegment;
	public static ArrayList<Double> dataset_quat_predictions; 
	
	public static Vector3 pos;      //space pos
	public static Vector3 velocity;	//space speed
	public static Vector3 linAcc;	//space acc
	public static Matrix4 mMatrix;	//space rotation matrix
	public static double stamp = 0.0151;  //in seconds
	
	public static int sampleNum = 32;
	private static int movingWindowSize = 32; 
	private static int movingCount = 0;
	
	//svm function for event
	public static PredictSVM predictSVM;
	public static double predictValue;
	
	public static DataSerialKeyPad instance;
	public static DataSerialKeyPad getSharedInstance()
	{
		if(instance == null)
		{
			instance = new DataSerialKeyPad();
		}
		return instance;
	}
	
	public DataSerialKeyPad()
	{
		quat3 = new Quaternion();  //imu 1+2
		acc1 = new Vector3();
		acc2 = new Vector3();
		dataset_acc1 = new ArrayList<Vector3>();
		dataset_acc2 = new ArrayList<Vector3>();
		dataset_quat3 = new ArrayList<Quaternion>();
		pos = new Vector3(); pos.Set(Vector3.Zero);
		velocity = new Vector3(); velocity.Set(Vector3.Zero);
		linAcc = new Vector3(); linAcc.Set(Vector3.Zero);
		mMatrix = new Matrix4();
		
		predictSVM = new PredictSVM("C:\\Users\\Teng\\Desktop\\dataset\\626-keyboardonwrist\\rbf_model_pilot.model", "C:\\Users\\Teng\\Desktop\\dataset\\626-keyboardonwrist\\range");
		
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
                					
                					//save for test data sample
                    				dataset_acc1.add(new Vector3(acc1));
                    				dataset_acc2.add(new Vector3(acc2));
                    				Quaternion tempQuat3 = new Quaternion();
                    				tempQuat3.Set(quat3);
                    				dataset_quat3.add(tempQuat3);
                    				
                    				if(dataset_acc1.size() > sampleNum)
                    				{
                    					dataset_acc1.remove(0);
                    					dataset_acc2.remove(0);
                    					dataset_quat3.remove(0);
                    				
                    					movingCount++;
                    					
                    					if(movingCount == movingWindowSize)
                    					{
                    						//predict
                    						//System.out.println(" " + dataset_acc2.size());
                    						if(dataset_acc1.size() == sampleNum && dataset_acc2.size() == sampleNum)
                    						{	
                    							predictValue = predictSVM.predictWithDefaultModel(dataset_acc1);	
                    							
                    							//direction
                    							if(predictValue == 1){
                    								getTranslatePos(dataset_acc1, dataset_quat3);
                    								System.out.println("1 , " + pos.x + " ,  " + pos.y + " ,  " + pos.z);
                    								reset();	
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
	
	
	private static void getTranslatePos(ArrayList<Vector3> accList, ArrayList<Quaternion> quatList)
	{
		if(accList.size() != quatList.size())
		{
			pos.Set(Vector3.Zero);
		}else
		{
			int sz = accList.size();
			Vector3 ac = new Vector3();
			Quaternion qu = new Quaternion();
			for(int itra = 0; itra < sz; itra++)
			{
				ac.Set(accList.get(itra));
				qu.Set(quatList.get(itra));
				
				mMatrix.Set(qu);
				linAcc.Set(ac);
				linAcc.Mul(mMatrix.inv());
				
				velocity.Add(linAcc.scl(stamp));
				pos.Add(velocity.scl(stamp));
			}
		}
	}
	
	private static void reset()
	{
		velocity.Set(Vector3.Zero);
		pos.Set(Vector3.Zero);
	}
	
}

public class BrowserKeypad extends PApplet{

	public DataSerialKeyPad mSerialData;
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
		mSerialData = new DataSerialKeyPad();
		
		try {
			mSerialData.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	}
	
	public void draw()
	{
		background(250);
		
		//indications
		drawArrow(20, 20, 180, 180, 0, 8, true);
		
		
		if(mSerialData.predictValue > 0){
			pushMatrix();
			textSize(32);
			fill(0, 102, 153);
			
			text(" ...", 10, 120);

			popMatrix();
		}
		
	}
	
	//draw arrows
	//drawArrow(20, 20, 180, 180, 0, 4, true);
	void drawArrow(float x0, float y0, float x1, float y1, float beginHeadSize, float endHeadSize, boolean filled) 
	{
		PVector d = new PVector(x1 - x0, y1 - y0);
		d.normalize();
		  
		float coeff = 1.5f;
		  
		strokeCap(SQUARE);
		strokeWeight(6.0f);  
		line(x0+d.x*beginHeadSize*coeff/(filled?1.0f:1.75f), 
		        y0+d.y*beginHeadSize*coeff/(filled?1.0f:1.75f), 
		        x1-d.x*endHeadSize*coeff/(filled?1.0f:1.75f), 
		        y1-d.y*endHeadSize*coeff/(filled?1.0f:1.75f));
		  
		float angle = atan2(d.y, d.x);
		  
		if (filled) {
		    // begin head
		    pushMatrix();
		    translate(x0, y0);
		    rotate(angle+PI);
		    triangle(-beginHeadSize*coeff, -beginHeadSize, 
		             -beginHeadSize*coeff, beginHeadSize, 
		             0, 0);
		    popMatrix();
		    // end head
		    pushMatrix();
		    translate(x1, y1);
		    rotate(angle);
		    triangle(-endHeadSize*coeff, -endHeadSize, 
		             -endHeadSize*coeff, endHeadSize, 
		             0, 0);
		    popMatrix();
		} 
		else {
		    // begin head
		    pushMatrix();
		    translate(x0, y0);
		    rotate(angle+PI);
		    strokeCap(ROUND);
		    line(-beginHeadSize*coeff, -beginHeadSize, 0, 0);
		    line(-beginHeadSize*coeff, beginHeadSize, 0, 0);
		    popMatrix();
		    // end head
		    pushMatrix();
		    translate(x1, y1);
		    rotate(angle);
		    strokeCap(ROUND);
		    line(-endHeadSize*coeff, -endHeadSize, 0, 0);
		    line(-endHeadSize*coeff, endHeadSize, 0, 0);
		    popMatrix();
		}
	}
	
	//text input
	void drawTextBox()
	{
		
	}
	
	//response to keyboard input
	void drawText()
	{
		
	}
	
	//delete the whole line
	void deleteLine(){
		
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialData.disConnect();
			exit();
		}
	}
	
	public static final void main(String args[]){
		PApplet.main(new String[] {"--present", "com.teng.demos.BrowserKeypad"});
	}
	
}
