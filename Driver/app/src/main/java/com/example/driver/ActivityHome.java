package com.example.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ActivityHome extends ActivityDrawer implements View.OnClickListener {
    Dialog myDialog;
    Switch status_duty;
    String driverStatus = "";
    private static final String TAG = "ActivityHome";
    ScrollView scrollView;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    FusedLocationProviderClient mFusedLocationClient;
    String lat, lng, str_phone, aadhar;
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String FIRST_LAUNCH = "FirstLaunch";
    public static final String DRIVER_STATUS = "DriverStatus";
    public static final String STATUS = "Status";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";
    public static final String MY_LAT = "MYSrcLAT";
    public static final String MY_LNG = "MYSrcLng";
    TextView newOrder, /*inProgress*/
            completedOrder, totalEarnings;

    Vibrator vibrator;

    private static ActivityHome instance;
    String auth;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};

    TextView notify;
    String strAuth;
    String tripID;

    ActivityHome a = ActivityHome.this;
    Map<String, String> params = new HashMap();

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_home, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        instance = this;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);// initializing vibration service for this activity

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth


        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        aadhar = sharedPreferences.getString(AADHAR, "");// retrieve aadhaar value stored locally and assign it to String aadhar

        SharedPreferences pref = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
        String stringStatus = pref.getString(STATUS, "");

        SharedPreferences delPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);

        tripID = delPref.getString(TID, "");
        //initializing variables
        status_duty = findViewById(R.id.dutyStatus);
        scrollView = findViewById(R.id.scrollLayout);
        newOrder = findViewById(R.id.new_ride);
        //inProgress = findViewById(R.id.ride_in_progress);
        //completedOrder = findViewById(R.id.completed_rides);
        //totalEarnings = findViewById(R.id.earnings);
        notify = findViewById(R.id.notifNo);

        newOrder.setOnClickListener(this);
        //inProgress.setOnClickListener(this);
        //completedOrder.setOnClickListener(this);
        //totalEarnings.setOnClickListener(this);

        myDialog = new Dialog(this);
        auth = strAuth;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityHome.this);// needed for gsp tracking

        if (stringStatus.equals("AV")) {
            status_duty.setChecked(true);
            getLastLocation();// method for getting the last know latitude and longitude of driver
            getStatus();
            Log.d(TAG, "call 1");
        }
        if (!stringStatus.equals("AV")) {
            status_duty.setChecked(false);
        }
        status_duty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    driverStatus = "AV";//driver online
                    //store the value of driver status (AV) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply(); // storing driver status locally for later use
                    Log.d(TAG, "driverStatus = " + driverStatus);
                    driverSetMode(driverStatus);
                    getLastLocation();// method for getting the last know latitude and longitude of driver
                    Log.d(TAG, "call 2");
                    getStatus();

                } else {
                    driverStatus = "OF";//driver offline
                    //store the value of driver status (OF) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply();
                    //alertDialog();
                    ShowPopup(2);
                    //TODO work on alert

                    vehicleRetire();
                    Log.d(TAG, "driverStatus = " + driverStatus);
                    driverSetMode(driverStatus);
                }
            }
        });
    }

    //method to initiate and populate dialog box
    private void ShowPopup(int id) {
        myDialog.setContentView(R.layout.popup_text);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        if (id == 2) {
            infoText.setText(R.string.offline);
        }
        if (id == 3) {
            //vibrate the device for 1000 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
            infoText.setText("YOU ARE CURRENTLY LOCKED ! \nCONTACT ADMIN");
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 4) {
            infoText.setText(R.string.ride_completed);
            retireRide();
        }
        if (id == 5) {
            infoText.setText(R.string.time_out);
            retireRide();
        }
        if (id == 6) {
            infoText.setText(R.string.ride_fail);
            retireRide();
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);// dialog box will be dismissed if screen outside the box is touched
    }

    public static ActivityHome getInstance() {
        return instance;
    }

    //method to check if permissions are granted to the app by the driver

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
    //method to check and record the last know location of the driver

    public void getLastLocation() {
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
                                SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                                sp_cookie.edit().putString(MY_LAT, lat).apply();
                                sp_cookie.edit().putString(MY_LNG, lng).apply();
                                sendLocation();// method to hit auth-location-update API
                            }
                        }
                    });
        }
    }
    //gps tracking with high accuracy. This means that the location is checked every 1 millisecond

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
    // with every change in location this method is called

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
        } else {
            getLastLocation();
            Log.d(TAG, "call 3");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.new_ride:
                //Intent newOrderIntent = new Intent(ActivityHome.this, ActivityNewRide.class);
                driverRideCheck();
                /*Intent newOrderIntent = new Intent(ActivityHome.this, MapsReachUser.class);
                startActivity(newOrderIntent);*/
                break;
           /* case R.id.ride_in_progress:
                Intent inProgressIntent = new Intent(ActivityHome.this, ActivityInProgress.class);
                startActivity(inProgressIntent);
                break;*/
            /*case R.id.completed_orders:
                Intent completedOrderIntent = new Intent(ActivityHome.this, ActivityCompletedOrders.class);
                startActivity(completedOrderIntent);
                break;*/
            /*case R.id.earnings:
                Intent earningsIntent = new Intent(ActivityHome.this, ActivityTotalEarnings.class);
                startActivity(earningsIntent);
                break;*/
        }
    }

    private void driverSetMode(String mode) {
        params.put("auth", auth);
        params.put("st", mode);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth= " + auth + " st=" + mode);
        Log.d(TAG, "UtilityApiRequestPost.doPOST driver-set-mode");
        UtilityApiRequestPost.doPOST(a, "driver-set-mode", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    private void vehicleRetire() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth= " + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST driver-vehicle-retire");
        UtilityApiRequestPost.doPOST(a, "driver-vehicle-retire", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void sendLocation() {
        params.put("an", aadhar);
        params.put("auth", auth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + auth + " lat =" + lat + " lng = " + lng + " an=" + aadhar);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void getStatus() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void driverRideCheck() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST driver-ride-check");
        UtilityApiRequestPost.doPOST(a, "driver-ride-check", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void rideGetInfo() {
        params.put("auth", auth);
        params.put("tid", tripID);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "auth = " + auth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void retireRide() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-retire");
        UtilityApiRequestPost.doPOST(a, "driver-ride-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 6);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void isVehicleSet() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-is-vehicle-set");
        UtilityApiRequestPost.doPOST(a, "driver-is-vehicle-set", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 7);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting driver-set-mode API
        if (id == 1) {
            String st = response.getString("st");
            switch (st) {
                case "AV":
                    isVehicleSet();
                    status_duty.setChecked(true);
                    break;
                case "LK":
                    ShowPopup(3);
                    break;
                case "OF":
                    status_duty.setChecked(false);
                    break;
                case "BK":
                    Log.d(TAG, "driver booked");
                    break;
            }
        }
        //response on hitting driver-is-vehicle-set API
        if (id == 7) {
            String set = response.getString("set");
            if (set.equals("true")) {
                getLastLocation();
                Log.d(TAG, "call 4");
            }
            if (set.equals("false")) {
                Intent getVehicle = new Intent(ActivityHome.this, VehicleList.class);
                startActivity(getVehicle);
            }
        }

        //response on hitting auth-location-update API
        if (id == 2) {
           /* Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("01");
            startService(i);*/
        }
        //response on hitting driver-ride-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TID, tid).apply();
                    tripID = tid;
                    if (status.equals("AS")) {
                        /*String srcLat = response.getString("srclat");
                        String srcLng = response.getString("srclng");
                        String dstLat = response.getString("dstlat");
                        String dstLng = response.getString("dstlng");
                        String phone = response.getString("uphone");
                        String name = response.getString("uname");*/

                        //getStatus();
                        Intent as = new Intent(ActivityHome.this, ActivityRideAccepted.class);
                        startActivity(as);

                    }
                    if (status.equals("ST")) {
                        String dstLat = response.getString("dstlat");
                        String dstLng = response.getString("dstlng");
                        SharedPreferences delvyPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(DSTLAT, dstLat).apply();
                        delvyPref.edit().putString(DSTLNG, dstLng).apply();
                        Intent st = new Intent(ActivityHome.this, MapsActivity2.class);
                        startActivity(st);

                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        Intent pd = new Intent(ActivityHome.this, ActivityRideCompleted.class);
                        startActivity(pd);
                    }

                } else if (active.equals("false")) {
                    try {
                        String status = response.getString("st");
                        String tid = response.getString("tid");
                        rideGetInfo();

                    } catch (Exception e) {
                        e.printStackTrace();
                        driverRideCheck();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        //response on hitting driver-ride-check API
        if (id == 4) {

            try {
                String tid = response.getString("tid");
                String srclat = response.getString("srclat");
                String srclng = response.getString("srclng");
                notify.setVisibility(View.VISIBLE);
                newOrder.setClickable(true);
                notify.setText("1");
                SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                sp_cookie.edit().putString(TID, tid).apply();
                sp_cookie.edit().putString(SRCLAT, srclat).apply();
                sp_cookie.edit().putString(SRCLNG, srclng).apply();

                Intent newOrderIntent = new Intent(ActivityHome.this, MapsReachUser.class);
                startActivity(newOrderIntent);

            } catch (Exception e) {
                // Toast.makeText(this, "No New Rides", Toast.LENGTH_LONG).show();
                notify.setVisibility(View.VISIBLE);
                newOrder.setClickable(false);
                notify.setText("0");
                Intent i = new Intent(this, UtilityPollingService.class);
                i.setAction("02");
                startService(i);
                e.printStackTrace();
            }
        }

        //response on hitting auth-trip-get-info API
        if (id == 5) {
            String status = response.getString("st");
            //String tid = response.getString("tid");
            if (status.equals("FL")) {
                ShowPopup(6);
            }
            if (status.equals("DN")) {
                ShowPopup(6);
            }
            if (status.equals("CN")) {
                ShowPopup(6);
            }
            if (status.equals("TO")) {
                ShowPopup(5);
            }
            if (status.equals("PD")) {
                ShowPopup(4);
            }

        }
        //response on hitting driver-delivery-retire API
        if (id == 6) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            sendLocation();
        }
        //response on hitting driver-delivery-retire API
        if (id == 8) {
            status_duty.setChecked(false);
            Log.d(TAG, "vehicle retired successfully");

        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(instance, "Something went wrong! Please try again later.", Toast.LENGTH_SHORT).show();
    }
}
