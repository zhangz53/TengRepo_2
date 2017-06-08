package com.teng.tracking;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;

public class TrackingVisualization extends PApplet{
	
	float xOrigin=0;
	float yOrigin=0;
	float t=0;

	ArrayList<Particle> particles = new ArrayList<Particle>();
	
	public void settings(){
		size(1000, 1000, "processing.opengl.PGraphics3D");
    }

    public void setup(){
    	
    	smooth();
    	background(0);
    //	camera(500.0f, 500.0f, 1000.0f, 500.0f, 500.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    	
    	//test
    	Random rand = new Random();
    	for(int itr = 0; itr < 1000; itr++)
    	{
    		float dx = rand.nextFloat() * 500;
    		float dy = rand.nextFloat() * 500;
    		float dz = rand.nextFloat() * 500;
    		Particle p = new Particle(10, dx, dy, dz);
    		particles.add(p);
    	}
    }
    
    public void draw(){	  
    	
    	translate(0,200,0);
    	translate(0, 250, 0);
    	rotateX(radians(25));
    	translate(0, -450, 0);
    	    
    	background(0);
    	lights();
    	directionalLight(175, 175, 175, 0, 1, 0);
    	
    	for(int itrp = 0; itrp < particles.size(); itrp++)
    	{
    		Particle par = particles.get(itrp);
    		pushMatrix();
            translate(par.locX, par.locY, par.locZ-240);
            noStroke();
            fill(255,255,0);
            sphere(1);
            popMatrix();
    	}
    	
    	
    }
    
    
    public void keyPressed() {
    	if (key == 'q') {
    	    exit();
    	}
    }
	
	public static final void main(String args[]){
		PApplet.main(new String[]{"--present", "com.teng.tracking.TrackingVisualization"});
	}

}
