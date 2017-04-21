package com.teng.svm;

public class DensityExp {
	private ModifiedSVMTrain svmTrain;
	private String filesurfix = "o";
	private String rawfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_objects\\result_raw.csv";
	private String accuracyfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_objects\\result_accuracy.csv";
	
	
	public DensityExp()
	{
		//svmTrain = new ModifiedSVMTrain("C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\1\\features\\p0_p2.scale");
	}
	
	public void runExp()
	{		
		
		for(int itrp = 1; itrp < 13; itrp++)  //1 - 13
		{
			
			String surfix = filesurfix;
			
			for(int itra = 1; itra < 3; itra++)
			{
				for(int itri = 1; itri < 6; itri++)  //density 1 - 5
				{
					for(int itrj = (itri+1); itrj < 7; itrj++)  //density rest to 6
					{
							String f1 = surfix + String.valueOf(itri) + "acc" + String.valueOf(itra);
							String f2 = surfix + String.valueOf(itrj) + "acc" + String.valueOf(itra);
							
							String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_objects\\";
							String participant = String.valueOf(itrp);
							String targetfile = targetDir + participant + "\\features\\" + f1 + "_" + f2 + ".scale";
							
							int densityIndex =  itri * 10 + itrj;
							svmTrain = new ModifiedSVMTrain(targetfile, rawfile, accuracyfile, itrp, itra, densityIndex);
					}
				}
			}
				
		}
	}
	
	
	public static void main(String argv[])
	{
		DensityExp exp = new DensityExp();
		exp.runExp();
	}
}
