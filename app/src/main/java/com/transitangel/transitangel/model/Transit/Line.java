package com.transitangel.transitangel.model.Transit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class Line {
    String id;
    String name;
    String operator;

    public Line(JSONObject lineObj) throws JSONException {
        this.id = lineObj.getString("Id");
        this.name = lineObj.getString("Name");
        this.operator = lineObj.getString("OperatorRef");
    }
}
