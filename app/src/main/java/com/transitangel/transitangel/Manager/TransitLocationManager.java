package com.transitangel.transitangel.Manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.utils.PermissionUtils;

/**
 * Created by vidhurvoora on 8/21/16.
 */
public class TransitLocationManager implements com.google.android.gms.location.LocationListener{

    private static TransitLocationManager sInstance;

    public static final int GET_LOCATION_REQUEST_CODE  = 100;
    public static final int GET_UPDATES_LOCATION_REQUEST_CODE  = 105;
    public static final String BROADCAST_ACTION = "TransitLocationManager.LocationUpdates";


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    protected Context mApplicationContext;
    protected Context activityContext;
    protected LocationResponseHandler activityLocationResponseHandler;

    public static synchronized TransitLocationManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new TransitLocationManager();
        }
        return sInstance;
    }

    public void setup(Context context) {
        mApplicationContext = context;
    }

    public interface LocationResponseHandler {
        public void OnLocationReceived (boolean isSuccess, LatLng latLng);
    }

    private GoogleApiClient.ConnectionCallbacks getLocationListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    getLocationConnectHandle();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };

    private GoogleApiClient.ConnectionCallbacks locationUpdatesListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    getLocationUpdatesHandle();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {

                }
            };

    private void getLocationConnectHandle() {
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( activityContext instanceof Activity ) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,GET_LOCATION_REQUEST_CODE,(Activity)activityContext);
            }
            else {
                // throw new IllegalArgumentException("Context not activity");
                activityLocationResponseHandler.OnLocationReceived(false,null);
            }
        }
        else {

            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // Note that this can be NULL if last location isn't already known.
            if (mCurrentLocation != null) {
                // Print current location if not null
                Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                activityLocationResponseHandler.OnLocationReceived(true, latLng);
            }
        }
    }

    private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        mGoogleApiClient = new GoogleApiClient.Builder(sInstance.mApplicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();
    }


    public void start() {

        mGoogleApiClient.connect();
    }

    public void stop() {
        // Disconnecting the client invalidates it.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }



    public void getCurrentLocation(Context context,LocationResponseHandler handler) {
        activityContext = context;
        activityLocationResponseHandler = handler;

        connectWithCallbacks(getLocationListener);

    }

    // Trigger new location updates at interval
    public void getLocationUpdates(Context context) {
        activityContext = context;
        connectWithCallbacks(locationUpdatesListener);
    }

    private void getLocationUpdatesHandle() {

        // Request location updates
        if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( activityContext instanceof Activity ) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,GET_UPDATES_LOCATION_REQUEST_CODE,(Activity)activityContext);
            }
            else {
                throw new IllegalArgumentException("Context not activity");
            }
        }
        else {
            // Create the location request
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, (LocationListener) this);
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //TODO decide if we want to broadcast nearest stop instead?
        //create intent
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("Latitude",latLng.latitude);
        intent.putExtra("Longitude",latLng.longitude);
        //send broadcast
        LocalBroadcastManager.getInstance(mApplicationContext).sendBroadcast(intent);

    }

    //reference:http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
    public boolean isLocationModeEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(mApplicationContext.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(mApplicationContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !(locationProviders.isEmpty());
        }
    }

    public boolean isLocationAccessible() {
        LocationManager lm = (LocationManager)mApplicationContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        return (gps_enabled && network_enabled);
    }

}
