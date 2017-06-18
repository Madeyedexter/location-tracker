package app.locationrecorder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import app.locationrecorder.models.LocationStamp;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        //A Listener to listen for updates whenever the SimpleLocationCallback receives an update from the FusedLocationAPI
        SimpleLocationCallback.LocationUpdateListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_LOCATION_STAMP = "KEY_LOCATION_STAMP";
    private static final String KEY_TRACKING = "KEY_TRACKING";


    private Toast toast;

    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.buttonStartTracking)
    Button buttonStartTracking;
    @BindView(R.id.tvTimestamp)
    TextView tvTimestamp;
    @BindView(R.id.tvLatitude)
    TextView tvLatitude;
    @BindView(R.id.tvLongitude)
    TextView tvLongitude;
    @BindView(R.id.tvCurrentInterval)
    TextView tvCurrentInterval;
    @BindView(R.id.tvNextInterval)
    TextView tvNextInterval;
    @BindView(R.id.tvSpeed)
    TextView tvSpeed;
    @BindView(R.id.tableData)
    TableLayout tableData;

    /**
     * state variable to control location tracker button behavior
     */
    private boolean tracking = false;

    /**
     * FusedLocationProvider client retrieves location details using Google Location API
     * and hides complexities of managing GPS connections and requests
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback to listen for location updates.
     */
    private SimpleLocationCallback mLocationCallback = new SimpleLocationCallback();


    /**
     * This method binds location data to the UI.
     *
     * @param locationStamp The LocationStamp whose data should be bound to the UI
     */
    private void bindLocationData(LocationStamp locationStamp) {
        tvTimestamp.setText(LocationUtils.getFormattedDate(locationStamp.getTimestamp()));
        tvLatitude.setText(LocationUtils.getFormattedDecimal(locationStamp.getLatitude()));
        tvLongitude.setText(LocationUtils.getFormattedDecimal(locationStamp.getLongitude()));
        tvCurrentInterval.setText(locationStamp.getCurrentInterval() == null ? "N/A" : String.valueOf(locationStamp.getCurrentInterval().getInterval()));
        tvNextInterval.setText(String.valueOf(locationStamp.getNextInterval().getInterval()));
        tvSpeed.setText(LocationUtils.getFormattedDecimal(locationStamp.getSpeed()));
    }

    /**
     * Clears location data from the UI. Called whenever the user clicks on the Stop tracking button
     * to clear old location info from the UI. Iterates over each table row in the UI, and clears the TextView
     * within each row.
     */
    private void clearLocationData() {
        for (int i = 0; i < tableData.getChildCount(); i++) {
            ((TextView) ((TableRow) tableData.getChildAt(i)).getChildAt(1)).setText(null);
        }
    }

    /**
     * Callback invoked when the current location Settings and the location settings requested by
     * requestLocationSettings() method are satisfied.
     */
    private OnSuccessListener<LocationSettingsResponse> onSuccessListener = new OnSuccessListener<LocationSettingsResponse>() {
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            //2000ms to calculate speed.
            startLocationUpdates(2000);
        }
    };

    /**
     * Callback invoked when the current location settings of the device do not match the
     * requested location settings
     */
    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            showToast(MainActivity.this.getString(R.string.message_setting_high_accruacy));
            buttonStartTracking.setEnabled(true);
            buttonStartTracking.setText(R.string.start_tracking);
            tracking = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        buttonStartTracking.setOnClickListener(this);
        mLocationCallback.setLocationUpdateListener(this);

        LocationUtils.requestLocationAccess(this);
        LocationUtils.requestExternalStorageAccess(this);

        //Our activity auto-manages the Google API Client connectivity lifecycle.
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        //Get fused location client from Google Location API for location updates.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (savedInstanceState != null) {
            //restore tracking status
            tracking = savedInstanceState.getBoolean(KEY_TRACKING);
            if (tracking) {
                //Restore the last location stamp.
                mLocationCallback.setPreviousLocationStamp((LocationStamp) savedInstanceState.getParcelable(KEY_LOCATION_STAMP));
                //if the location tracking was enabled when the device was rotated, we need to restart location tracking by considering
                // the time elapsed since last update.
                long elapsedTime = System.currentTimeMillis() - mLocationCallback.getPreviousLocationStamp().getTimestamp();
                //remove the elapsed time from the next update interval and start a new location update since activity was restarted due to configuration change.
                int interval = (int) (mLocationCallback.getPreviousLocationStamp().getNextInterval().getInterval() * 1000 - elapsedTime);
                if (interval > 0)
                    //The calculated interval can be less than zero if the GPS is unable to find the device's location, so we make sure that the interval is a positive value
                    startLocationUpdates(interval > 0 ? interval : 2000);
            }
        } else {
            //this is the first launch of the activity, display appropriate messages
            tvStatus.setText(R.string.message_start_tracking);
            buttonStartTracking.setText(R.string.start_tracking);
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save location stamp encapsulated within the LocationCallback implementation.
        outState.putParcelable(KEY_LOCATION_STAMP, mLocationCallback.getPreviousLocationStamp());
        outState.putBoolean(KEY_TRACKING, tracking);
    }

    /**
     * Requests location updates from the fused location API client, by creating a location request for
     * every @param interval milli-seconds.
     */
    private void startLocationUpdates(long interval) {
        try {
            mFusedLocationClient.requestLocationUpdates(createLocationRequest(interval),
                    mLocationCallback,
                    null /* Looper */);
            tvStatus.setText(R.string.requesting_location);
            buttonStartTracking.setEnabled(true);
            buttonStartTracking.setText(R.string.stop_tracking);
            tracking = true;
        } catch (SecurityException se) {
            tvStatus.setText(R.string.message_no_permission);
            buttonStartTracking.setEnabled(true);
            buttonStartTracking.setText(R.string.start_tracking);
            tracking = false;
        }
    }

    @Override
    public void onClick(View v) {
        LocationUtils.requestLocationAccess(this);
        switch (v.getId()) {
            case R.id.buttonStartTracking:
                if (tracking) stopTracking();
                else {
                    requestLocationSettings();
                }
                break;
        }
    }

    /**
     * requests location settings from the SettingsClient. Registers a success/failure listener to be invoked based on the
     * current GPS settings of the user's device.
     */
    private void requestLocationSettings() {
        //disable the button
        buttonStartTracking.setEnabled(false);
        tvStatus.setText(R.string.connecting);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> response = settingsClient.checkLocationSettings(builder.build());
        response.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    /**
     * Called when the user clicks on stop tracking button. Removes location listener and clears data from
     * the UI.
     */
    private void stopTracking() {
        tracking = false;
        tvStatus.setText(R.string.message_start_tracking);
        buttonStartTracking.setText(R.string.start_tracking);
        //remove listeners
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mLocationCallback.setPreviousLocationStamp(null);
        clearLocationData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LocationUtils.PERMISSION_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0) {
                    showToast(getString(R.string.message_need_permission_location));
                    finish();
                }
            }
            case LocationUtils.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length == 0) {
                    showToast(getString(R.string.message_no_storage_permission));
                    finish();
                }
        }
    }

    /**
     * Shows a toast with the specified @param message
     * Cancels any previous toasts.
     */
    private void showToast(String message) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Creates a @return LocationRequest object for the specified @param interval.
     */
    protected LocationRequest createLocationRequest(long interval) {
        LocationRequest locationRequest = new LocationRequest();
        //get an update atleast every 15 seconds.
        locationRequest.setInterval(interval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(interval);
        return locationRequest;
    }

    /**
     * Invoked when the connection to the Google Location API is unsuccessful.
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast(getString(R.string.connection_failed));
    }

    /**
     * Invoked when the SimpleLocationCallback receives Location response from Fused LocationAPI client for the first time.
     *
     * @param locationStamp
     */
    @Override
    public void onFirstUpdate(LocationStamp locationStamp) {
        tvStatus.setText(R.string.tracking_in_progress);
        bindLocationData(locationStamp);
        Log.d(TAG, "Before Parcel: " + locationStamp);
        DataPersistanceService.startActionWriteToDb(this, locationStamp);
        DataPersistanceService.startActionWriteToFile(this, locationStamp);
    }

    /**
     * Invoked on subsequesnt location updates.
     *
     * @param locationStamp
     */
    @Override
    public void onUpdate(LocationStamp locationStamp) {
        bindLocationData(locationStamp);
        DataPersistanceService.startActionWriteToDb(this, locationStamp);
        DataPersistanceService.startActionWriteToFile(this, locationStamp);
    }
}
