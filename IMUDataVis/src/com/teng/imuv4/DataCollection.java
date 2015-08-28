package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import processing.core.PApplet;

import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

class SerialDataCap{
	private static CommPort commPort;
	private static String outputString = new String();
	public static Quaternion quat1;
	public static Quaternion quat2;
	public static Quaternion quat3;
	public static Vector3 acc1;
	public static Vector3 acc2;
	public static Vector3 xAxis;
	
	
	public static Vector3 geoAcc;
	public static double stamp;
	public static double typeValue = 1.0;
	
	//data log
	public static DataStorage dataStorage;
	public static double sampleCount = 0.0;
	public static boolean isRecording = false;
	
	//get rid of DC shift
	public static ArrayList<Vector3> firstTenAcc1;
	public static ArrayList<Vector3> firstTenAcc2;
	public static boolean isFirstTen = false;
	public static Vector3 baseAcc1;
	public static Vector3 baseAcc2;
	
	public SerialDataCap()
	{
		acc1 = new Vector3();
		acc2 = new Vector3();
		quat3 = new Quaternion();
		quat1 = new Quaternion();
		quat2 = new Quaternion();
		xAxis = new Vector3(1.0, 0, 0);
		
		firstTenAcc1 = new ArrayList<Vector3>();
		firstTenAcc2 = new ArrayList<Vector3>();
		baseAcc1 = new Vector3();
		baseAcc2 = new Vector3();
		
		dataStorage = DataStorage.getInstance();
		
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
                
                InputStream mInputStream = serialPort.getInputStream();	
                (new Thread(new SerialReader(mInputStream))).start();
                
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
				//save data
				dataStorage.savef();
				
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
                while ( ( len = this.in.read(buffer)) > -1)
                {
                	//read single byte
                    for(int itrl = 0; itrl < len; itrl++ )
                    {
                    	String inputString = new String(buffer, itrl, 1);  //1 or 2?
                    	outputString += inputString;
                    	
                    	if(inputString.equals("\n"))
                    	{
                    		//System.out.print(outputString);
                    		//System.out.println(outputString.length());  // for quaternions should equal to 109, for acc should equal to 55
                    		if(outputString.length() == 100 && outputString != null)  
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                    			
                    			//this is for accelerometers
                    			if(outPutStringArr.length == 12)
                    			{
                    				acc1.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				acc2.Set(decodeFloat(outPutStringArr[3])/100.0,
                    						decodeFloat(outPutStringArr[4])/100.0, 
                    						decodeFloat(outPutStringArr[5])/100.0);
                    				
                    				/*
                    				Quaternion tempQuat = new Quaternion();                					
                					tempQuat.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat.Nor();
                					quat1.Set(tempQuat);
                					
                					Quaternion tempQuat2 = new Quaternion();                					
                					tempQuat2.Set(decodeFloat(outPutStringArr[11]),  	//x 
                							decodeFloat(outPutStringArr[12]),    	//y
                							decodeFloat(outPutStringArr[13]), 		//z
                							decodeFloat(outPutStringArr[10]));		//w
                					
                					tempQuat2.Nor();
                					quat2.Set(tempQuat2);
                					*/
                    				Quaternion tempQuat2 = new Quaternion();                					
                					tempQuat2.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat2.Nor();
                					quat2.Set(tempQuat2);
                    				
                					stamp = decodeFloat(outPutStringArr[10]) / 1000.0; 
                					
                    				
                    				if(isFirstTen)
                    				{
                    					Vector3 tempAcc1 = new Vector3();
                    					Vector3 tempAcc2 = new Vector3();
                    					tempAcc1.Set(acc1);
                    					tempAcc2.Set(acc2);
                    					
                    					firstTenAcc1.add(tempAcc1);
                    					firstTenAcc2.add(tempAcc2);
                    					
                    					if(firstTenAcc1.size() == 66){
                    						isFirstTen = false;
                    						
                    						//calculate the average
                    						double[] meansAcc1 = means(firstTenAcc1);
                    						double[] meansAcc2 = means(firstTenAcc2);
                    						
                    						baseAcc1.Set(meansAcc1[0], meansAcc1[1], meansAcc1[2]);
                    						baseAcc2.Set(meansAcc2[0], meansAcc2[1], meansAcc2[2]);
                    					}
                    				}else{
                    					//record
                        				if(isRecording)
                        				{
                        					//deduce by base
                        					//acc1.Sub(baseAcc1);
                        					//acc2.Sub(baseAcc2);
                        					//double tamp = System.currentTimeMillis();
                        					
                        					//do the first feature selection here
                        					//for acc2/ring
                        					//double aroundXRad_Acc2 = quat2.getAngleAroundRad(xAxis);
                        					//double alongMovementAcc2 = acc2.y * Math.cos(aroundXRad_Acc2) + acc2.z * Math.sin(aroundXRad_Acc2);
                        					//double orthoMovementAcc2 = -acc2.y * Math.sin(aroundXRad_Acc2) + acc2.z * Math.cos(aroundXRad_Acc2);
                        					
                        					
                        					
                        					//for acc1/watch
                        					//double aroundXRad_Acc1 = quat1.getAngleAroundRad(xAxis);
                        					//double orthoMovementAcc1 = -acc1.y * Math.sin(aroundXRad_Acc1) + acc1.z * Math.cos(aroundXRad_Acc1);
                        					
                        					
                        					DataStorage.AddSampleF(sampleCount, acc1.x, acc1.y, acc1.z, acc2.x, acc2.y, acc2.z,
                            						 quat2.x, quat2.y, quat2.z, quat2.w, stamp, 0.0);
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
	
	static double[] means(ArrayList<Vector3> list)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int sz = list.size();
		for(Vector3 acc : list)
		{
			axeValues[0] += acc.x;
			axeValues[1] += acc.y;
			axeValues[2] += acc.z;
		}
		
		axeValues[0] = axeValues[0] / sz;
		axeValues[1] = axeValues[1] / sz;
		axeValues[2] = axeValues[2] / sz;
		
		return axeValues;
	}
	
	public ArrayList<Vector3> getAroundAxisAcc(ArrayList<Vector3> accList, ArrayList<Quaternion> quatList, Vector3 axis)
	{
		ArrayList<Vector3> resultAccList = new ArrayList<Vector3>();
		
		if(accList.size() != quatList.size())
		{
			return resultAccList;
		}else
		{
			int sz = accList.size();
			for(int itra = 0; itra < sz; itra++)
			{
				//
				double aroundAxisRadius = quatList.get(itra).getAngleAround(axis);
				double alongMovementAcc = 0;
				double orthoMovementAcc = 0;
				
				if(axis.x == 1.0 && axis.y == 0 && axis.z == 0){
					alongMovementAcc = accList.get(itra).y * Math.cos(aroundAxisRadius) + accList.get(itra).z * Math.sin(aroundAxisRadius);
					orthoMovementAcc = -accList.get(itra).y * Math.sin(aroundAxisRadius) + accList.get(itra).z * Math.cos(aroundAxisRadius);  //pay attention to the +-
				}else if(axis.x == 0.0 && axis.y == 0 && axis.z == 1.0)
				{
					
				}
				
				
				
				Vector3 temp = new Vector3();
				temp.Set(accList.get(itra).x, alongMovementAcc, orthoMovementAcc);
				resultAccList.add(temp);
			}
			
			return resultAccList;
		}
	}
}

public class DataCollection extends PApplet{
	
	public SerialDataCap mSerial;
	public String indicator;
	public int[] rgb;
	private int countNumber;
	private int savedTime;
	private int oneSecond = 350;   //1 second  //1000
	private int twoSecond = 1500;  //1.5seconds  //1500
	
	public int sampleCount = 0;
	
	public void setup()
	{
		mSerial = new SerialDataCap();
		try {
			mSerial.connect("COM2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		countNumber = 3;
		indicator = str(countNumber);
		rgb = new int[]{200, 200, 0};
		savedTime = millis();
		size(1000, 1000);
		background(250);
	}
	
	public void draw()
	{
		background(250);
		
		//update indicator
		{
			int passedTime = millis() - savedTime;
			
			if(countNumber == 0)
			{
				if(passedTime > twoSecond)
				{
					countNumber = 3;
					sampleCount++;
					indicator = str(countNumber);
					rgb[0] = 200; rgb[1] = 200; rgb[2] = 0;
					mSerial.isRecording = false;
					savedTime = millis();
				}
			}else
			{
				if(passedTime > oneSecond)
				{
					countNumber--;
					if(countNumber == 0)
					{
						indicator = "R";
						rgb[0] = 0; rgb[1] = 200; rgb[2] = 0;
						mSerial.isRecording = true;
						mSerial.sampleCount++;
					}else
					{
						indicator = str(countNumber);
					}
					
					savedTime = millis();
				}
			}
			
		}
		
		//draw indications, iterate loops for users performing the gesture
		{
			pushMatrix();
			
			fill(rgb[0], rgb[1], rgb[2], 150);
			noStroke();
			ellipse(500, 500, 200, 200);
			
			textSize(64);
			fill(250, 250, 250);
			text(indicator, 480, 525);
			
			//counts
			textSize(64);
			fill(250, 100, 100);
			text(sampleCount, 100, 100);
			
			
			popMatrix();
		}
	}
	
	public void keyPressed()
	{
		if(key == 'q'){
			mSerial.disConnect();
			exit();
		}
	}
	
	public static final void main(String args[]){
		PApplet.main(new String[] {"--present", "com.teng.imuv4.DataCollection"});
	}
}
