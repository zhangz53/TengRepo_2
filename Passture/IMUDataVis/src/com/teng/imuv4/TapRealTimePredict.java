package com.teng.imuv4;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.teng.math.Vector3;

//serial reader
class SerialPortReader
{
	private static CommPort commPort;
	private static String outputString = new String();
	public static Vector3 acc1;
	public static Vector3 acc2;
	
	public static Vector3 geoAcc;
	public static double stamp;
	public static double typeValue = 1.0;
	
	//data log
	public static double sampleCount = 0.0;
	public static int sampleNum = 8;  //has to be mapped with the number chosen in PreProcessing
	private static int movingWindowSize = 8;  //decide frequency to examine the windowed data
	private static int movingCount = 0;
	
	public static ArrayList<Vector3> dataset_acc1;
	public static ArrayList<Vector3> dataset_acc2;
	
	public static PredictSVM predictSVM;
	
	public SerialPortReader()
	{
		acc1 = new Vector3();
		acc2 = new Vector3();
		
		dataset_acc1 = new ArrayList<Vector3>();
		dataset_acc2 = new ArrayList<Vector3>();
		
		predictSVM = new PredictSVM();
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
                    			if(outPutStringArr.length == 7)
                    			{
                    				acc1.Set(decodeFloat(outPutStringArr[0])/100.0,
                    						decodeFloat(outPutStringArr[1])/100.0, 
                    						decodeFloat(outPutStringArr[2])/100.0);
                    				
                    				acc2.Set(decodeFloat(outPutStringArr[3])/100.0,
                    						decodeFloat(outPutStringArr[4])/100.0, 
                    						decodeFloat(outPutStringArr[5])/100.0);
                    				
                    				//save for test data sample
                    				dataset_acc1.add(new Vector3(acc1));
                    				dataset_acc2.add(new Vector3(acc2));
                    				
                    				if(dataset_acc1.size() > sampleNum)
                    				{
                    					dataset_acc1.remove(0);
                    					dataset_acc2.remove(0);
                    					
                    					movingCount++;
                    					
                    					if(movingCount == movingWindowSize)
                    					{
                    						//predict
                    						
                    						if(dataset_acc1.size() == dataset_acc2.size() || dataset_acc1.size() == sampleNum)
                    						{
                    							double predictValue = predictSVM.predictWithDefaultModel(dataset_acc1, dataset_acc2);
                    							System.out.println("predict: " + predictValue);
                    							
                    							movingCount = 0;
                    						}
                    						
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
}



public class TapRealTimePredict {
	
	/*
	public svm_model linear_model;
	
	public TapRealTimePredict()
	{
		linear_model = loadModel("C:\\Users\\Teng\\Documents\\matlab-workspace\\MLToolbox\\libsvm-3.20\\matlab\\linear_model_pilot.model");
	}
	
	public svm_model trainModel()  
	{
		svm_model model = new svm_model();;
		
		//todo... train the model
		
		return model;
	}
	
	public svm_model loadModel(String modelFile)
	{
		String dataFile = modelFile;
		BufferedReader br_model = null;
		
		try {
			br_model = new BufferedReader(new FileReader(dataFile));	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		svm_model model = null;
		
		try {
			model = svm.svm_load_model(br_model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return model;
	}
	
	public double predictWithModel(svm_model model, double[] features)
	{
		svm_node[] nodes = new svm_node[features.length-1];
	    for (int i = 1; i < features.length; i++)
	    {
	        svm_node node = new svm_node();
	        node.index = i;
	        node.value = features[i];

	        nodes[i-1] = node;
	    }

	    int totalClasses = 2;       
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(model,labels);

	    double[] prob_estimates = new double[totalClasses];
	    //double v = svm.svm_predict_probability(model, nodes, prob_estimates);
	    double v = svm.svm_predict(model, nodes);
	    
	    for (int i = 0; i < totalClasses; i++){
	        System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	    }
	    System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    return v;
	    
	}*/
	
	public static final void main(String args[])
	{
		/*
		TapRealTimePredict tapPredict = new TapRealTimePredict();
		
		double[] testData = new double[]{1, 
				1, 0.228999, -0.774073, 0.863091, 0.889829, -0.508119, -0.872794, -0.218122, 0.168953, 0.641652, -0.212306,
				0.258847, -0.107667, -0.676148, 0.149769, 0.387664, 0.525764, 0.123622, -0.488845, 0.134238, -1,
				0.673046, 0.678306, -0.879998, 1, -0.698213, 0.205726, -0.346612, 0.679252, -1, 1};
		
		double testResult = tapPredict.predictWithModel(tapPredict.linear_model, testData);
		
		System.out.println("   " + testResult);
		*/
		
		SerialPortReader mSerial = new SerialPortReader();
		try {
			mSerial.connect("COM11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Recording... Press to Stop");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mSerial.disConnect();
		
		System.exit(0);
		
	}
}
