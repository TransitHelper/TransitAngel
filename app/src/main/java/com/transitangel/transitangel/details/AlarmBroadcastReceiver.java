package com.transitangel.transitangel.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Vibrator;

import com.google.gson.Gson;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.DateUtil;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_LOCATION_UNAVAILABLE = 100;
    private String TAG = AlarmBroadcastReceiver.class.getSimpleName();
    public static final String ARG_STOP = "ARG_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        Gson gson = new Gson();
        TrainStop nextStop = gson.fromJson(intent.getStringExtra(ARG_STOP), TrainStop.class);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled && PrefManager.getOnGoingTrip() != null) {
            String notificationMessgae = "Your Stop will arrive in" +
                    DateUtil.getRelativeTime(DateUtil.getTimeStamp(nextStop.getArrrivalTime()).getTime(),
                            System.currentTimeMillis());
            NotificationProvider.getInstance().updateTripStartedNotification(context, notificationMessgae);
            Vibrator vibrator = (Vibrator) context
                    .getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
        }

    }
}