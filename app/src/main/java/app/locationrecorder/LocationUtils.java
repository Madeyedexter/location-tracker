package app.locationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import app.locationrecorder.models.LocationStamp;

/**
 * Created by n188851 on 14-06-2017.
 */

public class LocationUtils {

    private static final String TAG = LocationUtils.class.getSimpleName();

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    public static final double RADIUS_EARTH_KM = 6371;



    private LocationUtils(){}

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = toRadian(lat2-lat1);  // deg2rad below
        double dLon = toRadian(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(toRadian(lat1)) * Math.cos(toRadian(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = RADIUS_EARTH_KM * c; // Distance in km
        return d;
    }

    private static final double toRadian(double degree) {
        return degree * (Math.PI/180);
    }

    public static void requestLocationAccess(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission Granted: True" );
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);

            }
        }
    }

    public static LocationStamp getLocationModelStamp(Location previousLocation, Location currentLocation) {

        float currentKmSpeed=currentLocation.getSpeed()/1000;
        Log.d(TAG,"Speed: "+currentKmSpeed);
        UpdateInterval currentIntervalBucket = getIntervalFromSpeed(previousLocation.getSpeed());
        UpdateInterval nextIntervalBucket =getIntervalFromSpeed(currentLocation.getSpeed());
        int nextInterval;
        if(currentIntervalBucket.getInterval() > nextIntervalBucket.getInterval()){
            nextInterval = currentIntervalBucket.increment().getInterval();
        }
        else if (currentIntervalBucket.getInterval() < nextIntervalBucket.getInterval()){
            nextInterval = currentIntervalBucket.decrement().getInterval();
        }
        else nextInterval = currentIntervalBucket.getInterval();


        LocationStamp locationStamp = new LocationStamp();
        locationStamp.setTimestamp(System.currentTimeMillis());
        locationStamp.setCurrentInterval(currentIntervalBucket.getInterval());
        locationStamp.setNextInterval(nextInterval);
        locationStamp.setLatitude(currentLocation.getLatitude());
        locationStamp.setLongitude(currentLocation.getLongitude());

        locationStamp.speed = currentKmSpeed;

        return locationStamp;
    }

    private static UpdateInterval getIntervalFromSpeed(float mSpeed){
        UpdateInterval interval;
        float kmSpeed = mSpeed/1000;
        if(kmSpeed >= 80)
            interval = UpdateInterval.TINY;
        else if(kmSpeed < 80 && kmSpeed >= 60)
            interval = UpdateInterval.SMALL;
        else if(kmSpeed < 60 && kmSpeed >= 30)
            interval = UpdateInterval.MEDIUM;
        else interval = UpdateInterval.LARGE;
        return interval;
    }

    private enum UpdateInterval{ TINY(30000), SMALL(60000), MEDIUM(120000), LARGE(300000);
        private int interval;

        UpdateInterval(int interval){
            this.interval=interval;
        }

        public int getInterval(){
            return interval;
        }

        public UpdateInterval increment(){
            return this.ordinal() < UpdateInterval.values().length-1
                    ? UpdateInterval.values()[this.ordinal()+1]
                    : UpdateInterval.values()[this.ordinal()];
        }

        public UpdateInterval decrement(){
            return this.ordinal() < UpdateInterval.values().length-1
                    ? UpdateInterval.values()[this.ordinal()+1]
                    : UpdateInterval.values()[this.ordinal()];
        }
    }
}
