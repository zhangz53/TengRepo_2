package com.teng.demos;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.teng.imuv4.PredictSVM;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class SensorSerial{
	CommPort commPort;
	private static String quatString = new String();
	
	public static Vector3 acc;
	public static Quaternion quat;
	public static ArrayList<Vector3> dataset_acc;
	public static ArrayList<Quaternion> dataset_quat;
	
	public static Vector3 xAxis;
	public static Vector3 yAxis;
	public static Vector3 zAxis;
	
	public static int sampleNum;  //has to be mapped with the number chosen in PreProcessing
	private static int movingWindowSize;  //decide frequency to examine the windowed data
	private static int movingCount = 0;
	
	public static PredictSVM predictSVM;
	public static double predictValue;
	
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
		
		xAxis = new Vector3(1.0, 0.0, 0.0);
		yAxis = new Vector3(0.0, 1.0, 0.0);
		zAxis = new Vector3(0.0, 0.0, 1.0);
		
		predictSVM = new PredictSVM("C:\\Users\\Teng\\Documents\\TestDataFolder\\test\\rbf_model_pilot.model", "C:\\Users\\Teng\\Documents\\TestDataFolder\\test\\range");
	
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
                    		//System.out.println(quatString.length());
                    		if(quatString.length() == 91 && quatString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = quatString.split(",");
                    			
                    			if(outPutStringArr.length == 11)
                				{
                    				
                    				
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
}



public class IMUSensor {
	
	public static final void main(String args[]){
		
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
			
			
		}
		
	}
}
