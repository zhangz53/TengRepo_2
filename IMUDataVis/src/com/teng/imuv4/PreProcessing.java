package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;

import com.teng.math.Quaternion;
import com.teng.math.Vector3;
import com.teng.phdata.DataStorage;

//data reading from csv
class DataLoad{
	
	public BufferedReader br;
	public String line = "";
	private String splitBy = ",";
	
	//data set per sample
	public ArrayList<Vector3> acc1;
	public ArrayList<Vector3> acc2;
	
	//public ArrayList<Quaternion> quat;
	
	public DataLoad()
	{
		acc1 = new ArrayList<Vector3>();
		acc2 = new ArrayList<Vector3>();
		//quat = new ArrayList<Quaternion>();
		
		//read the csv file
		String dataFile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_algorithm\\7\\8_testfilter.csv";
		try {
			br = new BufferedReader(new FileReader(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void fetchData(int index){
		try {
			if((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				
				if(values.length == 13)
				{
					double sIndexDouble = Double.parseDouble(values[0]);
					int sIndex = (int)sIndexDouble;
					if(sIndex == index)
					{
						//record the data
						acc1.add(new Vector3(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3])));
						acc2.add(new Vector3(Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6])));
						//quat.add(new Quaternion(Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]),  Double.parseDouble(values[10])));
						
						//do another fetch
						fetchData(index);
					}else
					{
						//end, data recording is missing
					}
				}else
				{
					//do anther fetch
					fetchData(index);
				}
			}else
			{
				//all data explored
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

//
public class PreProcessing extends PApplet{
	
	public DataLoad mDataLoad;
	public double widthSeg;
	public double heightSeg;
	public double heightThreshold;
	public int windowWidth;
	public int windowHeight;
	
	private int sampleNum = 32; // for convenient of fft
	private int curMousePos;
	private int leftBound;
	private int rightBound;
	private int alpha;
	
	private int sampleIndex = 0;
	public int sampleCount = 1;
	
	public DataStorage dataStorage;
	
	public void setup()
	{
		windowWidth = 1500;
		windowHeight = 1200;  //split into two
		heightThreshold = 10;  //+ - 10
		heightSeg = (windowHeight/2) / (2 * heightThreshold);
		
		size(windowWidth, windowHeight);
		background(250);
		
		mDataLoad = new DataLoad();
		sampleIndex++;
		mDataLoad.fetchData(sampleIndex);
		
		dataStorage = DataStorage.getInstance();
	}
	
	public void draw()
	{
		background(250);
		
		//detect mouse segments
		int acc1Size = mDataLoad.acc1.size();
		int acc2Size = mDataLoad.acc2.size();
		if((acc1Size != acc2Size) || acc1Size == 0 || acc2Size == 0)
		{
			pushMatrix();
			textSize(64);
			fill(250, 0, 0);
			text("the end", 480, 525);
			popMatrix();
			return;
		}
		
		widthSeg = windowWidth / acc1Size;
		curMousePos = (int)( mouseX / widthSeg);
		leftBound = curMousePos - sampleNum / 2;
		if(leftBound < 0)
			leftBound = 0;
		rightBound = curMousePos + sampleNum / 2;
		if(rightBound > (acc1Size - 1))
			rightBound = acc1Size - 1;
		
		//draw acc1
		{
			for(int itra = 0; itra < (acc1Size-1); itra ++)
			{
				if(itra >= leftBound && itra <= rightBound)
					alpha = 255;
				else
					alpha = 50;
				
				stroke(255, 0, 0, alpha);  //x
				line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra).x * heightSeg),
						 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra + 1).x * heightSeg));
				
				stroke(0, 255, 0, alpha);  //y
				line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra).y * heightSeg),
						 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra + 1).y * heightSeg));
				
				stroke(0, 0, 255, alpha);  //z
				line((float)(windowWidth - acc1Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra).z * heightSeg),
						 (float)(windowWidth - acc1Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 3 / 4 + mDataLoad.acc1.get(itra + 1).z * heightSeg));
			}

		}
		
		//draw acc2
		{
			for(int itra = 0; itra < (acc2Size-1); itra ++)
			{
				
				if(itra >= leftBound && itra <= rightBound)
					alpha = 255;
				else
					alpha = 50;
				
				stroke(255, 0, 0, alpha);  //x
				line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra).x * heightSeg),
						 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra + 1).x * heightSeg));
				
				stroke(0, 255, 0, alpha);  //y
				line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra).y * heightSeg),
						 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra + 1).y * heightSeg));
				
				stroke(0, 0, 255, alpha);  //z
				line((float)(windowWidth - acc2Size * widthSeg + itra * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra).z * heightSeg),
						 (float)(windowWidth - acc2Size * widthSeg + (itra + 1) * widthSeg), 
						 (float)(windowHeight * 1 / 4 + mDataLoad.acc2.get(itra + 1).z * heightSeg));
			}
		}
		
		//counts
		pushMatrix();
		textSize(32);
		fill(250, 100, 100);
		text(sampleCount, 100, 100);
		popMatrix();
	}
	
	public void mouseClicked()
	{
		//save the selected data
		for(int itrb = leftBound; itrb < rightBound; itrb++)
		{
			DataStorage.AddSampleF((double)sampleIndex, mDataLoad.acc1.get(itrb).x, mDataLoad.acc1.get(itrb).y, mDataLoad.acc1.get(itrb).z,
					mDataLoad.acc2.get(itrb).x, mDataLoad.acc2.get(itrb).y, mDataLoad.acc2.get(itrb).z, 
					//mDataLoad.quat.get(itrb).x, mDataLoad.quat.get(itrb).y, mDataLoad.quat.get(itrb).z, mDataLoad.quat.get(itrb).w, 
					0.0, 0.0, 0.0, 0.0,
					0.0, 0.0);
		}
		
		//clear acc1 and acc2
		mDataLoad.acc1.clear();
		mDataLoad.acc2.clear();
		//mDataLoad.quat.clear();
		
		//get new data for next index
		sampleIndex++;
		sampleCount++;
		mDataLoad.fetchData(sampleIndex);
	}
	
	public void keyPressed()
	{
		if(key == 'q'){
			dataStorage.savef();
			exit();
		}else if(key == 'b')
		{
			//reload the previous data
		}
		
	}
	
	public static final void main(String args[]){
		PApplet.main(new String[] {"--present", "com.teng.imuv4.PreProcessing"});
	}
}
