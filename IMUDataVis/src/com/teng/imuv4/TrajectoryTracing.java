package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

import processing.core.PApplet;

import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;

class SerialDataAcc {
	CommPort commPort;
	private static String outputString = new String();
	public static Quaternion quat;
	public static Vector3 acc;
	public static double stamp = 0.0151;  //in seconds
	
	public static Vector3 pos;      //space pos
	public static Vector3 velocity;	//space speed
	public static Vector3 linAcc;	//space acc
	public static Matrix4 mMatrix;	//space rotation matrix

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
		pos = new Vector3(); pos.Set(Vector3.Zero);
		velocity = new Vector3(); velocity.Set(Vector3.Zero);
		linAcc = new Vector3(); linAcc.Set(Vector3.Zero);
		mMatrix = new Matrix4();
		
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
                    		//System.out.print(outputString);
                    		//System.out.println(outputString.length());  //should equal to 73
                    		if(outputString.length() == 91 && outputString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                				if(outPutStringArr.length == 11)
                				{
                					//set acc
                					acc.Set(decodeFloat(outPutStringArr[0]) / 100.0, 
                							decodeFloat(outPutStringArr[1]) / 100.0, 
                							decodeFloat(outPutStringArr[2]) / 100.0);
                					
                					
                					//set quat
                					Quaternion tempQuat = new Quaternion();
                					tempQuat.Set(decodeFloat(outPutStringArr[7]),  	//x 
                							decodeFloat(outPutStringArr[8]),    	//y
                							decodeFloat(outPutStringArr[9]), 		//z
                							decodeFloat(outPutStringArr[6]));		//w
                					
                					tempQuat.Nor();
                					quat.Set(tempQuat);
                					
                					
                					//do the calculations
                					getWorldPos(acc, quat);
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
	
	static void getWorldPos(Vector3 ac, Quaternion qu)  //no filters at the moment, need to implement good filters, todo task
	{
		//add filters?
		
		mMatrix.Set(qu);
		linAcc.Set(ac);
		linAcc.Mul(mMatrix.inv());
		
		velocity.Add(linAcc.scl(stamp));
		pos.Add(velocity.scl(stamp));
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
		
		
		/*
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
		*/
		
		size(1000, 1000);
		background(250);
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
		
		//cube
		{
			pushMatrix();				
			fill(200, 10, 50);
			lights();
			translate(width/2 - (float)mSerialDataAcc.pos.y*100.0f, height*3/5 -  (float)mSerialDataAcc.pos.z*100.0f  ,  (float)mSerialDataAcc.pos.x*100.0f);
			
			//translate(width/2 , height*3/5 , 0f);
			
			//apply rotation matrix
			
			//applyMatrix((float) mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M00], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M01], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M02], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M03],
				//	(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M10], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M11], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M12], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M13],
				//	(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M20], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M21], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M22], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M23],
				//	(float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M30], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M31], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M32], (float)mSerialDataAcc.mMatrix.val[mSerialDataAcc.mMatrix.M33]);
			
			
			noStroke();
			box(100);
			popMatrix();
		}
		
		*/
		
		background(250);
		pushMatrix();
		
		fill(100, 50, 50, 150);
		noStroke();
		ellipse(500 + (float)mSerialDataAcc.pos.x*200.0f, 500 + (float)mSerialDataAcc.pos.y*200.0f, 200, 200);
		
		popMatrix();
	}
	
	public void keyPressed(){
		if(key == 'q'){
			mSerialDataAcc.disConnect();
			exit();
		}
		
		if(key == 'r')
		{
			mSerialDataAcc.velocity.Set(Vector3.Zero);
			mSerialDataAcc.pos.Set(Vector3.Zero);
			
		}
		
	}
	
	public static final void main(String args[]){
		
		PApplet.main(new String[] {"--present", "com.teng.imuv4.TrajectoryTracing"});
	}
	
}
