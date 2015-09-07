package com.teng.svm;

public class HeightExp {

	private ModifiedSVMTrain svmTrain;
	private String[] filesurfix = new String[]{"p", "pg", "t", "tg"};
	private String rawfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\result_raw.csv";
	private String accuracyfile = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\result_accuracy.csv";
	
	
	public HeightExp()
	{
		//svmTrain = new ModifiedSVMTrain("C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\1\\features\\p0_p2.scale");
	}
	
	public void runExp()
	{		
		
		for(int itrp = 1; itrp < 12; itrp++)   //1-11, 12 not done
		{
			for(int itrf = 0; itrf < 4; itrf++)
			{
				String surfix = filesurfix[itrf];
				for(int itri = 0; itri < 8; itri++)
				{
					for(int itrj = (itri+1); itrj < 9; itrj++)
					{
						String f1 = surfix + String.valueOf(itri);
						String f2 = surfix + String.valueOf(itrj);
						
						String targetDir = "C:\\Users\\Teng\\Documents\\TestDataFolder\\formal_height\\";
						String participant = String.valueOf(itrp);
						String targetfile = targetDir + participant + "\\features\\" + f1 + "_" + f2 + ".scale";
						
						int heightIndex =  itri * 10 + itrj;
						svmTrain = new ModifiedSVMTrain(targetfile, rawfile, accuracyfile, itrp, itrf, heightIndex);
					}
				}
				
			}
		}
	}
	
	
	public static void main(String argv[])
	{
		HeightExp exp = new HeightExp();
		exp.runExp();
	}
}
