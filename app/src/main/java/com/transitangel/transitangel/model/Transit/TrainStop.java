package com.transitangel.transitangel.model.Transit;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/19/16.
 */
public class TrainStop implements Parcelable {
    String stopId;
    String arrrivalTime;
    String departureTime;
    String stopOrder;

    public TrainStop(JSONObject trainStopObj) throws JSONException {
        stopOrder = trainStopObj.getString("order");
        stopId = trainStopObj.getJSONObject("ScheduledStopPointRef").getString("ref");
        arrrivalTime = trainStopObj.getJSONObject("Arrival").getString("Time");
        departureTime = trainStopObj.getJSONObject("Departure").getString("Time");
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
    }

    protected TrainStop(Parcel in) {
        this.stopId = in.readString();
        this.arrrivalTime = in.readString();
        this.departureTime = in.readString();
        this.stopOrder = in.readString();
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
}
