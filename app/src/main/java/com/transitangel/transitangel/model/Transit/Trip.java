package com.transitangel.transitangel.model.Transit;

import java.util.Date;
/**
 * Created by vidhurvoora on 8/20/16.
 */
public class Trip {
    Stop fromStop;
    Stop toStop;
    Train selectedTrain;
    Date date;
    boolean isFavorite;

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
}
