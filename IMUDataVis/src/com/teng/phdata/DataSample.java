package com.teng.phdata;

import java.util.ArrayList;
import java.util.Iterator;


public class DataSample {
	//group 1
	public double ax, ay, az;
	public double gx, gy, gz;
	public double mx, my, mz;
	public double timestamp;
	
	//group 2
	public double axf, ayf, azf;
	public double gxf, gyf, gzf;
	//public double mx, my, mz;
	
	//group 3
	public double label, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, 
	f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, 
	f21, f22, f23, f24, f25, f26, f27, f28, f29, f30,
	f31, f32, f33, f34, f35, f36, f37, f38, f39, f40, 
	f41, f42, f43, f44, f45, f46,  f47, f48, f49, f50,
	f51, f52, f53, f54, f55, f56, f57,  f58, f59, f60, 
	f61, f62,  f63, f64, f65, f66, f67, f68, f69, f70,
	f71, f72, f73, f74, f75, f76, f77, f78, f79, f80, 
	f81, f82, f83, f84, f85, f86, f87, f88, f89, f90, 
	f91, f92, f93, f94, f95, f96, f97, f98, f99, f100;
	

	public DataSample(double _time, 
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _mx, double _my, double _mz)
	{
		gx = _gx;
		gy = _gy;
		gz = _gz;
		ax = _ax;
		ay = _ay;
		az = _az;
		mx = _mx;
		my = _my;
		mz = _mz;
		timestamp = _time;
	}
	
	public DataSample(double _time,
			double _gx, double _gy, double _gz,
			double _ax, double _ay, double _az,
			double _gxf, double _gyf, double _gzf,
			double _axf, double _ayf, double _azf
			)
	{
		timestamp = _time;
		gx = _gx;
		gy = _gy;
		gz = _gz;
		ax = _ax;
		ay = _ay;
		az = _az;
		gxf = _gxf;
		gyf = _gyf;
		gzf = _gzf;
		axf = _axf;
		ayf = _ayf;
		azf = _azf;
	}
	
	public DataSample(double _label, double _f1, double _f2, double _f3, double _f4, double _f5, 
			double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, 
			double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25,
			double _f26, double _f27, double _f28, double _f29, double _f30, 
			double _f31
			)
	{
		label = _label;
		f1 = _f1;
		f2 = _f2;
		f3 = _f3;
		f4 = _f4;
		f5 = _f5;
		f6 = _f6;
		f7 = _f7;
		f8 = _f8;
		f9 = _f9;
		f10 = _f10;
		f11 = _f11;
		f12 = _f12;
		f13 = _f13;
		f14 = _f14;
		f15 = _f15;
		f16 = _f16;
		f17 = _f17;
		f18 = _f18;
		f19 = _f19;
		f20 = _f20;
		f21 = _f21;
		f22 = _f22;
		f23 = _f23;
		f24 = _f24;
		f25 = _f25;
		f26 = _f26;
		f27 = _f27;
		f28 = _f28;
		f29 = _f29;
		f30 = _f30;
		f31 = _f31;
	}
	
