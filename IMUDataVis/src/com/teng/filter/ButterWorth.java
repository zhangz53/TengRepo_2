package com.teng.filter;

import java.util.ArrayList;

import com.teng.math.Vector3;

public class ButterWorth {

	private int order;
	private boolean toScale;
	private double f1f;  //lower cutoff frequency, fraction of pi
	private double f2f; //higher cutoff frequency, fraction of pi
	public ArrayList<Double> dcof;
	public ArrayList<Double> ccof;
	private double sf;  //scaling factor, 1 when toscale is false
	
	//maintain input and output here
	public ArrayList<ArrayList<Vector3>> inputDataSets;
	public ArrayList<ArrayList<Vector3>> outputDataSets;
	
	
	private IIR mIIR;
	
	public enum BandType{
		high,
		low,
		band
	}
	
	private BandType bandType; 
	
	public ButterWorth(BandType _type)
	{
		//by default
		dcof = new ArrayList<Double>();
		ccof = new ArrayList<Double>();
		
		inputDataSets = new ArrayList<ArrayList<Vector3>>();
		outputDataSets = new ArrayList<ArrayList<Vector3>>();
		
		if(_type == BandType.high){
			//1 order,  0.1hz cutoff ?
			dcof.add(1.0);
			dcof.add(-0.969067417193793);
		
			ccof.add(0.984533708596897);
			ccof.add(-0.984533708596897);
			
			bandType = BandType.high;
		}else if(_type == BandType.low)
		{
			dcof.add(1.0);
			dcof.add(-0.726542528005361);
			
			ccof.add(0.136728735997320);
			ccof.add(0.136728735997320);
			bandType = BandType.low;
		}
		
	}
	
	public ButterWorth(int _order, boolean _toScale, BandType _type, double _f1f, double _f2f)
	{
		mIIR = new IIR();
		order = _order;
		toScale = _toScale;
		bandType = _type;
		
		f1f = _f1f;
		f2f = _f2f;
		sf = 1.0;
		
		if(bandType == BandType.high)
		{
			//updateCofHP(f2f);
		}
	}
	
	
	//create a new dataset for input and output
	public void createDataSet()
	{
		ArrayList<Vector3> inputSet = new ArrayList<Vector3>();
		ArrayList<Vector3> outputSet = new ArrayList<Vector3>();
		
		inputDataSets.add(inputSet);
		outputDataSets.add(outputSet);
	}
	
	/*
	private void updateCofHP(double freq)  //in fraction of pi
	{
		dcof = new ArrayList<double>(mIIR.dcof_bwhp(order, freq));
		if(dcof.size() == 0)
			return;
		
		ArrayList<Integer> tempccof = new ArrayList<Integer>(mIIR.ccof_bwhp(order));
		if(tempccof.size() == 0)
			return;
		
		sf = mIIR.sf_bwhp(order, freq);
		if(toScale == false)
			sf = 1.0;
		
		ccof = new ArrayList<double>();
		for(int itr = 0; itr <= order; itr++)  //for hp, size is n+1
		{
			ccof.add(sf * tempccof.get(itr));
		}
		
		//print
		System.out.println("sf  " + sf);
		
		for(int itr = 0; itr < ccof.size(); itr++)
		{
			System.out.print("  " + ccof.get(itr) + ",");
		}
		System.out.println();
		
		//print
		for(int itr = 0; itr < dcof.size(); itr++)
		{
			System.out.print("  " + dcof.get(itr) + ",");
		}
		System.out.println();
	}
	
	*/
	public Vector3 applyButterWorth(ArrayList<Vector3> xValues, ArrayList<Vector3> yValues)  //in real time
	{
		//y[n] = ccof[0] * x[n] + ccof[1]* x[n-1] - dcof[1] * y[n-1]
		if(xValues.size() - yValues.size() != 1){
			System.out.println("error");
			return Vector3.Zero;
		}
		    
		//double to double?
		
		Vector3 yValue = new Vector3();
		yValue.Set(
				((xValues.get(1).scl(ccof.get(0)))
				.add(  (xValues.get(0).scl(ccof.get(1))  )))
				.sub(  (yValues.get(0).scl(dcof.get(1))  ))
				);
		
		//if(bandType == BandType.high)
		//	System.out.println( xValues.get(1).x  + " * " + ccof.get(0) + " + " + xValues.get(0).x  + " * " + ccof.get(1) + " - "  + yValues.get(0).x + " * " + dcof.get(1));
		
		return yValue;
	}
	
	public Vector3 applyButterWorth(int datasetIndex, int order,  Vector3 xValue)
	{
		Vector3 yValue = new Vector3();
		if(datasetIndex > inputDataSets.size())
			return yValue;
		
		ArrayList<Vector3> xValues = inputDataSets.get(datasetIndex - 1);
		ArrayList<Vector3> yValues = outputDataSets.get(datasetIndex - 1);
		
		Vector3 tempx = new Vector3();
		tempx.Set(xValue);
		xValues.add(tempx);
		
		if(xValues.size() > (order + 1))
		{
			xValues.remove(0);
		}
		
		if(xValues.size() == (order + 1))
		{
			yValue = applyButterWorth(xValues, yValues);
			yValues.add(yValue);
		}else
		{
			Vector3 tempy = new Vector3();
			tempy.Set(xValue);
			yValues.add(tempy);
			
			yValue.Set(tempy);
		}
		
		if(yValues.size() > order)
		{
			yValues.remove(0);
		}
			
		return yValue;
	}
	
	//public static final void main(String args[]){
		//ButterWorth test = new ButterWorth(6, false, BandType.high, 0.0, 0.6);
	//}
}
