package com.transitangel.transitangel.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.home.HomeActivity;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.utils.UiUtils;

/**
 * author yogesh.shrivastava.
 */
public class NotificationProvider {

    private static final int NOTIFICATION_ONGOING_ID = 200;
    private static final int NOTIFICATION_DISMISS_ID = 201;
    private static final int NOTIFICATION_BIG_TEXT = 202;
    private static NotificationProvider INSTANCE;

    public static NotificationProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationProvider();
        }
        return INSTANCE;
    }

    public void showTripStartedNotification(Context context, Trip trip) {
        Intent dismiss = new Intent(context, DismissService.class);
        dismiss.setAction(DismissService.ACTION_DISMISS);
        dismiss.putExtra(DismissService.EXTRA_TRIP_ID, trip.getTripId());
        PendingIntent piDismiss = PendingIntent.getService(context, 0, dismiss, 0);


        // Get the train details:
        String title = "Trip to " + trip.getToStop().getName();
        // Set details from the ongoing trip
        String contentTitle = "Arrive at " + UiUtils.convert24TimeTo12hr(trip.getSelectedTrain().getTrainStop(trip.getToStop().getId()).getArrrivalTime());

        Intent showTrip = new Intent(context, HomeActivity.class);
        showTrip.setAction(HomeActivity.ACTION_SHOW_ONGOING);
        showTrip.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent piShowTrip = PendingIntent.getActivity(context, 1, showTrip, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.train)
                        .setContentTitle(title)
                        .setContentText(contentTitle)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // Required to show like phone call notifications
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOngoing(true) // This will make it stick to the top.
                        .addAction(R.drawable.close_notification, context.getString(R.string.cancel_trip_notification), piDismiss)// Dismissed the notification.
                        .setContentIntent(piShowTrip);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ONGOING_ID, notification.build());
    }


    public void updateTripStartedNotification(Context context, String updateString) {
        Trip trip = PrefManager.getOnGoingTrip();
        if (trip == null) {
            return;
        }

        Intent dismiss = new Intent(context, DismissService.class);
        dismiss.setAction(DismissService.ACTION_DISMISS);
        dismiss.putExtra(DismissService.EXTRA_TRIP_ID, trip.getTripId());
        PendingIntent piDismiss = PendingIntent.getService(context, 0, dismiss, 0);

        // Get the train details:
        String title = "Trip to " + trip.getToStop().getName();
        // Set details from the ongoing trip
        String contentTitle = updateString;

        Intent showTrip = new Intent(context, HomeActivity.class);
        showTrip.setAction(HomeActivity.ACTION_SHOW_ONGOING);
        showTrip.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent piShowTrip = PendingIntent.getActivity(context, 1, showTrip, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.train)
                        .setContentTitle(title)
                        .setContentText(contentTitle)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // Required to show like phone call notifications
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOngoing(true) // This will make it stick to the top.
                        .addAction(R.drawable.close_notification, context.getString(R.string.cancel_trip_notification), piDismiss)// Dismissed the notification.
                        .setContentIntent(piShowTrip);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ONGOING_ID, notification.build());
    }

    private NotificationProvider() {
    }

    public void dismissOnGoingNotification(Context context) {

        Trip onGoingTrip = PrefManager.getOnGoingTrip();
        // Set details from the ongoing trip
        if (onGoingTrip == null) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ONGOING_ID);

        // Get the train details:
        String title = "Trip cancelled";

        String contentTitle = "Transit from " + onGoingTrip.getFromStop().getName()
                + " to " + onGoingTrip.getToStop().getName() + " has been cancelled";

        Intent onDismissIntent = new Intent(context, HomeActivity.class);
        onDismissIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        onDismissIntent.setAction(HomeActivity.ACTION_TRIP_CANCELLED);

        PendingIntent piOnDismiss = PendingIntent.getActivity(context, 1, onDismissIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.train)
                        .setContentTitle(title)
                        .setContentText(contentTitle)
                        .setContentIntent(piOnDismiss)
                        .setTicker(contentTitle)
                        .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_DISMISS_ID, mBuilder.build());

        //remove the ongoing trip
        PrefManager.removeOnGoingTrip();

        //remove all geofences
        GeofenceManager.getSharedInstance().removeAllGeofences(new GeofenceManager.GeofenceManagerListener() {
            @Override
            public void onGeofencesUpdated() {
                //succesfully removed
            }

            @Override
            public void onError() {
                //Error determine what to do
            }
        });
        removeCurrentTripAlarms(context);
    }

    public void showBigTextNotification(Context context, Intent intent, String title, String contentText) {
        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Create a PendingIntent
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 3. Create and send a notification
        Notification notification = new android.support.v7.app.NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new android.support.v7.app.NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_BIG_TEXT, notification);
    }

    private void removeCurrentTripAlarms(Context context) {
//        try {
//            Log.e("jevitha","insde");
//            Type type = new TypeToken<List<PendingIntent>>() {}.getType();
//            Gson gson = new Gson();
//            String json = Prefs.getString(TAConstants.AlarmIntents, "");
//            List<PendingIntent> intentList = gson.fromJson(json, type);
//            for (PendingIntent intent : intentList
//                    ) {
//
//                AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
//                alarmManager.cancel(intent);
//            }
//        } catch (Exception e) {
//            Log.e(NotificationProvider.class.getSimpleName(), e.getMessage());
//        }
    }
}
