package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.teng.fft.RealDoubleFFT;
import com.teng.filter.ButterWorth;
import com.teng.math.Matrix4;
import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

public class Featurization {

	public BufferedReader br;
	public String line = "";
	private String splitBy = ",";
	
	//data set per sample
	public ArrayList<Vector3> acc1;
	public ArrayList<Vector3> acc2;
	public ArrayList<Quaternion> quats;
	public ArrayList<Vector3> aroundAxisAngles;
	public Vector3 xAxis;
	public Vector3 yAxis;
	public Vector3 zAxis;
	
	private String dataFile;// = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\1\\tg0pro.csv";
	private int directionIndex = 1; //1:vertical, 0:horizontal
	private double rLabel; // = 0.0;
	private int accIndex = 2;
	
	private int index = 1;  //start from 1
	
	//for fft
	public int fftBins = 64;///////8;  //try to change
	public int Fs = 100;  //about 66hz
	public double Ts = 1.0/Fs;  //about 0.015s
	public RealDoubleFFT mRealFFT;
	public double scale;
	private final static float MEAN_MAX = 16384f;   // Maximum signal value
	
	//for position translation
	public Vector3 pos;
	public Vector3 velocity;
	public Vector3 linAcc;
	public Vector3 filteredAcc;
	public Vector3 filter_velocity;
	public Vector3 filter_pos;
	public Matrix4 mMatrix;
	public static double stamp = 0.01;  //in seconds
	
	//for filter
	public ButterWorth mButterHp;
	public ButterWorth mButterLp;
	
	public DataStorage dataStorage;
	
	public Featurization()
	{
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
		quats = new ArrayList<Quaternion>();
		aroundAxisAngles = new ArrayList<Vector3>();
		xAxis = new Vector3(1.0, 0, 0);
		yAxis = new Vector3(0.0, 1.0, 0);
		zAxis = new Vector3(0.0, 0.0, 1.0);
		
		pos = new Vector3();
		velocity = new Vector3();
		linAcc = new Vector3();
		filteredAcc = new Vector3();
		filter_velocity = new Vector3();
		filter_pos = new Vector3();
		mMatrix = new Matrix4();		
		
		mButterHp = new ButterWorth(ButterWorth.BandType.high);
		mButterHp.createDataSet(); 
		mButterHp.createDataSet(); 
		mButterLp = new ButterWorth(ButterWorth.BandType.low);
		mButterLp.createDataSet();
		
		mRealFFT = new RealDoubleFFT(fftBins);
		scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
		
		dataStorage = DataStorage.getInstance();
	}
	
	public Featurization(String fileName, int fileIndex, String saveFilename)
	{
		
		dataFile = fileName;
		rLabel = fileIndex;
		
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
		xAxis = new Vector3(1.0, 0, 0);
		yAxis = new Vector3(0.0, 1.0, 0);
		zAxis = new Vector3(0.0, 0.0, 1.0);
		aroundAxisAngles = new ArrayList<Vector3>();
		
		mRealFFT = new RealDoubleFFT(fftBins);
		scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
		
		mButterHp = new ButterWorth(ButterWorth.BandType.high);
		mButterHp.createDataSet(); 
		mButterHp.createDataSet(); 
		pos = new Vector3();
		velocity = new Vector3();
		linAcc = new Vector3();
		filteredAcc = new Vector3();
		filter_velocity = new Vector3();
		filter_pos = new Vector3();
		mMatrix = new Matrix4();
		
		dataStorage = DataStorage.getInstance();
		dataStorage.clearData();
		
	}
	
