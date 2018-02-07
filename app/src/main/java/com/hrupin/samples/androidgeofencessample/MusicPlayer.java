package com.hrupin.samples.androidgeofencessample;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Gabriel Fernades (gab.frosa@gmail.com) on 07/02/2018.
 */

public class MusicPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    private static final String TAG = "MusicPlayer";

    //E1 path
    private String BASE_PATH = "/mnt/extsd/E1GRU/";
    private String AUDIO_PATH = BASE_PATH + "audio/";
    private String STOP1_PT = "terminal1_pt.wav";
    private String STOP1_EN = "terminal1_en.wav";
    private String STOP1_ES = "terminal1_es.wav";
    private String STOP2_PT = "terminal2_pt.wav";
    private String STOP2_EN = "terminal2_en.wav";
    private String STOP2_ES = "terminal2_es.wav";
    private String STOP3_PT = "terminal3_pt.wav";
    private String STOP3_EN = "terminal3_en.wav";
    private String STOP3_ES = "terminal3_es.wav";
    private String SOUND_1= null;
    private String SOUND_2= null;
    private String SOUND_3= null;


    private String sStopPoint = null;
    private int iStopPoint;

    private MediaPlayer mPlayer = null;
    private MediaPlayer mPlayer_next = null;


    public void  SetStopPoint(String point){
        this.sStopPoint = point;
        this.iStopPoint = Integer.parseInt(this.sStopPoint);
        Log.d(TAG, "::SetStopPoint::"+this.sStopPoint);
    }

    public void Prepare() throws IOException {
        if(iStopPoint == 2){
            this.SOUND_1 = AUDIO_PATH + STOP3_PT;
            this.SOUND_2 = AUDIO_PATH + STOP3_EN;
            this.SOUND_3 = AUDIO_PATH + STOP3_ES;
        }else if (iStopPoint == 1){
            this.SOUND_1 = AUDIO_PATH + STOP2_PT;
            this.SOUND_2 = AUDIO_PATH + STOP2_EN;
            this.SOUND_3 = AUDIO_PATH + STOP2_ES;
        }else{
            this.SOUND_1 = AUDIO_PATH + STOP1_PT;
            this.SOUND_2 = AUDIO_PATH + STOP1_EN;
            this.SOUND_3 = AUDIO_PATH + STOP1_ES;
        }
        Log.d(TAG, "::Prepare::\n"+this.SOUND_1+"\n"+this.SOUND_2+"\n"+this.SOUND_3);

        if (this.mPlayer == null) {
            this.mPlayer = new MediaPlayer();
            this.mPlayer.setDataSource(this.SOUND_1);
            Log.d(TAG,"setDataSource = "+ this.SOUND_1);

            this.mPlayer.prepareAsync();
            Log.d(TAG,"MediaPlayer has been prepareted...");
        }else{
            Log.d(TAG,"MediaPlayer already instantiated!!!");
        }
    }

    public void Play(){
        if(this.mPlayer != null){
            this.mPlayer.start();
            this.mPlayer.setLooping(true);
        }else{
            Log.e(TAG, "Error while executing!!! [mPlayer=null]");
        }
    }

    public void Stop(){
        Log.e(TAG, "Stop Running...");
        this.mPlayer.stop();
        this.mPlayer.release();
    }

    public void Finalize(){
        Log.e(TAG, "Finalize Running...");
        this.mPlayer.stop();
        this.mPlayer.release();
        this.mPlayer = null;
    }

    @Override
    public  void onCompletion(MediaPlayer mp){
        Log.e(TAG, "onCompletion Running...");
        this.Stop();
        //this.Finalize();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.e(TAG, "onPrepared Running...");
        //Play();
        //this.mPlayer.start();
    }
}
