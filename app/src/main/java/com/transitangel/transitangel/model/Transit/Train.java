package com.transitangel.transitangel.model.Transit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class Train {
    String number;
    String name;
    String direction;
    ArrayList<TrainStop> trainStops;

    public Train(JSONObject trainObj) throws JSONException {
        number = trainObj.getString("id");
        direction = trainObj.getJSONObject("JourneyPatternView").getJSONObject("DirectionRef").getString("ref");
        JSONArray stopObjs = trainObj.getJSONObject("calls").getJSONArray("Call");
        trainStops = new ArrayList<TrainStop>();
        for (int i=0;i<stopObjs.length();i++){
            JSONObject trainStopObj = stopObjs.getJSONObject(i);
            TrainStop trainStop = new TrainStop(trainStopObj);
            trainStops.add(trainStop);
        }
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getDirection() {
        return direction;
    }

    public ArrayList<TrainStop> getTrainStops() {
        return trainStops;
    }
}
