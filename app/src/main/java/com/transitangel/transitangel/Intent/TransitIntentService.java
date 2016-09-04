package com.transitangel.transitangel.Intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;
import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.home.HomeActivity;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStopFence;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vidhurvoora on 8/23/16.
 */
public class TransitIntentService extends IntentService {

    private final String TAG = TransitIntentService.class.getName();
    private SharedPreferences prefs;
    private Gson gson;

    public TransitIntentService() {
        super(TransitIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Got geo fence callback");
        prefs = getApplicationContext().getSharedPreferences(
                TAConstants.SharedPrefGeofences, Context.MODE_PRIVATE);
        gson = new Gson();

// 1. Get the event
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {

                // 2. Get the transition type
                int transition = event.getGeofenceTransition();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                        transition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                        transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();

                    // 3. Accumulate a list of event geofences
                    for (Geofence geofence : event.getTriggeringGeofences()) {
                        geofenceIds.add(geofence.getRequestId());
                    }
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                            transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        // 4. Pass the geofence list to the notification method
                        onEnteredGeofences(geofenceIds);
                    }
                }
            }

        }
    }

    private void onEnteredGeofences(List<String> geofenceIds) {
        // 1. Outer loop over all geofenceIds
        for (String geofenceId : geofenceIds) {
            String geofenceName = "";
            TrainStopFence trainStopFence = null;
            // 2, Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreferences
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String jsonString = prefs.getString(entry.getKey(), null);
                trainStopFence = gson.fromJson(jsonString, TrainStopFence.class);
                if (trainStopFence.getFenceId().equals(geofenceId)) {
                    geofenceName = trainStopFence.getTrainStop().getName();
                    break;
                }
            }

            if ( trainStopFence != null ) {

                //check if it is the last geofence
                Trip trip = PrefManager.getOnGoingTrip();
                if ( trip != null ) {
                    Stop finalStop = trip.getToStop();
                    String stopIdForFence = trainStopFence.getTrainStop().getStopId();
                    if ( stopIdForFence.equalsIgnoreCase(finalStop.getId())) {
                        //this is the last stop
                        //end the ongoing the notification
                        NotificationProvider.getInstance().endOngoingNotification(this);

                        //show the destination is approaching
                        String notificationContent = "Approaching " + trainStopFence.getTrainStop().getName();

                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //TODO verify text
                        NotificationProvider.getInstance().showBigTextNotification(this, intent, "This is your final destination", notificationContent);

                        //remove all geofences
                        GeofenceManager.getSharedInstance().removeAllGeofences(new GeofenceManager.GeofenceManagerListener() {
                            @Override
                            public void onGeofencesUpdated() {
                                Log.d("Geofence Success","Successfully removed all geofences");
                            }

                            @Override
                            public void onError() {
                                Log.d("Geofence Error","Error removing all geofences");
                            }
                        });
                        return;
                    }
                }

                //TODO decide proper notification name
                String notificationContent = "Approaching " + trainStopFence.getTrainStop().getName();
                NotificationProvider.getInstance().updateTripStartedNotification(this,notificationContent);

                //remove that geofence
                ArrayList<TrainStopFence> fenceToRemove = new ArrayList<>();
                fenceToRemove.add(trainStopFence);
                GeofenceManager.getSharedInstance().removeGeofences(fenceToRemove, new GeofenceManager.GeofenceManagerListener() {
                    @Override
                    public void onGeofencesUpdated() {
                        //successfully removed
                    }

                    @Override
                    public void onError() {
                        //TODO determine what todo
                    }
                });
            }

           /*
            String contextText = String.format(this.getResources().getString(R.string.Notification_Text), geofenceName);

            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            NotificationProvider.getInstance().showBigTextNotification(this, intent, getString(R.string.Notification_Title), contextText);
            */
        }
    }

    private void onError(int i) {
        Log.e(TAG, "Geofencing Error: " + i);
    }
}