	public Featurization(int purpose)
	{
		//to be used by other class
		mRealFFT = new RealDoubleFFT(fftBins);
		scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
		
		xAxis = new Vector3(1.0, 0, 0);
		yAxis = new Vector3(0.0, 1.0, 0);
		zAxis = new Vector3(0.0, 0.0, 1.0);
		
		pos = new Vector3();
		velocity = new Vector3();
		linAcc = new Vector3();
		filteredAcc = new Vector3();
		filter_velocity = new Vector3();
		filter_pos = new Vector3();
		mMatrix = new Matrix4();		
		
		mButterHp = new ButterWorth(ButterWorth.BandType.high);
		mButterHp.createDataSet(); 
		mButterHp.createDataSet(); 
		mButterLp = new ButterWorth(ButterWorth.BandType.low);
		mButterLp.createDataSet();
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
						
						/*
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						
						//for swipe bumps
						//acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						//quats.add(new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10])));
						
						//for swipe start and end
						Quaternion tempQuat = new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10]));
						quats.add(tempQuat);
						
						Vector3 tempAcc = new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6]));
						double aroundXRad_Acc2 = tempQuat.getAngleAroundRad(xAxis);
						double aroundYRad_Acc2 = tempQuat.getAngleAroundRad(yAxis);
						double aroundZRad_Acc2 = tempQuat.getAngleAroundRad(zAxis);
						
    					double alongMovementAcc = tempAcc.y * Math.cos(aroundXRad_Acc2) + tempAcc.z * Math.sin(aroundXRad_Acc2);
    					double orthoMovementAcc = -tempAcc.y * Math.sin(aroundXRad_Acc2) + tempAcc.z * Math.cos(aroundXRad_Acc2);
    					aroundAxisAngles.add(new Vector3(aroundXRad_Acc2, aroundYRad_Acc2, aroundZRad_Acc2));
    					acc2.add(new Vector3(tempAcc.x, alongMovementAcc, orthoMovementAcc));
						*/
						
						//for height
						//acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						
						
						//for chopping board
						Vector3 tempAcc = new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
						
						Quaternion tempQuat = new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10]));
						double aroundXRad = tempQuat.getAngleAroundRad(xAxis);
						double aroundYRad = tempQuat.getAngleAroundRad(yAxis);
						double aroundZRad = tempQuat.getAngleAroundRad(zAxis);
						
						//when chopping on board, mainly rotation around Y, having effect on z and x
						double fixedX = tempAcc.x * Math.cos(aroundYRad) + tempAcc.z * Math.sin(aroundYRad);
						double fixedZ = -tempAcc.x * Math.sin(aroundYRad) + tempAcc.z  * Math.cos(aroundYRad);
						double fixedY = tempAcc.y;
						
						acc1.add(new Vector3(fixedX, fixedY, fixedZ));
						aroundAxisAngles.add(new Vector3(aroundXRad, aroundYRad, aroundZRad));
					
						
					}else
					{
						//all the sample for index collected, find the features
						//calculateFeatures(acc1, acc2);
						//highPassFilter(acc2);
						
						/*
						if(accIndex == 1){
							//calculateFeatures(acc1, quats);
							//calculateFeatures_Start_End(acc1, quats, aroundAxisAngles);
							calculateFeatures_height(acc1);
						}else if(accIndex == 2)
						{
							//calculateFeatures(acc2, quats);
							//calculateFeatures_Start_End(acc2, quats, aroundAxisAngles);
							calculateFeatures_height(acc2);						
						}*/
						
						calculateFeatures_cutting_board(acc1, aroundAxisAngles);
						
						//clear the acc and start for index+1
						acc1.clear();
						acc2.clear();
						//quats.clear();
						aroundAxisAngles.clear();
						index++;
						
						
						/*
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						//acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						//quats.add(new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10])));
						
						Quaternion tempQuat = new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10]));
						quats.add(tempQuat);
						
						Vector3 tempAcc = new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6]));
						double aroundXRad_Acc2 = tempQuat.getAngleAroundRad(xAxis);
						double aroundYRad_Acc2 = tempQuat.getAngleAroundRad(yAxis);
						double aroundZRad_Acc2 = tempQuat.getAngleAroundRad(zAxis);
						
    					double alongMovementAcc = tempAcc.y * Math.cos(aroundXRad_Acc2) + tempAcc.z * Math.sin(aroundXRad_Acc2);
    					double orthoMovementAcc = -tempAcc.y * Math.sin(aroundXRad_Acc2) + tempAcc.z * Math.cos(aroundXRad_Acc2);
    					aroundAxisAngles.add(new Vector3(aroundXRad_Acc2, aroundYRad_Acc2, aroundZRad_Acc2));
    					acc2.add(new Vector3(tempAcc.x, alongMovementAcc, orthoMovementAcc));
    					*/
						
						//acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						
						
						//for chopping board
						Vector3 tempAcc = new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
						
						Quaternion tempQuat = new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]), Double.parseDouble(values[10]));
						double aroundXRad = tempQuat.getAngleAroundRad(xAxis);
						double aroundYRad = tempQuat.getAngleAroundRad(yAxis);
						double aroundZRad = tempQuat.getAngleAroundRad(zAxis);
						
						//when chopping on board, mainly rotation around Y, having effect on z and x
						double fixedX = tempAcc.x * Math.cos(aroundYRad) + tempAcc.z * Math.sin(aroundYRad);
						double fixedZ = -tempAcc.x * Math.sin(aroundYRad) + tempAcc.z  * Math.cos(aroundYRad);
						double fixedY = tempAcc.y;
						
						acc1.add(new Vector3(fixedX, fixedY, fixedZ));
						aroundAxisAngles.add(new Vector3(aroundXRad, aroundYRad, aroundZRad));
					}
				}
				
			}
			
			//the last one
			//calculateFeatures(acc1, acc2);
			//highPassFilter(acc2);
			
			/*
			
			if(accIndex == 1){
				//calculateFeatures(acc1, quats);
				//calculateFeatures_Start_End(acc1, quats, aroundAxisAngles);
				calculateFeatures_height(acc1);
			}else if(accIndex == 2)
			{
				//calculateFeatures(acc2, quats);
				//calculateFeatures_Start_End(acc2, quats, aroundAxisAngles);
				calculateFeatures_height(acc2);
			}
			*/
			
			calculateFeatures_cutting_board(acc1, aroundAxisAngles);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataStorage.savess();
	}
	
	/*
	//high pass filter the data
	public void highPassFilter(ArrayList<Vector3> ac)
	{
		ArrayList<Vector3> temp = new ArrayList<Vector3>();
		int sz = ac.size();
		for(int i = 0; i < sz; i++)
		{
			temp.add(ac.get(i));
		}
		
		for(int itrt = 0; itrt < sz; itrt++)
		{
			Vector3 fResult = new Vector3();
			fResult.Set(mButterHp.applyButterWorth(1, 1, temp.get(itrt)));
			ac.set(itrt, fResult);
		}
		
	}*/
	
	/*
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
	*/
	
	//for study height
	public void calculateFeatures_height(ArrayList<Vector3> ac)
	{
		double[] means = meanAxes(ac);  //not using means cause the offset problem
		
		//stdv
		double[] stdvs = stdvAxes(ac, means);
		
		//skewness
		double[] skews = skewnessAxes(ac, means, stdvs);
		
		//kurtosis
		double[] kurs = kurtosisAxes(ac, means, stdvs);
		
		//diff peak
		Vector3 diffPeaks = largestNeighbourAbsDiff(ac);  //might be most important feature
		
		//largest values?
		
		//frequency
		double[][] freqs = freq(ac);   //3 by fftBins/2 array
		//feature X frequencies
		///double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//save data
		DataStorage.AddSampleS(rLabel, 
				stdvs[1], stdvs[2], skews[1], skews[2], kurs[1], kurs[2], diffPeaks.y, diffPeaks.z, 
				
				freqY[1], freqY[2], freqY[3], freqY[4],freqY[5], freqY[6], freqY[7], freqY[8], freqY[9], freqY[10], 
				freqY[11], freqY[12], freqY[13], freqY[14], freqY[15], freqY[16], freqY[17], freqY[18], freqY[19], freqY[20],
				freqY[21],freqY[22],freqY[23],freqY[24],freqY[25],freqY[26],freqY[27],freqY[28],freqY[29],freqY[30],freqY[31],
				
				freqZ[1], freqZ[2], freqZ[3], freqZ[4],freqZ[5], freqZ[6], freqZ[7], freqZ[8], freqZ[9], freqZ[10], 
				freqZ[11], freqZ[12], freqZ[13], freqZ[14], freqZ[15], freqZ[16], freqZ[17], freqZ[18], freqZ[19], freqZ[20],
				freqZ[21],freqZ[22],freqZ[23],freqZ[24],freqZ[25],freqZ[26],freqZ[27],freqZ[28],freqZ[29],freqZ[30],freqZ[31],
				
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0,0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0,0.0, 0.0
				);
		
	}
	
	//for demo chopping board
	public void calculateFeatures_cutting_board(ArrayList<Vector3> ac, ArrayList<Vector3> aroundAxisAngles)
	{
		double[] means = meanAxes(ac);  //not using means cause the offset problem
		
		//stdv
		double[] stdvs = stdvAxes(ac, means);
		
		//skewness
		double[] skews = skewnessAxes(ac, means, stdvs);
		
		//kurtosis
		double[] kurs = kurtosisAxes(ac, means, stdvs);
		
		//diff peak
		Vector3 diffPeaks = largestNeighbourAbsDiff(ac);  //might be most important feature
		
		//largest values?
		
		//velocity values
		getDisplacement(ac);
		
		
		//frequency
		double[][] freqs = freq(ac);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//features on around x angles
		double[] means3 = meanAxes(aroundAxisAngles);
		double[] stdvs3 = stdvAxes(aroundAxisAngles, means3);
		double[] skews3 = skewnessAxes(aroundAxisAngles, means3, stdvs3);
		double[] kurs3 = kurtosisAxes(aroundAxisAngles, means3, stdvs3);
		
		//save data
		DataStorage.AddSampleSS(rLabel, 
				stdvs[0], stdvs[1], stdvs[2], skews[0], skews[1], skews[2], kurs[0], kurs[1], kurs[2], diffPeaks.x, diffPeaks.y, diffPeaks.z, //12
				
				//features on around y
				means3[1], stdvs3[1], skews3[1], kurs3[1],   //4
				
				//velocity features, to tell direction
				filter_velocity.x, filter_velocity.y, filter_velocity.z,    //3
				
				freqX[1], freqX[2], freqX[3], freqX[4],freqX[5], freqX[6], freqX[7], freqX[8], freqX[9], freqX[10], 
				freqX[11], freqX[12], freqX[13], freqX[14], freqX[15], freqX[16], freqX[17], freqX[18], freqX[19], freqX[20],
				freqX[21],freqX[22],freqX[23],freqX[24],freqX[25],freqX[26],freqX[27],freqX[28],freqX[29],freqX[30],freqX[31],
				
				freqY[1], freqY[2], freqY[3], freqY[4],freqY[5], freqY[6], freqY[7], freqY[8], freqY[9], freqY[10], 
				freqY[11], freqY[12], freqY[13], freqY[14], freqY[15], freqY[16], freqY[17], freqY[18], freqY[19], freqY[20],
				freqY[21],freqY[22],freqY[23],freqY[24],freqY[25],freqY[26],freqY[27],freqY[28],freqY[29],freqY[30],freqY[31],
				
				freqZ[1], freqZ[2], freqZ[3], freqZ[4],freqZ[5], freqZ[6], freqZ[7], freqZ[8], freqZ[9], freqZ[10], 
				freqZ[11], freqZ[12], freqZ[13], freqZ[14], freqZ[15], freqZ[16], freqZ[17], freqZ[18], freqZ[19], freqZ[20],
				freqZ[21],freqZ[22],freqZ[23],freqZ[24],freqZ[25],freqZ[26],freqZ[27],freqZ[28],freqZ[29],freqZ[30],freqZ[31]  //3 * 31
				
				);
		
	}
	
	
	
	//for start and end
	public void calculateFeatures_Start_End(ArrayList<Vector3> ac, ArrayList<Quaternion> quat, ArrayList<Vector3> aroundAxisAngs)
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
		//use absolute values
		//since there is no rules for the waves
		ArrayList<Vector3> absAc = ac;//getAroundXAxisAcc(ac, quat);//toAbsList(ac);  //no need for absolute values
		//ArrayList<Vector3> absoluteAc = toAbsList(ac);
		
		double[] means1 = meanAxes(absAc);
		double[] middles = middleAxes(absAc);
		
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
		
		double[] firstMeans = meanAxes(firstHalf);
		double[] secondMeans = meanAxes(secondHalf);
		
		//compare abs means and middles
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
		
		//mean and std
		//double[] mr = mean_std(f1, f2, f3);
		//double ff1 = mr[0];
		//double ff2 = mr[1];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = stdvAxes(absAc, means1);
		double f4 = stdvs1[0];
		double f5 = stdvs1[1];
		double f6 = stdvs1[2];
		
		//mean and std
		//double[] sr = mean_std(f4, f5, f6);
		//double ff3 = sr[0];
		//double ff4 = sr[1];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = skewnessAxes(absAc, means1, stdvs1);
		double f7 = skews1[0];
		double f8 = skews1[1];
		double f9 = skews1[2];
		
		//mean and std
		//double[] skr = mean_std(f7, f8, f9);
		//double ff5 = skr[0];
		//double ff6 = skr[1];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = kurtosisAxes(absAc, means1, stdvs1);
		double f10 = kurs1[0];
		double f11 = kurs1[1];
		double f12 = kurs1[2];
		
		//mean and std
		//double[] kur = mean_std(f10, f11, f12);
		//double ff7 = kur[0];
		//double ff8 = kur[1];
		
		//displacement, taken care of mean
		getDisplacement(ac);
		
		//diff peak
		Vector3 diffPeak = largestNeighbourAbsDiff(absAc);
		
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
		
		double[][] freqs = freq(groundAc);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//mean and std
		double[] fmeans = new double[fftBins/2];
		double[] fstds = new double[fftBins/2];
		
		//for(int itrf = 0; itrf<(fftBins/2); itrf++)
		//{
			//double[] fr = mean_std(freqX[itrf], freqY[itrf], freqZ[itrf]);
			//fmeans[itrf] = fr[0];
			//fstds[itrf] = fr[1];
		//}
		
		//features on quat
		double[] means2 = meanQuats(quat);
		double[] stdvs2 = stdvQuats(quat, means2);
		double[] skews2 = skewnessQuats(quat, means2, stdvs2);
		double[] kurs2 = kurtosisQuats(quat, means2, stdvs2);
		
		double fq1 = means2[0]; double fq2 = means2[1]; double fq3 = means2[2]; double fq4 = means2[3];   
		double fq5 = stdvs2[0]; double fq6 = stdvs2[1]; double fq7 = stdvs2[2]; double fq8 = stdvs2[3];
		double fq9 = skews2[0]; double fq10 = skews2[1]; double fq11 = skews2[2]; double fq12 = skews2[3];
		double fq13 = kurs2[0]; double fq14 = kurs2[1]; double fq15 = kurs2[2]; double fq16 = kurs2[3];
		
		//features on around x angles
		double[] means3 = meanAxes(aroundAxisAngs);
		double[] stdvs3 = stdvAxes(aroundAxisAngs, means3);
		double[] skews3 = skewnessAxes(aroundAxisAngs, means3, stdvs3);
		double[] kurs3 = kurtosisAxes(aroundAxisAngs, means3, stdvs3);
		
		double fa1 = means3[0]; double fa2 = means3[1]; double fa3 = means3[2];   
		double fa4 = stdvs3[0]; double fa5 = stdvs3[1]; double fa6 = stdvs3[2]; 
		double fa7 = skews3[0]; double fa8 = skews3[1]; double fa9 = skews3[2]; 
		double fa10 = kurs3[0]; double fa11 = kurs3[1]; double fa12 = kurs3[2]; 
		
		
		if(directionIndex == 1 && accIndex == 2 && fftBins == 8)
		{
			DataStorage.AddSampleS(rLabel, 
					ft2, diffPeak.y, diffPeak.z, 
					f2, f5, f6, f3, f8, f9, ft3, f11, f12,
					ft5, ft6, ft8, ft9, ft11, ft12, fq1, fq2, fq3, fq4, fq5, fq6, fq7, fq8, fq9, 
					fq10,freqY[1],freqY[2],freqY[3],fq11,fq12,fq13,fq14,fq15,fq16,filter_pos.y,filter_pos.z,0.0,0.0,0.0,
					0.0,freqZ[1],freqZ[2],freqZ[3],fa1,fa2,fa3,fa4,fa5,fa6,fa7,fa8,fa9,fa10,fa11,
					fa12, 0.0, 0.0,
					//average values don't help much
					0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					//32
					//pos.x, pos.y, pos.z, 
					0.0, 0.0, 0.0,
					0.0, 0.0, 0.0,
					0.0, 0.0, 0.0, 0.0,0.0, 0.0,
					0.0, 0.0, 0.0, 0.0,0.0, 0.0,0.0, 0.0, 0.0, 0.0,0.0, 0.0,
					0.0, 0.0, 0.0, 0.0,0.0, 0.0,0.0, 0.0
					);
		}
		
	}
	
	
	/*
	//features for single acc
	public void calculateFeatures(ArrayList<Vector3> ac, ArrayList<Quaternion> quat)
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
		//use absolute values
		//since there is no rules for the waves
		ArrayList<Vector3> absAc = ac;//getAroundXAxisAcc(ac, quat);//toAbsList(ac);  //no need for absolute values
		//ArrayList<Vector3> absoluteAc = toAbsList(ac);
		
		double[] means1 = meanAxes(absAc);
		double[] middles = middleAxes(absAc);
		
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
		
		double[] firstMeans = meanAxes(firstHalf);
		double[] secondMeans = meanAxes(secondHalf);
		
		//compare abs means and middles
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
		
		//mean and std
		//double[] mr = mean_std(f1, f2, f3);
		//double ff1 = mr[0];
		//double ff2 = mr[1];
		
		//feature 14-19: standard dev of acc1 and acc2
		double[] stdvs1 = stdvAxes(absAc, means1);
		double f4 = stdvs1[0];
		double f5 = stdvs1[1];
		double f6 = stdvs1[2];
		
		//mean and std
		//double[] sr = mean_std(f4, f5, f6);
		//double ff3 = sr[0];
		//double ff4 = sr[1];
		
		//feature 20-25: skewness of acc1 and acc2
		double[] skews1 = skewnessAxes(absAc, means1, stdvs1);
		double f7 = skews1[0];
		double f8 = skews1[1];
		double f9 = skews1[2];
		
		//mean and std
		//double[] skr = mean_std(f7, f8, f9);
		//double ff5 = skr[0];
		//double ff6 = skr[1];
		
		//feature 26-31: kurtosis of acc1 and acc2
		double[] kurs1 = kurtosisAxes(absAc, means1, stdvs1);
		double f10 = kurs1[0];
		double f11 = kurs1[1];
		double f12 = kurs1[2];
		
		//mean and std
		//double[] kur = mean_std(f10, f11, f12);
		//double ff7 = kur[0];
		//double ff8 = kur[1];
		
		//displacement, taken care of mean
		getDisplacement(ac);
		
		//diff peak
		Vector3 diffPeak = largestNeighbourAbsDiff(absAc);
		
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
		
		double[][] freqs = freq(groundAc);   //3 by fftBins/2 array
		//feature X frequencies
		double[] freqX = freqs[0];
		//feature Y frequencies
		double[] freqY = freqs[1];
		//feature Z frequencies
		double[] freqZ = freqs[2];
		
		//mean and std
		double[] fmeans = new double[fftBins/2];
		double[] fstds = new double[fftBins/2];
		
		//for(int itrf = 0; itrf<(fftBins/2); itrf++)
		//{
			//double[] fr = mean_std(freqX[itrf], freqY[itrf], freqZ[itrf]);
			//fmeans[itrf] = fr[0];
			//fstds[itrf] = fr[1];
		//}
		
		//features on quat
		double[] means2 = meanQuats(quat);
		double[] stdvs2 = stdvQuats(quat, means2);
		double[] skews2 = skewnessQuats(quat, means2, stdvs2);
		double[] kurs2 = kurtosisQuats(quat, means2, stdvs2);
		
		double fq1 = means2[0]; double fq2 = means2[1]; double fq3 = means2[2]; double fq4 = means2[3];   
		double fq5 = stdvs2[0]; double fq6 = stdvs2[1]; double fq7 = stdvs2[2]; double fq8 = stdvs2[3];
		double fq9 = skews2[0]; double fq10 = skews2[1]; double fq11 = skews2[2]; double fq12 = skews2[3];
		double fq13 = kurs2[0]; double fq14 = kurs2[1]; double fq15 = kurs2[2]; double fq16 = kurs2[3];
		
		//1 + 12 + 16*3 + 8 + 16*2 = 1 + 60 + 40
		//for ring acc2, y and z
		if(directionIndex == 1){
			if(accIndex == 2){
				DataStorage.AddSampleS(rLabel, 
						0.0, diffPeak.y, diffPeak.z, 
						0.0, f5, f6, 0.0, f8, f9, 0.0, f11, f12,
						//freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
						freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
						freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
						0.0, 0.0, 0.0,
						//average values don't help much
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						//32
						//pos.x, pos.y, pos.z, 
						0.0, 0.0, 0.0,
						0.0, 0.0, 0.0,
						0.0, fstds[3],fmeans[4], fstds[4],fmeans[5], fstds[5],
						fmeans[6], fstds[6],fmeans[7], fstds[7],fmeans[8], fstds[8],fmeans[9], fstds[9],fmeans[10], fstds[10],fmeans[11], fstds[11],
						fmeans[12], fstds[12],fmeans[13], fstds[13],fmeans[14], fstds[14],fmeans[15], fstds[15]
						);
				}else if(accIndex == 1){
				//for watch acc1, x and z
				DataStorage.AddSampleS(rLabel, 
						diffPeak.x, 0, diffPeak.z, 
						f4, 0.0, f6, f7, 0.0, f9, f10, 0.0, f12,
						freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
						//freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
						freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
						0.0, 0.0, 0.0,
						//average values don't help much
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						//32
						//pos.x, pos.y, pos.z, 
						0.0, 0.0, 0.0,
						0.0, 0.0, 0.0,
						0.0, fstds[3],fmeans[4], fstds[4],fmeans[5], fstds[5],
						fmeans[6], fstds[6],fmeans[7], fstds[7],fmeans[8], fstds[8],fmeans[9], fstds[9],fmeans[10], fstds[10],fmeans[11], fstds[11],
						fmeans[12], fstds[12],fmeans[13], fstds[13],fmeans[14], fstds[14],fmeans[15], fstds[15]
						);
					
				}
		}else if(directionIndex == 0)  //horizontal
		{
			if(accIndex == 2){
				DataStorage.AddSampleS(rLabel, 
						diffPeak.x, 0.0, diffPeak.z, 
						f4, 0.0, f6, f7, 0.0, f9, f10, 0.0, f12,
						freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
						//freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
						freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
						0.0, 0.0, 0.0,
						//average values don't help much
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						//32
						//pos.x, pos.y, pos.z, 
						0.0, 0.0, 0.0,
						0.0, 0.0, 0.0,
						0.0, fstds[3],fmeans[4], fstds[4],fmeans[5], fstds[5],
						fmeans[6], fstds[6],fmeans[7], fstds[7],fmeans[8], fstds[8],fmeans[9], fstds[9],fmeans[10], fstds[10],fmeans[11], fstds[11],
						fmeans[12], fstds[12],fmeans[13], fstds[13],fmeans[14], fstds[14],fmeans[15], fstds[15]
						);
				}else if(accIndex == 1){
				//for watch acc1, x and z
				DataStorage.AddSampleS(rLabel, 
						0.0, diffPeak.y, diffPeak.z, 
						0.0, f5, f6, 0.0, f8, f9, 0.0, f11, f12,
						//freqX[1],freqX[2],freqX[3],freqX[4],freqX[5],freqX[6],freqX[7],freqX[8],freqX[9],freqX[10],freqX[11],freqX[12],freqX[13],freqX[14],freqX[15],
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
						freqY[1],freqY[2],freqY[3],freqY[4],freqY[5],freqY[6],freqY[7],freqY[8],freqY[9],freqY[10],freqY[11],freqY[12],freqY[13],freqY[14],freqY[15],
						freqZ[1],freqZ[2],freqZ[3],freqZ[4],freqZ[5],freqZ[6],freqZ[7],freqZ[8],freqZ[9],freqZ[10],freqZ[11],freqZ[12],freqZ[13],freqZ[14],freqZ[15],
						0.0, 0.0, 0.0,
						//average values don't help much
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
						//32
						//pos.x, pos.y, pos.z, 
						0.0, 0.0, 0.0,
						0.0, 0.0, 0.0,
						0.0, fstds[3],fmeans[4], fstds[4],fmeans[5], fstds[5],
						fmeans[6], fstds[6],fmeans[7], fstds[7],fmeans[8], fstds[8],fmeans[9], fstds[9],fmeans[10], fstds[10],fmeans[11], fstds[11],
						fmeans[12], fstds[12],fmeans[13], fstds[13],fmeans[14], fstds[14],fmeans[15], fstds[15]
						);
					
				}
		}
		
		
	
	}*/
	
	
	public ArrayList<Vector3> getAroundXAxisAcc(ArrayList<Vector3> accList, ArrayList<Quaternion> quatList)
	{
		ArrayList<Vector3> resultAccList = new ArrayList<Vector3>();
		
		if(accList.size() != quatList.size())
		{
			return resultAccList;
		}else
		{
			int sz = accList.size();
			for(int itra = 0; itra < sz; itra++)
			{
				//
				double aroundXRadius = quatList.get(itra).getAngleAround(xAxis);
				
				double alongMovementAcc = accList.get(itra).y * Math.cos(aroundXRadius) + accList.get(itra).z * Math.sin(aroundXRadius);
				double orthoMovementAcc = -accList.get(itra).y * Math.sin(aroundXRadius) + accList.get(itra).z * Math.cos(aroundXRadius);  //pay attention to the +-
				
				Vector3 temp = new Vector3();
				temp.Set(accList.get(itra).x, alongMovementAcc, orthoMovementAcc);
				resultAccList.add(temp);
			}
			
			return resultAccList;
		}
	}
	
	
	public void getDisplacement(ArrayList<Vector3> accList, ArrayList<Quaternion> quatList)
	{
		if(accList.size() != quatList.size())
		{
			filter_pos.Set(Vector3.Zero);
		}else
		{
			int sz = accList.size();
			velocity.Set(Vector3.Zero);
			pos.Set(Vector3.Zero);
			filter_velocity.Set(Vector3.Zero);
			filter_pos.Set(Vector3.Zero);
			Vector3 ac = new Vector3();
			Quaternion qu = new Quaternion();
			
			mButterHp.refreshDataSet(1);
			mButterHp.refreshDataSet(2);
			for(int itra = 0; itra < sz; itra++)
			{
				ac.Set(accList.get(itra));
				qu.Set(quatList.get(itra));
				
				mMatrix.Set(qu);
				linAcc.Set(ac);
				//System.out.println(linAcc.y);
				linAcc.Mul(mMatrix.inv());
				
				velocity.Add(linAcc.scl(stamp));
				filter_velocity.Set(mButterHp.applyButterWorth(1, 1, velocity));
				
				pos.Add(filter_velocity.scl(stamp));
				filter_pos.Set(mButterHp.applyButterWorth(2, 1, pos));
					
				//System.out.println(linAcc.x);
				//System.out.println(velocity.z);
				//System.out.println(filter_pos.x);
				
				
				
			}
			
			//System.out.println();		
		}
	}
	
	
	public void getDisplacement(ArrayList<Vector3> accList)  //already converted to along and orthogonal to movements
	{	
		int sz = accList.size();
		velocity.Set(Vector3.Zero);
		pos.Set(Vector3.Zero);
		
		//mButterLp.refreshDataSet(1);
		mButterHp.refreshDataSet(1);
		mButterHp.refreshDataSet(2);
		
		for(int itra = 0; itra < sz; itra++)
		{
			//how about filter the noise of acc
			linAcc.Set(accList.get(itra).x, accList.get(itra).y, accList.get(itra).z);
			//filteredAcc.Set(mButterLp.applyButterWorth(1, 1, linAcc));

			velocity.Add(linAcc.scl(stamp));
			filter_velocity.Set(mButterHp.applyButterWorth(1, 1, velocity));
			
			
			pos.Add(filter_velocity.scl(stamp));
			filter_pos.Set(mButterHp.applyButterWorth(2, 1, pos));
			
			//System.out.println(filteredAcc.y);
			//System.out.println(velocity.y);
			//System.out.println(pos.x);
		}
		
		//System.out.println();		
	}
	
	public ArrayList<Vector3> toAbsList(ArrayList<Vector3> list)
	{
		ArrayList<Vector3> result = new ArrayList<Vector3>();
		//copy
		for(int itrl = 0; itrl < list.size(); itrl++)
		{
			Vector3 ori = list.get(itrl);
			Vector3 temp = new Vector3();
			
			//change to abs values
			temp.x = Math.abs(ori.x);
			temp.y = Math.abs(ori.y);
			temp.z = Math.abs(ori.z);
			
			result.add(temp);
		}
		
		return result;
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
	
	public ArrayList<Vector3> neighbourAbsDiff(ArrayList<Vector3> list)
	{
		ArrayList<Vector3> diffs = new ArrayList<Vector3>();	
		if(list.size() < 2)
			return diffs;
		
		for(int itr = 1; itr < list.size(); itr++)
		{
			Vector3 diff = new Vector3();
			diff.Set(list.get(itr).sub(list.get(itr-1)));
			diff.Abs();
			diffs.add(diff);
		}
		
		return diffs;
	}
	
	public Vector3 largestNeighbourAbsDiff(ArrayList<Vector3> list)
	{
		ArrayList<Vector3> diffs = neighbourAbsDiff(list);
		int peakIndex = localPeakIndex(diffs)[0];
		Vector3 result = new Vector3();
		result.Set(diffs.get(peakIndex));
		return result;
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
	
	public double[] meanQuats(ArrayList<Quaternion> quatList)
	{
		double[] quatValues = new double[4];
		quatValues[0] = 0.0; //x
		quatValues[1] = 0.0; //y
		quatValues[2] = 0.0; //z
		quatValues[3] = 0.0;	//w
		
		int sz = quatList.size();
		for(Quaternion quat : quatList)
		{
			quatValues[0] += quat.x;
			quatValues[1] += quat.y;
			quatValues[2] += quat.z;
			quatValues[3] += quat.w;
		}
		
		quatValues[0] = quatValues[0] / sz;
		quatValues[1] = quatValues[1] / sz;
		quatValues[2] = quatValues[2] / sz;
		quatValues[3] = quatValues[3] / sz;
		
		return quatValues;
		
	}
	
	public double[] middleAxes(ArrayList<Vector3> list)
	{
		double[] axeValues = new double[3];
		axeValues[0] = 0.0;
		axeValues[1] = 0.0;
		axeValues[2] = 0.0;
		
		int firstNumIndex = 0;
		int secondNumIndex = 0;
		int size = list.size();
		if(size % 2 == 0){
			firstNumIndex = size/2;
			secondNumIndex = firstNumIndex+1;
			
			axeValues[0] = (list.get(firstNumIndex).x + list.get(secondNumIndex).x)/2;
			axeValues[1] = (list.get(firstNumIndex).y + list.get(secondNumIndex).y)/2;
			axeValues[2] = (list.get(firstNumIndex).z + list.get(secondNumIndex).z)/2;
		}else
		{
			firstNumIndex = (int)(size/2) + 1;
			axeValues[0] = list.get(firstNumIndex).x;
			axeValues[1] = list.get(firstNumIndex).y;
			axeValues[2] = list.get(firstNumIndex).z;
		}
		
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
	
	public double[] stdvQuats(ArrayList<Quaternion> list, double[] mean)
	{
		double[] quatValues = new double[4];
		quatValues[0] = 0.0;
		quatValues[1] = 0.0;
		quatValues[2] = 0.0;
		quatValues[3] = 0.0;
		
		int sz = list.size();
		for(Quaternion quat : list)
		{
			quatValues[0] += ((quat.x - mean[0]) * (quat.x - mean[0]));
			quatValues[1] += ((quat.y - mean[1]) * (quat.y - mean[1]));
			quatValues[2] += ((quat.z - mean[2]) * (quat.z - mean[2]));
			quatValues[3] += ((quat.w - mean[3]) * (quat.w - mean[3]));
		}
		
		quatValues[0] = Math.sqrt( quatValues[0] / sz);
		quatValues[1] = Math.sqrt( quatValues[1] / sz);
		quatValues[2] = Math.sqrt( quatValues[2] / sz);
		quatValues[3] = Math.sqrt( quatValues[3] / sz);
		
		return quatValues;
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
	
	public double[] skewnessQuats(ArrayList<Quaternion> list, double[] mean, double[] stdv)
	{
		double[] quatValues = new double[4];
		quatValues[0] = 0.0;
		quatValues[1] = 0.0;
		quatValues[2] = 0.0;
		quatValues[3] = 0.0;
				
		int sz = list.size();
		for(Quaternion quat : list)
		{
			quatValues[0] += ((quat.x - mean[0]) * (quat.x - mean[0]) * (quat.x - mean[0]));
			quatValues[1] += ((quat.y - mean[1]) * (quat.y - mean[1]) * (quat.y - mean[1]));
			quatValues[2] += ((quat.z - mean[2]) * (quat.z - mean[2]) * (quat.z - mean[2]));
			quatValues[3] += ((quat.w - mean[3]) * (quat.w - mean[3]) * (quat.w - mean[3]));
		}
		
		quatValues[0] = (quatValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0]);
		quatValues[1] = (quatValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1]);
		quatValues[2] = (quatValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2]);
		quatValues[3] = (quatValues[3] / sz) / (stdv[3] * stdv[3] * stdv[3]);
		
		return quatValues;
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
			axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]));
			axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]));
		}
		
		axeValues[0] = (axeValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0] * stdv[0]) - 3.0;
		axeValues[1] = (axeValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1] * stdv[1]) - 3.0;
		axeValues[2] = (axeValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2] * stdv[2]) - 3.0;
		
		return axeValues;
	}
	
	public double[] kurtosisQuats(ArrayList<Quaternion> list, double[] mean, double[] stdv)
	{
		double[] quatValues = new double[4];
		quatValues[0] = 0.0;
		quatValues[1] = 0.0;
		quatValues[2] = 0.0;
		quatValues[3] = 0.0;
		
		int sz = list.size();
		for(Quaternion quat : list)
		{
			quatValues[0] += ((quat.x - mean[0]) * (quat.x - mean[0]) * (quat.x - mean[0]) * (quat.x - mean[0]));
			quatValues[1] += ((quat.y - mean[1]) * (quat.y - mean[1]) * (quat.y - mean[1]) * (quat.y - mean[1]));
			quatValues[2] += ((quat.z - mean[2]) * (quat.z - mean[2]) * (quat.z - mean[2]) * (quat.z - mean[2]));
			quatValues[3] += ((quat.w - mean[3]) * (quat.w - mean[3]) * (quat.w - mean[3]) * (quat.w - mean[3]));
			
		}
		
		quatValues[0] = (quatValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0] * stdv[0]) - 3.0;
		quatValues[1] = (quatValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1] * stdv[1]) - 3.0;
		quatValues[2] = (quatValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2] * stdv[2]) - 3.0;
		quatValues[3] = (quatValues[3] / sz) / (stdv[3] * stdv[3] * stdv[3] * stdv[3]) - 3.0;
		
		return quatValues;
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
	
	/*
	public static final void main(String args[])
	{
		Featurization fea = new Featurization();
		fea.getFeatures();
		
	}*/
	
	
	public static final void main(String args[])
	{
		/*
		String[] filename = new String[]{"p", "pg", "t", "tg"};
		for(int itrf = 0; itrf < 4; itrf++)
		{
			for(int itri = 0; itri < 9; itri++)
			{
				String dataFileName = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\12\\" + filename[itrf] + String.valueOf(itri) + "pro.csv"; 
				int fileIndex = itri;
				String targetName = filename[itrf] + String.valueOf(itri) + "f";
				
				Featurization fea = new Featurization(dataFileName, fileIndex, targetName);
				fea.getFeatures();
			}
		}*/
		
		String dataFileName = "C:\\Users\\Teng\\Desktop\\dataset\\911demos\\raw\\wrist\\random_2_pro.csv";
		String targetName = "";
		
		Featurization fea = new Featurization(dataFileName, 1, targetName);
		fea.getFeatures();
	}
}
