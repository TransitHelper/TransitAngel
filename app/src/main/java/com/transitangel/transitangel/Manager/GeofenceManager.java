package com.transitangel.transitangel.Manager;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.transitangel.transitangel.Intent.TransitIntentService;
import com.transitangel.transitangel.model.Transit.TrainStopFence;
import com.transitangel.transitangel.utils.PermissionUtils;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by vidhurvoora on 8/23/16.
 */
public class GeofenceManager {

    private final String TAG = GeofenceManager.class.getName();

    private Context context;
    private Context activityContext;
    private GoogleApiClient googleApiClient;
    private Gson gson;
    private SharedPreferences prefs;

    private List<TrainStopFence> trainStopFences;
    private List<TrainStopFence> trainStopFencesToRemove;
    public static final int GEOFENCE_GET_FINE_LOC_REQ_CODE  = 200;

    private GeofenceManagerListener listener;

    private static GeofenceManager sInstance;

    private Geofence geofenceToAdd;
    private TrainStopFence trainStopFenceToAdd;
    private Boolean isRemoveGeofences = false;



    public static synchronized GeofenceManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new GeofenceManager();
        }
        return sInstance;
    }

    public void setup(Context context) {
        this.context = context;
        gson = new Gson();
        trainStopFences = new ArrayList<>();
        trainStopFencesToRemove = new ArrayList<>();
        prefs = this.context.getSharedPreferences(TAConstants.SharedPrefGeofences, Context.MODE_PRIVATE);
        //load stored geofences
        loadGeofences();
    }

    public interface GeofenceManagerListener {
        void onGeofencesUpdated();

        void onError();
    }

    private GoogleApiClient.ConnectionCallbacks connectionAddListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.d("Geofence ","On Connected");
                    addGeofenceOnConnectedHandle();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d("Geofence ","Connection suspended");
                }
            };

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.d("Geofence ","Connection failed");
                }
            };

    private GoogleApiClient.ConnectionCallbacks connectionRemoveListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    removeGeofenceOnConnectHandle();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.e(TAG, "Connecting to GoogleApiClient suspended.");
                    sendError();
                }
            };

    private GeofencingRequest getAddGeofencingRequest() {
        List<Geofence> geofencesToAdd = new ArrayList<>();
        geofencesToAdd.add(geofenceToAdd);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofencesToAdd);
        return builder.build();
    }

    private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        if ( googleApiClient== null || !googleApiClient.isConnected() ) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(callbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
            googleApiClient.connect();
        }
        else {
            if ( !isRemoveGeofences && this.trainStopFenceToAdd  != null ) {
                try {
                    addGeofenceOnConnectedHandle();
                } catch (java.lang.IllegalStateException e) {
                    e.printStackTrace();
                    sendError();
                }
            }
            else {
                if ( isRemoveGeofences) {
                    try {
                        removeGeofenceOnConnectHandle();
                    } catch (java.lang.IllegalStateException e) {
                        e.printStackTrace();
                        sendError();
                    }
                }
            }
        }

    }

    private void sendError() {
        if (listener != null) {
            listener.onError();
        }
    }

    private void saveGeofence() {
        trainStopFences.add(trainStopFenceToAdd);
        //save to pref
        String json = gson.toJson(trainStopFenceToAdd);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(trainStopFenceToAdd.getFenceId(), json);
        editor.apply();
        if (listener != null) {
            listener.onGeofencesUpdated();
        }
    }

    private void loadGeofences() {
        // Loop over all geofence keys in prefs and add to namedGeofences
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String jsonString = prefs.getString(entry.getKey(), null);
            TrainStopFence trainStopFence = gson.fromJson(jsonString, TrainStopFence.class);
            trainStopFences.add(trainStopFence);
        }

        Log.d(TAG,trainStopFences.toString());
        // Sort namedGeofences by name
        //Collections.sort(trainStopFences);
    }

    private void addGeofenceOnConnectedHandle() {
        Intent intent = new Intent(context, TransitIntentService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 2. Associate the service PendingIntent with the geofence and call addGeofences
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if ( context instanceof Activity) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,GEOFENCE_GET_FINE_LOC_REQ_CODE,(Activity)context);
            }
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        if ( !googleApiClient.isConnected()) {
            sendError(); //google api client is not connected
            return;
        }
        PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(
                googleApiClient, getAddGeofencingRequest(), pendingIntent);

        // 3. Implement PendingResult callback
        result.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                Log.d(TAG, "geo fence result");
                if (status.isSuccess()) {
                    // 4. If successful, save the geofence
                    Log.d("Add Geofence","Successfully added geofence");
                    saveGeofence();
                } else {
                    // 5. If not successful, log and send an error
                    Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                            " : " + status.getStatusCode());
                    sendError();
                }
            }
        });
    }

    private void removeGeofenceOnConnectHandle() {
        // 1. Create a list of geofences to remove
        List<String> removeIds = new ArrayList<>();
        for (TrainStopFence trainStopFence : trainStopFencesToRemove) {
            removeIds.add(trainStopFence.getFenceId());
        }

        if (removeIds.size() > 0 && googleApiClient.isConnected()) {
            // 2. Use GoogleApiClient and the GeofencingApi to remove the geofences
            PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient, removeIds);
            result.setResultCallback(new ResultCallback<Status>() {

                // 3. Handle the success or failure of the PendingResult
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        if (listener != null) {
                            listener.onGeofencesUpdated();
                        }
                        //TODO handle this on main thread
                        removeSavedGeofences();
                    } else {
                        Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
                        sendError();
                    }
                }
            });
        }
        else {
            //either google api is not connected or the removeIds size is 0
            sendError();
        }
    }

    public void addGeofence(Context context,TrainStopFence trainStopFence, GeofenceManagerListener listener) {
        //make sure we have the permissions
        isRemoveGeofences = false;
        if ( !TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            listener.onError();
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if ( context instanceof Activity) {
                PermissionUtils.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,GEOFENCE_GET_FINE_LOC_REQ_CODE,(Activity)context);
            }
        }
        else {
            activityContext  = context;
            this.trainStopFenceToAdd = trainStopFence;
            this.geofenceToAdd = trainStopFence.geofence();
            this.listener = listener;
            connectWithCallbacks(connectionAddListener);
        }

    }

    //removes list of geofences
    public void removeGeofences(List<TrainStopFence> trainStopFencesToRemove,
                                GeofenceManagerListener listener) {
        isRemoveGeofences = true;
        this.trainStopFencesToRemove = trainStopFencesToRemove;
        this.listener = listener;
        connectWithCallbacks(connectionRemoveListener);
    }

    //removes all geofences
    public void removeAllGeofences(GeofenceManagerListener listener) {
        isRemoveGeofences = true;
        trainStopFencesToRemove = new ArrayList<>();
        for (TrainStopFence trainStopFence : trainStopFences) {
            trainStopFencesToRemove.add(trainStopFence);
        }
        this.listener = listener;
        connectWithCallbacks(connectionRemoveListener);
    }

    //removes from shared prefs
    private void removeSavedGeofences() {

        SharedPreferences.Editor editor = prefs.edit();

        try {
            Boolean isChanged = false;
            for (TrainStopFence trainStopFence : trainStopFencesToRemove) {

                for (Iterator<TrainStopFence> iterator = trainStopFences.iterator(); iterator.hasNext();) {
                    TrainStopFence existingFence = iterator.next();
                    if ( trainStopFence.getFenceId().equalsIgnoreCase(existingFence.getFenceId())) {
                        // Remove the current element from the iterator and the list.
                        editor.remove(existingFence.getFenceId());
                        iterator.remove();
                    }
                }
            }

            editor.apply();
        }
        catch(java.util.ConcurrentModificationException exception ) {
            //TODO handle this modification on a UI thread
            Log.d("Exception","Encountered exception while removing geofences from shared pref");
        }


        if (listener != null) {
            listener.onGeofencesUpdated();
        }
    }
}
