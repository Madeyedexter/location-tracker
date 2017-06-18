package app.locationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by n188851 on 14-06-2017.
 */

public class LocationUtils {

    private static final String TAG = LocationUtils.class.getSimpleName();

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public static final double RADIUS_EARTH_METER = 6371000;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy - hh:mm:ss:SSS");
    private static final DecimalFormat decimalFormat = new DecimalFormat(".##");

    public static String getFormattedDate(long millis) {
        return simpleDateFormat.format(new Date(millis));
    }

    public static String getFormattedDecimal(double decimal) {
        return decimalFormat.format(decimal);
    }


    private LocationUtils() {
    }

    public static double getDistanceFromLatLon(double lat1, double lon1, double lat2, double lon2) {
        double dLat = toRadian(lat2 - lat1);  // deg2rad below
        double dLon = toRadian(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(toRadian(lat1)) * Math.cos(toRadian(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIUS_EARTH_METER * c; // Distance in m
    }

    private static final double toRadian(double degree) {
        return degree * (Math.PI / 180);
    }

    public static void requestLocationAccess(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
    }

    public static UpdateInterval getIntervalFromSpeed(double mSpeed) {
        UpdateInterval interval;
        double kmSpeed = mSpeed * 3.6;
        if (kmSpeed >= 80)
            interval = UpdateInterval.TINY;
        else if (kmSpeed < 80 && kmSpeed >= 60)
            interval = UpdateInterval.SMALL;
        else if (kmSpeed < 60 && kmSpeed >= 30)
            interval = UpdateInterval.MEDIUM;
        else interval = UpdateInterval.LARGE;
        return interval;
    }

    public static void requestExternalStorageAccess(MainActivity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}
