package com.transitangel.transitangel.utils;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author yvastavaus.
 */
public class UiUtils {

    private static final String TAG = UiUtils.class.getSimpleName();

    /**
     * Show snack bar at the bottom of the screen to notify our users.
     *
     * @param parent
     * @param displayText
     */
    public static void showSnackBar(View parent, String displayText, int length) {
        Snackbar.make(parent, displayText, length).show();
    }

    public static void showSnackBar(View parent, String displayText) {
        showSnackBar(parent, displayText, Snackbar.LENGTH_LONG);
    }

    /**
     * Convets 24hr to 12hr
     * @param hr24Time
     * @return
     */
    public static String convert24TimeTo12hr(String hr24Time) {
        String time = "";
        try {
            time = hr24Time;
            DateFormat f1 = new SimpleDateFormat("HH:mm:ss"); //HH for hour of the day (0 - 23)
            Date d = f1.parse(time);
            DateFormat f2 = new SimpleDateFormat("h:mm a");
            time = f2.format(d).toLowerCase(); // "12:18am"
        } catch (ParseException e) {
            Log.e(TAG, "parsing error for date: ", e);
        }

        return time;
    }
}
