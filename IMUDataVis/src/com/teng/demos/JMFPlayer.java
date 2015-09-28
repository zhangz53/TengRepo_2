package com.teng.demos;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.JPanel;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

class MediaPanel extends JPanel
 {
      	public MediaPanel( URL mediaURL )
      	{
         	setLayout( new BorderLayout() ); // use a BorderLayout
   
         	// Use lightweight components for Swing compatibility
         		Manager.setHint( Manager.LIGHTWEIGHT_RENDERER, true );
         
         	try
         	{
            	// create a player to play the media specified in the URL
            	Player mediaPlayer = Manager.createRealizedPlayer( mediaURL );
   
            	// get the components for the video and the playback controls
            	Component video = mediaPlayer.getVisualComponent();
            	Component controls = mediaPlayer.getControlPanelComponent();
            
            	if ( video != null )
               	add( video, BorderLayout.CENTER ); // add video component
   
            	if ( controls != null )
               	add( controls, BorderLayout.SOUTH ); // add controls
   
            	mediaPlayer.start(); // start playing the media clip
         	} // end try
         	catch ( NoPlayerException noPlayerException )
         	{
            	System.err.println( "No media player found" );
         	} // end catch
         	catch ( CannotRealizeException cannotRealizeException )
   	{
            	System.err.println( "Could not realize media player" );
         	} // end catch
         	catch ( IOException iOException )
	   {
            	System.err.println( "Error reading from the source" );
         	} // end catch
	      } // end MediaPanel constructor
   	} // end class MediaPanel

	public class JMFPlayer{
	// launch the application
	      	public static void main( String args[] )
	      	{
	         	// create a file chooser
	         	JFileChooser fileChooser = new JFileChooser();
	   
	         	// show open file dialog
	         	int result = fileChooser.showOpenDialog( null );
	   
	        	if ( result == JFileChooser.APPROVE_OPTION ) // user chose a file
	        	{
	            	URL mediaURL = null;
	   
	           	try
	            	{
	               	// get the file as URL
	               	mediaURL = fileChooser.getSelectedFile().toURL();
	             } // end try
	            	catch ( MalformedURLException malformedURLException )
	            	{
	               	System.err.println( "Could not create URL for the file" );
	            	} // end catch
	   
	            	if ( mediaURL != null ) // only display if there is a valid URL
	            	{
	               	JFrame mediaTest = new JFrame( "Media Tester" );
	               	mediaTest.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	   
	               	MediaPanel mediaPanel = new MediaPanel( mediaURL );
	               	mediaTest.add( mediaPanel );
	   
	               	mediaTest.setSize( 300, 300 );
	               	mediaTest.setVisible( true );
	            	} // end inner if
	         	} // end outer if
	      	} // end main
}
