package com.transitangel.transitangel.model.Transit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class Route {

    String id;
    String name; //Example LIMITED:N :Weekday
    String direction; //N or S
    String type; //LIMITED , BABY BULLET , LOCAL
    ArrayList<String> stopsInSequence; //stopIds

    public Route(JSONObject routeObj) throws JSONException {
        id = routeObj.getString("id");
        name = routeObj.getString("Name");
        type = routeObj.getJSONObject("LineRef").getString("ref");
        direction = routeObj.getJSONObject("DirectionRef").getString("ref");
        JSONArray pointsInSequence = routeObj.getJSONObject("pointsInSequence").getJSONArray("PointOnRoute");
        stopsInSequence = new ArrayList<String>();
        for (int i =0 ; i<pointsInSequence.length();i++){
            JSONObject pointObj = pointsInSequence.getJSONObject(i);
            String stopId = pointObj.getJSONObject("PointRef").getString("ref");
            stopsInSequence.add(stopId);
        }

    }

}
