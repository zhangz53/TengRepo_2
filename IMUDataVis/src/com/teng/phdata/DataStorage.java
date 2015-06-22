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
	
	public static boolean AddSampleS(double _label, 
			double _f1, double _f2, double _f3, double _f4, double _f5, double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25, double _f26, double _f27, double _f28, double _f29, double _f30,  
			double _f31, double _f32, double _f33, double _f34, double _f35, double _f36, double _f37, double _f38, double _f39, double _f40, 
			double _f41, double _f42, double _f43, double _f44, double _f45, double _f46, double _f47, double _f48, double _f49, double _f50,
			double _f51, double _f52, double _f53, double _f54, double _f55, double _f56, double _f57, double _f58, double _f59, double _f60, 
			double _f61, double _f62, double _f63, double _f64, double _f65, double _f66, double _f67, double _f68, double _f69, double _f70, 
			double _f71, double _f72, double _f73, double _f74, double _f75, double _f76, double _f77, double _f78, double _f79, double _f80,
			double _f81, double _f82, double _f83, double _f84, double _f85, double _f86, double _f87, double _f88, double _f89, double _f90,
			double _f91, double _f92, double _f93, double _f94, double _f95, double _f96, double _f97, double _f98, double _f99, double _f100
			)
	{
		if(instance!= null)
		{
			instance.adds(_label, _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, 
					_f11, _f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20, 
					_f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29, _f30, 
					_f31, _f32, _f33, _f34, _f35, _f36, _f37, _f38, _f39, _f40, 
					_f41, _f42, _f43, _f44, _f45, _f46,  _f47, _f48, _f49, _f50,
					_f51, _f52, _f53, _f54, _f55, _f56, _f57,  _f58, _f59, _f60, 
					_f61, _f62,  _f63, _f64, _f65, _f66, _f67, _f68, _f69, _f70,
					_f71, _f72, _f73, _f74, _f75, _f76, _f77, _f78, _f79, _f80, 
					_f81, _f82, _f83, _f84, _f85, _f86, _f87, _f88, _f89, _f90, 
					_f91, _f92, _f93, _f94, _f95, _f96, _f97, _f98, _f99, _f100);
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
	
	public void adds(double _label, 
			double _f1, double _f2, double _f3, double _f4, double _f5, double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25, double _f26, double _f27, double _f28, double _f29, double _f30,  
			double _f31, double _f32, double _f33, double _f34, double _f35, double _f36, double _f37, double _f38, double _f39, double _f40, 
			double _f41, double _f42, double _f43, double _f44, double _f45, double _f46, double _f47, double _f48, double _f49, double _f50,
			double _f51, double _f52, double _f53, double _f54, double _f55, double _f56, double _f57, double _f58, double _f59, double _f60, 
			double _f61, double _f62, double _f63, double _f64, double _f65, double _f66, double _f67, double _f68, double _f69, double _f70, 
			double _f71, double _f72, double _f73, double _f74, double _f75, double _f76, double _f77, double _f78, double _f79, double _f80,
			double _f81, double _f82, double _f83, double _f84, double _f85, double _f86, double _f87, double _f88, double _f89, double _f90,
			double _f91, double _f92, double _f93, double _f94, double _f95, double _f96, double _f97, double _f98, double _f99, double _f100
			)
	{
		if(samples != null)
		{
			DataSample sample = new DataSample(_label, _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, 
					_f11, _f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20, 
					_f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29, _f30, 
					_f31, _f32, _f33, _f34, _f35, _f36, _f37, _f38, _f39, _f40, 
					_f41, _f42, _f43, _f44, _f45, _f46,  _f47, _f48, _f49, _f50,
					_f51, _f52, _f53, _f54, _f55, _f56, _f57,  _f58, _f59, _f60, 
					_f61, _f62,  _f63, _f64, _f65, _f66, _f67, _f68, _f69, _f70,
					_f71, _f72, _f73, _f74, _f75, _f76, _f77, _f78, _f79, _f80, 
					_f81, _f82, _f83, _f84, _f85, _f86, _f87, _f88, _f89, _f90, 
					_f91, _f92, _f93, _f94, _f95, _f96, _f97, _f98, _f99, _f100);
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
	
	public String saves()
	{
		return saves(null);
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
	
	public String saves(String surfix)
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
	
	        outputstreamwriter.write( DataSample.toCSVS(samples) );
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
