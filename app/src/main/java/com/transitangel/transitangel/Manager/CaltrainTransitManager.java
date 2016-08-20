package com.transitangel.transitangel.Manager;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.utils.TAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CaltrainTransitManager extends TransitManager {

    private static CaltrainTransitManager sInstance;

    public ArrayList<Service> mServices;

    public HashMap<String, Stop> mStopLookup = new HashMap<String, Stop>();

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

        try {
            mServices = new ArrayList<Service>();

            //load the json from files
            String jsonLimitedTrainString = loadJSONFromAsset("Timetable_Caltrain_Limited.json");
            JSONObject limitedTrainObj = new JSONObject(jsonLimitedTrainString);
            Service limitedService = new Service(limitedTrainObj, TAConstants.SERVICE_TYPE.LIMITED);
            mServices.add(limitedService);

            String jsonLocalTrainString = loadJSONFromAsset("Timetable_Caltrain_Local.json");
            JSONObject localTrainObj = new JSONObject(jsonLocalTrainString);
            Service localService = new Service(localTrainObj, TAConstants.SERVICE_TYPE.LOCAL);
            mServices.add(localService);

            String jsonBulletTrainString = loadJSONFromAsset("Timetable_Caltrain_BabyBullet.json");
            JSONObject bulletTrainObj = new JSONObject(jsonBulletTrainString);
            Service bulletService = new Service(bulletTrainObj, TAConstants.SERVICE_TYPE.BABYBULLET);
            mServices.add(bulletService);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Stop> getStops() {
        //load the json from files
        try {
            String jsonStopsString = loadJSONFromAsset("Caltrain_Stops.json");
            JSONObject stopsObj = new JSONObject(jsonStopsString);
            JSONArray stopArr = stopsObj.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
            ArrayList<Stop> stops = fetchStopArrFromJson(stopArr);
            //populate hashmap
            for (Stop stop : stops) {
                mStopLookup.put(stop.getId(), stop);
            }

            return stops;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //NOTE from Vidhur: I have edited the original model ( for Local and Babybullet) and removed few holiday schedule trains and other
    //weekend schedule train to make the fetchup logic simpler.
    //We can add the original model back once we understand the duplicate weekend train schedules in the current model
    public ArrayList<Train> fetchTrains(
                             String fromStopId //from station
                            , String toStopId //to station
                            , int limit // number of results to return, 0 or -ve implies no limit
                            , Date leavingAfter //determines its a weekday/weekend , defaults to today
                            ,boolean shouldIncludeAllTrainsForThatDay // includes all the trains for that day irrespective of time
                        ) {



        if (leavingAfter == null) {
            //if leaving after  is null default it to today
            leavingAfter = new Date();
        }
        ArrayList<Train> trains = new ArrayList<Train>();
        //foreach service
        ArrayList<Service> services = getServices();
        for (Service service : services) {
            //fetch the trains
            //check if weekday or weekend.
            String day = new SimpleDateFormat("EE").format(leavingAfter);

            ArrayList<Train> trainList = new ArrayList<Train>();
            if ( day.contains("Sat") || day.contains("Sun")) {
                trainList = service.getWeekendTrains();
            }
            else {
                trainList = service.getWeekendTrains();
            }

            for (Train train : trainList) {
                //fetch stops
                TrainStop fromStop = null;
                TrainStop toStop = null;

                ArrayList<TrainStop> trainStopList = train.getTrainStops();

                //check if the train has the fromStopId and toStopId
                // and check if the fromStopOrder < toStopOrder
                for (TrainStop trainStop : trainStopList) {

                    if (trainStop.getStopId().equals(fromStopId)) {
                        fromStop = trainStop;
                    } else if (trainStop.getStopId().equals(toStopId)) {
                        toStop = trainStop;
                    }

                    //check the order
                    if (fromStop != null
                            && toStop != null
                            && fromStop.getStopOrder() < toStop.getStopOrder()) {

                        if ( shouldIncludeAllTrainsForThatDay) {
                            trains.add(train);

                            if (limit > 0 && trains.size() == limit) {
                                return trains;
                            }
                            //reset the from stop and to stop to avoid duplicates
                            fromStop = null;
                            toStop = null;
                        }
                        else {
                            //matches our list of train
                            //check if arrival time is greater than the from time
                            String arrivalTimeStr = fromStop.getArrrivalTime();
                            String[] parts = arrivalTimeStr.split(":");
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                            cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                            Date arrivalTime = cal.getTime();
                            //add only if the arrival time is after the leavingAfter time
                            if (arrivalTime.after(leavingAfter)) {
                                trains.add(train);

                                if (limit > 0 && trains.size() == limit) {
                                    return trains;
                                }

                                //reset the from stop and to stop to avoid duplicates
                                fromStop = null;
                                toStop = null;
                            }
                        }
                    }
                }
            }
        }

        //return train list
        return trains;
    }

    //Given a destination and the hour limit, fetch all the trains which will arrive at the destination
    //within that hour limit
    //TODO missed an important part, we need to know if it is north bound or south bound.
    public ArrayList<Train> fetchTrainsArrivingAtDestination(
             String toStopId //to station
            , int hourLimit //
    ) {

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, hourLimit); //adds the hour
        Date arrivingOnOrBefore = cal.getTime();
        ArrayList<Train> trains = new ArrayList<Train>();

        Date arrivingAfter = new Date(); //the train should arrive after now

        //foreach service
        ArrayList<Service> services = getServices();
        for (Service service : services) {
            //fetch the trains
            //check if weekday or weekend.
            String day = new SimpleDateFormat("EE").format(arrivingOnOrBefore);

            ArrayList<Train> trainList = new ArrayList<Train>();
            if ( day.contains("Sat") || day.contains("Sun")) {
                trainList = service.getWeekendTrains();
            }
            else {
                trainList = service.getWeekendTrains();
            }

            for (Train train : trainList) {

                TrainStop toStop = null;

                ArrayList<TrainStop> trainStopList = train.getTrainStops();

                //check if the train has the fromStopId and toStopId
                // and check if the fromStopOrder < toStopOrder
                for (TrainStop trainStop : trainStopList) {

                    if (trainStop.getStopId().equals(toStopId)) {
                        toStop = trainStop;

                        //get arrival time
                        String arrivalTimeStr = toStop.getArrrivalTime();
                        String[] parts = arrivalTimeStr.split(":");
                        Calendar arrivalCal = Calendar.getInstance();
                        arrivalCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                        arrivalCal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                        Date arrivalTime = arrivalCal.getTime();

                        //TODO we need to know if it is a northbound or southbound
                        if (arrivalTime.before(arrivingOnOrBefore)
                                && arrivalTime.after(arrivingAfter)
                                && toStop.getStopOrder() > 1) {
                            trains.add(train);
                        }

                    }
                }
            }
        }

        //return train list
        return trains;
    }

    public HashMap<String, Stop> getStopLookup() {
        return mStopLookup;
    }

    public ArrayList<Service> getServices() {
        if (mServices == null) {
            populateServices();
        }
        return mServices;
    }
}
