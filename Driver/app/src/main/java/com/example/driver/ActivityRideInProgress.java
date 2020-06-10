package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideInProgress extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityRideInProgress";
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    Button endRide;
    private static ActivityRideInProgress instance;
    String lat, lng, aadhar, auth;
    FusedLocationProviderClient mFusedLocationClient;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    public void onSuccess(JSONObject response, int id) throws JSONException {
        if (id == 1) {
            //polling to send driver's device location every 30 sec to server
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("3");
            startService(i);

        }
        if (id == 2) {
            //if ride has ended successfully, call the next activity
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

            Intent endIntent = new Intent(ActivityRideInProgress.this, ActivityRideCompleted.class);
            startActivity(endIntent);
            finish();
        }
        if (id == 3) {
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
            String active = response.getString("active");
            if (active.equals("false")) {
                Intent home = new Intent(ActivityRideInProgress.this, ActivityHome.class);
                startActivity(home);
                finish();

            } else if (active.equals("true")) {
                String tid = response.getString("tid");
                String st = response.getString("st");
                if (st.equals("FN") || st.equals("TR")) {
                    Intent inProgress = new Intent(ActivityRideInProgress.this, ActivityRideCompleted.class);
                    startActivity(inProgress);
                    finish();
                }
            }
            //polling to check the status of ride
            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("5");
            startService(i);
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_in_progress);
        instance = this;
//retrieving locally stored data
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        aadhar = sharedPreferences.getString(AADHAR, "");
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        auth = cookie.getString(COOKIE, "");
        //request the last known location of the driver's device
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRideInProgress.this);
//initializing variables
        endRide = findViewById(R.id.btn_endRide);
        endRide.setOnClickListener(this);
        rideStatus();// method to check status of ride
    }
    // method to end ride
    private void endTrip() {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideInProgress a = ActivityRideInProgress.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-end");
        UtilityApiRequestPost.doPOST(a, "driver-ride-end", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }
//method to check status of ride
    protected void rideStatus() {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideInProgress a = ActivityRideInProgress.this;

        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 3);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    public static ActivityRideInProgress getInstance() {
        return instance;
    }
//method to send location update of driver's device
    protected void sendLocation() {

        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("an", aadhar);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);
        ActivityRideInProgress a = ActivityRideInProgress.this;

        Log.d(TAG, "Values: auth=" + auth + " an=" + aadhar + " lat=" + lat + " lng=" + lng);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }
// method to check permissions granted by user
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }
//method to get last know location of driver's device
    public void getLastLocation() {
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    task -> {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            lat = location.getLatitude() + "";
                            lng = location.getLongitude() + "";
                            Log.d(TAG, "lat = " + lat + " lng = " + lng);
                            sendLocation();
                        }
                    });
        }
    }
//method to store new location data
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else
            getLastLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_endRide:
                endTrip();//method to end ride
                break;
        }
    }
}
