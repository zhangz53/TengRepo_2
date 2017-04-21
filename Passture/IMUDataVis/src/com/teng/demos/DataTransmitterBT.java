package com.teng.demos;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class DataTransmitterBT {

	OutputStream mOutputStream;
    CommPort commPort;

    public static DataTransmitterBT instance;
    public static DataTransmitterBT getSharedInstance()
    {
    	if(instance == null)
    	{
    		instance = new DataTransmitterBT();
    	}
    	return instance;
    }
    
    public DataTransmitterBT()
    {
    	instance = this;
    }
    
    void connect ( String portName ) throws Exception
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
	                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);         
	             
	                mOutputStream = serialPort.getOutputStream();
	                		
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
		 if(mOutputStream != null)
		 {
			 try {
				mOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 if(commPort != null)
		 {
			 new CloseThread().start();
		 }
	}
    
    void sendData(String dataString)
	 {
		 if(mOutputStream != null)
		 {
			 byte[] valueOutputBytes = dataString.getBytes(Charset.forName("UTF-8"));
			 
			 try {
				 	mOutputStream.write(valueOutputBytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
	 }
    
	
}
