package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.teng.filter.ButterWorth;
import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class v4DataRecorder {
	
	CommPort commPort;
	private static String outputString = new String();
	public static Quaternion quat;
	private static Matrix4 mMatrix;
	public static Vector3 acc1;
	public static Vector3 acc2;
	public static Vector3 geoAcc;
	public static double stamp;
	
	private static Vector3 mLinVel;
	private static Vector3 mLinVelPrev;
	private static Vector3 mLinPos;
	private static Vector3 mLinPosPrev;
	private static ArrayList<Vector3> xValuesVel; private static ArrayList<Vector3> yValuesVel;
	private static ArrayList<Vector3> xValuesPos; private static ArrayList<Vector3> yValuesPos;
	
	private static double vX;
	private static double vY;
	private static double vZ;
	
	private static double pX;
	private static double pY;
	private static double pZ;
	
	//data log
	public static DataStorage dataStorage;
	
	//filter 
	private static ArrayList<Vector3> xValuesAcc; private static ArrayList<Vector3> yValuesAcc;
	private static ButterWorth mButterLP;
	private static ButterWorth mButterHP;
	private static int order = 1;
	
	public v4DataRecorder()
	{
		super();
		quat = new Quaternion();
		mMatrix = new Matrix4();
		acc1 = new Vector3();
		acc2 = new Vector3();
		geoAcc = new Vector3();
		mLinVel = new Vector3();
		mLinVelPrev = new Vector3(); mLinVelPrev.Set(0, 0, 0);
		mLinPos = new Vector3();
		mLinPosPrev = new Vector3();  mLinPosPrev.Set(0, 0, 0);
		
		dataStorage = DataStorage.getInstance();
		
		xValuesAcc = new ArrayList<Vector3>();  //size 2
		yValuesAcc = new ArrayList<Vector3>();  //size 1
		xValuesVel = new ArrayList<Vector3>();  //size 2
		yValuesVel = new ArrayList<Vector3>();  //size 1
		xValuesPos = new ArrayList<Vector3>();	//size 2
		yValuesPos = new ArrayList<Vector3>();  //size 1
		
		mButterLP = new ButterWorth(ButterWorth.BandType.low);
		mButterHP = new ButterWorth(ButterWorth.BandType.high);
		
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
                    		if(outputString.length() == 55 && outputString != null)
                    		{
                    			//decode the hex
                    			String[] outPutStringArr = outputString.split(",");
                				if(outPutStringArr.length == 7)
                				{
                					//set quat
                					/*
                					Quaternion tempQuat = new Quaternion();
                					tempQuat.Set(decodeFloat(outPutStringArr[1]),  	//x 
                							decodeFloat(outPutStringArr[2]),    	//y
                							decodeFloat(outPutStringArr[3]), 		//z
                							decodeFloat(outPutStringArr[0]));		//w
                					
                					tempQuat.Nor();
                					quat.Set(tempQuat);
                					*/
                					
                					
                					//set acc
                					acc1.Set(decodeFloat(outPutStringArr[4]) / 100.0, 
                							decodeFloat(outPutStringArr[5]) / 100.0, 
                							decodeFloat(outPutStringArr[6]) / 100.0);
                					
                					
                					
                					//set the time stamp
                					stamp = decodeFloat(outPutStringArr[7]) / 1000.0;  //s
                					
                					
                					/*
                					////////////////////////////////////////////////////////////////////////////////////filter
                					//filter the acc data lp
            						xValuesAcc.add(acc);
            						if(xValuesAcc.size() > (order + 1))
            						{
            							xValuesAcc.remove(0);
            						}
            					
            						if(xValuesAcc.size() == (order + 1))
            						{
            							//filter
            							Vector3 yValue = mButterLP.applyButterWorth(xValuesAcc, yValuesAcc);
            							yValuesAcc.add(yValue);
            						}else
            						{
            							yValuesAcc.add(acc);
            						}
            						
            						if(yValuesAcc.size() > order)
            						{
            							yValuesAcc.remove(0);
            						}
                					
                					//DataStorage.AddSample(stamp, yValuesAcc.get(order-1).x, yValuesAcc.get(order-1).y, yValuesAcc.get(order-1).z, acc.x, acc.y, acc.z, 0.0, 0.0, 0.0);
                					
            						//apply geo-coordinates acc
            						mMatrix.Set(quat);
            						geoAcc.Set(yValuesAcc.get(order-1));
            						geoAcc.Mul(mMatrix.inv());
            						
            						//System.out.println(geoAcc.x + ",    " + geoAcc.y + ",    " + geoAcc.z);
            						
            						//velocity
            						//linear velocity
            						mLinVel.Set( mLinVelPrev.add(geoAcc.scl(stamp)));   
            						mLinVelPrev.Set(mLinVel.x, mLinVel.y, mLinVel.z);
            						
            						//DataStorage.AddSample(stamp, 0.0, 0.0, 0.0, mLinVel.x, mLinVel.y, mLinVel.z, 0.0, 0.0, 0.0);
            						
            						Vector3 temp = new Vector3();  //this is an important step
            						temp.Set(mLinVel);
            						
            						xValuesVel.add(temp);
            						
            						//filter the vel hp
            						if(xValuesVel.size() > (order + 1))
            						{
            							xValuesVel.remove(0);		
            						}
            					
            						if(xValuesVel.size() == (order + 1))
            						{
            							//filter
            							Vector3 yValue = mButterHP.applyButterWorth(xValuesVel, yValuesVel);
            							yValuesVel.add(yValue);
            						}else
            						{
            							yValuesVel.add(mLinVel);
            						}
            						
            						if(yValuesVel.size() > order)
            						{
            							yValuesVel.remove(0);
            						}
            						
            						vX =  yValuesVel.get(yValuesVel.size() - 1).x;
            						vY =  yValuesVel.get(yValuesVel.size() - 1).y;
            						vZ =  yValuesVel.get(yValuesVel.size() - 1).z;
            						
            						DataStorage.AddSample(stamp, vX, vY, vZ, mLinVel.x, mLinVel.y, mLinVel.z, 0.0, 0.0, 0.0);
            						
            						//linear position
            						mLinPos.Set(mLinPosPrev.add(yValuesVel.get(yValuesVel.size() - 1).scl(stamp)));
            						mLinPosPrev.Set(mLinPos);
            						
            						//high pass filter linear position to remove drift
            						Vector3 temppos = new Vector3();
            						temppos.Set(mLinPos);
            						
            						xValuesPos.add(temppos);
            						if(xValuesPos.size() > (order + 1))
            						{
            							xValuesPos.remove(0);
            						}
            					
            						if(xValuesPos.size() == (order + 1))
            						{
            							//filter
            							Vector3 yValue = mButterHP.applyButterWorth(xValuesPos, yValuesPos);
            							yValuesPos.add(yValue);
            						}else
            						{
            							yValuesPos.add(mLinPos);
            						}
            						
            						if(yValuesPos.size() > order)
            						{
            							yValuesPos.remove(0);
            						}
            						
            						pX =  yValuesPos.get(yValuesPos.size() - 1).x;
            						pY =  yValuesPos.get(yValuesPos.size() - 1).y;
            						pZ =  yValuesPos.get(yValuesPos.size() - 1).z;
            						//
            						*/
            						 
            						DataStorage.AddSample(stamp, vX, vY, vZ, pX, pY, pZ, geoAcc.x, geoAcc.y, geoAcc.z);
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
		v4DataRecorder recorder = new v4DataRecorder();
		recorder.connect("COM11");
		
		System.out.println("Outputting events.  Input to stop.");
		System.in.read();
		System.out.print("closing...");
		
		dataStorage.save();
		recorder.disConnect();
	}
}
