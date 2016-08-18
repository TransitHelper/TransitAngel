package com.transitangel.transitangel.utils;

//TODO: Add appropriate base url for CalTrain and BART
public class UrlProvider {
    public static String getServerUrl(TAConstants.TRANSIT_TYPE transitType) {
        switch (transitType) {
            case CALTRAIN:
                return "http://api.511.org/transit/";
            case BART:
                return "";
            default:
                return "";
        }
    }
}
