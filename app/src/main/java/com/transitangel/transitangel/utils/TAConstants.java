package com.transitangel.transitangel.utils;

public class TAConstants {
    public enum TRANSIT_TYPE {BART, CALTRAIN}

    public enum SAVED_PREF_TYPE {RECENT_SEARCH, RECENT_TRIP}

    public enum SERVICE_TYPE {
        CALTRAIN_LIMITED, CALTRAIN_LOCAL, CALTRAIN_BABYBULLET, BART_BAYPT_SFIA, BART_COLS_OAKL, BART_DALY_DUBLIN, BART_DALY_FREMONT, BART_DUBLIN_DALY, BART_FREMONT_DALY, BART_FREMONT_RICH, BART_MILL_RICH, BART_OAK_COLS, BART_RICH_FREMONT, BART_RICH_MILL, BART_SFIA_BAYPT
    }

    public static String SharedPrefGeofences = "SHARED_PREFS_GEOFENCES";
    public static String HomeScreenSettings = "HOME_SCREEN_SETTINGS";
    public static String AlarmIntents = "ALARM_PENDING_INTENTS";
    public static final int ALARM_REQUEST_CODE = 111;


}
