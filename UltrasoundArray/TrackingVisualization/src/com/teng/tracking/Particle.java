package com.teng.tracking;

public class Particle {

	float diameter = 9;
	float diameterF = diameter;
	  
	float locX;
	float locY;
	float locZ;
	
	public Particle(float diameter,float tx, float ty, float tz)
	{
		locX = tx;
	    locY = ty;
	    locZ = tz;		
	}	 
}
