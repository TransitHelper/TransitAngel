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
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        if ((diffHours < 0 || diffMinutes < 0) || (diffHours == 0 && diffMinutes == 0)) {
            return timeFormat.format(time);
        } else {
            if (diffHours == 0)
                return diffMinutes + "mins";
            else {
                return diffHours + " hr " + diffMinutes + "mins";
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
