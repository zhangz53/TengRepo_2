package com.example.android.wearable.jumpingjack;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Teng on 9/21/2015.
 */
class DataSample
{
    public float accX, accY, accZ;
    public long timeStamp;

    public DataSample(long _timeStamp, float _accX, float _accY, float _accZ){
        timeStamp = _timeStamp;
        accX = _accX;
        accY = _accY;
        accZ = _accZ;;
    }

    public static String toCSV(ArrayList<DataSample> arraylist)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Iterator<DataSample> iterator = arraylist.iterator();iterator.hasNext();  )
        {
            DataSample sample = iterator.next();


            stringbuilder.append( "" + sample.timeStamp + "," + sample.accX + "," + sample.accY + "," + sample.accZ + "," + "\r\n");

        }

        return stringbuilder.toString();
    }
}

public class DataStorage {
    private static Context context;
    public int storageIndex;
    private static DataStorage instance;

    //data to record
    private ArrayList<DataSample> samples;

    private DataStorage(Context c, int _storageIndex)
    {
        context = c;
        storageIndex = _storageIndex;

        samples = new ArrayList<DataSample>(10000);

    }

    public static DataStorage getInstance(Context c, int _storageIndex)
    {
        if(instance == null)
        {
            instance = new DataStorage(c, _storageIndex);
        }

        return instance;
    }

    public static boolean AddSample(long _timeStamp, float _accX, float _accY, float _accZ)
    {
        if(instance != null)
        {
            instance.addSample(_timeStamp, _accX, _accY, _accZ);
            return true;
        }

        return false;
    }

    public void addSample(long _timeStamp, float _accX, float _accY, float _accZ)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_timeStamp, _accX, _accY, _accZ);
            samples.add(sample);
        }
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

    public String save(String surfix)
    {
        if(samples == null || samples.size() ==   0)
        {
            return "";
        }

        Log.i("DataStorage", "end");
        if(surfix == null)
        {
            surfix = "";
        }

        if(!surfix.startsWith("_"))
        {
            surfix = "_" + surfix;
        }

        File dir;

        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WearDemo/");


        String time = String.valueOf(System.currentTimeMillis());
        String filename = time + surfix + "_samples.csv";

        File file = new File(dir, filename);

        if(!dir.exists())
        {
            dir.mkdir();
        }

        try
        {
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));

            outputstreamwriter.write( DataSample.toCSV(samples) );
            outputstreamwriter.close();
            Log.i("DataStorage", "write samples completes.");

        } catch (IOException e)
        {
            e.printStackTrace();
            Log.i("DataStorage", e.toString());
        }

        return surfix;
    }

}
