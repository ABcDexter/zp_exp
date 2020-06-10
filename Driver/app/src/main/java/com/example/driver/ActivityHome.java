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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

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
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityHome extends ActivityDrawer implements View.OnClickListener {
    Dialog myDialog;
    Switch status_duty;
    String driverStatus = "";
    private static final String TAG = "ActivityHome";
    ScrollView scrollView;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    FusedLocationProviderClient mFusedLocationClient;
    TextView nxt1, nxt2, prv1, prv2, nrPt;
    String lat, lng, str_phone, aadhar;
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String FIRST_LAUNCH = "FirstLaunch";
    public static final String DRIVER_STATUS = "DriverStatus";
    public static final String STATUS = "Status";

    String stringBuss, bussFlag;
    SharedPreferences prefBuss;
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    Vibrator vibrator;

    private static ActivityHome instance;
    String auth;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};

    public void onSuccess(JSONObject response, int id) throws JSONException {
        //Log the response from the server
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        if (id == 1) {
            //Polling auth-location-update APIs
            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("1");
            startService(i);
        }

        if (id == 2) {
            //check the mode of driver and show message accordingly
            String mode = response.getString("st");
            if (mode.equals("AV")) {
                rideStatus();// method to hit driver-ride-get-status API
            } else if (mode.equals("LK")) {
                ShowPopup(3);
            } else {
                ShowPopup(2);
            }
        }

        if (id == 3) {
            String active = response.getString("active");
            if (active.equals("false")) {
                try {
                    String tid = response.getString("tid");
                    rideInfo(tid);
                } catch (Exception e) {
                    Log.d(TAG, "no tid");
                }
                driverRideCheck();
            } else if (active.equals("true")) {
                String tid = response.getString("tid");
                String st = response.getString("st");
                if (st.equals("AS")) {
                    Intent as = new Intent(ActivityHome.this, ActivityRideAccepted.class);
                    startActivity(as);
                }
                if (st.equals("ST")) {
                    Intent stIntent = new Intent(ActivityHome.this, ActivityRideInProgress.class);
                    startActivity(stIntent);
                }
                if (st.equals("FN") | st.equals("TR")) {
                    Intent finishedIntent = new Intent(ActivityHome.this, ActivityRideCompleted.class);
                    startActivity(finishedIntent);
                }
            }
        }
        if (id == 4) {
            String st = response.getString("st");
            if (st.equals("PD")) {
                Log.d(TAG, "TRIP SUMMERY");
            } else if (st.equals("FL") || st.equals("DN") || st.equals("CN")) {
                ShowPopup(4);
            }
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            tripRetire();
        }
        if (id == 5) {
            Log.d(TAG, "ride retired");
        }
        if (id == 6) {
            try {
                String tid = response.getString("tid");
                getAvailableVehicle(tid);
            } catch (Exception e) {
                Log.d(TAG, "Error:" + e.getMessage());
                if (stringBuss.equals("BussMeNot")) {
                    Log.d(TAG, "user not interested in notifications");
                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();
                } else if (stringBuss.equals("BussMe")) {
                    Intent i = new Intent(this, UtilityPollingService.class);
                    i.setAction("6");
                    startService(i);
                }
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
    //method to check if vehicles are available at the hub near the driver's current location.
    private void getAvailableVehicle(String tid) {
        Intent vList = new Intent(ActivityHome.this, VehicleList.class);
        vList.putExtra("TID", tid);
        startActivity(vList);
    }

    // method to check new ride request
    protected void driverRideCheck() {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-check");
        UtilityApiRequestPost.doPOST(a, "driver-ride-check", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 6);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    //method to retire a trip
    private void tripRetire() {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;

        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-retire");
        UtilityApiRequestPost.doPOST(a, "driver-ride-retire", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 5);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

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
        auth = cookie.getString(COOKIE, ""); // retrieve auth value stored locally and assign it to String auth
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
        stringBuss = prefBuss.getString(BUSS, ""); // retrieve buss value stored locally and assign it to String stringBuss
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        aadhar = sharedPreferences.getString(AADHAR, "");// retrieve aadhaar value stored locally and assign it to String aadhar
        //initializing variables
        nxt2 = findViewById(R.id.nxtLoc2);
        nxt1 = findViewById(R.id.nxtLoc1);
        prv1 = findViewById(R.id.prevLoc1);
        prv2 = findViewById(R.id.prevLoc2);
        nrPt = findViewById(R.id.nearest);
        status_duty = findViewById(R.id.dutyStatus);
        scrollView = findViewById(R.id.scrollLayout);
        myDialog = new Dialog(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityHome.this);// needed for gsp tracking

        status_duty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    driverStatus = "OnDuty";
                    //store the value of driver status (AV) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply(); // storing driver status locally for later use
                    Log.d(TAG, "driverStatus = " + driverStatus);
                    getLastLocation();// method for getting the last know latitude and longitude of driver
                } else {
                    driverStatus = "OffDuty";
                    //store the value of driver status (OF) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply();
                    ShowPopup(2);
                    Log.d(TAG, "driverStatus = " + driverStatus);
                }
                driverSetMode();// method fro hitting driver-set-mode API
            }
        });
        firstLaunch(); // method to check id the app is launched for the first time in the day
        Snackbar snackbar = Snackbar
                .make(scrollView, "NO NEW RIDE CURRENTLY AVAILABLE !", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        ShowPopup(1);
        //enter this only if this activity is called from ActivityRideAccepted
        try {
            //this means that the driver canceled ride after initially accepting the ride
            Intent rideAccept = getIntent();
            String cancel = rideAccept.getStringExtra("CANCEL");
            assert cancel != null;
            if (!cancel.isEmpty())
                status_duty.setChecked(false);
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

    }

    private void driverSetMode() {
        SharedPreferences pref = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
        String stringStatus = pref.getString(STATUS, "");//retrieving value of driver status
        String mode;
        if (stringStatus.equals("OnDuty")) {
            mode = "AV";
        } else {
            mode = "OF";
        }
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("st", mode);
        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;

        Log.d(TAG, "Values: auth=" + auth + " mode=" + mode);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-set-mode");
        UtilityApiRequestPost.doPOST(a, "driver-set-mode", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    private void rideInfo(String tid) {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tid);
        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;

        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-ride-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-ride-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    private void rideStatus() {
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;

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
//method to check if the app is launched for the first time in the day
    private void firstLaunch() {
        SharedPreferences sharedPref = getSharedPreferences(FIRST_LAUNCH, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDate = sdf.format(new Date());
        if (sharedPref.getString("LAST_LAUNCH_DATE", "nodate").contains(currentDate)) {
            // Date matches. User has already Launched the app once today.
            SharedPreferences pref = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
            String stringStatus = pref.getString(STATUS, "");
            if (stringStatus.equals("OnDuty")) {
                status_duty.setChecked(true);
            }
            if (!stringStatus.equals("OnDuty")) {
                status_duty.setChecked(false);
            }

        } else {
            status_duty.setChecked(false);
            // Set the last Launched date to today.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("LAST_LAUNCH_DATE", currentDate);
            editor.apply();
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor1 = preferences.edit();
            editor1.clear();
            editor1.apply();
        }
    }
//method to initiate and populate dialog box
    private void ShowPopup(int id) {
        myDialog.setContentView(R.layout.popup_text);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        TextView reject_rq = myDialog.findViewById(R.id.reject_request);
        TextView accept_rq = myDialog.findViewById(R.id.accept_request);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            //vibrate the device for 1000 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
            ln.setVisibility(View.VISIBLE);
            infoText.setText("no ride available currently.\nNotify me when available.");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);// dialog box will not be dismissed if screen outside the box is touched

        }
        if (id == 2) {
            infoText.setText("YOU ARE OFFLINE ! ");
        }
        if (id == 3) {
            infoText.setText("YOU ARE CURRENTLY LOCKED ! \nCONTACT ADMIN");
        }
        if (id == 4) {
            infoText.setText("TRIP FAILED !");
        }
        if (id == 6) {
            infoText.setText("Rides are Available");
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);// dialog box will be dismissed if screen outside the box is touched
    }

    public static ActivityHome getInstance() {
        return instance;
    }
//method to send lat and lng of driver to server
    protected void sendLocation() {
        Map<String, String> params = new HashMap();
        String aadhaar = aadhar;
        params.put("auth", auth);
        params.put("an", aadhaar);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);
        ActivityHome a = ActivityHome.this;

        Log.d(TAG, "Values: auth=" + auth + " lat=" + lat + " lng=" + lng + " an=" + aadhaar);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
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
        } else
            getLastLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reject_request:
                bussFlag = "BussMeNot";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                Log.d(TAG, "User not interested in a buss");
                //showAlertDialog();
                myDialog.dismiss();
                break;
            case R.id.accept_request:
                bussFlag = "BussMe";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                myDialog.dismiss();
                driverRideCheck();
                break;
        }
    }
}
