package com.teng.tracking;

import processing.core.PApplet;

public class TrackingVisualization extends PApplet{
	
	public void settings(){
		print("hello\n");
		fullScreen();
		
    }

    public void setup(){
    	
    	
    }
    
    public void draw(){
    	background(255);
    }
    
    
    public void keyPressed() {
    	if (key == 'q') {
    	    exit();
    	}
    }
	
	public static final void main(String args[]){
		PApplet.main("com.teng.tracking.TrackingVisualization");
	}

}
