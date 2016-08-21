package com.transitangel.transitangel.model.Transit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/20/16.
 */
public class Tweet {
    String description;
    String dateStr;//Sun Aug 21 01:04:05 +0000 2016
    String profilePic;
    String name;
    String screenName;
    String tweetId;

    public Tweet(JSONObject tweetObj) throws JSONException {
        description = tweetObj.getString("description");
        dateStr = tweetObj.getString("tweetCreated");
        profilePic = tweetObj.getString("thumbnail_64_x_64");
        name = tweetObj.getString("name");
        screenName = tweetObj.getString("screen_name");
        tweetId = tweetObj.getString("id_str");
    }
}
