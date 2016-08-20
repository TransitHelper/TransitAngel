package com.transitangel.transitangel.Manager;

import com.transitangel.transitangel.model.Transit.Stop;

import java.util.ArrayList;

public interface StopResponseHandler {
    public void OnStopsResponseReceived(boolean isSuccess, ArrayList<Stop> stops);
}
