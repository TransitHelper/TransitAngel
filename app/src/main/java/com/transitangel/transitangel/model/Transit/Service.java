package com.transitangel.transitangel.model.Transit;

import com.transitangel.transitangel.utils.TAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class Service {

    TAConstants.SERVICE_TYPE serviceType; //Limited,Baby Bullet,Local
    ArrayList<Route> routes;
    ArrayList<Train> weekdayTrains;
    ArrayList<Train> weekendTrains;


    public Service(JSONObject serviceObj, TAConstants.SERVICE_TYPE service_type) throws JSONException {

        serviceType = service_type;

        JSONArray routeObjs = serviceObj
                .getJSONObject("Content")
                .getJSONObject("ServiceFrame")
                .getJSONObject("routes")
                .getJSONArray("Route");

        //populate routes
        routes = new ArrayList<Route>();
        for (int i = 0; i < routeObjs.length(); i++) {
            JSONObject routeObj = routeObjs.getJSONObject(i);
            Route route = new Route(routeObj);
            routes.add(route);
        }

        //populate trains
        weekdayTrains = new ArrayList<>();
        weekendTrains = new ArrayList<>();
        JSONArray ttFrames = serviceObj
                .getJSONObject("Content")
                .getJSONArray("TimetableFrame");
        //ttframe represent south bound -weekday, northbound-weekend etc..
        //each ttframe has multiple trains
        for (int i = 0; i < ttFrames.length(); i++) {
            JSONObject ttFrame = ttFrames.getJSONObject(i);
            String name = ttFrame.getString("Name"); //example LIMITED:N :Weekday

            JSONArray trainObjArr = ttFrame.getJSONObject("vehicleJourneys").getJSONArray("ServiceJourney");
            for (int j = 0; j < trainObjArr.length(); j++) {
                JSONObject trainObj = trainObjArr.getJSONObject(j);
                Train train = new Train(trainObj);
                train.name = name;

                if (name.contains("Weekday")) {
                    weekdayTrains.add(train);
                } else {
                    weekendTrains.add(train);
                }
            }
        }
    }

    public TAConstants.SERVICE_TYPE getServiceType() {
        return serviceType;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public ArrayList<Train> getWeekdayTrains() {
        return weekdayTrains;
    }

    public ArrayList<Train> getWeekendTrains() {
        return weekendTrains;
    }
}
