package com.teng.hci.tabletdemos;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Created by Teng on 9/7/2015.
 */
public class SurfaceCallback implements SurfaceHolder.Callback {

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        final Surface surface = holder.getSurface();
        if ( surface == null ) return;
        final boolean invalidSurfaceAccepted; /* is before 4.0 */;
        if(android.os.Build.VERSION.SDK_INT > 14.0)
        {
            invalidSurfaceAccepted = false;
        }else
        {
            invalidSurfaceAccepted = true;
        }

        final boolean invalidSurface = ! surface.isValid();

        if ( invalidSurface && ( ! invalidSurfaceAccepted ) ) return;

        //VideoActivity.getSharedInstance().mediaPlayer.setDisplay(holder);

        /*
        if(VideoActivity.getSharedInstance().position > 0){
            VideoActivity.getSharedInstance().play(VideoActivity.getSharedInstance().position, R.raw.video_04);
            VideoActivity.getSharedInstance().position = 0;
        }*/
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(VideoActivity.getSharedInstance().mediaPlayer != null) {

            VideoActivity.getSharedInstance().mediaPlayer.stop();
            VideoActivity.getSharedInstance().mediaPlayer.release();
            VideoActivity.getSharedInstance().mediaPlayer = null;
        }
    }
}
