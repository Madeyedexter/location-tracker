package app.locationrecorder;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;

import app.locationrecorder.models.LocationStamp;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Toast toast;

    private TextView tvStatus;
    private TextView tvLocationInfo;
    private Button buttonStartTracking;

    private long previousTime;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy - hh:mm:ss:SSS");


    private boolean tracking=false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG,"Location count: "+locationResult.getLocations().size());
            Location lastLocation = tvLocationInfo.getTag()==null?locationResult.getLastLocation():(Location)tvLocationInfo.getTag();
            Location currentlocation = locationResult.getLocations().get(0);
            LocationStamp locationStamp = LocationUtils.getLocationModelStamp(lastLocation,currentlocation);
            tvLocationInfo.setText(locationStamp.toString());
        }
    };

    private OnSuccessListener<LocationSettingsResponse> onSuccessListener = new OnSuccessListener<LocationSettingsResponse>() {
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            startLocationUpdates();
        }
    };

    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            showToast(MainActivity.this.getString(R.string.message_setting_high_accruacy));
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvLocationInfo = (TextView) findViewById(R.id.tvLocationInfo);
        buttonStartTracking = (Button) findViewById(R.id.buttonStartTracking);

        buttonStartTracking.setOnClickListener(this);

        LocationUtils.requestLocationAccess(this);
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(LocationServices.API)
                .build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


    }

    private void startLocationUpdates() {
        try{
        mFusedLocationClient.requestLocationUpdates(createLocationRequest(),
                mLocationCallback,
                null /* Looper */);
        }catch (SecurityException se){
            tvLocationInfo.setText(R.string.message_no_permission);
        }
    }

    @Override
    public void onClick(View v) {

        LocationUtils.requestLocationAccess(this);
        switch (v.getId()){
            case R.id.buttonStartTracking: if(tracking) stopTracking();
            else {
                requestLocationSettings();
            }
                break;
        }
    }

    private void requestLocationSettings() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> response = settingsClient.checkLocationSettings(builder.build());
        response.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    private void stopTracking() {
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
        }
    }

    private void showToast(String message){
        if(toast!=null)
            toast.cancel();
        toast = Toast.makeText(this,message,Toast.LENGTH_SHORT);
        toast.show();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        //get an update atleast every 30 seconds.
        locationRequest.setInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Unable to connect to Google Location API");
    }
}
