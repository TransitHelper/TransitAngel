package com.transitangel.transitangel.model.Transit;

import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;

import java.util.UUID;

/**
 * Created by vidhurvoora on 8/23/16.
 */
public class TrainStopFence implements Comparable {

    TrainStop trainStop;
    float radius;
    public String fenceId;

    public TrainStopFence(TrainStop trainStop,float radius) {
        this.trainStop = trainStop;
        this.radius = radius *1000.0f; //in km
    }

    public TrainStop getTrainStop() {
        return trainStop;
    }

    public float getRadius() {
        return radius;
    }

    public String getFenceId() {
        return fenceId;
    }

    public void setTrainStop(TrainStop trainStop) {
        this.trainStop = trainStop;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setFenceId(String fenceId) {
        this.fenceId = fenceId;
    }

    //currently set the transition type as enter and dwell
    //set to never expire -TODO double check if this is what we want
    public Geofence geofence() {
        fenceId = UUID.randomUUID().toString();
        return new Geofence.Builder()
                .setRequestId(fenceId)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(trainStop.getLatitude(), trainStop.getLongitude(), radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @Override
    public int compareTo(@NonNull Object another) {
        TrainStopFence other = (TrainStopFence) another;
        if ( trainStop.getName() != null
                && other.getTrainStop() != null &&
                other.getTrainStop().getName() != null ) {
            return trainStop.getName().compareTo(other.getTrainStop().getName());
        }
        return 0;

    }

}
