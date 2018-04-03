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

    //GPSTracker gps = null;
    private double latitude;
    private double longitude;
    private String geofenceCurrent;
    private String Buff;

    private GeofenceStorage geofenceStorage = null;
    private Context mContext = null;

    private int gFenceType;
    private int lastPt = 0;
    private boolean pt1 = false;
    private boolean pt2 = false;
    private boolean pt3 = false;
    private boolean pt3out = false;
    private boolean lockPt1 = false;
    private boolean lockPt2 = false;
    private boolean lockPt3 = false;

    MediaPlayer mpp = null;
    MediaPlayer mpp2 = null;
    MediaPlayer mpp3 = null;


    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        mContext = null;
        mPlayer = new MusicPlayer();
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
        Log.i(TAG, "GAAAAFR:: onHandleIntent running");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

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

            this.gFenceType = geofenceTransition;

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            //Log.i(TAG, geofenceTransitionDetails);
            Log.i("GAAAAAAAAAAAAAAAAAAAAR", "geofenceTransitionDetails:"+ geofenceTransitionDetails + "!");
            this.geofenceCurrent = geofenceTransitionDetails;
            Log.i("GAAAAAAAAAAAAAAAAAAAAR","this.geofenceCurrent:"+this.geofenceCurrent);

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
        Log.d(TAG,"getGeofenceTransitionDetails running...");

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
    /*
    private void setPlayer(){
        //MediaPlayer mpp = null;
        //MediaPlayer mpp2 = null;
        //MediaPlayer mpp3 = null;

        Log.i(TAG, "setPlayer running...");

        //if(this.pt1 && (this.lastPt == 1)) {
        if((this.pt1 && (this.lastPt == 1))||(this.pt3out)) {
            Log.i(TAG, "GAAAAAAFR is PT1");
            mpp = MediaPlayer.create(this, R.raw.terminal1_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal1_en);
            if(mpp != null){
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                //final MediaPlayer finalMpp = mpp;
                //mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                //    @Override
                //    public void onCompletion(MediaPlayer mediaPlayer) {
                //        finalizeMPlayer(finalMpp);
                //    }
                //});
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }

            this.pt1 = false;
            this.lastPt = 0;

            if(this.pt3out){
                this.pt3out = false;
            }

        }else if(this.pt2 && (this.lastPt == 2)){
            Log.i(TAG, "GAAAAAAFR is PT2");
            mpp = MediaPlayer.create(this, R.raw.terminal2_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal2_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal2_es);
            if(mpp != null){
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }

            this.pt2 = false;
            this.lastPt = 0;

        }else if(this.pt3 && (this.lastPt == 3)){
            Log.i(TAG, "GAAAAAAFR is PT3");
            mpp = MediaPlayer.create(this, R.raw.terminal3_pt);
            mpp2 = MediaPlayer.create(this, R.raw.terminal3_en);
            mpp3 = MediaPlayer.create(this, R.raw.terminal3_es);

            if(mpp != null){
                mpp.start();
                mpp.setNextMediaPlayer(mpp2);
                mpp.setNextMediaPlayer(mpp3);
            }else{
                Log.i(TAG, "error during MediaPlayer creation!!!");
            }
            this.pt3 = false;
            this.lastPt = 0;
        }else{
            Log.i(TAG, "GAAAAAAFR NO ONE POIT!!!!");
        }
    }
    */
    private void loadIndication() {
        Log.i(TAG, "loadIndication running...");
        SetStopPoint(this.Buff);

        //ENTERING TERMINAL 1
        if((this.iStopPoint == 0)||(this.iStopPoint == 4)||(this.iStopPoint == 8)||(this.iStopPoint == 12)||(this.iStopPoint == 16)||
                (this.iStopPoint == 18)||(this.iStopPoint == 20)||(this.iStopPoint == 24)||(this.iStopPoint == 28)||(this.iStopPoint == 32)||
                (this.iStopPoint == 36)||(this.iStopPoint == 40)||(this.iStopPoint == 44)||(this.iStopPoint == 48)||(this.iStopPoint == 52)||
                (this.iStopPoint == 56)||(this.iStopPoint == 60)||(this.iStopPoint == 64)||(this.iStopPoint == 68)||(this.iStopPoint == 72)||
                (this.iStopPoint == 76)||(this.iStopPoint == 80)||(this.iStopPoint == 84)||(this.iStopPoint == 88)||(this.iStopPoint == 92)||
                (this.iStopPoint == 96)||(this.iStopPoint == 100)||(this.iStopPoint == 104)||(this.iStopPoint == 108)||(this.iStopPoint == 112)||
                (this.iStopPoint == 116)||(this.iStopPoint == 120)||(this.iStopPoint == 124)||(this.iStopPoint == 128)||(this.iStopPoint == 132)||
                (this.iStopPoint == 136)||(this.iStopPoint == 140)||(this.iStopPoint == 144)||(this.iStopPoint == 148)||(this.iStopPoint == 152)||
                (this.iStopPoint == 156)||(this.iStopPoint == 160)||(this.iStopPoint == 164)||(this.iStopPoint == 168)||(this.iStopPoint == 172)||
                (this.iStopPoint == 176)||(this.iStopPoint == 180)||(this.iStopPoint == 184)||(this.iStopPoint == 188)||(this.iStopPoint == 192)||
                (this.iStopPoint == 196)||(this.iStopPoint == 200)){

            /*

             */
            //if((this.iStopPoint == 0)){
            Log.i(TAG, "Any point of the terminal 1");
            //if (this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i(TAG, "Entering...");
                //mpp.reset();
                //mpp2.reset();
                mpp = MediaPlayer.create(this, R.raw.t1pt);
                mpp2 = MediaPlayer.create(this, R.raw.t1ing);

                //mpp3 = MediaPlayer.create(this, R.raw.t1es);
                if (mpp != null) {
                    mpp.start();
                    mpp.setNextMediaPlayer(mpp2);
                    //mpp.setNextMediaPlayer(mpp3);
                }
                mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    Log.d(TAG,"GAAAAAFR:: onCompletion audio T1");
                                                    finalizeMPlayer(mediaPlayer);
                                                    finalizeMPlayer(mpp);
                                                    finalizeMPlayer(mpp2);
                                                }
                                            }
                );
            //} else {
                //Log.i(TAG, "Exiting...");
            //}
            /*
            if(!this.lockPt1){
                Log.i(TAG, "MediaPlayer creation for Entering to Point 1!!!");
                this.pt1 = true;
                this.lastPt = 1;
                this.lockPt1 = true;
                Log.i(TAG, "Lock point 1 = "+this.lockPt1);
                this.lockPt2 = false;
                this.lockPt3 = false;
                setPlayer();
            }else{
                Log.i(TAG, "Point 1 LOCKED!!!!!!!!!!");
            }*/

            //ENTERING TERMINAL 2
            //}else if((this.iStopPoint == 5)||(this.iStopPoint == 6)||(this.iStopPoint == 7)||(this.iStopPoint == 8)||(this.iStopPoint == 9)){
            //}else if((this.iStopPoint == 1)){
            }else if((this.iStopPoint == 1)||(this.iStopPoint == 5)||(this.iStopPoint == 9)||(this.iStopPoint == 13)||(this.iStopPoint == 17)||
                (this.iStopPoint == 21)||(this.iStopPoint == 25)||(this.iStopPoint == 29)||(this.iStopPoint == 33)||(this.iStopPoint == 37)||
                (this.iStopPoint == 41)||(this.iStopPoint == 45)||(this.iStopPoint == 49)||(this.iStopPoint == 53)||(this.iStopPoint == 57)||
                (this.iStopPoint == 61)||(this.iStopPoint == 65)||(this.iStopPoint == 69)||(this.iStopPoint == 73)||(this.iStopPoint == 77)||
                (this.iStopPoint == 81)||(this.iStopPoint == 85)||(this.iStopPoint == 89)||(this.iStopPoint == 93)||(this.iStopPoint == 97)||
                (this.iStopPoint == 101)||(this.iStopPoint == 105)||(this.iStopPoint == 109)||(this.iStopPoint == 113)||(this.iStopPoint == 117)||
                (this.iStopPoint == 121)||(this.iStopPoint == 125)||(this.iStopPoint == 129)||(this.iStopPoint == 133)||(this.iStopPoint == 137)||
                (this.iStopPoint == 141)||(this.iStopPoint == 145)||(this.iStopPoint == 149)||(this.iStopPoint == 153)||(this.iStopPoint == 157)||
                (this.iStopPoint == 161)||(this.iStopPoint == 165)||(this.iStopPoint == 169)||(this.iStopPoint == 173)||(this.iStopPoint == 177)||
                (this.iStopPoint == 181)||(this.iStopPoint == 185)||(this.iStopPoint == 189)||(this.iStopPoint == 193)||(this.iStopPoint == 197)||
                (this.iStopPoint == 201)||(this.iStopPoint == 205)||(this.iStopPoint == 209)||(this.iStopPoint == 213)){

            Log.i(TAG, "Any point of the terminal 2");
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.i(TAG, "Entering in Terminal 2...");
            //mpp.reset();
            //mpp2.reset();
            mpp = MediaPlayer.create(this, R.raw.t2pt);
                mpp2 = MediaPlayer.create(this, R.raw.t2ing);
                //mpp3 = MediaPlayer.create(this, R.raw.t2es);
            if(mpp != null) {
                    mpp.start();
                    mpp.setNextMediaPlayer(mpp2);
                    //mpp.setNextMediaPlayer(mpp3);
                }
                mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    Log.d(TAG,"GAAAAAFR:: onCompletion audio T2");
                                                    finalizeMPlayer(mediaPlayer);
                                                    finalizeMPlayer(mpp);
                                                    finalizeMPlayer(mpp2);
                                                }
                                            }
                );
            //}else{
                //Log.i(TAG, "Exiting of the Terminal 2...");
            //}
            /*
            if(!this.lockPt2){
                Log.i(TAG, "MediaPlayer creation for Entering to Point 2!!!");
                this.pt2 = true;
                this.lastPt = 2;
                this.lockPt1 = false;
                this.lockPt2 = true;
                this.lockPt3 = false;
                setPlayer();
            }else{
                Log.i(TAG, "Point 2 LOCKED!!!!!!!!!!");
            }*/

            //ENTERING TERMINAL 3
        //}else if((this.iStopPoint == 0)||(this.iStopPoint == 11)||(this.iStopPoint == 12)||(this.iStopPoint == 13)||(this.iStopPoint == 14)){
        //}else if((this.iStopPoint == 2)){
        }else if((this.iStopPoint == 2)||(this.iStopPoint == 6)||(this.iStopPoint == 10)||(this.iStopPoint == 14)||(this.iStopPoint == 18)||
                (this.iStopPoint == 22)||(this.iStopPoint == 26)||(this.iStopPoint == 30)||(this.iStopPoint == 34)||(this.iStopPoint == 38)||
                (this.iStopPoint == 42)||(this.iStopPoint == 46)||(this.iStopPoint == 50)||(this.iStopPoint == 54)||(this.iStopPoint == 58)||
                (this.iStopPoint == 62)||(this.iStopPoint == 66)||(this.iStopPoint == 70)||(this.iStopPoint == 74)||(this.iStopPoint == 78)||
                (this.iStopPoint == 82)||(this.iStopPoint == 86)||(this.iStopPoint == 90)||(this.iStopPoint == 94)||(this.iStopPoint == 98)||
                (this.iStopPoint == 102)||(this.iStopPoint == 106)||(this.iStopPoint == 110)||(this.iStopPoint == 114)||(this.iStopPoint == 118)||
                (this.iStopPoint == 122)||(this.iStopPoint == 126)||(this.iStopPoint == 130)||(this.iStopPoint == 134)||(this.iStopPoint == 138)||
                (this.iStopPoint == 142)||(this.iStopPoint == 146)||(this.iStopPoint == 150)||(this.iStopPoint == 154)||(this.iStopPoint == 158)||
                (this.iStopPoint == 162)||(this.iStopPoint == 166)||(this.iStopPoint == 170)||(this.iStopPoint == 174)||(this.iStopPoint == 178)||
                (this.iStopPoint == 182)||(this.iStopPoint == 186)||(this.iStopPoint == 190)||(this.iStopPoint == 194)||(this.iStopPoint == 198)||
                (this.iStopPoint == 202)){

            Log.i(TAG, "Any point of the terminal 3");
            //if(this.gFenceType == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.i(TAG, "Entering in Terminal 3...");
            //mpp.reset();
            //mpp2.reset();
            mpp = MediaPlayer.create(this, R.raw.t3pt);
                mpp2 = MediaPlayer.create(this, R.raw.t3ing);
                //mpp3 = MediaPlayer.create(this, R.raw.t3es);
                if(mpp != null) {
                    mpp.start();
                    mpp.setNextMediaPlayer(mpp2);
                    //mpp.setNextMediaPlayer(mpp3);
                }
                    mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                    @Override
                                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                                        Log.d(TAG,"GAAAAAFR:: onCompletion audio T2");
                                                        finalizeMPlayer(mediaPlayer);
                                                        finalizeMPlayer(mpp);
                                                        finalizeMPlayer(mpp2);
                                                    }
                                                }
                    );

                /*
            }else{
                Log.i(TAG, "Exiting of the Terminal 2...");
                mpp = MediaPlayer.create(this, R.raw.terminal1_pt);
                mpp2 = MediaPlayer.create(this, R.raw.terminal1_en);
                if(mpp != null) {
                    mpp.start();
                    mpp.setNextMediaPlayer(mpp2);
                }
                mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    finalizeMPlayer(mediaPlayer);
                                                }
                                            }
                );
            }*/
            /*
            if(!this.lockPt3){
                Log.i(TAG, "MediaPlayer creation for Entering to Point 3!!!");
                this.pt3 = true;
                this.pt3out = true;
                this.lastPt = 3;
                this.lockPt1 = false;
                this.lockPt2 = false;
                this.lockPt3 = true;
                setPlayer();
            }else{
                Log.i(TAG, "Point 3 LOCKED!!!!!!!!!!");
            }*/
        }else if((this.iStopPoint == 3)||(this.iStopPoint == 7)||(this.iStopPoint == 11)||(this.iStopPoint == 15)||(this.iStopPoint == 19)||
                (this.iStopPoint == 23)||(this.iStopPoint == 27)||(this.iStopPoint == 31)||(this.iStopPoint == 35)||(this.iStopPoint == 39)||
                (this.iStopPoint == 43)||(this.iStopPoint == 47)||(this.iStopPoint == 51)||(this.iStopPoint == 55)||(this.iStopPoint == 59)||
                (this.iStopPoint == 63)||(this.iStopPoint == 67)||(this.iStopPoint == 71)||(this.iStopPoint == 75)||(this.iStopPoint == 79)||
                (this.iStopPoint == 83)||(this.iStopPoint == 87)||(this.iStopPoint == 91)||(this.iStopPoint == 95)||(this.iStopPoint == 99)||
                (this.iStopPoint == 103)||(this.iStopPoint == 107)||(this.iStopPoint == 111)||(this.iStopPoint == 115)||(this.iStopPoint == 119)||
                (this.iStopPoint == 123)||(this.iStopPoint == 127)||(this.iStopPoint == 131)||(this.iStopPoint == 135)||(this.iStopPoint == 139)||
                (this.iStopPoint == 143)||(this.iStopPoint == 147)||(this.iStopPoint == 151)||(this.iStopPoint == 155)||(this.iStopPoint == 159)||
                (this.iStopPoint == 163)||(this.iStopPoint == 167)||(this.iStopPoint == 171)||(this.iStopPoint == 175)||(this.iStopPoint == 179)||
                (this.iStopPoint == 183)||(this.iStopPoint == 187)||(this.iStopPoint == 191)||(this.iStopPoint == 195)||(this.iStopPoint == 199)||
                (this.iStopPoint == 203)){

            Log.i(TAG, "Entering in Terminal Garage");
            //mpp.reset();
            mpp = MediaPlayer.create(this, R.raw.garagem);
            if(mpp != null) {
                mpp.start();
            }
            mpp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                Log.d(TAG,"GAAAAAFR:: onCompletion audio Garage");
                                                finalizeMPlayer(mediaPlayer);
                                                finalizeMPlayer(mpp);
                                            }
                                        }
            );
        }
    }

    public void finalizeMPlayer(MediaPlayer mediaPlayer){
        Log.e(TAG, "finalizeMPlayer Running...");
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion MPlayer Running...");
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}