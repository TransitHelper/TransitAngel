package com.jeevitharoyapathi.triphelper.utils;

public class UrlProvider {
    public static String getServerUrl(Constants.TRANSIT_TYPE transitType) {
        switch (transitType) {
            case CALTRAIN:
                return "";
            case BART:
                return "";
            default:
                return "";
        }
    }
}
