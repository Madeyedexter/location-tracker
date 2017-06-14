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
}
