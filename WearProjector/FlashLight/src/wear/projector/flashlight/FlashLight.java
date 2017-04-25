package wear.projector.flashlight;

import java.util.ArrayList;

import processing.core.PApplet;

public class FlashLight extends PApplet{

	private int mapW;
	private int mapH;
	private int windowW;
	private int windowH;
	private float scaleIndex = 5.0f;
	
	private ArrayList<int[]> targets;
	private int targetW = 200;
	private int targetH = 200;
	
	
	private int windowX = 0;
	private int windowY = 0;
	private int moveStep = 100;
	
	public void settings(){
		print("hello\n");
		//size(1280,800);
		fullScreen();
		
    }

    public void setup(){
    	background(255);
    	
    	windowW = width;
		windowH = height;
		mapW = (int)scaleIndex * windowW;
		mapH = (int)scaleIndex * windowH;
		//print(" "+ windowSizeW);
		
		targets = new ArrayList<int[]>();
		//generate random targets
		for(int itrt = 0; itrt < 10; itrt++)
		{
			int rx = (int)random(mapW);
			int ry = (int)random(mapH);
			int[] rt = {rx, ry};
			targets.add(rt);
		}
		
    }
    
    public void draw(){
    	background(255);
    	
    	fill(200, 0, 0, 150);
    	noStroke();
    	for(int itrt = 0; itrt < 10; itrt++)
		{
			int tx = targets.get(itrt)[0];
			int ty = targets.get(itrt)[1];
			
			//if(tx > windowX && ty > windowY && tx < (windowX + windowW) && ty < (windowY + windowH))
			//{
				//draw the target
				
			//}
			
			ellipse(tx - windowX, ty - windowY, targetW, targetH);
		}
    	
    	//determine windowX and windowY
    	windowX = mouseX * (int)scaleIndex;
    	windowY = mouseY * (int)scaleIndex;
    	if(windowX < 0)
		{
			windowX = 0;
		}else if((windowX + windowW) > mapW)
		{
			windowX = mapW - windowW;
		}
    	
    	if(windowY < 0 )
		{
			windowY = 0;
		}else if((windowY + windowH) > mapH)
		{
			windowY = mapH - windowH;
		}
    	
    	//draw flashing boarder
    	stroke(200, 0 , 0, 150);
    	strokeWeight(10);
    	noFill();
    	rect(0 - windowX, 0 - windowY, mapW, mapH);
    	
    	
    	//draw a overview
    	stroke(100, 100, 100, 150);
    	noFill();
    	strokeWeight(2);
    	rect(9.0f * windowW / 10.0f - 100, 100, 1.0f *windowW/10.0f, 1.0f * windowH/10.0f);
    	rect(9.0f * windowW / 10.0f - 100 + (float)(windowX * 1.0f / mapW) * (1.0f *windowW/10.0f), 100 + (float)(windowY * 1.0f/ mapH) * windowH / 10.0f, windowW/(10.0f * scaleIndex), windowH/(10.0f*scaleIndex));
    	
    	//draw overview targets
    	fill(200, 0, 0, 150);
    	noStroke();
    	
    	
    	
    	
    }
	
    public void keyPressed() {
    	if (key == 'q') {
    	    exit();
    	}else if(key == 'a')
    	{
    		windowX -= moveStep;
    		if(windowX < 0)
    		{
    			windowX = 0;
    		}
    	}else if(key == 'd')
    	{
    		windowX+=moveStep;
    		if((windowX + windowW) > mapW)
    		{
    			windowX = mapW - windowW;
    		}
    	}else if(key == 'w')
    	{
    		windowY-=moveStep;
    		if(windowY < 0 )
    		{
    			windowY = 0;
    		}
    	}else if(key == 's')
    	{
    		windowY+=moveStep;
    		if((windowY + windowH) > mapH)
    		{
    			windowY = mapH - windowH;
    		}
    	}
    	
    }
    
	public static final void main(String args[]){
		PApplet.main("wear.projector.flashlight.FlashLight");
	}
}
