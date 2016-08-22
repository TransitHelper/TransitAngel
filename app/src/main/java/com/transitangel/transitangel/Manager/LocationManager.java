package com.transitangel.transitangel.Manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.utils.PermissionUtils;

/**
 * Created by vidhurvoora on 8/21/16.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback ,LocationSource.OnLocationChangedListener{

    private static LocationManager sInstance;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    protected Context mApplicationContext;
    protected Context activityContext;
    protected LocationResponseHandler activityLocationResponseHandler;

    public static synchronized LocationManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new LocationManager();

        }
        return sInstance;
    }

    public void setup(Context context) {
        mApplicationContext = context;
        GoogleApiClient.Builder client = new GoogleApiClient.Builder(sInstance.mApplicationContext);


       mGoogleApiClient =  client.addApi(LocationServices.API)
                .addConnectionCallbacks(sInstance)
                .addOnConnectionFailedListener(sInstance).build();

    }


    public void start() {
        mGoogleApiClient.connect();
    }

    public void stop() {
        // Disconnecting the client invalidates it.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if ( requestCode == 100 && activityContext != null && activityLocationResponseHandler != null) {
            getCurrentLocation(activityContext,activityLocationResponseHandler);
        }
    }

    public void getCurrentLocation(Context context,LocationResponseHandler handler) {
        activityContext = context;
        activityLocationResponseHandler = handler;

        start();
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( context instanceof Activity ) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,100,(Activity)context);
            }
            else {
               // throw new IllegalArgumentException("Context not activity");
                handler.OnLocationReceived(false,null);
            }
        }
        else {

            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // Note that this can be NULL if last location isn't already known.
            if (mCurrentLocation != null) {
                // Print current location if not null
                Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                handler.OnLocationReceived(true, latLng);
            }
        }




        // Begin polling for new location updates.
        //startLocationUpdates(context);
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates(Context context) {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( context instanceof Activity ) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,100,(Activity)context);
            }
            else {
                throw new IllegalArgumentException("Context not activity");
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, (LocationListener) this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {

        } else if (i == CAUSE_NETWORK_LOST) {
           //do what
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
        //do broadcast
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
