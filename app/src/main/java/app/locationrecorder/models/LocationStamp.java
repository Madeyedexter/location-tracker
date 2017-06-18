package app.locationrecorder.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.converter.PropertyConverter;

import app.locationrecorder.UpdateInterval;

/**
 * Created by Madeyedexter on 14-06-2017.
 * Models location data required to show location updates to the user.
 */
@Entity
public class LocationStamp implements Parcelable {

    private static final String TAG = LocationStamp.class.getSimpleName();

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

    private long timestamp;
    private double latitude;
    private double longitude;
    @Convert(converter = UpdateIntervalConverter.class, columnType = Integer.class)
    private UpdateInterval currentInterval;
    @Convert(converter = UpdateIntervalConverter.class, columnType = Integer.class)
    private UpdateInterval nextInterval;
    private double speed;


    public LocationStamp() {
    }

    protected LocationStamp(Parcel in) {
        timestamp = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        speed = in.readDouble();
        String cInterval = in.readString();
        String nInterval = in.readString();
        try {
            currentInterval = UpdateInterval.valueOf((cInterval == null ? "" : cInterval));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        try {
            nextInterval = UpdateInterval.valueOf((nInterval == null ? "" : nInterval));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }


    }

    @Generated(hash = 1504942068)
    public LocationStamp(long timestamp, double latitude, double longitude,
                         UpdateInterval currentInterval, UpdateInterval nextInterval, double speed) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentInterval = currentInterval;
        this.nextInterval = nextInterval;
        this.speed = speed;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(speed);
        dest.writeString(currentInterval != null ? currentInterval.name() : null);
        dest.writeString(nextInterval != null ? nextInterval.name() : null);

    }

    @Override
    public int describeContents() {
        return 0;
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

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public UpdateInterval getCurrentInterval() {
        return this.currentInterval;
    }

    public void setCurrentInterval(UpdateInterval currentInterval) {
        this.currentInterval = currentInterval;
    }

    public UpdateInterval getNextInterval() {
        return this.nextInterval;
    }

    public void setNextInterval(UpdateInterval nextInterval) {
        this.nextInterval = nextInterval;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Greendao helper class to convert enum fields to SQLite persistable data type.
     */
    static class UpdateIntervalConverter implements PropertyConverter<UpdateInterval, Integer> {
        @Override
        public UpdateInterval convertToEntityProperty(Integer databaseValue) {
            for (UpdateInterval updateInterval : UpdateInterval.values()) {
                if (updateInterval.getInterval() == databaseValue)
                    return updateInterval;
            }
            return UpdateInterval.TINY;
        }

        @Override
        public Integer convertToDatabaseValue(UpdateInterval entityProperty) {
            return entityProperty.getInterval();
        }
    }
}
