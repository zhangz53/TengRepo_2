package com.teng.hci.tabletdemos;

import com.teng.hci.tabletdemos.util.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.teng.hci.tabletdemos.R;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class BookActivity extends Activity implements ViewSwitcher.ViewFactory{

    ImageSwitcher imageSwitcher;

    //recipe images
    private static int[] recipes = new int[]{
            R.drawable.recipe_01,
            R.drawable.recipe_02,
            R.drawable.recipe_03,
            R.drawable.recipe_04,
            R.drawable.recipe_05,
            R.drawable.recipe_06,
            R.drawable.recipe_07
    };
    private static int recipeIndex = 0;
    private static int recipeSum = 7;

    private static String delims = ",";

    private DataThread2 dataLogThread2;

    public static BookActivity instance;
    public static BookActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new BookActivity();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        instance = this;

        imageSwitcher = (ImageSwitcher)findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(this);

        imageSwitcher.setImageResource(R.drawable.recipe_01);

        dataLogThread2 = new DataThread2();
        dataLogThread2.start();

    }

    private static class DataThread2 extends Thread{
        private boolean mRunning = false;
        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {
                dataLog2();
            }
        }

        public void close() {
            mRunning = false;
        }
    }

    private static void dataLog2()
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
                recipeIndex++;
                if(recipeIndex == recipeSum)
                    recipeIndex = 0;

                BookActivity.getSharedInstance().switchImage(recipes[recipeIndex]);

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


    private void switchImage(final int recipe) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(imageSwitcher.getContext(),
                        android.R.anim.slide_in_left));
                imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(imageSwitcher.getContext(),
                        android.R.anim.slide_out_right));

                imageSwitcher.setImageResource(recipe);
            }
        });

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

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

        dataLogThread2.close();

        try {
            BluetoothReceiver.CloseBT();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }


    @Override
    public View makeView() {
        ImageView iView = new ImageView(this);
        iView.setScaleType(ImageView.ScaleType.FIT_XY);
        iView.setLayoutParams(new
                ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        iView.setBackgroundColor(0x0099cc);
        return iView;
    }



}
