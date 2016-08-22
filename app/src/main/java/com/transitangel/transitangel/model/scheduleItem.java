package com.transitangel.transitangel.model;

import com.transitangel.transitangel.model.Transit.Train;

public class scheduleItem {
    String from;
    String to;
    String DepatureTime;
    String ImportantInformation;
    Train train;

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
    public scheduleItem(String from, String to, String depatureTime, String importantInformation, Train train) {
        this.from = from;
        this.to = to;
        this.DepatureTime=depatureTime;
        this.ImportantInformation=importantInformation;
        this.train = train;
    }
}
