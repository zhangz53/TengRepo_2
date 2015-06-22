package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.teng.fft.RealDoubleFFT;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class Featurization {

	public BufferedReader br;
	public String line = "";
	private String splitBy = ",";
	
	//data set per sample
	public ArrayList<Vector3> acc1;
	public ArrayList<Vector3> acc2;
	
	private String dataFile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\1435009415360_testfilter.csv";
	private int index = 1;  //start from 1
	
	//for fft
	public int fftBins = 32;  //try to change
	public int Fs = 66;  //about 66hz
	public double Ts = 1.0/Fs;  //about 0.015s
	public RealDoubleFFT mRealFFT;
	public double scale;
	private final static float MEAN_MAX = 16384f;   // Maximum signal value
	
	public DataStorage dataStorage;
	
	public Featurization()
	{
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
		
		mRealFFT = new RealDoubleFFT(fftBins);
		scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
		
		dataStorage = DataStorage.getInstance();
	}
	
	public Featurization(int purpose)
	{
		//to be used by other class
		mRealFFT = new RealDoubleFFT(fftBins);
		scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
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
					//System.out.println("" + sIndexDouble);
					int sIndex = (int)sIndexDouble;
					if(sIndex == index)
					{
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						
					}else
					{
						//all the sample for index collected, find the features
						//calculateFeatures(acc1, acc2);
						calculateFeatures(acc2);
						
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
			//calculateFeatures(acc1, acc2);
			calculateFeatures(acc2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataStorage.saves();
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
		
		DataStorage.AddSampleX(-1.0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, 
				f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, 
				f21, f22, f23, f24, f25, f26, f27, f28, f29, f30, 
				f31);
	}
	
	//features for single acc
	public void calculateFeatures(ArrayList<Vector3> ac)
	{
		//largest peak and their absolute values
		//int[] peakIndex = localPeakIndex(ac);
		
		//double f1 = ac.get(peakIndex[0]).x;
		//double f2 = ac.get(peakIndex[0]).y;
		//double f3 = ac.get(peakIndex[0]).z;
		
		//double f4 = ac.get(peakIndex[1]).x;
		//double f5 = ac.get(peakIndex[1]).y;
		//double f6 = ac.get(peakIndex[1]).z;
		
		////////////////////////brute force features
		
		double[] means1 = meanAxes(ac);
		double f1 = means1[0];
		double f2 = means1[1];
		double f3 = means1[2];
		
		//mean and std
		double[] mr = mean_std(f1, f2, f3);
		double ff1 = mr[0];
		double ff2 = mr[1];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = stdvAxes(ac, means1);
		double f4 = stdvs1[0];
		double f5 = stdvs1[1];
		double f6 = stdvs1[2];
		
		//mean and std
		double[] sr = mean_std(f4, f5, f6);
		double ff3 = sr[0];
		double ff4 = sr[1];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = skewnessAxes(ac, means1, stdvs1);
		double f7 = skews1[0];
		double f8 = skews1[1];
		double f9 = skews1[2];
		
		//mean and std
		double[] skr = mean_std(f7, f8, f9);
		double ff5 = skr[0];
		double ff6 = skr[1];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = kurtosisAxes(ac, means1, stdvs1);
		double f10 = kurs1[0];
		double f11 = kurs1[1];
		double f12 = kurs1[2];
		
		//mean and std
		double[] kur = mean_std(f10, f11, f12);
		double ff7 = kur[0];
		double ff8 = kur[1];
		
		double[][] freqs = freq(ac);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//mean and std
		double[] fmeans = new double[fftBins/2];
		double[] fstds = new double[fftBins/2];
		
		for(int itrf = 0; itrf<(fftBins/2); itrf++)
		{
			double[] fr = mean_std(freqX[itrf], freqY[itrf], freqZ[itrf]);
			fmeans[itrf] = fr[0];
			fstds[itrf] = fr[1];
		}
		
		//1 + 12 + 16*3 + 8 + 16*2 = 1 + 60 + 40
		DataStorage.AddSampleS(4.0, 
				f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12,
				freqX[0], freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
				freqY[0], freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
				freqZ[0], freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
				ff1, ff2, ff3, ff4, ff5, ff6, ff7, ff8,
				fmeans[0], fstds[0], fmeans[1], fstds[1],fmeans[2], fstds[2],fmeans[3], fstds[3],fmeans[4], fstds[4],fmeans[5], fstds[5],
				fmeans[6], fstds[6],fmeans[7], fstds[7],fmeans[8], fstds[8],fmeans[9], fstds[9],fmeans[10], fstds[10],fmeans[11], fstds[11],
				fmeans[12], fstds[12],fmeans[13], fstds[13],fmeans[14], fstds[14],fmeans[15], fstds[15]
				);
	
	}
	
	
	public double[][] freq(ArrayList<Vector3> ac)  //size of ac should equal to fftBins
	{
		double[][] result = new double[3][fftBins/2];  //get fftbins samples
		if(ac.size() != fftBins)
		{
			return result;
		}
		
		double[] fftDataX = new double[fftBins];
		double[] fftDataY = new double[fftBins];
		double[] fftDataZ = new double[fftBins];
		
		for(int itra = 0; itra < fftBins; itra++)
		{
			fftDataX[itra] = ac.get(itra).x;
			fftDataY[itra] = ac.get(itra).y;
			fftDataZ[itra] = ac.get(itra).z;
		}
		
		mRealFFT.ft(fftDataX);
		mRealFFT.ft(fftDataY);
		mRealFFT.ft(fftDataZ);
		
		//convert to db
		convertToDb(fftDataX, scale);
		convertToDb(fftDataY, scale);
		convertToDb(fftDataZ, scale);
		
		double[] resultX = new double[fftBins/2];
		double[] resultY = new double[fftBins/2];
		double[] resultZ = new double[fftBins/2];
		
		for(int itr = 0; itr < fftBins/2; itr++)
		{
			resultX[itr] = fftDataX[itr];
			resultY[itr] = fftDataY[itr];
			resultZ[itr] = fftDataZ[itr];
		}
		
		result[0] = resultX;
		result[1] = resultY;
		result[2] = resultZ;
		
		return result;
		
	}
	
	public double[] convertToDb(double[] data, double maxSquared) {
	    data[0] = db2(data[0], 0.0, maxSquared);
	    int j = 1;
	    for (int i=1; i < data.length - 1; i+=2, j++) {
	      data[j] = db2(data[i], data[i+1], maxSquared);
	    }
	    data[j] = data[0];
	    return data;
	}
	
	private double db2(double r, double i, double maxSquared) {
	    return 5.0 * Math.log10((r * r + i * i) / maxSquared);
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
		
		//check pool
		if(candyValue.size() < 2)
		{
			int[] result = new int[2];
			result[0] = 0;
			result[1] = 1;
			return result;
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
	
	
	public ArrayList<Double> localPeakValues(ArrayList<Vector3> list, int count){
		//get top count + 3 peaks
		//remove the largest
		//remove the most left and most right
		
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
		
		ArrayList<Double> results = new ArrayList<Double>();
		ArrayList<Integer> resultIndex = new ArrayList<Integer>();
		
		for(int itrc = 0; itrc < (count + 3); itrc++)
		{
			results.add(0.0);
			resultIndex.add(0);
		}
		
		if(peakCandy.size() < (count + 3))
			return results;
		
		for(int itrr = 0; itrr < (count + 3); itrr++)
		{
			int index = getLargestInList(candyValue);
			int oriIndex = peakCandy.get(index);
			
			results.set(itrr, candyValue.get(index));
			resultIndex.set(itrr, oriIndex);
			
			candyValue.remove(index);
			peakCandy.remove(index);
		}
		
		//remove the largest
		results.remove(0);
		resultIndex.remove(0);
		
		//remove the most left
		int leftIndex = getSmallestIntegerInList(resultIndex);
		results.remove(leftIndex);
		resultIndex.remove(leftIndex);
		
		//remove the most right
		int rightIndex = getLargestIntegerInList(resultIndex);
		results.remove(rightIndex);
		resultIndex.remove(rightIndex);
		
		return results;
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
	
	
	public int getLargestIntegerInList(ArrayList<Integer> list)   // > 0
	{
		int temp = 0;
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
	
	public int getSmallestIntegerInList(ArrayList<Integer> list)
	{
		int temp = getLargestIntegerInList(list);
		int target = 0;
		
		for(int itrl = 0; itrl < list.size(); itrl++)
		{
			if(list.get(itrl) < temp)
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
	
	public double[] mean_std(double a1, double a2, double a3)
	{
		double[] result = new double[2];
		double mean = (a1 + a2 + a3)/ 3;
		result[0] = mean;
		if(mean != 0){
			double std = Math.sqrt(((a1- mean) * (a1 - mean) + (a2 - mean) * (a2 - mean) + (a3- mean) * (a3 - mean))/3 );
			result[1] = std;
		}else
		{
			result[1] = 0.0;
		}
		
		return result;
		
	}
	
	
	public static final void main(String args[])
	{
		Featurization fea = new Featurization();
		fea.getFeatures();
		
	}
}
