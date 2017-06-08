package com.teng.tracking;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;

public class SocketConnector {

	public ServerSocket serverSocket;
	String message = "";
    static final int socketServerPORT = 9090;
    private ArrayList<Integer> byteArray;
    private Socket clientSocket;
    private OutputStream outputStream;
    private PrintStream printStream;
    
    public SocketConnector() throws IOException
    {
    	byteArray = new ArrayList<Integer>();
    	Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }
    
    public void Destroy()
	{
		if (serverSocket != null) {
            try {
            	printStream.close();
                serverSocket.close();
                System.out.println("server closed");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
    
    private class SocketServerThread extends Thread {
		int count = 0;
		
		@Override
		public void run()
		{
			try{
				serverSocket = new ServerSocket(socketServerPORT);	
				clientSocket = serverSocket.accept();
				
                count++;
                message += "#" + count + " from "
                        + clientSocket.getInetAddress() + ":"
                        + clientSocket.getPort() + "\n";

                System.out.println(message);
                    
                outputStream = clientSocket.getOutputStream();
                printStream = new PrintStream(outputStream);
                
                CommunicationThread commThread = new CommunicationThread(clientSocket);
            	new Thread(commThread).start();
				
			}catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
		
	}
    
    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedInputStream input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedInputStream(this.clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int byteRead = input.read();
                    byteArray.add(byteRead);

                    if(byteArray.size() > 12)  //this is wrong
                    {
                        if(byteArray.get(byteArray.size() - 1) == 0 &&
                                byteArray.get(byteArray.size() - 2) == 0 &&
                                byteArray.get(byteArray.size() - 3) == 0 &&
                                byteArray.get(byteArray.size() - 4) == 20 &&
                                byteArray.get(byteArray.size() - 5) == 0 &&
                                byteArray.get(byteArray.size() - 6) == 0 &&
                                byteArray.get(byteArray.size() - 7) == 0 &&
                                byteArray.get(byteArray.size() - 8) == 132  //4
                        ){
                            if(byteArray.get(0) == 132 &&
                                    byteArray.get(1) == 0 &&
                                    byteArray.get(2) == 0 &&
                                    byteArray.get(3) == 0 &&
                                    byteArray.get(4) == 20 &&
                                    byteArray.get(5) == 0 &&
                                    byteArray.get(6) == 0 &&
                                    byteArray.get(7) == 0
                                    )
                            {
                                interpretCode(byteArray);
                            }
                            byteArray.clear();
                            byteArray.add(132);
                            byteArray.add(0);
                            byteArray.add(0);
                            byteArray.add(0);
                            byteArray.add(20);
                            byteArray.add(0);
                            byteArray.add(0);
                            byteArray.add(0);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
  //convert from byte to int
    private int getIntegerFromByte(int inta, int intb, int intc, int intd)
    {
        int result = 0;
        byte[] byteArray = new byte[]{(byte)inta, (byte)intb, (byte)intc, (byte)intd};
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order( ByteOrder.LITTLE_ENDIAN);
        while( bb.hasRemaining()) {
            result = bb.getInt();
        }

        return  result;
    }
    
    //convert from byte to float
    private float getFloatFromByte(int inta, int intb, int intc, int intd)
    {
        float result = 0;
        byte[] byteArray = new byte[]{(byte)inta, (byte)intb, (byte)intc, (byte)intd};
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order( ByteOrder.LITTLE_ENDIAN);
        while( bb.hasRemaining()) {
            result = bb.getFloat();
        }

        return result;
    }
    
    private boolean isExist(int target, ArrayList<Integer> array)
    {
        for(int itra = 0; itra<array.size(); itra++)
        {
            if(target == array.get(itra))
            {
                return true;
            }
        }
        return false;
    }
    
    private void interpretCode(ArrayList<Integer> frameData)
    {
        //frame count, 8 - 11
        int frameCount = getIntegerFromByte(frameData.get(8), frameData.get(9), frameData.get(10), frameData.get(11));
        //number of sounds , 32 - 35
        int numSounds = getIntegerFromByte(frameData.get(32), frameData.get(33), frameData.get(34), frameData.get(35));

        boolean hasSingleEvent = true;
        if(numSounds > 1)
        {
            hasSingleEvent = false;
        }        
        
        for(int itrs = 0; itrs < numSounds; itrs++)
        {
            int sId = getIntegerFromByte(frameData.get(35 + 20 * itrs + 1), frameData.get(35 + 20 * itrs + 2), frameData.get(35 + 20 * itrs + 3), frameData.get(35 + 20 * itrs + 4));
            float sX = getFloatFromByte(frameData.get(35 + 20 * itrs + 5), frameData.get(35 + 20 * itrs + 6), frameData.get(35 + 20 * itrs + 7), frameData.get(35 + 20 * itrs + 8));
            float sY = getFloatFromByte(frameData.get(35 + 20 * itrs + 9), frameData.get(35 + 20 * itrs + 10), frameData.get(35 + 20 * itrs + 11), frameData.get(35 + 20 * itrs + 12));
            float sZ = getFloatFromByte(frameData.get(35 + 20 * itrs + 13), frameData.get(35 + 20 * itrs + 14), frameData.get(35 + 20 * itrs + 15), frameData.get(35 + 20 * itrs + 16));
            float sPower = getFloatFromByte(frameData.get(35 + 20 * itrs + 17), frameData.get(35 + 20 * itrs + 18), frameData.get(35 + 20 * itrs + 19), frameData.get(35 + 20 * itrs + 20));
        
            //send message to draw
            TrackingVisualization.getInstance().addParticle(sX, sY, sZ);
        }
    }
   
    public String getIpAddress() {
        String ip = "get ip address ";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    ip += "Server running at : "
                            //+ inetAddress.getHostAddress();
                                +inetAddress.toString();
                    
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
    
    
}
