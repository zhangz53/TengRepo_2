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
	
	public static boolean AddSampleX(double _label, double _f1, double _f2, double _f3, double _f4, double _f5, 
			double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, 
			double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25,
			double _f26, double _f27, double _f28, double _f29, double _f30, 
			double _f31)
	{
		if(instance!= null)
		{
			instance.addx(_label, _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, 
					_f11, _f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20, 
					_f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29, _f30,
					_f31);
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
	
	public void addx(double _label, double _f1, double _f2, double _f3, double _f4, double _f5, 
			double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, 
			double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25,
			double _f26, double _f27, double _f28, double _f29, double _f30, 
			double _f31)
	{
		if(samples != null)
		{
			DataSample sample = new DataSample(_label, _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, 
					_f11, _f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20, 
					_f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29, _f30,
					_f31);
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
	
	public String savex()
	{
		return savex(null);
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

	public String savex(String surfix)
	{
		if(samples == null || samples.size() == 0) 
        {	
        	return "";
        }
		
		if(surfix == null) 
        {
        	surfix = "featured";
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
	
	        outputstreamwriter.write( DataSample.toCSVX(samples) );
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
