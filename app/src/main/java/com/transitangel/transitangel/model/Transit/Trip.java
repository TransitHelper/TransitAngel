package com.transitangel.transitangel.model.Transit;

import com.transitangel.transitangel.utils.TAConstants;

import java.util.Date;
import java.util.UUID;

/**
 * Created by vidhurvoora on 8/20/16.
 */
@org.parceler.Parcel
public class Trip {
    String tripId;
    Stop fromStop;
    Stop toStop;
    Train selectedTrain;
    Date date;
    boolean isFavorite;
    TAConstants.TRANSIT_TYPE type;


    public TAConstants.TRANSIT_TYPE getType() {
        return type;
    }

    public void setType(TAConstants.TRANSIT_TYPE type) {
        this.type = type;
    }

    public Stop getFromStop() {
        return fromStop;
    }

    public Stop getToStop() {
        return toStop;
    }

    public Train getSelectedTrain() {
        return selectedTrain;
    }

    public Date getDate() {
        return date;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getTripId() {
        return tripId;
    }

    public void setFromStop(Stop fromStop) {
        this.fromStop = fromStop;
    }

    public void setToStop(Stop toStop) {
        this.toStop = toStop;
    }

    public void setSelectedTrain(Train selectedTrain) {
        this.selectedTrain = selectedTrain;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Trip() {
        tripId = UUID.randomUUID().toString();
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
