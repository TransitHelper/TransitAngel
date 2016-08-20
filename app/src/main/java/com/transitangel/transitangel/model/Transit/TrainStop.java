package com.transitangel.transitangel.model.Transit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class TrainStop {
    String stopId;
    String arrrivalTime;
    String departureTime;
    String stopOrder;

    public TrainStop(JSONObject trainStopObj) throws JSONException {
        stopOrder = trainStopObj.getString("order");
        stopId = trainStopObj.getJSONObject("ScheduledStopPointRef").getString("ref");
        arrrivalTime = trainStopObj.getJSONObject("Arrival").getString("Time");
        departureTime = trainStopObj.getJSONObject("Departure").getString("Time");
    }

    public String getStopId() {
        return stopId;
    }

    public String getArrrivalTime() {
        return arrrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getStopOrder() {
        return Integer.parseInt(stopOrder);
    }
}
