package app.locationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by n188851 on 14-06-2017.
 */

public class LocationUtils {

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSION_REQUEST_FINE_LOCATION);

            }
        }
    }
}
