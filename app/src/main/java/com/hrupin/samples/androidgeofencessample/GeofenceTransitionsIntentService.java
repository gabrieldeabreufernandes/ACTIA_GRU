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
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.hrupin.samples.androidgeofencessample.db.GeofenceStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gabriel Fernandes 12/17.
 */
public class  GeofenceTransitionsIntentService extends IntentService implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "GTIntentService";

    private MediaPlayer mMediaPlayer = null;
    private MusicPlayer mPlayer = null;

    private String BASE_PATH = "/mnt/internal_sd/E1GRU/"; //E1-PRO //internal_sd
    private String AUDIO_PATH = BASE_PATH + "audio/";
    private String sStopPoint = null;
    private int iStopPoint;

    GPSTracker gps = null;
    private double latitude;
    private double longitude;
    private String geofenceCurrent;
    private String Buff;

    private GeofenceStorage geofenceStorage = null;
    private Context mContext = null;

    private int gFenceType;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        mContext = null;
        mPlayer = new MusicPlayer();
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
        //if (gps == null){
        //this.gps = new GPSTracker(ctx.getApplicationContext());
        //Log.i(TAG, "Seting Context...");
        mContext = ctx.getApplicationContext();
        Log.i(TAG, "CONTEXT SETED!!!!");
        //Toast.makeText(mContext, "CONTEXT SETED!!!!", Toast.LENGTH_LONG).show();
        //}else{
        //    Log.i(TAG, "Context already seted!");
        //}
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

        Location location = geofencingEvent.getTriggeringLocation();
        Log.d(TAG, "LocationC: " + location.getLatitude() + ", " + location.getLongitude());

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
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        this.gFenceType = transitionType;
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

    private void  SetStopPoint(String point){
        this.sStopPoint = point;
        this.iStopPoint = Integer.parseInt(this.sStopPoint);
        Log.d(TAG, "::SetStopPoint::"+this.sStopPoint);
    }


    //GAFR
    private void loadIndication() {

        final MediaPlayer mpp;
        final MediaPlayer mpp2;
        MediaPlayer mpp3 = null;

        Log.i(TAG, "loadIndication running...");

        SetStopPoint(this.Buff);

        //ENTERING TERMINAL 1
        if(this.iStopPoint == 0){
            Log.i(TAG, "MediaPlayer creation for Entering to Point 1!!!");
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal1_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal1_en);
            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation for Point 1!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}

        //EXITING TERMINAL 1
        }else if(this.iStopPoint == 1){
            Log.i(TAG, "MediaPlayer creation for Exiting to Point 1!!!");
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal2_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal2_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal2_es);

            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation entering of the Point 2!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
                //mpp3.start();
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}

        //ENTERING TERMINAL 2
        }else if(this.iStopPoint == 2){
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal2_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal2_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal2_es);

            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation for entering of the Point 3!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}

        //EXITING TERMINAL 2
        }else if(this.iStopPoint == 3){
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal3_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal3_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal3_es);

            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation for entering of the Point 3!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}

        //ENTERING TERMINAL 3
        }else if(this.iStopPoint == 4){
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal3_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal3_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal3_es);

            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation for entering of the Point 3!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}

        //EXITING TERMINAL 3
        }else if(this.iStopPoint == 5){
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
            mpp = MediaPlayer.create(this, R.raw.terminal1_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal1_en);
            //mpp3 = MediaPlayer.create(this, R.raw.terminal1_es);

            if(mpp != null){
                Log.i(TAG, "MediaPlayer creation for entering of the Point 3!!!");
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                //mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            //}
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.e(TAG, "Finalize Running...");
        mediaPlayer.stop();
        mediaPlayer.release();
        //mediaPlayer = null;
    }
}
