package com.teng.svm;

public class ObjectExp {

	private ModifiedSVMTrain svmTrain;
	private String filesurfix = "all";
	private String rawfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_mix_density_object_single\\result_raw.csv";
	private String accuracyfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_mix_density_object_single\\result_accuracy.csv";
	
	
	public ObjectExp()
	{
		
	}
	
	public void runExp()
	{		
		
		for(int itrp = 1; itrp < 13; itrp++)  //1 - 13
		{
			
			String surfix = filesurfix;
			
			for(int itra = 1; itra < 3; itra++)
			{			
				String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_mix_density_object_single\\";
				String participant = String.valueOf(itrp);
				String targetfile = targetDir + participant + "\\" + filesurfix + "acc" + String.valueOf(itra) + ".scale";
							
				int densityIndex =  0;
				svmTrain = new ModifiedSVMTrain(targetfile, rawfile, accuracyfile, itrp, itra, densityIndex);
					
				
			}
				
		}
	}
	
	
	
	public static void main(String argv[])
	{
		ObjectExp exp = new ObjectExp();
		exp.runExp();
	}
}
