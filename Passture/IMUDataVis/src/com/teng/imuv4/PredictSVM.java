package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class PredictSVM {

	public svm_model linear_model;
	public Featurization featurization;
	public double[] feature_max;
	public double[] feature_min;
	private double y_lower;
	private double y_upper;
	private boolean y_scaling = false;
	private double y_max = -Double.MAX_VALUE;
	private double y_min = Double.MAX_VALUE;
	public int maxIndex;
	public BufferedReader fp_restore = null;
	public double lower = -1.0;
	public double upper = 1.0;
	
	
	public PredictSVM()
	{
		featurization = new Featurization(1);
		linear_model = loadModel("C:\\Users\\Teng\\Desktop\\dataset\\526\\linear_model_pilot.model");
		
		//load scale range file
		getScaleRange("C:\\Users\\Teng\\Desktop\\dataset\\526\\range");
	}
	
	public PredictSVM(String modelFile)
	{
		featurization = new Featurization(1);
		linear_model = loadModel(modelFile);
	}
	
	public PredictSVM(String modelFile, String rangeFile)
	{
		featurization = new Featurization(1);
		linear_model = loadModel(modelFile);
		getScaleRange(rangeFile);
	}
	
	private BufferedReader rewind(BufferedReader fp, String filename) throws IOException
	{
		fp.close();
		return new BufferedReader(new FileReader(filename));
	}
	
	private void getScaleRange(String rangeFile)
	{
		maxIndex = 0;
		
		//get max index size
		if(rangeFile != null)
		{
			int idx, c;

			try {
				fp_restore = new BufferedReader(new FileReader(rangeFile));
			}
			catch (Exception e) {
				System.err.println("can't open file " + rangeFile);
				System.exit(1);
			}
			try {
				if((c = fp_restore.read()) == 'y')
				{
					fp_restore.readLine();
					fp_restore.readLine();		
					fp_restore.readLine();		
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fp_restore.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fp_restore.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String restore_line = null;
			try {
				while((restore_line = fp_restore.readLine())!=null)
				{
					StringTokenizer st2 = new StringTokenizer(restore_line);
					idx = Integer.parseInt(st2.nextToken());
					maxIndex = Math.max(maxIndex, idx);
				}
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fp_restore = rewind(fp_restore, rangeFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			feature_max = new double[(maxIndex+1)];
			feature_min = new double[(maxIndex+1)];
		} catch(OutOfMemoryError e) {
			System.err.println("can't allocate enough memory");
			System.exit(1);
		}
		
		for(int i=0;i<=maxIndex;i++)
		{
			feature_max[i] = -Double.MAX_VALUE;
			feature_min[i] = Double.MAX_VALUE;
		}
		
		if(rangeFile != null)
		{
			// fp_restore rewinded in finding max_index 
			int idx, c;
			double fmin, fmax;

			try {
				fp_restore.mark(2);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}				// for reset
			try {
				if((c = fp_restore.read()) == 'y')
				{
					fp_restore.readLine();		// pass the '\n' after 'y'
					StringTokenizer st = new StringTokenizer(fp_restore.readLine());
					y_lower = Double.parseDouble(st.nextToken());
					y_upper = Double.parseDouble(st.nextToken());
					st = new StringTokenizer(fp_restore.readLine());
					y_min = Double.parseDouble(st.nextToken());
					y_max = Double.parseDouble(st.nextToken());
					y_scaling = true;
				}
				else
					fp_restore.reset();
			} catch (NumberFormatException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				if(fp_restore.read() == 'x') {
					fp_restore.readLine();		// pass the '\n' after 'x'
					StringTokenizer st = new StringTokenizer(fp_restore.readLine());
					lower = Double.parseDouble(st.nextToken());
					upper = Double.parseDouble(st.nextToken());
					String restore_line = null;
					while((restore_line = fp_restore.readLine())!=null)
					{
						StringTokenizer st2 = new StringTokenizer(restore_line);
						idx = Integer.parseInt(st2.nextToken());
						fmin = Double.parseDouble(st2.nextToken());
						fmax = Double.parseDouble(st2.nextToken());
						if (idx <= maxIndex)
						{
							feature_min[idx] = fmin;
							feature_max[idx] = fmax;
						}
					}
				}
			} catch (NumberFormatException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				fp_restore.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	    int totalClasses = 5;       
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(linear_model,labels);

	    double[] prob_estimates = new double[totalClasses];
	    double v = svm.svm_predict_probability(linear_model, nodes, prob_estimates);
	    
	    //double v = svm.svm_predict(linear_model, nodes);
	    
	   // for (int i = 0; i < totalClasses; i++){
	    //    System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	   // }
	    //System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    return v;
	}
	
	public double[] predictWithDefaultModel_Prob(double[] features)
	{
		double[] result = new double[2];
		
		svm_node[] nodes = new svm_node[features.length-1];
	    for (int i = 1; i < features.length; i++)
	    {
	        svm_node node = new svm_node();
	        node.index = i;
	        node.value = features[i];

	        nodes[i-1] = node;
	    }

	    int totalClasses = 2;  //5       
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(linear_model,labels);

	    double[] prob_estimates = new double[totalClasses];
	    double v = svm.svm_predict_probability(linear_model, nodes, prob_estimates);
	    
	    //double v = svm.svm_predict(linear_model, nodes);
	    
	   // for (int i = 0; i < totalClasses; i++){
	    //    System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	   // }
	    //System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    result[0] = v;
	    result[1] = prob_estimates[(int) (v-1)];
	    
	    return result;
	}
	
	//this is for acc1 and acc2
	public double predictWithDefaultModel(ArrayList<Vector3> dataset1, ArrayList<Vector3> dataset2)
	{
		//get features
		double[] testData = calculateFeatures(dataset1, dataset2);
		
		double predictValue = predictWithDefaultModel(testData);
		
		return predictValue;
	}
	
	
	//this is for acc2
	public double predictWithDefaultModel(ArrayList<Vector3> dataset)
	{
		double[] testData = calculateFeatures(dataset);
		double predictValue = predictWithDefaultModel(testData);
		return predictValue;
	}
	
	//this is for swipe start/end
	public double predictSwipeStartEndWithDefaultModel(ArrayList<Vector3> ac, ArrayList<Quaternion> quat, ArrayList<Vector3> ang)
	{
		double[] testData = calculateFeatures_SwipeStartEnd(ac, quat, ang);
		double predictValue = predictWithDefaultModel(testData);
		return predictValue;
	}
	
	//this is for chopping board
	public double predictSwipeChoppingBoard(ArrayList<Vector3> ac, ArrayList<Vector3> ang)
	{
		double[] testData = calculateFeatures_ChoppingBoard(ac, ang);
		double predictValue = predictWithDefaultModel(testData);
		return predictValue;
	}
	
	public double[] predictWithDefaultModel_Prob(ArrayList<Vector3> dataset)
	{
		double[] testData = calculateFeatures(dataset);
		double[] predictValue = predictWithDefaultModel_Prob(testData);
		return predictValue;
	}
	
	//this is for quat
	public double predictWithDefaultModel(Quaternion quat)  //frame by frame
	{
		double[] testData = new double[]{12.0, quat.x, quat.y, quat.z, quat.w};
		svm_node[] nodes = new svm_node[testData.length-1];
		
		for (int i = 1; i < testData.length; i++)
	    {
	        svm_node node = new svm_node();
	        node.index = i;
	        node.value = testData[i];

	        nodes[i-1] = node;
	    }
		
		int totalClasses = 12;       
	    int[] labels = new int[totalClasses];
	    
	    svm.svm_get_labels(linear_model,labels);
	   // double[] prob_estimates = new double[totalClasses];
	    
	    double v = svm.svm_predict(linear_model, nodes);
	    
	    return v;
	}
	
	public double[] calculateFeatures_SwipeStartEnd(ArrayList<Vector3> ac,  ArrayList<Quaternion> quat, ArrayList<Vector3> ang)
	{
		ArrayList<Vector3> absAc = ac;//featurization.toAbsList(ac);
		
		double[] means1 = featurization.meanAxes(absAc);
		double[] middles = featurization.middleAxes(absAc);
		ArrayList<Vector3> firstHalf = new ArrayList<Vector3>();
		ArrayList<Vector3> secondHalf = new ArrayList<Vector3>();
		
		int acSize = ac.size();
		int halfSize = (int)(acSize / 2);
		for(int ith = 0; ith <= halfSize; ith++)
		{
			firstHalf.add(ac.get(ith));
		}
		
		for(int ith = (halfSize+1); ith < acSize; ith++)
		{
			secondHalf.add(ac.get(ith));
		}
		
		double[] firstMeans = featurization.meanAxes(firstHalf);
		double[] secondMeans = featurization.meanAxes(secondHalf);
		
		double ft1 = Math.abs(firstMeans[0]) > Math.abs(secondMeans[0]) ? 1.0 : -1.0;
		double ft2 = Math.abs(firstMeans[1]) > Math.abs(secondMeans[1]) ? 1.0 : -1.0;
		double ft3 = Math.abs(firstMeans[2]) > Math.abs(secondMeans[2]) ? 1.0 : -1.0;
		
		double ft4 = middles[0];
		double ft5 = middles[1];
		double ft6 = middles[2];
		
		double ft7 = firstMeans[0];
		double ft8 = firstMeans[1];
		double ft9 = firstMeans[2];
		
		double ft10 = secondMeans[0];
		double ft11 = secondMeans[1];
		double ft12 = secondMeans[2];
		
		double f1 = means1[0];
		double f2 = means1[1];
		double f3 = means1[2];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = featurization.stdvAxes(absAc, means1);
		double f4 = stdvs1[0];
		double f5 = stdvs1[1];
		double f6 = stdvs1[2];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = featurization.skewnessAxes(absAc, means1, stdvs1);
		double f7 = skews1[0];
		double f8 = skews1[1];
		double f9 = skews1[2];
		
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = featurization.kurtosisAxes(absAc, means1, stdvs1);
		double f10 = kurs1[0];
		double f11 = kurs1[1];
		double f12 = kurs1[2];
		
		featurization.getDisplacement(absAc);
		Vector3 diffPeak = featurization.largestNeighbourAbsDiff(absAc);
		
		//frequency, taken care of mean
		//signal sub by mean
		ArrayList<Vector3> groundAc = new ArrayList<Vector3>();
		
		for(int itra = 0; itra < ac.size(); itra++)
		{
			Vector3 temp = new Vector3();
			temp.Set(absAc.get(itra));
			temp.Sub(means1[0], means1[1], means1[2]);  //remove the means
			groundAc.add(temp);
		}
		
		
		double[][] freqs = featurization.freq(groundAc);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//feature on quat
		double[] means2 = featurization.meanQuats(quat);
		double[] stdvs2 = featurization.stdvQuats(quat, means2);
		double[] skews2 = featurization.skewnessQuats(quat, means2, stdvs2);
		double[] kurs2 = featurization.kurtosisQuats(quat, means2, stdvs2);
		
		double fq1 = means2[0]; double fq2 = means2[1]; double fq3 = means2[2]; double fq4 = means2[3];   
		double fq5 = stdvs2[0]; double fq6 = stdvs2[1]; double fq7 = stdvs2[2]; double fq8 = stdvs2[3];
		double fq9 = skews2[0]; double fq10 = skews2[1]; double fq11 = skews2[2]; double fq12 = skews2[3];
		double fq13 = kurs2[0]; double fq14 = kurs2[1]; double fq15 = kurs2[2]; double fq16 = kurs2[3];
		
		//feature on angles
		double[] means3 = featurization.meanAxes(ang);
		double[] stdvs3 = featurization.stdvAxes(ang, means3);
		double[] skews3 = featurization.skewnessAxes(ang, means3, stdvs3);
		double[] kurs3 = featurization.kurtosisAxes(ang, means3, stdvs3);
		
		double fa1 = means3[0]; double fa2 = means3[1]; double fa3 = means3[2];   
		double fa4 = stdvs3[0]; double fa5 = stdvs3[1]; double fa6 = stdvs3[2]; 
		double fa7 = skews3[0]; double fa8 = skews3[1]; double fa9 = skews3[2]; 
		double fa10 = kurs3[0]; double fa11 = kurs3[1]; double fa12 = kurs3[2]; 
		
		double[] features = new double[]{1.0, ft2, diffPeak.y, diffPeak.z, 
				f2, f5, f6, f3, f8, f9, ft3, f11, f12,
				ft5, ft6, ft8, ft9, ft11, ft12, fq1, fq2, fq3, fq4, fq5, fq6, fq7, fq8, fq9, 
				fq10,freqY[1],freqY[2],freqY[3],fq11,fq12,fq13,fq14,fq15,fq16,featurization.filter_pos.y,featurization.filter_pos.z,
				freqZ[1],freqZ[2],freqZ[3],fa1,fa2,fa3,fa4,fa5,fa6,fa7,fa8,fa9,fa10,fa11,fa12};
		
		//need to be scaled
		for(int itrf = 1; itrf < features.length; itrf++)
		{
			features[itrf] =  scaleOutput(features[itrf], itrf);
		}
		
		return features;
	}
	
	
	public double[] calculateFeatures_ChoppingBoard(ArrayList<Vector3> ac, ArrayList<Vector3> aroundAxisAngles)
	{
		double[] means = featurization.meanAxes(ac);  //not using means cause the offset problem
		
		//stdv
		double[] stdvs = featurization.stdvAxes(ac, means);
		
		//skewness
		double[] skews = featurization.skewnessAxes(ac, means, stdvs);
		
		//kurtosis
		double[] kurs = featurization.kurtosisAxes(ac, means, stdvs);
		
		//diff peak
		Vector3 diffPeaks = featurization.largestNeighbourAbsDiff(ac);  //might be most important feature
		
		featurization.getDisplacement(ac);
		
		//frequency
		double[][] freqs = featurization.freq(ac);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//features on around x angles
		double[] means3 = featurization.meanAxes(aroundAxisAngles);
		double[] stdvs3 = featurization.stdvAxes(aroundAxisAngles, means3);
		double[] skews3 = featurization.skewnessAxes(aroundAxisAngles, means3, stdvs3);
		double[] kurs3 = featurization.kurtosisAxes(aroundAxisAngles, means3, stdvs3);
		
		double[] features = new double[]{1.0, 
				stdvs[0], stdvs[1], stdvs[2], skews[0], skews[1], skews[2], kurs[0], kurs[1], kurs[2], diffPeaks.x, diffPeaks.y, diffPeaks.z, //12
				
				//features on around y
				means3[1], stdvs3[1], skews3[1], kurs3[1],   //4
				
				featurization.filter_velocity.x, featurization.filter_velocity.y, featurization.filter_velocity.z, 
				
				freqX[1], freqX[2], freqX[3], freqX[4],freqX[5], freqX[6], freqX[7], freqX[8], freqX[9], freqX[10], 
				freqX[11], freqX[12], freqX[13], freqX[14], freqX[15], freqX[16], freqX[17], freqX[18], freqX[19], freqX[20],
				freqX[21],freqX[22],freqX[23],freqX[24],freqX[25],freqX[26],freqX[27],freqX[28],freqX[29],freqX[30],freqX[31],
				
				freqY[1], freqY[2], freqY[3], freqY[4],freqY[5], freqY[6], freqY[7], freqY[8], freqY[9], freqY[10], 
				freqY[11], freqY[12], freqY[13], freqY[14], freqY[15], freqY[16], freqY[17], freqY[18], freqY[19], freqY[20],
				freqY[21],freqY[22],freqY[23],freqY[24],freqY[25],freqY[26],freqY[27],freqY[28],freqY[29],freqY[30],freqY[31],
				
				freqZ[1], freqZ[2], freqZ[3], freqZ[4],freqZ[5], freqZ[6], freqZ[7], freqZ[8], freqZ[9], freqZ[10], 
				freqZ[11], freqZ[12], freqZ[13], freqZ[14], freqZ[15], freqZ[16], freqZ[17], freqZ[18], freqZ[19], freqZ[20],
				freqZ[21],freqZ[22],freqZ[23],freqZ[24],freqZ[25],freqZ[26],freqZ[27],freqZ[28],freqZ[29],freqZ[30],freqZ[31]  //3 * 31
				
		};
		
		//need to be scaled
		for(int itrf = 1; itrf < features.length; itrf++)
		{
			features[itrf] =  scaleOutput(features[itrf], itrf);
		}
		
		return features;
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
	
	public double[] calculateFeatures(ArrayList<Vector3> ac)
	{
		ArrayList<Vector3> absAc = ac;//featurization.toAbsList(ac);
		
		double[] means1 = featurization.meanAxes(absAc);
		double f1 = means1[0];
		double f2 = means1[1];
		double f3 = means1[2];
		
		//mean and std
		double[] mr = featurization.mean_std(f1, f2, f3);
		double ff1 = mr[0];
		double ff2 = mr[1];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = featurization.stdvAxes(absAc, means1);
		double f4 = stdvs1[0];
		double f5 = stdvs1[1];
		double f6 = stdvs1[2];
		
		//mean and std
		double[] sr = featurization.mean_std(f4, f5, f6);
		double ff3 = sr[0];
		double ff4 = sr[1];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = featurization.skewnessAxes(absAc, means1, stdvs1);
		double f7 = skews1[0];
		double f8 = skews1[1];
		double f9 = skews1[2];
		
		//mean and std
		double[] skr = featurization.mean_std(f7, f8, f9);
		double ff5 = skr[0];
		double ff6 = skr[1];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = featurization.kurtosisAxes(absAc, means1, stdvs1);
		double f10 = kurs1[0];
		double f11 = kurs1[1];
		double f12 = kurs1[2];
		
		//mean and std
		double[] kur = featurization.mean_std(f10, f11, f12);
		double ff7 = kur[0];
		double ff8 = kur[1];
		
		featurization.getDisplacement(absAc);
		double ff9 = featurization.pos.x;
		double ff10 = featurization.pos.y;
		double ff11 = featurization.pos.z;
		
		double[][] freqs = featurization.freq(ac);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		double[] features = new double[]{1.0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12,
				freqX[0],freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
				freqY[0],freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
				freqZ[0],freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
				ff1, ff2, ff3, ff4, ff5, ff6, ff7, ff8, ff9, ff10, ff11};
		
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
