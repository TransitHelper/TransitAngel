package com.transitangel.transitangel.model.Transit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class Stop
{
    String id;
    String name;
    String longitude;
    String latitude;

    public Stop(JSONObject stopObj) throws JSONException {
        this.id = stopObj.getString("id");
        this.name = stopObj.getString("Name");
        this.longitude = stopObj.getJSONObject("Location").getString("Longitude");
        this.latitude = stopObj.getJSONObject("Location").getString("Latitude");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    //to display object as a string in spinner
    public String toString() {
        return name;
    }
}
