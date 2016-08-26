package com.transitangel.transitangel.model.Transit;

import android.os.Parcel;
import android.os.Parcelable;

import com.transitangel.transitangel.utils.TAConstants;

import java.util.Date;
import java.util.UUID;

/**
 * Created by vidhurvoora on 8/20/16.
 */
public class Trip implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tripId);
        dest.writeParcelable(this.fromStop, flags);
        dest.writeParcelable(this.toStop, flags);
        dest.writeParcelable(this.selectedTrain, flags);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeByte(this.isFavorite ? (byte) 1 : (byte) 0);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }

    protected Trip(Parcel in) {
        this.tripId = in.readString();
        this.fromStop = in.readParcelable(Stop.class.getClassLoader());
        this.toStop = in.readParcelable(Stop.class.getClassLoader());
        this.selectedTrain = in.readParcelable(Train.class.getClassLoader());
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.isFavorite = in.readByte() != 0;
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : TAConstants.TRANSIT_TYPE.values()[tmpType];
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}
