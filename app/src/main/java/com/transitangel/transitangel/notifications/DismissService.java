package com.transitangel.transitangel.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.transitangel.transitangel.R;

/**
 *
 */
public class DismissService extends IntentService {

    private static final String TAG = DismissService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 201;
    public static final String ACTION_DISMISS = "ACTION_DISMISS";
    public static final String EXTRA_TRAIN_ID = "EXTRA_TRAIN_ID";

    public DismissService() {
        super("DismissService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DISMISS.equals(action)) {
                String trainId = intent.getStringExtra(EXTRA_TRAIN_ID);
                handleDismiss(trainId);
            }

        }
    }

    /**
     * Handles cancelling the train information.
     */
    private void handleDismiss(String trainId) {
        // TODO: Handle action dismiss
        Log.d(TAG, "Train dismissed : " + trainId);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.train)
                        .setContentTitle("Trip cancelled")
                        .setContentText("Transit from MTV to SFO has been cancelled");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        NotificationProvider.getInstance().dismissNotification(this);
    }
}
