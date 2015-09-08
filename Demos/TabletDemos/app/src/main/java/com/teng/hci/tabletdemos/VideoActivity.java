package com.teng.hci.tabletdemos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.teng.hci.tabletdemos.util.SystemUiHider;

import java.io.IOException;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class VideoActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    public static MediaPlayer mediaPlayer;
    private static SurfaceView surfaceView;
    public static int duration;
    public static int position;

    public static VideoActivity instance;
    public static VideoActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new VideoActivity();
        }
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        instance = this;

        //video surface view
        mediaPlayer = new MediaPlayer();
        surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        play(0);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    //media play
    public void play(int position){
        try{
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(this, R.raw.vid);
            if(mediaPlayer != null)
            {
                mediaPlayer.stop();
            }
            //mediaPlayer.setDisplay(surfaceView.getHolder());  //problem
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new PrepareListener(position));
        }catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private final class PrepareListener implements MediaPlayer.OnPreparedListener {
        private int position;

        public PrepareListener(int position){
            super();
            this.position = position;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            duration = mediaPlayer.getDuration();
            mediaPlayer.start();
            if(position > 0) mediaPlayer.seekTo(position);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

}
