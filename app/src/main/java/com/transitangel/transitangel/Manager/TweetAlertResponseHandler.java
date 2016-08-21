package com.transitangel.transitangel.Manager;

import com.transitangel.transitangel.model.Transit.Tweet;

import java.util.ArrayList;

public interface TweetAlertResponseHandler {
    public void onTweetsReceived(boolean isSuccess, ArrayList<Tweet> tweetAlerts);
}
