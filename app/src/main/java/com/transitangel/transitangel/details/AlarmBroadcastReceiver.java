package com.transitangel.transitangel.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.transitangel.transitangel.R;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_LOCATION_UNAVAILABLE=100;
    private String TAG=AlarmBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            String title = "Location Unavailable";
            String contentTitle = "Unable to locate your location";
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.train)
                            .setContentTitle(title)
                            .setContentText(contentTitle)
                            .setTicker(contentTitle)
                            .setAutoCancel(true);
            notificationManager.notify(NOTIFICATION_LOCATION_UNAVAILABLE, mBuilder.build());
        }
    }
}