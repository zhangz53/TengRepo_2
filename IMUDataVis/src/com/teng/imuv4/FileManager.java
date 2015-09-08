package com.teng.imuv4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.teng.phdata.DataSample;

/**
 * 
 * @author Teng
 *
 * To produce csv files
 */

public class FileManager {
	
	public String line = "";
	private String splitBy = ",";
	public BufferedReader br;
	double negLabel = -1.0;
	double posLabel = 1.0;
	int targetSize = 71;
	
	
	public FileManager()
	{
		//initialize
	}
	
	public void combineFiles(String file1name, String file2name)  //e.g., t1, t2
	{
		
		//open a new file
		File dir = new File("C:\\Users\\Teng\\Documents\\TestDataFolder");
		String filename = file1name + "_" + file2name + ".csv";
		File file = new File(dir, filename);
		if(!dir.exists())
			dir.mkdir();
		
		StringBuilder stringbuilder = new StringBuilder();
		
		//read files
		//file 1
		String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\12\\features\\";
		String file1 = targetDir + file1name + "f.csv";
		String file2 = targetDir + file2name + "f.csv";
		
		try{
			br = new BufferedReader(new FileReader(file1));
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				if(values.length == 101)
				{
					if(Double.parseDouble(values[10]) != 0)  //valid arrow
					{
						stringbuilder.append("" + negLabel + ",");
						for(int itr = 1; itr < targetSize; itr++)
						{
							stringbuilder.append(values[itr] + ",");
						}
						stringbuilder.append("\r\n");
					}
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//file 2
		try{
			br = new BufferedReader(new FileReader(file2));
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while((line = br.readLine()) != null)
			{
				String[] values = line.split(splitBy);
				if(values.length == 101)
				{
					if(Double.parseDouble(values[10]) != 0)  //valid arrow
					{
						stringbuilder.append("" + posLabel + ",");
						for(int itr = 1; itr < targetSize; itr++)
						{
							stringbuilder.append(values[itr] + ",");
						}
						stringbuilder.append("\r\n");
					}
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//write and save
		try {
	        OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));
	
	        outputstreamwriter.write(stringbuilder.toString() );
	        outputstreamwriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
		
		System.out.println("saved");
		
	}
	
	public static final void main(String args[])
	{
		String[] filesurfix = new String[]{"p", "pg", "t", "tg"};
		
		FileManager fm = new FileManager();
		
		for(int itrf = 0; itrf < 4; itrf++)
		{
			String surfix = filesurfix[itrf];
			for(int itri = 0; itri < 8; itri++)
			{
				for(int itrj = (itri+1); itrj < 9; itrj++)
				{
					String f1 = surfix + String.valueOf(itri);
					String f2 = surfix + String.valueOf(itrj);
					
					fm.combineFiles(f1, f2);
				}
			}
			
		}
		
	}

}
