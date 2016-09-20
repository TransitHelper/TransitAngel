package com.transitangel.transitangel.Manager;

import android.app.PendingIntent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.pixplicity.easyprefs.library.Prefs;
import com.transitangel.transitangel.model.Transit.Trip;

import java.util.List;

/**
 * @author yogesh.shrivastava.
 */
public class PrefManager {
    private final static String TAG = PrefManager.class.getSimpleName();
    private final static String EXTRA_TRIP_INFO = TAG + ".EXTRA_TRIP_INFO";
    private static Trip trip;
    private static List<PendingIntent> mCurrentTripAlarms;

    public static void addOnGoingTrip(Trip newTrip) {
        trip = newTrip;
        Gson gson = new Gson();
        Prefs.putString(EXTRA_TRIP_INFO, gson.toJson(newTrip));
    }

    @Nullable
    public static Trip getOnGoingTrip() {
        try {
            if (trip == null) {
                String tripString = Prefs.getString(EXTRA_TRIP_INFO, null);
                if (!TextUtils.isEmpty(tripString)) {
                    Gson gson = new Gson();
                    trip = gson.fromJson(tripString, Trip.class);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getOnGoingTrip: exception while getting on going trip", e);
        }

        return trip;
    }

    public static void removeOnGoingTrip() {
        trip = null;
        Prefs.remove(EXTRA_TRIP_INFO);
    }

    public static void addAlarmIntents(List<PendingIntent> currentTripAlarms) {
        mCurrentTripAlarms = currentTripAlarms;
    }

    public static void cancelAlarmIntents() {
        if (mCurrentTripAlarms != null) {

        }
    }

}