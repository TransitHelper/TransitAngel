package com.transitangel.transitangel.model.Transit;

import android.os.Parcel;
import android.os.Parcelable;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class TrainStop implements Parcelable {
    String stopId;
    String arrrivalTime;
    String departureTime;
    String stopOrder;
    String name;
    String longitude;
    String latitude;

    public TrainStop(JSONObject trainStopObj) throws JSONException {
        stopOrder = trainStopObj.getString("order");
        stopId = trainStopObj.getJSONObject("ScheduledStopPointRef").getString("ref");
        arrrivalTime = trainStopObj.getJSONObject("Arrival").getString("Time");
        departureTime = trainStopObj.getJSONObject("Departure").getString("Time");
        HashMap<String,Stop> stopLookup = CaltrainTransitManager.getSharedInstance().getStopLookup();
        Stop stop =  stopLookup.get(stopId);
        if ( stop == null ) {
            //check bart
            HashMap<String,Stop> bartStopLookup = BartTransitManager.getSharedInstance().getStopLookup();
            stop = bartStopLookup.get(stopId);
        }
        if ( stop != null ) {
            name = stop.getName();
            latitude = stop.getLatitude();
            longitude = stop.getLongitude();
        }
    }

    public TrainStop() {

    }

    public String getStopId() {
        return stopId;
    }

    public String getArrrivalTime() {
        return arrrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getStopOrder() {
        return Integer.parseInt(stopOrder);
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return Double.parseDouble(longitude);
    }

    public double getLatitude() {
        return Double.parseDouble(latitude);
    }

    public String getLongitudeStr() {
        return longitude;
    }

    public String getLatitudeStr() {
        return latitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.stopId);
        dest.writeString(this.arrrivalTime);
        dest.writeString(this.departureTime);
        dest.writeString(this.stopOrder);
        dest.writeString(this.name);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
    }

    protected TrainStop(Parcel in) {
        this.stopId = in.readString();
        this.arrrivalTime = in.readString();
        this.departureTime = in.readString();
        this.stopOrder = in.readString();
        this.name = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
    }

    public static final Parcelable.Creator<TrainStop> CREATOR = new Parcelable.Creator<TrainStop>() {
        @Override
        public TrainStop createFromParcel(Parcel source) {
            return new TrainStop(source);
        }

        @Override
        public TrainStop[] newArray(int size) {
            return new TrainStop[size];
        }
    };

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public void setArrrivalTime(String arrrivalTime) {
        this.arrrivalTime = arrrivalTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setStopOrder(String stopOrder) {
        this.stopOrder = stopOrder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
