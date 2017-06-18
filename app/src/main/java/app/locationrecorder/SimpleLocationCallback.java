package app.locationrecorder;

import android.location.Location;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import app.locationrecorder.models.LocationStamp;

/**
 * Created by Madeyedexter on 17-06-2017.
 */

public class SimpleLocationCallback extends LocationCallback {
    public LocationStamp getPreviousLocationStamp() {
        return previousLocationStamp;
    }

    public void setPreviousLocationStamp(LocationStamp previousLocationStamp) {
        this.previousLocationStamp = previousLocationStamp;
    }

    public void setLocationUpdateListener(LocationUpdateListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;
    }

    private LocationUpdateListener locationUpdateListener;

    interface LocationUpdateListener {
        void onFirstUpdate(LocationStamp locationStamp);

        void onUpdate(LocationStamp locationStamp);
    }

    private LocationStamp previousLocationStamp;

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Location currentLocation = locationResult.getLocations().get(0);
        LocationStamp locationStamp = new LocationStamp();
        if (previousLocationStamp == null) {
            //we will use the last known location
            Location lastKnownLocation = locationResult.getLastLocation();
            previousLocationStamp = new LocationStamp();
            previousLocationStamp.setTimestamp(System.currentTimeMillis());
            previousLocationStamp.setLatitude(currentLocation.getLatitude());
            previousLocationStamp.setLongitude(currentLocation.getLongitude());
            //calculate speed based on current location and last known location
            double distanceMeters = LocationUtils.getDistanceFromLatLon(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());
            double speedMS = (distanceMeters * 1000) / (previousLocationStamp.getTimestamp() - lastKnownLocation.getTime());

            previousLocationStamp.setNextInterval(LocationUtils.getIntervalFromSpeed(speedMS));
            previousLocationStamp.setSpeed(speedMS * 3.6);

            if (locationUpdateListener != null) {
                locationUpdateListener.onFirstUpdate(previousLocationStamp);
            }
        } else {
            long currentTimestamp = System.currentTimeMillis();
            //This block should not be executed unless we have the next interval or
            if (currentTimestamp - previousLocationStamp.getTimestamp() >= previousLocationStamp.getNextInterval().getInterval() * 1000) {
                long elapsedTime = currentTimestamp - previousLocationStamp.getTimestamp();
                double distanceMeters = LocationUtils.getDistanceFromLatLon(previousLocationStamp.getLatitude(), previousLocationStamp.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());
                double speedMS = (distanceMeters * 1000) / elapsedTime;
                UpdateInterval actualInterval = LocationUtils.getIntervalFromSpeed(speedMS);

                UpdateInterval currentInterval = previousLocationStamp.getNextInterval() == null ? actualInterval : previousLocationStamp.getNextInterval();
                UpdateInterval calculatedNextInterval = actualInterval;
                if (actualInterval.getInterval() > currentInterval.getInterval())
                    calculatedNextInterval = currentInterval.increment();
                else if (actualInterval.getInterval() < currentInterval.getInterval())
                    calculatedNextInterval = currentInterval.decrement();

                locationStamp.setCurrentInterval(currentInterval);
                locationStamp.setLatitude(currentLocation.getLatitude());
                locationStamp.setLongitude(currentLocation.getLongitude());
                locationStamp.setNextInterval(calculatedNextInterval);
                locationStamp.setTimestamp(currentTimestamp);
                locationStamp.setSpeed(speedMS * 3.6);

                previousLocationStamp = locationStamp;

                if (locationUpdateListener != null) {
                    locationUpdateListener.onUpdate(locationStamp);
                }
            }
        }

    }
}
