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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CaltrainTransitManager extends TransitManager{

    private static CaltrainTransitManager sInstance;

    public ArrayList<Service> mServices;

    public HashMap<String,Stop> mStopLookup = new HashMap<String, Stop>();

    public static synchronized CaltrainTransitManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new CaltrainTransitManager();
            httpClient = new AsyncHttpClient();
            mTransitType = TAConstants.TRANSIT_TYPE.CALTRAIN;

        }
        return sInstance;
    }

    private void populateServices (Context context){


        try {
            mServices = new ArrayList<Service>();

            //load the json from files
            String jsonLimitedTrainString =  loadJSONFromAsset(context,"Timetable_Caltrain_Limited.json");
            JSONObject limitedTrainObj = new JSONObject(jsonLimitedTrainString);
            Service limitedService = new Service(limitedTrainObj,TAConstants.SERVICE_TYPE.LIMITED);
            mServices.add(limitedService);

            String jsonLocalTrainString =  loadJSONFromAsset(context,"Timetable_Caltrain_Local.json");
            JSONObject localTrainObj = new JSONObject(jsonLocalTrainString);
            Service localService = new Service(localTrainObj,TAConstants.SERVICE_TYPE.LOCAL);
            mServices.add(localService);

            String jsonBulletTrainString =  loadJSONFromAsset(context,"Timetable_Caltrain_BabyBullet.json");
            JSONObject bulletTrainObj = new JSONObject(jsonBulletTrainString);
            Service bulletService = new Service(bulletTrainObj,TAConstants.SERVICE_TYPE.BABYBULLET);
            mServices.add(bulletService);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Stop> getStops(Context context) {
        //load the json from files
        try {
            String jsonStopsString =  loadJSONFromAsset(context,"Caltrain_Stops.json");
            JSONObject stopsObj = new JSONObject(jsonStopsString);
            JSONArray stopArr = stopsObj.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
            ArrayList<Stop> stops = fetchStopArrFromJson(stopArr);
            //populate hashmap
            for (Stop stop : stops) {
                mStopLookup.put(stop.getId(),stop);
            }

            return stops;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Train> fetchTrains(Context context,String fromStopId, String toStopId, Date leavingAfter) {

        //for now ignore the leavingAt

        ArrayList<Train> trains = new ArrayList<Train>();
        //foreach service
        ArrayList<Service> services = getServices(context);
        for (Service service: services) {
            //fetch the trains
            ArrayList<Train> trainList = service.getTrains();

            for (Train train : trainList ) {
                //fetch stops
                TrainStop fromStop = null;
                TrainStop toStop = null;

                ArrayList<TrainStop> trainStopList = train.getTrainStops();

                //check if the train has the fromStopId and toStopId
                // and check if the fromStopOrder < toStopOrder
                for (TrainStop trainStop: trainStopList ) {

                    if ( trainStop.getStopId().equals(fromStopId)) {
                        fromStop = trainStop;
                    }
                    else if ( trainStop.getStopId().equals(toStopId) ) {
                        toStop = trainStop;
                    }

//                    if (fromStop != null && toStop != null ) {
//                        //found both fromStop and toStop
//                        break;
//                    }

                    //check the order
                    if (fromStop!=null
                            && toStop!=null
                            && fromStop.getStopOrder() < toStop.getStopOrder()) {
                        //matches our list of train
                        //TODO check if arrival time is greater than the from time
                        trains.add(train);
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

    public ArrayList<Service> getServices(Context context) {
        if ( mServices == null ) {
            populateServices(context);
        }
        return mServices;
    }
}
