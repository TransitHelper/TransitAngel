package com.transitangel.transitangel.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO : check for network/gps for location update
        Log.e(AlarmBroadcastReceiver.class.getSimpleName(),"inside OnAlarm");
    }
}