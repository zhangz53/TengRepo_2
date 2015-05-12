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
}
