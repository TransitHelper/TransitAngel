package com.transitangel.transitangel.model;

public class scheduleItem {
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

    String from;
    String to;
    String DepatureTime;
    String ImportantInformation;


    public scheduleItem(String from, String to) {
        this.from = from;
        this.to = to;
    }
    public scheduleItem(String from, String to, String depatureTime, String importantInformation) {
        this.from = from;
        this.to = to;
        this.DepatureTime=depatureTime;
        this.ImportantInformation=importantInformation;

    }
}
