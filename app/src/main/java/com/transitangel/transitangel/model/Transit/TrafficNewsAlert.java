package com.transitangel.transitangel.model.Transit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/20/16.
 */
public class TrafficNewsAlert {
    String date; //8/20/2016 5:34:00 PM
    String type; //Breaking News
    String description;
    String title;
    String link;

    public TrafficNewsAlert(JSONObject newsObj) throws JSONException {

        date = newsObj.getString("Date");
        link = newsObj.getString("Link");
        type = newsObj.getString("Type");
        description = newsObj.getString("Description");
        title = newsObj.getString("Title");
    }

}
