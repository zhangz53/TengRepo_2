package com.teng.math;

import java.util.ArrayList;

public class MathTest {
	
	public static double[] convertToDb(double[] data, double maxSquared) {
	    data[0] = db2(data[0], 0.0, maxSquared);
	    int j = 1;
	    for (int i=1; i < data.length - 1; i+=2, j++) {
	      data[j] = db2(data[i], data[i+1], maxSquared);
	    }
	    data[j] = data[0];
	    return data;
	}
	
	private static double db2(double r, double i, double maxSquared) {
	    return 5.0 * Math.log10((r * r + i * i) / maxSquared);
	}
	
	
	public static final void main(String args[]){
		
		/*
		System.out.println(Math.sin(Math.PI/2));
		
		Matrix4 inputMatrix = new Matrix4();
		
		Quaternion quat = new Quaternion();
		Vector3 rotAxis = new Vector3(7.0f, 5.9f, 3.4f);
		rotAxis.Nor();
		double rotAngle =  (Math.PI / 6);
		quat.Set(rotAxis.x *  Math.sin(rotAngle/2), rotAxis.y *  Math.sin(rotAngle/2), rotAxis.z *  Math.sin(rotAngle/2), Math.cos(rotAngle/2));	
		    
		Quaternion quat2 = new Quaternion();
		quat2.Set(rotAxis, rotAngle * MathUtils.radiansToDegrees);
		
		inputMatrix.Set(quat);
		System.out.println(inputMatrix.toString());
		
		inputMatrix.Set(quat2);
		System.out.println(inputMatrix.toString());
		
		Matrix4 inputMatrix2 = new Matrix4();
		inputMatrix2.Set(inputMatrix);
		inputMatrix2.Inv();
		
		System.out.println(inputMatrix2.toString());
		*/
		
		
		/*
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < 11; i++)
		{
			list.add(i);
		}
		
		for(int itr = 0; itr < list.size(); itr++)
		{
			System.out.println(" " + list.get(itr));
		}
		
		ArrayList<Integer> newList = list;
		newList.set(0, 10);
		
		for(int itr = 0; itr < list.size(); itr++)
		{
			System.out.println(" " + list.get(itr));
		}
		for(int itr = 0; itr < list.size(); itr++)
		{
			System.out.println(" " + newList.get(itr));
		}*/
		
		/*
		list.add(0);
		for(int itr = 0; itr < list.size(); itr++)
		{
			System.out.println(" " + list.get(itr));
		}
		
		list.remove(0);
		list.remove(0);
		
		//list.add(1, 15);

		for(int itr = 0; itr < list.size(); itr++)
		{
			System.out.println(" " + list.get(itr));
		}*/
		
		//Matrix4 output = inputMatrix.Mul(inputMatrix2);
		//System.out.println((inputMatrix.Mul(inputMatrix2)).toString());
		
		
		//test fft
		/*
		RealDoubleFFT mfft = new RealDoubleFFT(32);
		
		double x[] = new double[32];
		
		String dataFile = "C:\\Users\\Teng\\Documents\\matlab-workspace\\MLToolbox\\libsvm-3.20\\matlab\\sample.txt";
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		String splitBy = ",";
		int count = 0;
		
		try {
			while((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				if(values.length == 1)
				{
					x[count] = Double.parseDouble(values[0]);
					count++;
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mfft.ft(x);
		float MEAN_MAX = 16384f;
		int fftBins = 32;
		double scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
		convertToDb(x, scale);
		
		for(int itr = 0; itr < x.length/2; itr++)
		{
			System.out.println("" + x[ itr]);
		}*/
		
		
		String a = "abc";
		String b = a;
		b = "efg";
		
		System.out.println(a + "  " + b);
		
	}
	
	
}
