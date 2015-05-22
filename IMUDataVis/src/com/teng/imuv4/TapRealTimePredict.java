package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

//serial reader
class SerialPortReader
{
	
}



public class TapRealTimePredict {
	
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
	    
	}
	
	public static final void main(String args[])
	{
		TapRealTimePredict tapPredict = new TapRealTimePredict();
		
		double[] testData = new double[]{1, 
				1, 0.228999, -0.774073, 0.863091, 0.889829, -0.508119, -0.872794, -0.218122, 0.168953, 0.641652, -0.212306,
				0.258847, -0.107667, -0.676148, 0.149769, 0.387664, 0.525764, 0.123622, -0.488845, 0.134238, -1,
				0.673046, 0.678306, -0.879998, 1, -0.698213, 0.205726, -0.346612, 0.679252, -1, 1};
		
		double testResult = tapPredict.predictWithModel(tapPredict.linear_model, testData);
		
		System.out.println("   " + testResult);
		
		
	}
}
