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
		String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_objects\\6\\features\\";
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
				if(values.length == 113)
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
				if(values.length == 113)
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
	
	
	public void combineFiles(String[] filenames, int size)  //e.g., t1, t2
	{
		
		//open a new file
		File dir = new File("C:\\Users\\Teng\\Documents\\TestDataFolder");
		String filename = "all" + filenames[0] + ".csv";
		File file = new File(dir, filename);
		if(!dir.exists())
			dir.mkdir();
		
		StringBuilder stringbuilder = new StringBuilder();
		
		//read files
		//file 1
		String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_mix_density_object_single\\12\\";
		
		
		for(int itrf = 0; itrf < size; itrf++)
		{
			String file1 = targetDir + filenames[itrf] + "f.csv";
			
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
					if(values.length == 113)
					{
						if(Double.parseDouble(values[10]) != 0)  //valid arrow
						{
							stringbuilder.append("" + (itrf+1) + ",");
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
		
		/*
		 * for height
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
			
		}*/
		
		//for density
		/*
		String surfix = "o";
		FileManager fm = new FileManager();
		
		for(int itra = 1; itra < 3; itra++)
		{
			for(int itri = 1; itri < 6; itri++)
			{
				for(int itrj = (itri+1); itrj < 7; itrj++)
				{
					String f1 = surfix + String.valueOf(itri) + "acc" + String.valueOf(itra);
					String f2 = surfix + String.valueOf(itrj) + "acc" + String.valueOf(itra);
					
					fm.combineFiles(f1, f2);
					
				}
			}
		}*/
		
		
		/*
		String surfix = "o";
		FileManager fm = new FileManager();
		
		for(int itra = 1; itra < 3; itra++)
		{
			String[] files = new String[6];
			for(int itri = 1; itri < 7; itri++)
			{
					String f1 = surfix + String.valueOf(itri) + "acc" + String.valueOf(itra);
					files[itri - 1] = f1;
			}
			
			fm.combineFiles(files, 6);
		}
		*/
		
		
		/*
		String surfix = "o";
		FileManager fm = new FileManager();
		
		for(int itra = 1; itra < 3; itra++)
		{
			for(int itri = 1; itri < 6; itri++)
			{
				for(int itrj = (itri+1); itrj < 7; itrj++)
				{
					String f1 = surfix + String.valueOf(itri) + "acc" + String.valueOf(itra);
					String f2 = surfix + String.valueOf(itrj) + "acc" + String.valueOf(itra);
					
					fm.combineFiles(f1, f2);
					
				}
			}
		}*/
		
		
		//for combinations
		FileManager fm = new FileManager();
		for(int itra = 1; itra < 3; itra++)
		{
			String[] files = new String[7];
			files[0] = "d1" + "acc" + String.valueOf(itra);
			files[1] = "d4"+ "acc" + String.valueOf(itra);
			files[2] = "o1"+ "acc" + String.valueOf(itra);
			files[3] = "o2"+ "acc" + String.valueOf(itra);
			files[4] = "o5"+ "acc" + String.valueOf(itra);
			files[5] = "o6"+ "acc" + String.valueOf(itra);
			files[6] = "sb"+ "acc" + String.valueOf(itra);
			
			fm.combineFiles(files, 7);
			
			
		}
		
		
	}

}
