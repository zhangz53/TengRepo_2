package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class Featurization {

	public BufferedReader br;
	public String line = "";
	private String splitBy = ",";
	
	//data set per sample
	public ArrayList<Vector3> acc1;
	public ArrayList<Vector3> acc2;
	
	private String dataFile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\1432324376884_preprocess_sample.csv";
	private int index = 1;  //start from 1
	
	public DataStorage dataStorage;
	
	public Featurization()
	{
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
		
		dataStorage = DataStorage.getInstance();
	}
	
	public Featurization(int purpose)
	{
		//to be used by other class
	}
	
	public void getFeatures()
	{
		try {
			br = new BufferedReader(new FileReader(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				if(values.length == 13)
				{
					double sIndexDouble = Double.parseDouble(values[0]);
					int sIndex = (int)sIndexDouble;
					if(sIndex == index)
					{
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						
					}else
					{
						//all the sample for index collected, find the features
						calculateFeatures(acc1, acc2);
						
						//clear the acc and start for index+1
						acc1.clear();
						acc2.clear();
						index++;
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
					}
				}
				
			}
			
			//the last one
			calculateFeatures(acc1, acc2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataStorage.savex();
	}
	
	//calculate all the needed features and storage them
	public void calculateFeatures(ArrayList<Vector3> ac1, ArrayList<Vector3> ac2)
	{
		//ac1 local peaks (1st and 2nd)
		int[] peakIndex = localPeakIndex(ac1);
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
		double[] means1 = meanAxes(ac1);
		double f8 = means1[0];
		double f9 = means1[1];
		double f10 = means1[2];
		
		double[] means2 = meanAxes(ac2);
		double f11 = means2[0];
		double f12 = means2[1];
		double f13 = means2[2];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = stdvAxes(ac1, means1);
		double f14 = stdvs1[0];
		double f15 = stdvs1[1];
		double f16 = stdvs1[2];
		
		double[] stdvs2 = stdvAxes(ac2, means2);
		double f17 = stdvs2[0];
		double f18 = stdvs2[1];
		double f19 = stdvs2[2];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = skewnessAxes(ac1, means1, stdvs1);
		double f20 = skews1[0];
		double f21 = skews1[1];
		double f22 = skews1[2];
		
		double[] skews2 = skewnessAxes(ac2, means2, stdvs2);
		double f23 = skews2[0];
		double f24 = skews2[1];
		double f25 = skews2[2];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = kurtosisAxes(ac1, means1, stdvs1);
		double f26 = kurs1[0];
		double f27 = kurs1[1];
		double f28 = kurs1[2];
		
		double[] kurs2 = kurtosisAxes(ac2, means2, stdvs2);
		double f29 = kurs2[0];
		double f30 = kurs2[1];
		double f31 = kurs2[2];
		
		DataStorage.AddSampleX(1.0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, 
				f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, 
				f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, 
				f31);
	}
	
	public int[] localPeakIndex(ArrayList<Vector3> list){
		
		//get the 1st and 2nd peak index
		int sz = list.size();
		ArrayList<Double> absValue = new ArrayList<Double>();
		for(Vector3 acc : list){
			double sum = Math.abs(acc.x) + Math.abs(acc.y) + Math.abs(acc.z);
			absValue.add(sum);
		}
		
		//difference values
		ArrayList<Double> diff = new ArrayList<Double>();
		diff.add(0.0);
		for(int itra = 1; itra < absValue.size(); itra++)
		{
			diff.add(absValue.get(itra) - absValue.get(itra-1));
		}
		
		ArrayList<Integer> peakCandy = new ArrayList<Integer>();
		ArrayList<Double> candyValue = new ArrayList<Double>();
		for(int itrd = 0; itrd < (diff.size() -1 ); itrd++)
		{
			if(diff.get(itrd) > 0 && diff.get(itrd+1) < 0)
			{
				peakCandy.add(itrd);
				candyValue.add(absValue.get(itrd));
			}
		}
		
		//rand candyValue and the 1st and 2nd largest
		int index = getLargestInList(candyValue);
		int firstIndex = peakCandy.get(index);
		
		candyValue.remove(index);
		peakCandy.remove(index);
		
		index = getLargestInList(candyValue);
		int secondIndex = peakCandy.get(index);
		
		int[] result = new int[2];
		result[0] = firstIndex;
		result[1] = secondIndex;
		
		return result;
	}
	
	public int getLargestInList(ArrayList<Double> list)
	{
		double temp = 0.0;
		int target = 0;
		
		for(int itrl = 0; itrl < list.size(); itrl++)
		{
			if(list.get(itrl) > temp)
			{
				target = itrl;
				temp = list.get(itrl);
			}
		}
		
		return target;
	}
	
	public double[] meanAxes(ArrayList<Vector3> list)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int sz = list.size();
		for(Vector3 acc : list)
		{
			axeValues[0] += acc.x;
			axeValues[1] += acc.y;
			axeValues[2] += acc.z;
		}
		
		axeValues[0] = axeValues[0] / sz;
		axeValues[1] = axeValues[1] / sz;
		axeValues[2] = axeValues[2] / sz;
		
		return axeValues;
	}
	
	public double[] stdvAxes(ArrayList<Vector3> list, double[] mean)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int sz = list.size();
		for(Vector3 acc : list)
		{
			axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]));
			axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]));
			axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]));
		}
		
		axeValues[0] = Math.sqrt( axeValues[0] / sz);
		axeValues[1] = Math.sqrt( axeValues[1] / sz);
		axeValues[2] = Math.sqrt( axeValues[2] / sz);
		
		return axeValues;
	}
	
	public double[] skewnessAxes(ArrayList<Vector3> list, double[] mean, double[] stdv)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int sz = list.size();
		for(Vector3 acc : list)
		{
			axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]));
			axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]));
			axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]));
		}
		
		axeValues[0] = (axeValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0]);
		axeValues[1] = (axeValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1]);
		axeValues[2] = (axeValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2]);
	
		//double para = Math.sqrt(sz * (sz - 1)) / (sz - 1);
		//axeValues[0] = axeValues[0] * para;
		//axeValues[1] = axeValues[1] * para;
		//axeValues[2] = axeValues[2] * para;
		
		return axeValues;
	}
	
	public double[] kurtosisAxes(ArrayList<Vector3> list, double[] mean, double[] stdv)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int sz = list.size();
		for(Vector3 acc : list)
		{
			axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]));
			axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]) * (acc.x - mean[1]));
			axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]) * (acc.x - mean[2]));
		}
		
		axeValues[0] = (axeValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0] * stdv[0]) - 3.0;
		axeValues[1] = (axeValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1] * stdv[0]) - 3.0;
		axeValues[2] = (axeValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2] * stdv[0]) - 3.0;
		
		return axeValues;
	}
	
	
	
	public static final void main(String args[])
	{
		Featurization fea = new Featurization();
		fea.getFeatures();
		
	}
}
