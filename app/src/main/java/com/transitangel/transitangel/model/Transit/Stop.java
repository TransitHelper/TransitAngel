package com.transitangel.transitangel.model.Transit;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class Stop implements Parcelable {
    String id;
    String name;
    String longitude;
    String latitude;

    public Stop(JSONObject stopObj) throws JSONException {
        this.id = stopObj.getString("id");
        String name = stopObj.getString("Name");
        //name clean up
        if ( name.contains("CALTRAIN - ")) {
            name = name.replace("CALTRAIN - ","");
        }
        else if (name.contains("BART ")) {
            name = name.replace("BART ","");
        }
        this.name = name;
        this.longitude = stopObj.getJSONObject("Location").getString("Longitude");
        this.latitude = stopObj.getJSONObject("Location").getString("Latitude");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    //to display object as a string in spinner
    public String toString() {
        return name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.longitude);
        dest.writeString(this.latitude);
    }

    protected Stop(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.longitude = in.readString();
        this.latitude = in.readString();
    }

    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel source) {
            return new Stop(source);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };
}
