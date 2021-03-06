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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
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
            Log.e("TripID", tripId);
            Log.e("PrefTripId", PrefManager.getOnGoingTrip() != null ? PrefManager.getOnGoingTrip().getTripId() : "null");
            String notificationMessage;
            Timestamp stopTimeStamp = DateUtil.getTimeStamp(nextStop.getArrrivalTime());
            long minutes = getTimeStampDifference(stopTimeStamp);
            if (minutes > 0) {
                notificationMessage = "Approaching " + nextStop.getName() + " in " + minutes + " minutes";
            } else {
                notificationMessage = "Approached " + nextStop.getName() + " at " + dateFormat.format(stopTimeStamp);
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
        if (PrefManager.getOnGoingTrip() == null) return true;
        if (PrefManager.getOnGoingTrip().getTripId() == tripId) return true;
        else {
            return false;
        }

    }
}