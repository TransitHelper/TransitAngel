package com.transitangel.transitangel.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.home.HomeActivity;

/**
 * author yogesh.shrivastava.
 */
public class NotificationProvider {

    private static final int NOTIFICATION_ID = 200;
    private static NotificationProvider INSTANCE;

    public static NotificationProvider getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NotificationProvider();
        }
        return INSTANCE;
    }

    public void showNotification(Context context, String trainId) {
        Intent dismiss = new Intent(context, DismissService.class);
        dismiss.setAction(DismissService.ACTION_DISMISS);
        dismiss.putExtra(DismissService.EXTRA_TRAIN_ID, trainId);
        PendingIntent piDismiss = PendingIntent.getService(context, 0, dismiss, 0);


        Intent showTrip = new Intent(context, HomeActivity.class);
        showTrip.setAction(HomeActivity.ACTION_SHOW_ONGOING);
        PendingIntent piShowTrip = PendingIntent.getActivity(context, 1, showTrip, 0);
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.train)
                .setContentTitle("Train enroute")
                .setContentText("Train is sheduled to arrive at 8:30am")
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Required to show like phone call notifications
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true) // This will make it stick to the top.
                .addAction(R.drawable.close_notification, "Cancel Trip", piDismiss)
                .addAction(R.drawable.train, "Show Trip", piShowTrip);// Dismissed the notification.

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    private NotificationProvider() {
    }

    public void dismissNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