	public DataSample(double _label, 
			double _f1, double _f2, double _f3, double _f4, double _f5, double _f6, double _f7, double _f8, double _f9, double _f10, 
			double _f11, double _f12, double _f13, double _f14, double _f15, double _f16, double _f17, double _f18, double _f19, double _f20, 
			double _f21, double _f22, double _f23, double _f24, double _f25, double _f26, double _f27, double _f28, double _f29, double _f30,  
			double _f31, double _f32, double _f33, double _f34, double _f35, double _f36, double _f37, double _f38, double _f39, double _f40, 
			double _f41, double _f42, double _f43, double _f44, double _f45, double _f46,  double _f47, double _f48, double _f49, double _f50,
			double _f51, double _f52, double _f53, double _f54, double _f55, double _f56, double _f57, double _f58, double _f59, double _f60, 
			double _f61, double _f62,  double _f63, double _f64, double _f65, double _f66, double _f67, double _f68, double _f69, double _f70, 
			double _f71, double _f72, double _f73, double _f74, double _f75, double _f76, double _f77, double _f78, double _f79, double _f80,
			double _f81, double _f82, double _f83, double _f84, double _f85, double _f86, double _f87, double _f88, double _f89, double _f90,
			double _f91, double _f92, double _f93, double _f94, double _f95, double _f96, double _f97, double _f98, double _f99, double _f100)
	{
		label = _label;
		f1 = _f1;
		f2 = _f2;
		f3 = _f3;
		f4 = _f4;
		f5 = _f5;
		f6 = _f6;
		f7 = _f7;
		f8 = _f8;
		f9 = _f9;
		f10 = _f10;
		f11 = _f11;
		f12 = _f12;
		f13 = _f13;
		f14 = _f14;
		f15 = _f15;
		f16 = _f16;
		f17 = _f17;
		f18 = _f18;
		f19 = _f19;
		f20 = _f20;
		f21 = _f21;
		f22 = _f22;
		f23 = _f23;
		f24 = _f24;
		f25 = _f25;
		f26 = _f26;
		f27 = _f27;
		f28 = _f28;
		f29 = _f29;
		f30 = _f30;
		f31 = _f31;
		f32 = _f32; 
		f33 = _f33; 
		f34 = _f34; 
		f35 = _f35;  
		f36 = _f36;  
		f37 = _f37;  
		f38 = _f38;  
		f39 = _f39;  
		f40 = _f40;  
		f41 = _f41; 
		f42 = _f42;  
		f43 = _f43;  
		f44 = _f44;  
		f45 = _f45; 
		f46 = _f46;   
		f47 = _f47; 
		f48 = _f48;  
		f49 = _f49;  
		f50 = _f50; 
		f51 = _f51;  
		f52 = _f52;  
		f53 = _f53;  
		f54 = _f54;  
		f55 = _f55;  
		f56 = _f56;  
		f57 = _f57;   
		f58 = _f58;  
		f59 = _f59;  
		f60 = _f60;  
		f61 = _f61;  
		f62 = _f62;   
		f63 = _f63;  
		f64 = _f64;  
		f65 = _f65;  
		f66 = _f66;
		f67 = _f67;  
		f68 = _f68;  
		f69 = _f69;  
		f70 = _f70;
		f71 = _f71;
		f72 = _f72;
		f73 = _f73;
		f74 = _f74;
		f75 = _f75;
		f76 = _f76;
		f77 = _f77;
		f78 = _f78;
		f79 = _f79;
		f80 = _f80;
		f81 = _f81;
		f82 = _f82;
		f83 = _f83;
		f84 = _f84;
		f85 = _f85;
		f86 = _f86;
		f87 = _f87;
		f88 = _f88;
		f89 = _f89;
		f90 = _f90;
		f91 = _f91;
		f92 = _f92;
		f93 = _f93;
		f94 = _f94;
		f95 = _f95;
		f96 = _f96;
		f97 = _f97;
		f98 = _f98;
		f99 = _f99;
		f100 = _f100;
	}
	
