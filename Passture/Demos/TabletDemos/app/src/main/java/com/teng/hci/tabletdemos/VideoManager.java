package com.teng.hci.tabletdemos;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Teng on 9/9/2015.
 */
public class VideoManager {

    //sd card
    private ArrayList<HashMap<String, String>> videosList = new ArrayList<HashMap<String, String>>();

    public VideoManager(){

    }

    public ArrayList<HashMap<String, String>> getPlayList(){
        File home = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/");

        if (home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("videoTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("videoPath", file.getPath());

                // Adding each song to SongList
                videosList.add(song);
            }
        }
        // return songs list array
        return videosList;
    }

    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp4") || name.endsWith(".MP4"));
        }
    }

}
