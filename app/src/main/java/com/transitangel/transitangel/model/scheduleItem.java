package com.transitangel.transitangel.model;

import com.transitangel.transitangel.model.Transit.Train;

public class scheduleItem {
    String from;
    String to;
    String DepatureTime;
    String ImportantInformation;
    Train train;
    String fromStopID;

    public String getFromStopID() {
        return fromStopID;
    }

    public void setFromStopID(String fromStopID) {
        this.fromStopID = fromStopID;
    }

    public String getToStopID() {
        return toStopID;
    }

    public void setToStopID(String toStopID) {
        this.toStopID = toStopID;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    String toStopID;

    public String getDepatureTime() {
        return DepatureTime;
    }

    public void setDepatureTime(String depatureTime) {
        DepatureTime = depatureTime;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getImportantInformation() {
        return ImportantInformation;
    }

    public void setImportantInformation(String importantInformation) {
        ImportantInformation = importantInformation;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Train getTrain() {
        return train;
    }

    public scheduleItem(String from, String to) {
        this.from = from;
        this.to = to;
    }
    public scheduleItem(String from, String to,String fromStopID,String toStopID, String depatureTime,Train train) {
        setTo(to);
        setFrom(from);
        setFromStopID(fromStopID);
        setToStopID(toStopID);
        setDepatureTime(depatureTime);
        setTrain(train);
    }
}