	public static String toCSV(ArrayList<DataSample> arraylist)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Iterator<DataSample> iterator = arraylist.iterator(); iterator.hasNext();)
        {
            DataSample sample = iterator.next();
            stringbuilder.append("" + sample.timestamp + "," 
            		+ sample.gx + "," + sample.gy + "," + sample.gz + ","
            		+ sample.ax + "," + sample.ay + "," + sample.az + ","
            		+ sample.mx + "," + sample.my + "," + sample.mz + ","
            		+ "\r\n");
        }
        
        return stringbuilder.toString();
    }
	
	public static String toCSVF(ArrayList<DataSample> arraylist)
    {
        StringBuilder stringbuilder = new StringBuilder();
        
        //System.out.println("true size: " + arraylist.size());
        //for (Iterator<DataSample> iterator = arraylist.iterator(); iterator.hasNext();)
        for(int itrs = 0; itrs < arraylist.size(); itrs++)
        {
            DataSample sample = arraylist.get(itrs); // iterator.next();
            stringbuilder.append("" + sample.timestamp + ","
            		+ sample.gx + "," + sample.gy + "," + sample.gz + ","
            		+ sample.ax + "," + sample.ay + "," + sample.az + ","
            		+ sample.gxf + "," + sample.gyf + "," + sample.gzf + ","
            		+ sample.axf + "," + sample.ayf + "," + sample.azf + ","
            		+ "\r\n");
        }
        
        return stringbuilder.toString();
    }
	
	public static String toCSVX(ArrayList<DataSample> arraylist)
	{
		StringBuilder stringbuilder = new StringBuilder();
		 for(int itrs = 0; itrs < arraylist.size(); itrs++)
		 {
			 DataSample sample = arraylist.get(itrs);
			 stringbuilder.append("" + sample.label + ","
					 + sample.f1 + "," + sample.f2 + "," + sample.f3 + "," + sample.f4 + "," + sample.f5 + "," + sample.f6 + "," + sample.f7 + "," + sample.f8 + "," + sample.f9 + "," + sample.f10 + "," 
					 + sample.f11 + "," + sample.f12 + "," + sample.f13 + "," + sample.f14 + "," + sample.f15 + "," + sample.f16 + "," + sample.f17 + "," + sample.f18 + "," + sample.f19 + "," + sample.f20 + "," 
					 + sample.f21 + "," + sample.f22 + "," + sample.f23 + "," + sample.f24 + "," + sample.f25 + "," + sample.f26 + "," + sample.f27 + "," + sample.f28 + "," + sample.f29 + "," + sample.f30 + "," 
					 + sample.f31 + "," 
					 + "\r\n");
		 }
		 
		 return stringbuilder.toString();
	}
	
	public static String toCSVS(ArrayList<DataSample> arraylist)
	{
		StringBuilder stringbuilder = new StringBuilder();
		 for(int itrs = 0; itrs < arraylist.size(); itrs++)
		 {
			 DataSample sample = arraylist.get(itrs);
			 stringbuilder.append("" + sample.label + ","
					 + sample.f1 + "," + sample.f2 + "," + sample.f3 + "," + sample.f4 + "," + sample.f5 + "," + sample.f6 + "," + sample.f7 + "," + sample.f8 + "," + sample.f9 + "," + sample.f10 + "," 
					 + sample.f11 + "," + sample.f12 + "," + sample.f13 + "," + sample.f14 + "," + sample.f15 + "," + sample.f16 + "," + sample.f17 + "," + sample.f18 + "," + sample.f19 + "," + sample.f20 + "," 
					 + sample.f21 + "," + sample.f22 + "," + sample.f23 + "," + sample.f24 + "," + sample.f25 + "," + sample.f26 + "," + sample.f27 + "," + sample.f28 + "," + sample.f29 + "," + sample.f30 + "," 
					 + sample.f31 + ","  + sample.f32 + "," + sample.f33 + "," + sample.f34 + "," + sample.f35 + "," + sample.f36 + "," + sample.f37 + "," + sample.f38 + "," + sample.f39 + "," + sample.f40 + ","
					 + sample.f41 + "," + sample.f42 + "," + sample.f43 + "," + sample.f44 + "," + sample.f45 + "," + sample.f46 + "," + sample.f47 + "," + sample.f48 + "," + sample.f49 + "," + sample.f50 + "," 
					 + sample.f51 + "," + sample.f52 + "," + sample.f53 + "," + sample.f54 + "," + sample.f55 + "," + sample.f56 + "," + sample.f57 + "," + sample.f58 + "," + sample.f59 + "," + sample.f60 + "," 
					 + sample.f61 + "," + sample.f62 + "," + sample.f63 + "," + sample.f64 + "," + sample.f65 + "," + sample.f66 + "," + sample.f67 + "," + sample.f68 + "," + sample.f69 + "," + sample.f70 + "," 
					 + sample.f71 + "," + sample.f72 + "," + sample.f73 + "," + sample.f74 + "," + sample.f75 + "," + sample.f76 + "," + sample.f77 + "," + sample.f78 + "," + sample.f79 + "," + sample.f80 + "," 
					 + sample.f81 + "," + sample.f82 + "," + sample.f83 + "," + sample.f84 + "," + sample.f85 + "," + sample.f86 + "," + sample.f87 + "," + sample.f88 + "," + sample.f89 + "," + sample.f90 + "," 
					 + sample.f91 + "," + sample.f92 + "," + sample.f93 + "," + sample.f94 + "," + sample.f95 + "," + sample.f96 + "," + sample.f97 + "," + sample.f98 + "," + sample.f99 + "," + sample.f100 + "," 
					 + "\r\n");
		 }
		 
		 return stringbuilder.toString();
	}
}
