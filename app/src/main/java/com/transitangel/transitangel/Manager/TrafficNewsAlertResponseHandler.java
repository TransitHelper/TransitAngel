package com.transitangel.transitangel.Manager;

import com.transitangel.transitangel.model.Transit.TrafficNewsAlert;

import java.util.ArrayList;

public interface TrafficNewsAlertResponseHandler {
    public void onNewsAlertsReceived(boolean isSuccess, ArrayList<TrafficNewsAlert> trafficNewsAlerts);
}

