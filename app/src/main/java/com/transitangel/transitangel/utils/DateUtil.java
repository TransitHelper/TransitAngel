package com.transitangel.transitangel.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    static DateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    public static String getRelativeTime(long time, long time2) {
        //TODO: Try to get destination arrival date
        long diff = time - time2;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        if ((diffHours < 0 || diffMinutes < 0) || (diffHours == 0 && diffMinutes == 0 && diffSeconds <= 0)) {
            return timeFormat.format(time);
        } else {
            if (diffHours == 0 && diffMinutes > 0) {
                return "Leaving in " + diffMinutes + "min";
            } else if (diffHours == 0 && diffMinutes == 0) {
                return "Leaving now";
            } else {
                return "Leaving in " + diffHours + "hr " + diffMinutes + "min";
            }
        }
    }

    public static Timestamp getTimeStamp(String time) {
        return Timestamp.valueOf(
                new SimpleDateFormat("yyyy-MM-dd ")
                        .format(new Date())
                        .concat(time));
    }
}
