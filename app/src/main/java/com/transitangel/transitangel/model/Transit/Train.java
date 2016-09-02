package com.transitangel.transitangel.model.Transit;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vidhurvoora on 8/19/16.
 */

public class Train implements Parcelable {
    String number;
    String name;
    String direction;
    ArrayList<TrainStop> trainStops;

    public Train(JSONObject trainObj) throws JSONException {
        number = trainObj.getString("id");
        direction = trainObj.getJSONObject("JourneyPatternView").getJSONObject("DirectionRef").getString("ref");
        JSONArray stopObjs = trainObj.getJSONObject("calls").getJSONArray("Call");
        trainStops = new ArrayList<TrainStop>();
        for (int i=0;i<stopObjs.length();i++){
            JSONObject trainStopObj = stopObjs.getJSONObject(i);
            TrainStop trainStop = new TrainStop(trainStopObj);
            trainStops.add(trainStop);
        }
    }

    public Train(){

    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getDirection() {
        return direction;
    }

    public ArrayList<TrainStop> getTrainStops() {
        return trainStops;
    }

    public TrainStop getTrainStop(String stopId) {
        for(TrainStop trainStop: trainStops) {
            if(trainStop.getStopId().equalsIgnoreCase(stopId)) {
                return trainStop;
            }
        }
        return null;
    }

    public ArrayList<TrainStop> getTrainStopsBetween(String fromStopId,String toStopId ) {
        ArrayList<TrainStop> trainStopsInBetween = new ArrayList<>();
        boolean shouldInclude = false;
        for ( TrainStop  trainStop : trainStops ) {
            if ( trainStop.getStopId().equalsIgnoreCase(fromStopId)) {
                shouldInclude = true;
            }
            if ( shouldInclude ) {
                trainStopsInBetween.add(trainStop);
            }
            if (trainStop.getStopId().equalsIgnoreCase(toStopId)) {
                shouldInclude = false;
            }
        }
        return trainStopsInBetween;
    }

    public int getTrainStopsBetweenCount(String fromStopId, String toStopId ) {
        ArrayList<TrainStop> trainStopsInBetween = getTrainStopsBetween(fromStopId,toStopId);
        return trainStopsInBetween.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
        dest.writeString(this.name);
        dest.writeString(this.direction);
        dest.writeTypedList(this.trainStops);
    }

    protected Train(Parcel in) {
        this.number = in.readString();
        this.name = in.readString();
        this.direction = in.readString();
        this.trainStops = in.createTypedArrayList(TrainStop.CREATOR);
    }

    public static final Creator<Train> CREATOR = new Creator<Train>() {
        @Override
        public Train createFromParcel(Parcel source) {
            return new Train(source);
        }

        @Override
        public Train[] newArray(int size) {
            return new Train[size];
        }
    };
}
