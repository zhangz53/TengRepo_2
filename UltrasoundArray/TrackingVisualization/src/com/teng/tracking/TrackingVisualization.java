package com.teng.tracking;

import java.io.IOException;
import java.util.ArrayList;
import processing.core.PApplet;

public class TrackingVisualization extends PApplet{
	
	float xOrigin=0;
	float yOrigin=0;
	float zOrigin=0;
	float t=0;
	int screenWidth;
	int screenHeight;
	
	ArrayList<Particle> particles = new ArrayList<Particle>();
	
	public static TrackingVisualization instance;
	public static TrackingVisualization getInstance()
	{
		if(instance == null)
		{
			instance = new TrackingVisualization();
		}
		return instance;
	}
	
	private SocketConnector socketConnector;
	
	public void settings(){
		fullScreen("processing.opengl.PGraphics3D");
    }

    public void setup(){
    	instance = this;
    	smooth();
    	background(0);
    	
    	screenWidth = width;
    	screenHeight = height;
    	xOrigin = screenWidth / 2.0f;
    	yOrigin = screenHeight * 1.0f;
    	
    	camera(800.0f, 500.0f, 1000.0f, screenWidth / 2.0f, screenHeight / 2.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    	
    	try {
			socketConnector = new SocketConnector();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	println("" + socketConnector.getIpAddress());
    }
    
    public void draw(){	  
    	background(0);
    	lights();
    	directionalLight(175, 175, 175, 0, 1, 0);
    	
    	//reference coordinates
    	stroke(204, 102, 0);
    	strokeWeight(4);
    	line(xOrigin, yOrigin, zOrigin, 1000, yOrigin, zOrigin);
    	line(xOrigin, yOrigin, zOrigin, xOrigin, 0, zOrigin);
    	line(xOrigin, yOrigin, zOrigin, xOrigin, yOrigin, -1000);
    	
    	
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
    
    public void addParticle(float px, float py, float pz)
    {
    	//need some transformation
    	
    	Particle pp = new Particle(10, px, py, pz);
    	particles.add(pp);
    }
    
    public void keyPressed() {
    	if (key == 'q') {
    		socketConnector.Destroy();
    	    exit();
    	}
    }
	
	public static final void main(String args[]){
		PApplet.main(new String[]{"--present", "com.teng.tracking.TrackingVisualization"});
	}

}
