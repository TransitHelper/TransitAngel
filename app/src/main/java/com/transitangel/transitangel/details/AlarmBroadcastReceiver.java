package com.transitangel.transitangel.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Vibrator;
import android.util.Log;

import com.google.gson.Gson;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.DateUtil;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_LOCATION_UNAVAILABLE = 100;
    private String TAG = AlarmBroadcastReceiver.class.getSimpleName();
    public static final String ARG_STOP = "ARG_STOP";
    public static final String TRIP_ID = "TRIP_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("inside", "inside");
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        Gson gson = new Gson();
        TrainStop nextStop = gson.fromJson(intent.getStringExtra(ARG_STOP), TrainStop.class);
        String tripId = intent.getStringExtra(TRIP_ID);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            String notificationMessage;
            Timestamp stopTimeStamp=DateUtil.getTimeStamp(nextStop.getArrrivalTime());
            long mintues = getTimeStampDifference(stopTimeStamp);

            if (mintues>0) {
                notificationMessage = "Approaching " + nextStop.getName() + " in " + mintues + " minutes";
            }else{
                notificationMessage ="Approached "+ nextStop.getName() + " at " + TimeUnit.MILLISECONDS.toHours(stopTimeStamp.getTime());
            }
                NotificationProvider.getInstance().updateTripStartedNotification(context, notificationMessage);
                Vibrator vibrator = (Vibrator) context
                        .getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(2000);
            }

        }

    private long getTimeStampDifference(Timestamp timeStamp) {
        long difference = timeStamp.getTime() - System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toMinutes(difference);
    }

    private boolean isTripValid(String tripId) {
        Log.e("TripID", tripId);
        Log.e("PrefTripId", PrefManager.getOnGoingTrip().getTripId());
        return (PrefManager.getOnGoingTrip() != null && PrefManager.getOnGoingTrip().getTripId() == tripId);
    }
}