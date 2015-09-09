package com.teng.hci.tabletdemos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.teng.hci.tabletdemos.util.SystemUiHider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


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
    private static VideoManager videoManager;
    public ArrayList<HashMap<String, String>> videosList = new ArrayList<HashMap<String, String>>();

    private static SurfaceView surfaceView;
    public static int duration;
    public static int position;

    private static String delims = ",";

    private static int clipIndex = 0;
    private static int clipSum;

    public static VideoActivity instance;
    public static VideoActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new VideoActivity();
        }
        return instance;
    }

    private DataThread dataLogThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        instance = this;

        //video surface view
        mediaPlayer = new MediaPlayer();
        videoManager = new VideoManager();
        videosList = videoManager.getPlayList();
        clipSum = videosList.size();

        surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        play(0, clipIndex);

        //play(0, clips[clipIndex]);
        dataLogThread = new DataThread();
        dataLogThread.start();

    }

    private static class DataThread extends Thread{
        private boolean mRunning = false;
        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {
                dataLog();
            }
        }

        public void close() {
            mRunning = false;
        }
    }

    private static void dataLog()
    {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] tokens = BluetoothReceiver.getInstance().serialData.split(delims);
        //Log.d("serial", "" + tokens.length);
        if(tokens.length == 2)
        {
            int cmd = Integer.parseInt(tokens[0]);
            if(cmd == 1)
            {
                //next video
                clipIndex++;
                if(clipIndex == clipSum)
                    clipIndex = 0;

                VideoActivity.getSharedInstance().SwitchChannel(clipIndex);
            }else if(cmd == 2)
            {
                //previous video
            }else if(cmd == 3)
            {
                //increase volume
            }else if(cmd == 4)
            {
                //decrease volume
            }

            BluetoothReceiver.getInstance().serialData = "empty";
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void SwitchChannel(int videoClip)
    {
        Random rand = new Random();
        int tempPos = rand.nextInt(15000);
        play(tempPos, videoClip);
    }

    //media play
    public void play(int position, int videoClipIndex){

        try{
            mediaPlayer.reset();
            //mediaPlayer = MediaPlayer.create(VideoActivity.this, videoClip);
            mediaPlayer.setDataSource(videosList.get(videoClipIndex).get("videoPath"));
            //if(mediaPlayer != null)
            //{
              //  mediaPlayer.stop();
            //}
            //mediaPlayer.setDisplay(surfaceView.getHolder());  //problem
            mediaPlayer.prepareAsync();
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

        dataLogThread.close();

        try {
            BluetoothReceiver.CloseBT();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(mediaPlayer!=null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

}
