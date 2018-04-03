package com.hrupin.samples.androidgeofencessample;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hrupin.samples.androidgeofencessample.db.GeofenceContract;
import com.hrupin.samples.androidgeofencessample.db.GeofenceStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MainActivity";
    public static final String SHARED_PREFERENCES_NAME = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES_NAME";
    public static final String NEW_GEOFENCE_NUMBER = BuildConfig.APPLICATION_ID + ".NEW_GEOFENCE_NUMBER";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 65; // 100 m
    private static final int PERMISSIONS_REQUEST = 105;

    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    private GoogleMap googleMap;

    private Geofence buffGeofence1 = null;
    private Geofence buffGeofence2 = null;
    private Geofence buffGeofence3 = null;

    GeofencingEvent geofencingEven;

    GeofenceTransitionsIntentService geoFence = null;
    //GPSTracker gps = null;//new GPSTracker(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        buildGoogleApiClient();
        clearApplicationData();
    }

    public void clearApplicationData() {
        Log.d(TAG, "clearApplicationData Running...");
        File cache = getCacheDir();
        /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            File Data = getFilesDir();// DataDir();
            Log.d(TAG, "getDataDir Running...");
        }*/

        File Data = getFilesDir();
        Log.d(TAG, "Data"+Data);
        deleteDir(Data);

        Log.d(TAG, "cache = " +cache);
        File appDir = new File(cache.getParent());
        Log.d(TAG, "appDir = " +appDir);
        if (appDir.exists()) {
            String[] children = appDir.list();
            Log.d(TAG, "children = " +children);
            for (String s : children) {
                Log.d(TAG, "s = " +s);
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }
    public static boolean deleteDir(File dir) {
        Log.d(TAG, "deleteDir Running...");
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        clearApplicationData();
        mGoogleApiClient.connect();
        //
        //if (gps == null){
        //    GPSTracker gps = new GPSTracker(this);
        //}
        //File file = Environment.getExternalStorageDirectory();
        //Log.i(TAG, "GAAAAAAAAAAFR :: SD paht :" + file);

        //File file2 = Environment.getDataDirectory();
        //Log.i(TAG, "GAAAAAAAAAAFR :: Data paht :" + file2);

        //File file3 = Environment.getRootDirectory();
        //Log.i(TAG, "GAAAAAAAAAAFR :: Root paht :" + file3);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop Running...");
        super.onStop();
        unloadMarkers();
         tryReset("0");
        tryReset("1");
        tryReset("2");
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        //ACTIA
        final double actLa1 = -30.007741;
        final double actLo1 = -51.204438;
        //final double actLa2 = -30.006410;
        //final double actLo2 = -51.203650;
        //final double actLa3 = -30.007900;
        //final double actLo3 = -51.201722;

        //T1 = -23.429454;-46.4935208;
        //-23.428351, -46.493306
        final double latT1 = -23.428351;
        final double lonT1 = -46.493306;
        //T2
        //-23.424314, -46.484332
        //-23.423405, -46.484326
        final double latT2 = -23.423405;
        final double lonT2 = -46.484326;
        //T3
        //-23.422756, -46.477669
        //-23.423832, -46.479663
        final double latT3 = -23.423832;
        final double lonT3 = -46.479663;

        //Garagem In-HAus
        //-23.420730, -46.480048
        final double latGar = -23.428351;
        final double lonGar = -46.493306;

        //tryLoad(latT1, lonT1, 0);
        //tryLoad(latT2, lonT2, 1);
        //tryLoad(latT3, lonT3, 2);
        //tryLoad(latGar, lonGar, 3);

        //tryReset("0");
        //tryReset("1");
        //tryReset("2");
        //tryReset("3");
        unloadMarkers();

        tryLoad(actLa1, actLo1, "0");
        tryLoad(actLa1, actLo1, "1");
        tryLoad(actLa1, actLo1, "2");
        tryLoad(actLa1, actLo1, "3");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            Log.d(TAG, "getGeofencePendingIntent:: mGeofencePendingIntent = NULL");
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        //GeofenceTransitionsIntentService geoFence = new GeofenceTransitionsIntentService();
        if (geoFence == null){
            geoFence = new GeofenceTransitionsIntentService();
        }
        geoFence.setContext(this);

//TEST////////////////////
        /*
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        List triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);
        Log.i("GAAAAAAAAAAAAAAAAAAAAR", "geofenceTransitionDetails:"+ geofenceTransitionDetails + "!");
        String geofenceCurrent = geofenceTransitionDetails;
        Log.i("GAAAAAAAAAAAAAAAAAAAAR","this.geofenceCurrent:"+geofenceCurrent);*/

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);
        ArrayList triggeringGeofencesIdsList = new ArrayList();

        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
            String Buff = geofence.getRequestId();
            Log.i("GAAAAAAFR", "Buff:"+Buff);
            tryReset(Buff);
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);
        Log.i("GAAAAAAAAAAAAAAR", "triggeringGeofencesIdsString: "+triggeringGeofencesIdsString);
        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
        //MapView mapView = (MapView) findViewById(R.id.map_view) ;
        //mapView.setClickable(true) ;
        //int measure = mapView.getMeasuredState();
    }

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
//END TEST////////////////////


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        initMap(googleMap);
        Log.d(TAG, "onMapReady OOOOOOOOOOOOOOOOOOOOOOKKKKKKKKKK");
    }

    private void initMap(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);

        //tryLoad();
        reloadMapMarkers();
    }


    private void tryReset(final String ID){
        Log.e(TAG, "tryReset running...");
        try {
            List<String> idList = new ArrayList<>();
            idList.add(ID);
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "Not success in onResult!!!");
                        GeofenceStorage.removeGeofence(ID);
                        Toast.makeText(MainActivity.this, getString(R.string.geofences_removed), Toast.LENGTH_SHORT).show();
                        reloadMapMarkers();
                    } else {
                        // Get the status code for the error and log it using a user-friendly message.
                        String errorMessage = GeofenceTransitionsIntentService.getErrorString(MainActivity.this,
                                status.getStatusCode());
                        Log.e(TAG, errorMessage);
                        Log.e(TAG, "Not success in onResult!!! = " +errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void tryLoad(final Double lat, final Double lon, String cont){

        //final String key = getNewGeofenceNumber() + "";
        final String key = cont;
        final long expTime = System.currentTimeMillis() + GEOFENCE_EXPIRATION_IN_MILLISECONDS;
        //addMarker(key, latLng);

        Geofence geofence = new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        lat,
                        lon,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        /*
        if(cont == 0){
            this.buffGeofence1 = geofence;
        }else if(cont == 1){
            this.buffGeofence2 = geofence;
        }else if(cont == 2){
            this.buffGeofence3 = geofence;
        }*/
        //String requestID = geofence. getRequestId();
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(geofence),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        //GeofenceStorage.saveToDbGAFR(key, lat, lon, expTime);
                        Toast.makeText(MainActivity.this, getString(R.string.geofences_added), Toast.LENGTH_SHORT).show();
                        //geoFence.setGeoStorage(GeofenceStorage);
                    } else {
                        String errorMessage = GeofenceTransitionsIntentService.getErrorString(MainActivity.this, status.getStatusCode());
                        Log.e(TAG, errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void reloadMapMarkers() {
        googleMap.clear();
        try (Cursor cursor = GeofenceStorage.getCursor()) {
            while (cursor.moveToNext()) {
                long expires = Long.parseLong(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES)));
                if(System.currentTimeMillis() < expires) {
                    String key = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY));
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT)));
                    double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG)));
                    addMarker(key, new LatLng(lat, lng));

                }
            }
        }
    }

    private void unloadMarkers() {
        googleMap.clear();
        try (Cursor cursor = GeofenceStorage.getCursor()) {
            while (cursor.moveToNext()) {
                long expires = Long.parseLong(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES)));
                if(System.currentTimeMillis() < expires) {
                    String key = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY));
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT)));
                    double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG)));
                    //addMarker(key, new LatLng(lat, lng));
                    tryReset(key);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap(googleMap);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
                }
                return;
            }
        }
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        final String key = getNewGeofenceNumber() + "";
        final long expTime = System.currentTimeMillis() + GEOFENCE_EXPIRATION_IN_MILLISECONDS;
        addMarker(key, latLng);

        Geofence geofence = new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        String requestID = geofence. getRequestId();

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(geofence),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.e(TAG, "GAAAFR:: isSuccess");
                        GeofenceStorage.saveToDb(key, latLng, expTime);
                        Toast.makeText(MainActivity.this, getString(R.string.geofences_added), Toast.LENGTH_SHORT).show();
                        //geoFence.setGeoStorage(GeofenceStorage);
                    } else {
                        String errorMessage = GeofenceTransitionsIntentService.getErrorString(MainActivity.this, status.getStatusCode());
                        Log.e(TAG, errorMessage);
                        Log.e(TAG, "GAAAFR:: onResult = ERROR!!");
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void addMarker(String key, LatLng latLng) {
        googleMap.addMarker(new MarkerOptions()
                .title("G:" + key)
                .snippet("Click here if you want delete this geofence")
                .position(latLng));
        googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(GEOFENCE_RADIUS_IN_METERS)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#80ff0000")));
    }

    private int getNewGeofenceNumber(){
        int number = mSharedPreferences.getInt(NEW_GEOFENCE_NUMBER, 0);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(NEW_GEOFENCE_NUMBER, number + 1);
        editor.commit();
        return number;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        final String requestId = marker.getTitle().split(":")[1];
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            List<String> idList = new ArrayList<>();
            idList.add(requestId);
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        GeofenceStorage.removeGeofence(requestId);
                        Toast.makeText(MainActivity.this, getString(R.string.geofences_removed), Toast.LENGTH_SHORT).show();
                        reloadMapMarkers();
                    } else {
                        // Get the status code for the error and log it using a user-friendly message.
                        String errorMessage = GeofenceTransitionsIntentService.getErrorString(MainActivity.this,
                                status.getStatusCode());
                        Log.e(TAG, errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }
}
