package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.teng.math.Vector3;

public class Featurization {

	public BufferedReader br;
	public String line = "";
	private String splitBy = ",";
	
	//data set per sample
	public ArrayList<Vector3> acc1;
	public ArrayList<Vector3> acc2;
	
	private String dataFile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\1432240953238_preprocess_sample.csv";
	private int index = 1;  //start from 1
	
	public Featurization()
	{
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
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
		
	}
	
	//calculate all the needed features and storage them
	public void calculateFeatures(ArrayList<Vector3> ac1, ArrayList<Vector3> ac2)
	{
		//ac1 local peaks (1st and 2nd)
		int[] peakIndex = localPeakIndex(ac1);
		System.out.println("local peak index: " + peakIndex[0] + ",  and   " + peakIndex[1]);
		
		//feature 1: 1st peak is behind the 2nd peak, and dominate values on axis are inversed
		double f1 = 0.0;
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
		
		
		//feature 14-19: standard dev of acc1 and acc2
		
		
		
	}
	
	private int[] localPeakIndex(ArrayList<Vector3> list){
		
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
	
	private int getLargestInList(ArrayList<Double> list)
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
	
	private double[] meanAxes(ArrayList<Vector3> list)
	{
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static final void main(String args[])
	{
		Featurization fea = new Featurization();
		fea.getFeatures();
		
	}
}
