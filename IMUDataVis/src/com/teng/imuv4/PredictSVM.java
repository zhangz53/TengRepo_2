package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import com.teng.math.Vector3;

public class PredictSVM {

	public svm_model linear_model;
	public Featurization featurization;
	public double[] feature_max;
	public double[] feature_min;
	public double lower = -1.0;
	public double upper = 1.0;
	
	public PredictSVM()
	{
		featurization = new Featurization(1);
		linear_model = loadModel("C:\\Users\\Teng\\Documents\\matlab-workspace\\MLToolbox\\libsvm-3.20\\matlab\\linear_model_pilot.model");
	
		//just for now
		feature_max = new double[]{1, 1, 3.11382, 2.86537, 3.13689, 3.12249, 3.14159, 3.06354, 1.49167, 0.817333, 0.610667, 1.60867,
				0.848, 1.03633, 3.45228, 2.02518, 3.34926, 5.48354, 6.14244, 9.712210000000001, 4.48168, 3.62476, 4.26625, 4.84796,
				2.59108, 4.85818, 22.1457, 20.8762, 15.1993, 22.5061, 16.6794, 23.3872};
		
		feature_min = new double[]{-1, -1, -3.10882, -2.96007, -3.13216, -3.04491, -3.09617, -3.09617, -0.814333, -1.567,
				-1.985, -0.787, -1.56367, -1.945, 0.140896, 0.0393616, 0.09303889999999999, 0.131354, 0.09184530000000001,
				0.0814105, -4.77007, -4.7536, -3.62855, -3.90524, -4.59617, -3.18207, -1.33319, -10.6843, -26.0035, -1.45346,
				-26.0471, -23.4418};
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
	
	public double predictWithDefaultModel(double[] features)
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
	    svm.svm_get_labels(linear_model,labels);

	    double[] prob_estimates = new double[totalClasses];
	    //double v = svm.svm_predict_probability(model, nodes, prob_estimates);
	    double v = svm.svm_predict(linear_model, nodes);
	    
	    for (int i = 0; i < totalClasses; i++){
	        System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	    }
	    System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    return v;
	}
	
	//this is for acc1 and acc2
	public double predictWithDefaultModel(ArrayList<Vector3> dataset1, ArrayList<Vector3> dataset2)
	{
		//get features
		double[] testData = calculateFeatures(dataset1, dataset2);
		
		double predictValue = predictWithDefaultModel(testData);
		
		return predictValue;
	}
	
	//calculate all the needed features and storage them
	public double[] calculateFeatures(ArrayList<Vector3> ac1, ArrayList<Vector3> ac2)
	{
		//ac1 local peaks (1st and 2nd)
		int[] peakIndex = featurization.localPeakIndex(ac1);
		//System.out.println("local peak index: " + peakIndex[0] + ",  and   " + peakIndex[1]);
		
		//feature 1: 1st peak is behind the 2nd peak, and dominate values on axis are inversed
		double f1 = -1.0;
		if(peakIndex[0] > peakIndex[1])
		{
			if(ac1.get(peakIndex[0]).hasOppositeDirection( ac1.get(peakIndex[1])))
			{
				f1 = 1.0;
			}
		}
		
		//feature 2-4: moving direction on the 1st peak of acc1
		//thumb move towards palm
		double f2 = Math.atan2(ac1.get(peakIndex[1]).y, ac1.get(peakIndex[1]).x);
		double f3 = Math.atan2(ac1.get(peakIndex[1]).z, ac1.get(peakIndex[1]).y);
		double f4 = Math.atan2(ac1.get(peakIndex[1]).x, ac1.get(peakIndex[1]).z);
		
		//feature 5-7: moving diretion on the 1st peak of acc2
		//index finger movements effected by thumb
		double f5 = Math.atan2(ac2.get(peakIndex[1]).y, ac2.get(peakIndex[1]).x);
		double f6 = Math.atan2(ac2.get(peakIndex[1]).z, ac2.get(peakIndex[1]).y);
		double f7 = Math.atan2(ac2.get(peakIndex[1]).x, ac2.get(peakIndex[1]).z);
		
		////////////////////////brute force features
		
		//feature 8-13: mean of acc1 and acc2
		double[] means1 = featurization.meanAxes(ac1);
		double f8 = means1[0];
		double f9 = means1[1];
		double f10 = means1[2];
		
		double[] means2 = featurization.meanAxes(ac2);
		double f11 = means2[0];
		double f12 = means2[1];
		double f13 = means2[2];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = featurization.stdvAxes(ac1, means1);
		double f14 = stdvs1[0];
		double f15 = stdvs1[1];
		double f16 = stdvs1[2];
		
		double[] stdvs2 = featurization.stdvAxes(ac2, means2);
		double f17 = stdvs2[0];
		double f18 = stdvs2[1];
		double f19 = stdvs2[2];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = featurization.skewnessAxes(ac1, means1, stdvs1);
		double f20 = skews1[0];
		double f21 = skews1[1];
		double f22 = skews1[2];
		
		double[] skews2 = featurization.skewnessAxes(ac2, means2, stdvs2);
		double f23 = skews2[0];
		double f24 = skews2[1];
		double f25 = skews2[2];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = featurization.kurtosisAxes(ac1, means1, stdvs1);
		double f26 = kurs1[0];
		double f27 = kurs1[1];
		double f28 = kurs1[2];
		
		double[] kurs2 = featurization.kurtosisAxes(ac2, means2, stdvs2);
		double f29 = kurs2[0];
		double f30 = kurs2[1];
		double f31 = kurs2[2];
		
		double[] features = new double[]{1.0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, 
				f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, 
				f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, 
				f31};
		
		//need to be scaled
		for(int itrf = 1; itrf < features.length; itrf++)
		{
			features[itrf] =  scaleOutput(features[itrf], itrf);
		}
		
		return features;
	}
	
	private double scaleOutput(double value, int index)
	{
		/* skip single-valued attribute */
		if(feature_max[index] == feature_min[index])
			return 0.0;

		double sValue = 0.0;
		
		if(value == feature_min[index])
			sValue = lower;
		else if(value == feature_max[index])
			sValue = upper;
		else
			sValue = lower + (upper-lower) * 
				(value-feature_min[index])/
				(feature_max[index]-feature_min[index]);
		
		
		return sValue; 
	}
	
}
