package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import processing.core.PApplet;

import com.teng.math.Vector3;

class SerialDataCapture{

	private static CommPort commPort;
	private static String outputString = new String();
	
	public static Vector3 acc1;
	public static Vector3 acc2;
	
	//filters
	//public static 
	
	//data log for plot
	public static int logSize = 65*2; //250hz for 5 secs
	public static ArrayList<Vector3> acc1Log;
	public static ArrayList<Vector3> acc2Log;
	
	public SerialDataCapture()
	{
		acc1 = new Vector3();
		acc2 = new Vector3();
		
		acc1Log = new ArrayList<Vector3>();
		acc2Log = new ArrayList<Vector3>();
		
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
                    		//System.out.println(outputString.length());  // for quaternions should equal to 109, for acc should equal to 55
                    		if(outputString.length() == 91 && outputString != null)  
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                				
                    			/*// this is for quaternions
                    			if(outPutStringArr.length == 13)
                				{
                					//set quat
                					Quaternion tempQuat = new Quaternion();
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
                					
                					tempQuat.Set(decodeFloat(outPutStringArr[9]),  	//x 
                							decodeFloat(outPutStringArr[10]),    	//y
                							decodeFloat(outPutStringArr[11]), 		//z
                							decodeFloat(outPutStringArr[8]));		//w
                					
                					tempQuat.Nor();
                					quat3.Set(tempQuat);
                					
                					if(typeValue != 0.0)
                					{
                						DataStorage.AddSampleF(typeValue, 
                								quat1.w, quat1.x, quat1.y, quat1.z, 
                								quat2.w, quat2.x, quat2.y, quat2.z, 
                								quat3.w, quat3.x, quat3.y, quat3.z);
                						
                						
                					}
                					
                				}*/
                    			
                    			//this is for accelerometers
                    			if(outPutStringArr.length == 11)
                    			{
                    				acc1.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				acc2.Set(decodeFloat(outPutStringArr[3])/100.0,
                    						decodeFloat(outPutStringArr[4])/100.0, 
                    						decodeFloat(outPutStringArr[5])/100.0);
                    				
                    				
                    				//apply filters
                    				//low pass, to remove noise
                    				
                    				
                    				//... to remove dc shift
                    				
                    					
                    				Vector3 temp1 = new Vector3();
                    				temp1.Set(acc1);
                    				acc1Log.add(temp1);
                    				Vector3 temp2 = new Vector3();
                    				temp2.Set(acc2);
                    				acc2Log.add(temp2);
                    				
                    				//control the size
                    				if(acc1Log.size() > logSize)
                    				{
                    					acc1Log.remove(0);
                    				}
                    				
                    				if(acc2Log.size() > logSize)
                    				{
                    					acc2Log.remove(0);
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
}

public class v4RealTimePlot extends PApplet{

	public SerialDataCapture mSerial;
	public double widthSeg;
	public double heightSeg;
	public double heightThreshold;
	public int windowWidth;
	public int windowHeight;
	
	public void setup()
	{
		mSerial = new SerialDataCapture();
		try {
			mSerial.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		windowWidth = 1500;
		windowHeight = 1200;  //split into two
		widthSeg = windowWidth / mSerial.logSize;
		heightThreshold = 10;  //+ - 10
		heightSeg = (windowHeight/2) / (2 * heightThreshold);
		
		size(windowWidth, windowHeight);
		background(250);
		
	}
	
	public void draw()  //loop at around 60hz
	{
		background(250);
		
		//draw acc1log, up side
		{
			int acc1Size = mSerial.acc1Log.size();  //since it's not in the same thread, size could be larger than the logsize
			if(acc1Size > mSerial.logSize)
			{
				acc1Size = mSerial.logSize;
			}
			
			if(acc1Size > 10)
			{
				for(int itra = 0; itra < (acc1Size-1); itra ++)
				{
					stroke(255, 0, 0);  //x
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra).x * heightSeg),
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra + 1).x * heightSeg));
					
					stroke(0, 255, 0);  //y
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra).y * heightSeg),
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra + 1).y * heightSeg));
					
					stroke(0, 0, 255);  //z
					line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra).z * heightSeg),
							 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 3 / 4 + mSerial.acc1Log.get(itra + 1).z * heightSeg));
				}
			}
			
		}
		
		//draw acc2log, downside
		{
			int acc2Size = mSerial.acc2Log.size();  //since it's not in the same thread, size could be larger than the logsize
			if(acc2Size > mSerial.logSize)
			{
				acc2Size = mSerial.logSize;
			}
			
			if(acc2Size > 10)
			{
				for(int itra = 0; itra < (acc2Size-1); itra ++)
				{
					stroke(255, 0, 0);  //x
					line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra).x * heightSeg),
							 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra + 1).x * heightSeg));
					
					stroke(0, 255, 0);  //y
					line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra).y * heightSeg),
							 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra + 1).y * heightSeg));
					
					stroke(0, 0, 255);  //z
					line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra).z * heightSeg),
							 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
							 (float)(windowHeight * 1 / 4 + mSerial.acc2Log.get(itra + 1).z * heightSeg));
				}
			}
			
		}
	}
	
	public void dataReceiveCallback()
	{
		redraw();
	}
	
	public void keyPressed()
	{
		if(key == 'q'){
			mSerial.disConnect();
			exit();
		}
	}
	
	public static final void main(String args[]){
		PApplet.main(new String[] {"--present", "com.teng.imuv4.v4RealTimePlot"});
	}
}
