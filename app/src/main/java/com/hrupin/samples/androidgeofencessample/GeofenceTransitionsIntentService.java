package com.hrupin.samples.androidgeofencessample;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.hrupin.samples.androidgeofencessample.db.GeofenceStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gabriel Fernandes 12/17.
 */
public class  GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GTIntentService";

    private MediaPlayer mMediaPlayer = null;
    private MusicPlayer mPlayer = null;

    //String BASE_PATH = "/sdcard/E1GRU/"; //Moto G Path
    private String BASE_PATH = "/mnt/extsd/E1GRU/"; //E1 path
    private String IMAGE_PATH = BASE_PATH + "image/";
    private String AUDIO_PATH = BASE_PATH + "audio/";
    private String VIDEO_PATH = BASE_PATH + "video/";
    private String GPS_FILE = BASE_PATH + "GPS_GRU.json";
    private String IMG_BACKGROUND = IMAGE_PATH + "bg.png";

    GPSTracker gps = null;
    private double latitude;
    private double longitude;
    //private double latitudeGeo1;
    //private double longitudeGeo1;
    //private double latitudeGeo2;
    //private double longitudeGeo2;
    //private double latitudeGeo3;
    //private double longitudeGeo3;
    private String geofenceCurrent;
    private String Buff;

    private GeofenceStorage geofenceStorage = null;
    private Context mContext = null;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        mContext = null;
    }

    public void setGeoStorage(GeofenceStorage geoStoreage) {

        Log.i(TAG, "Tryng setGeoStorage ");

        if (gps == null){
            //this.gps = new GPSTracker(ctx.getApplicationContext());
            Log.i(TAG, "Seting Storage...");
            this.geofenceStorage = geoStoreage;//.getApplicationContext();
            //this.geofenceStorage
        }else{
            Log.i(TAG, "Storage already seted!");
        }
    }

    public void setContext(Context ctx) {

        Log.i(TAG, "Tryng setContext");

        if (gps == null){
            //this.gps = new GPSTracker(ctx.getApplicationContext());
            Log.i(TAG, "Seting Context...");
            mContext = ctx.getApplicationContext();
            Toast.makeText(mContext, "CONTEXT SETED!!!!", Toast.LENGTH_LONG).show();

        }else{
            Log.i(TAG, "Context already seted!");
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        //Toast.makeText(mContext, "onHandleIntent", Toast.LENGTH_LONG).show();

        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(this, geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
         //geofenceTransition = geofencingEvent.getGeofenceTransition();


        Location location = geofencingEvent.getTriggeringLocation();
        Log.d(TAG, "LocationC: " + location.getLatitude() + ", " + location.getLongitude());

        //this.latitudeGeo1 = location.getLatitude();
        //this.longitudeGeo1 = location.getLongitude();


        if(mContext!= null){
            Toast.makeText(mContext, "location: "+location, Toast.LENGTH_LONG).show();
        }

        Log.i("GAAAAAAAAAAAAAAAAAAAAR", "geofenceTransition: " + geofenceTransition);

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            Log.i("GAAAAAAAAAAAAAAAAAAAAR", "GEOFENCE_TRANSITION_ENTER !!!");

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            //Log.i(TAG, geofenceTransitionDetails);
            Log.i("GAAAAAAAAAAAAAAAAAAAAR", "geofenceTransitionDetails:"+ geofenceTransitionDetails + "!");
            this.geofenceCurrent = geofenceTransitionDetails;
            Log.i("GAAAAAAAAAAAAAAAAAAAAR","this.geofenceCurrent:"+this.geofenceCurrent);

                    ///*
            if(gps != null){
                Log.d(TAG, "gps OK ");
                // verifica ele
                if (gps.canGetLocation()) {
                    // passa sua latitude e longitude para duas variaveis
                    this.latitude = gps.getLatitude();
                    this.longitude = gps.getLongitude();

                    // e mostra no Toast
                    //Toast.makeText(getApplicationContext(), "Sua localização - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "LAT: "+latitude);
                    Log.d(TAG, "LONG: "+longitude);
                }else{
                    Log.d(TAG, "can NOT GetLocation ");
                }

            }else{
                Log.d(TAG, "gps NOT OK ");
            }
            //*/




            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);

        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {


        String geofenceTransitionString = getTransitionString(geofenceTransition);
        ArrayList triggeringGeofencesIdsList = new ArrayList();

        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
            Buff = geofence.getRequestId();
            Log.i("GAAAAAAFR", "Buff:"+Buff);

        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        Log.i("GAAAAAAAAAAAAAAR", "triggeringGeofencesIdsString: "+triggeringGeofencesIdsString);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;

        //MapView mapView = (MapView) findViewById(R.id.map_view) ;
        //mapView.setClickable(true) ;
        //int measure = mapView.getMeasuredState();


    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(defaultSoundUri);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        loadIndication();

        // Issue the notification
        //mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }


    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();

        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return mResources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return mResources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return mResources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return mResources.getString(R.string.unknown_geofence_error);
        }
    }

    //GAFR
    private void loadIndication() {

        //mMediaPlayer = new MediaPlayer();
        if (mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }
        Log.i(TAG, "loadIndication running ");
        Log.i(TAG, "LAT: "+ this.latitude);
        Log.i(TAG, "LONG: "+ this.longitude);
        //setNewAudioDataFile();

        mPlayer = new MusicPlayer();
        mPlayer.SetStopPoint(this.Buff);
        try {
            mPlayer.Prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.Play();
        //mPlayer.Finalize();
    }

    //GAFR
    /*
    private void setNewAudioDataFile() {
        try {
            String audioPath = AUDIO_PATH + "error";
            String audioPath_es = AUDIO_PATH + "error";
            String audioPath_en = AUDIO_PATH + "error";

            int buff = Integer.parseInt(this.Buff);

            if(buff == 0){
                audioPath = AUDIO_PATH + "terminal1_en.wav";
                audioPath_es = AUDIO_PATH + "terminal1_es.wav";
                audioPath_en = AUDIO_PATH + "terminal1_pt.wav";
                Log.d(TAG, "setNewAudioDataFile = " + audioPath);
            }else if(buff == 1){
                audioPath = AUDIO_PATH + "terminal2_en.wav";
                audioPath_es = AUDIO_PATH + "terminal2_es.wav";
                audioPath_en = AUDIO_PATH + "terminal2_pt.wav";
                Log.d(TAG, "setNewAudioDataFile = " + audioPath);
            }else if(buff == 2){
                audioPath = AUDIO_PATH + "terminal3_en.wav";
                audioPath_es = AUDIO_PATH + "terminal3_es.wav";
                audioPath_en = AUDIO_PATH + "terminal3_pt.wav";
                Log.d(TAG, "setNewAudioDataFile = " + audioPath);
            }else{
                Log.d(TAG, "setNewAudioDataFile = Point is not 1 or 2 or 3!!");
            }

            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        };
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
    */
}
