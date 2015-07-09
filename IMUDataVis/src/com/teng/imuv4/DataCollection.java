package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

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
	
	public static Vector3 geoAcc;
	public static double stamp;
	public static double typeValue = 1.0;
	
	//data log
	public static DataStorage dataStorage;
	public static double sampleCount = 0.0;
	public static boolean isRecording = false;
	
	public SerialDataCap()
	{
		acc1 = new Vector3();
		acc2 = new Vector3();
		quat3 = new Quaternion();
		
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
                    		if(outputString.length() == 91 && outputString != null)  
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                    			
                    			//this is for accelerometers
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
                    				
                    				//record
                    				if(isRecording)
                    				{
                    					DataStorage.AddSampleF(sampleCount, acc1.x, acc1.y, acc1.z, acc2.x, acc2.y, acc2.z,
                        						 quat3.x, quat3.y, quat3.z, quat3.w, 0.0, 0.0);
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
			mSerial.connect("COM11");
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
