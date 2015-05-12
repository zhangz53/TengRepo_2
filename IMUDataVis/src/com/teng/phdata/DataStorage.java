package com.teng.phdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class DataStorage {
	private DataStorage()
	{		
		samples = new ArrayList<DataSample>(100000);
	}
	
	public static boolean AddSample(double _time, 
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _mx, double _my, double _mz)
	{
		if(instance!= null)
		{
			instance.add(_time, _gx, _gy, _gz, _ax, _ay, _az, _mx, _my, _mz);
			return true;
		}
		
		return false;
	}
	
	public static boolean AddSampleF( double _time,
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _gxf, double _gyf, double _gzf,
			double _axf, double _ayf, double _azf)
	{
		if(instance!= null)
		{
			instance.addf(_time, _gx, _gy, _gz, _ax, _ay, _az, _gxf, _gyf, _gzf, _axf, _ayf, _azf);
			return true;
		}
		
		return false;
	}
	
	public void add(double _time, 
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _mx, double _my, double _mz)
	{
		if(samples != null)
		{
			DataSample sample = new DataSample(_time, _gx, _gy, _gz, _ax, _ay, _az, _mx, _my, _mz);
			samples.add(sample);
		}
	}
	
	public void addf(double _time,
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _gxf, double _gyf, double _gzf,
			double _axf, double _ayf, double _azf)
	{
		if(samples != null)
		{
			DataSample sample = new DataSample(_time, _gx, _gy, _gz, _ax, _ay, _az, _gxf, _gyf, _gzf, _axf, _ayf, _azf);
			samples.add(sample);
		}
	}
	
	public static DataStorage getInstance()
    {
        if(instance == null)
        {
            instance = new DataStorage();
        }
        
        return instance;
    }
	
	public void clearData()
	{
		if(samples != null)
		{
			samples.clear();
		}
	}
	
	public String save()
	{
		return save(null);
	}
	
	public String savef()
	{
		return savef(null);
	}
	
	
	public String save(String surfix)
	{
		if(samples == null || samples.size() == 0) 
        {	
        	return "";
        }
		
		if(surfix == null) 
        {
        	surfix = "LogData_CalInertialAndMag";
        }
		if(!surfix.startsWith("_"))
        {
        	surfix = "_" + surfix;
        }
		
		File dir;
		dir = new File("C:\\Users\\Teng\\Documents\\matlab-workspace\\FiltersAndTest\\LogData");
		String time = String.valueOf(System.currentTimeMillis());
		String filename = time + surfix + ".csv";
		File file = new File(dir, filename);
		if(!dir.exists())
            dir.mkdir();
		
		try {
	        OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));
	
	        outputstreamwriter.write( DataSample.toCSV(samples) );
	        outputstreamwriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
		
		 return surfix;
	}
	
	public String savef(String surfix)
	{
		if(samples == null || samples.size() == 0) 
        {	
        	return "";
        }
		
		if(surfix == null) 
        {
        	surfix = "testfilter";
        }
		if(!surfix.startsWith("_"))
        {
        	surfix = "_" + surfix;
        }
		
		File dir;
		dir = new File("C:\\Users\\Teng\\Documents\\TestDataFolder");
		String time = String.valueOf(System.currentTimeMillis());
		String filename = time + surfix + ".csv";
		File file = new File(dir, filename);
		if(!dir.exists())
            dir.mkdir();
		
		try {
	        OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));
	
	        outputstreamwriter.write( DataSample.toCSVF(samples) );
	        outputstreamwriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
		
		System.out.println("saved");
		
		 return surfix;
	}

	private static DataStorage instance;
	public ArrayList<DataSample> samples;
}
