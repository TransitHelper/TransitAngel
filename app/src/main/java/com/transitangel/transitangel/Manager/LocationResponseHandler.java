package com.transitangel.transitangel.Manager;

import com.google.android.gms.maps.model.LatLng;

public interface LocationResponseHandler {
    public void OnLocationReceived (boolean isSuccess, LatLng latLng);
}
