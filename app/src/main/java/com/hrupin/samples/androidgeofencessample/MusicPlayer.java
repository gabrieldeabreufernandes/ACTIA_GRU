package com.hrupin.samples.androidgeofencessample;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Gabriel Fernades (gab.frosa@gmail.com) on 07/02/2018.
 */

public class MusicPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    private static final String TAG = "MusicPlayer";

    //E1 path
    //private String BASE_PATH = "/mnt/extsd/E1GRU/";
    //private String BASE_PATH = "/mnt/external_sd/E1GRU/"; //E1-PRO
    private String BASE_PATH = "/mnt/internal_sd/E1GRU/"; //E1-PRO //internal_sd
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
    private MediaPlayer mPlayer_PT = null;
    private MediaPlayer mPlayer_EN = null;
    private MediaPlayer mPlayer_ES = null;

    private Context mContext = null;


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
            //this.mPlayer = new MediaPlayer();
            //this.mPlayer = MediaPlayer.create(this, R.raw.terminal1_pt);
            try {
                //this.mPlayer.setDataSource(this.SOUND_1);
                this.mPlayer.setOnErrorListener((MediaPlayer.OnErrorListener) this);
                this.mPlayer.prepareAsync();

                this.mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }// catch (IOException e) {
             //   e.printStackTrace();
            //}


            /*
            this.mPlayer.setDataSource(this.SOUND_1);
            Log.d(TAG,"setDataSource = "+ this.SOUND_1);

            this.mPlayer.setOnPreparedListener(this);
            this.mPlayer.prepareAsync();
            */
            Log.d(TAG,"MediaPlayer has been prepareted...");
        }else{
            Log.d(TAG,"MediaPlayer already instantiated!!!");
        }
    }

    //public void PlayPath(String PATH){
    public void PlayPath(){
        if (this.mPlayer == null) {
            //this.mPlayer = new MediaPlayer();
            //this.mPlayer = MediaPlayer.create(this, R.raw.terminal1_pt);
            try {
                //this.mPlayer.setDataSource(PATH);
                this.mPlayer.setOnErrorListener((MediaPlayer.OnErrorListener) this);
                this.mPlayer.prepareAsync();

                this.mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }// catch (IOException e) {
             //   e.printStackTrace();
            //}


            /*
            this.mPlayer.setDataSource(this.SOUND_1);
            Log.d(TAG,"setDataSource = "+ this.SOUND_1);

            this.mPlayer.setOnPreparedListener(this);
            this.mPlayer.prepareAsync();
            */
            Log.d(TAG,"MediaPlayer has been prepareted...");
        }else{
            Log.d(TAG,"MediaPlayer already instantiated!!!");
        }
    }


    public void Play(){
        if(this.mPlayer != null){
            if(!(this.mPlayer.isPlaying())) {
                this.mPlayer.start();
            }
            //this.mPlayer.setLooping(true);
        }else{
            Log.e(TAG, "Error while executing!!! [mPlayer=null]");
        }
    }

    public void Stop(){
        Log.e(TAG, "Stop Running...");
        if(!(mPlayer.isPlaying())) {
            Log.e(TAG, "Music not running, lets stop...");
            this.mPlayer.stop();
            this.mPlayer.release();
        }else{
            Log.e(TAG, "The song is still running!!");
        }
    }

    public void TryFinalize(){
        Log.e(TAG, "TryFinalize Running...");
        if(!(this.mPlayer.isPlaying())){
            this.mPlayer.stop();
            this.mPlayer.release();
            this.mPlayer = null;
            Log.e(TAG, "Player Finished!");
        }else{
            Log.e(TAG, "Does not possible finalize the player!!!");
        }
    }

    public void Finalize(){
        Log.e(TAG, "Finalize Running...");
        this.mPlayer.stop();
        this.mPlayer.release();
        this.mPlayer = null;
    }

    //public void setMP(MediaPlayer mpPt, MediaPlayer mpEn, MediaPlayer mpEs){
    public void setMP(MediaPlayer mpPt){

        Log.e(TAG, "setMP Running...");
        this.mPlayer = mpPt;
        this.mPlayer_PT = mpPt;
        //this.mPlayer_EN = mpEn;
        //this.mPlayer_ES = mpEs;
    }

    @Override
    public  void onCompletion(MediaPlayer mp){
        Log.e(TAG, "onCompletion Running...");
        this.Stop();
        this.Finalize();
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.e(TAG, "onPrepared Running...");
        //Play();
        //this.mPlayer.start();
        mediaPlayer.start();
    }

    public void setContext(Context ctx) {
        Log.i(TAG, "setContext running...");
        mContext = ctx;//.getApplicationContext();
        Log.i(TAG, "CONTEXT SETED!!!!");
    }
}