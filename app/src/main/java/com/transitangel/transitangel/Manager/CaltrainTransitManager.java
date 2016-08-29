package com.transitangel.transitangel.Manager;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CaltrainTransitManager extends TransitManager {

    private static CaltrainTransitManager sInstance;

    private ArrayList<String> sortedStopIds = new ArrayList<String>(){{
        add("70011");
        add("70021");
        add("70031");
        add("70042");
        add("70052");
        add("70062");
        add("70081");
        add("70091");
        add("70101");
        add("70111");
        add("70122");
        add("70131");
        add("70141");
        add("70161");
        add("70172");
        add("70191");
        add("70201");
        add("70211");
        add("70221");
        add("70231");
        add("70242");
        add("70251");
        add("70262");
        add("70271");
        add("70281");
        add("70292");
        add("70301");
        add("70311");
        add("70322");
    }};

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
        ArrayList<Stop> orderedStops = new ArrayList<Stop>();
        for (String stopId : sortedStopIds) {
            orderedStops.add(mStopLookup.get(stopId));
        }
        return orderedStops;

//        ArrayList<Stop> stopsWithoutDuplicates = new ArrayList<Stop>();
//       for (Stop originalStop : stops ) {
//           if ( stopsWithoutDuplicates.size() == 0 ) {
//               stopsWithoutDuplicates.add(originalStop);
//           }
//           else {
//               Boolean alreadyContains = false;
//               for ( Stop stop: stopsWithoutDuplicates ) {
//                   if ( stop.getName().equals(originalStop.getName())) {
//                       alreadyContains = true;
//                   }
//               }
//               if ( !alreadyContains ) {
//                   stopsWithoutDuplicates.add(originalStop);
//               }
//           }
//       }
//        return stopsWithoutDuplicates;
    }

    public HashMap<String, Stop> getStopLookup() {
        if ( mStopLookup == null || mStopLookup.size() == 0 ) {
            getStops();
        }
        return mStopLookup;
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

    public ArrayList<Train> fetchTrainsDepartingFromStation(
            String toStopId //to station
            , int hourLimit //
    ) {
        return fetchTrainsDepartingFromStation(toStopId,hourLimit,getServices());
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



    //gets a local train stop and converts all the trainstop to stop
    public ArrayList<Stop> getLocalStops() {
        ArrayList<Stop> stops = new ArrayList<>();
        ArrayList<Train> localTrains = getLocalTrains();
        if (localTrains.size() > 0) {
           ArrayList<TrainStop> localTrainStops  =  localTrains.get(0).getTrainStops();
           for (TrainStop trainStop : localTrainStops) {
               Stop stop = new Stop();
               stop.setId(trainStop.getStopId());
               stop.setName(trainStop.getName());
               stop.setLatitude(trainStop.getLatitudeStr());
               stop.setLongitude(trainStop.getLongitudeStr());
               stops.add(stop);
           }
        }
        return stops;
    }

    public ArrayList<Train> getLocalTrains() {
        if (mServices == null) {
            populateServices();
        }
        ArrayList<Train> localTrains = new ArrayList<>();
        for (Service service : mServices) {
            if (service.getServiceType() == TAConstants.SERVICE_TYPE.CALTRAIN_LOCAL) {
                localTrains.addAll(service.getWeekdayTrains());
                localTrains.addAll(service.getWeekendTrains());
                return localTrains;
            }
        }
        return localTrains;
    }



    public void fetchTrainsDepartingFromNearestStation(
            Context context
            , int hourLimit //
            , TrainsDepartingFromStationResponseHandler handler
    ) {
        //check if location is accessible
        if ( !TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            handler.trainsDeparting(false,null);
        }

        ArrayList<Train> departingTrains = new ArrayList<>();
        getNearestStop(context, new NearestStopResponseHandler() {
            @Override
            public void nearestStop(boolean isSuccesss, Stop stop) {
                if ( isSuccesss ) {
                    ArrayList<Train> trains = fetchTrainsDepartingFromStation(stop.getId(),hourLimit,getServices());
                    if ( trains != null ) {
                        handler.trainsDeparting(true,trains);
                    }
                    else {
                        handler.trainsDeparting(false,null);
                    }
                }
                else {
                    handler.trainsDeparting(false,null);
                }
            }
        });


    }

    public  void getNearestStop(Context context,NearestStopResponseHandler handler) {

        //check if location is accessible
        if ( !TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            handler.nearestStop(false,null);
        }

        TransitLocationManager.getSharedInstance().getCurrentLocation(context, new TransitLocationManager.LocationResponseHandler() {
            @Override
            public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                if ( isSuccess ) {
                    Stop stop  = getNearestStop(latLng.latitude,latLng.longitude,getStops());
                    if ( stop != null ) {
                        handler.nearestStop(true,stop);
                    }
                    else {
                        handler.nearestStop(false,null);
                    }

                }
                else {
                    handler.nearestStop(false,null);
                }

            }
        });
    }
}
