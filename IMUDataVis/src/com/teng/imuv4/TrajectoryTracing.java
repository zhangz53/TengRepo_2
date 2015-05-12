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

class SerialDataAcc {
	CommPort commPort;
	private static String outputString = new String();
	public static Quaternion quat;
	public static Vector3 acc;
	public static double stamp;

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
	
	void disConnect()
	{
		 if(commPort != null)
		 {
			 commPort.close();
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
                    		if(outputString.length() == 73 && outputString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                				if(outPutStringArr.length == 9)
                				{
                					//set quat
                					Quaternion tempQuat = new Quaternion();
                					tempQuat.Set(decodeFloat(outPutStringArr[1]),  	//x 
                							decodeFloat(outPutStringArr[2]),    	//y
                							decodeFloat(outPutStringArr[3]), 		//z
                							decodeFloat(outPutStringArr[0]));		//w
                					
                					tempQuat.Nor();
                					quat.Set(tempQuat);
                					
                					//set acc
                					acc.Set(decodeFloat(outPutStringArr[4]) / 100.0, 
                							decodeFloat(outPutStringArr[5]) / 100.0, 
                							decodeFloat(outPutStringArr[6]) / 100.0);
                					
                					//set the time stamp
                					stamp = decodeFloat(outPutStringArr[7]) / 1000.0;  //s
                					
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

public class TrajectoryTracing extends PApplet{

	public SerialDataAcc mSerialDataAcc;
	
	boolean ready4Interpolate = false;
	
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
		/*
		background(250);
		
		//coordinates
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
		//cube
		
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialDataAcc.disConnect();
			exit();
		}
		
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.imuv4.TrajectoryTracing"});
	}
	
}
