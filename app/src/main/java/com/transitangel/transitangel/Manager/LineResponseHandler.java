package com.transitangel.transitangel.Manager;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.model.Transit.Line;

import java.util.ArrayList;

/**
 * Created by vidhurvoora on 8/18/16.
 */

public interface LineResponseHandler {
    public void OnLinesResponseReceived(boolean isSuccess, ArrayList<Line> lines);
}

