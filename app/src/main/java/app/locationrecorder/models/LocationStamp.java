package app.locationrecorder.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Madeyedexter on 14-06-2017.
 */

public class LocationStamp implements Parcelable {

    private long id;
    private double timestamp;
    private double latitude;
    private double longitude;
    private int currentInterval;
    private int nextInterval;

    protected LocationStamp(Parcel in) {
        id = in.readLong();
        timestamp = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        currentInterval = in.readInt();
        nextInterval = in.readInt();
    }

    public LocationStamp(){}


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCurrentInterval() {
        return currentInterval;
    }

    public void setCurrentInterval(int currentInterval) {
        this.currentInterval = currentInterval;
    }

    @Override
    public String toString() {
        return "LocationStamp{" +
                "timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", currentInterval=" + currentInterval +
                ", nextInterval=" + nextInterval +
                ", speed=" + speed +
                '}';
    }

    public int getNextInterval() {
        return nextInterval;
    }

    public void setNextInterval(int nextInterval) {
        this.nextInterval = nextInterval;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(timestamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(currentInterval);
        dest.writeInt(nextInterval);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationStamp> CREATOR = new Creator<LocationStamp>() {
        @Override
        public LocationStamp createFromParcel(Parcel in) {
            return new LocationStamp(in);
        }

        @Override
        public LocationStamp[] newArray(int size) {
            return new LocationStamp[size];
        }
    };

    public float speed;
}
