package com.transitangel.transitangel.Manager;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.Date;

public class CaltrainTransitManager extends TransitManager {

    private static CaltrainTransitManager sInstance;


    public static synchronized CaltrainTransitManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new CaltrainTransitManager();
            httpClient = new AsyncHttpClient();
            mTransitType = TAConstants.TRANSIT_TYPE.CALTRAIN;

        }
        return sInstance;
    }

    public void setup(Context context) {
        mApplicationContext = context;
    }

    private void populateServices() {
        ArrayList<String> filenames = new ArrayList<String>() {{
            add("Timetable_Caltrain_Local.json");
            add("Timetable_Caltrain_Limited.json");
            add("Timetable_Caltrain_BabyBullet.json");
        }};

        ArrayList<TAConstants.SERVICE_TYPE> serviceTypes = new ArrayList<TAConstants.SERVICE_TYPE>() {{
            add(TAConstants.SERVICE_TYPE.CALTRAIN_LOCAL);
            add(TAConstants.SERVICE_TYPE.CALTRAIN_LIMITED);
            add(TAConstants.SERVICE_TYPE.CALTRAIN_BABYBULLET);
        }};

         populateServices(filenames,serviceTypes);
    }

    public ArrayList<Stop> getStops() {
        //load the json from files
        ArrayList<Stop> stops =  getStops("Caltrain_Stops.json");
        ArrayList<Stop> stopsWithoutDuplicates = new ArrayList<Stop>();
       for (Stop originalStop : stops ) {
           if ( stopsWithoutDuplicates.size() == 0 ) {
               stopsWithoutDuplicates.add(originalStop);
           }
           else {
               Boolean alreadyContains = false;
               for ( Stop stop: stopsWithoutDuplicates ) {
                   if ( stop.getName().equals(originalStop.getName())) {
                       alreadyContains = true;
                   }
               }
               if ( !alreadyContains ) {
                   stopsWithoutDuplicates.add(originalStop);
               }
           }
       }
        return stopsWithoutDuplicates;
    }


    public ArrayList<Train> fetchTrains(
            String fromStopId //from station
            , String toStopId //to station
            , int limit // number of results to return, 0 or -ve implies no limit
            , Date leavingAfter //determines its a weekday/weekend , defaults to today
            , boolean shouldIncludeAllTrainsForThatDay // includes all the trains for that day irrespective of time

    ) {
        return fetchTrains(fromStopId
                ,toStopId
                ,limit
                ,leavingAfter
                ,shouldIncludeAllTrainsForThatDay
                ,getServices()
               );
    }

    //Given a destination and the hour limit, fetch all the trains which will arrive at the destination
    //within that hour limit
    //TODO missed an important part, we need to know if it is north bound or south bound.
    public ArrayList<Train> fetchTrainsArrivingAtDestination(
            String toStopId //to station
            , int hourLimit //

    ) {
        return fetchTrainsArrivingAtDestination(
                toStopId
                ,hourLimit
                ,getServices()
        );
    }

    public ArrayList<Service> getServices() {
        if (mServices == null) {
            populateServices();
        }
        return mServices;
    }

    public Stop getNearestStop(double lat, double lon) {
        return  getNearestStop(lat,lon,getStops());
    }

}
