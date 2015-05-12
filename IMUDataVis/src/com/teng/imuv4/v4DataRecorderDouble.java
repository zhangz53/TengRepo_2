package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class v4DataRecorderDouble {

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
	
	public v4DataRecorderDouble()
	{
		super();
		//quat1 = new Quaternion();  //imu 1
		//quat2 = new Quaternion();  //imu 2
		//quat3 = new Quaternion();  //imu 1+2
		
		acc1 = new Vector3();
		acc2 = new Vector3();
		
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
                    		if(outputString.length() == 55 && outputString != null)  
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
                    			if(outPutStringArr.length == 7)
                    			{
                    				acc1.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				acc2.Set(decodeFloat(outPutStringArr[3])/100.0,
                    						decodeFloat(outPutStringArr[4])/100.0, 
                    						decodeFloat(outPutStringArr[5])/100.0);
                    				
                    				//record
                    				DataStorage.AddSampleF(1.0, acc1.x, acc1.y, acc1.z, acc2.x, acc2.y, acc2.z,
                    						0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
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
	
	public static final void main(String args[]) throws Exception{
		v4DataRecorderDouble recorder = new v4DataRecorderDouble();
		recorder.connect("COM11");
		
		System.out.println("Recording... Press to Stop");
		System.in.read();
		recorder.disConnect();
		
		System.exit(0);
	}
}
