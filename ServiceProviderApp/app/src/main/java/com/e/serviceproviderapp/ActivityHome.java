package com.e.serviceproviderapp;

import android.Manifest;
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
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
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
    private static final String TAG = "ActivityHome";
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String AN = "An";

    FusedLocationProviderClient mFusedLocationClient;
    Dialog myDialog;
    ScrollView scrollView;
    TextView newOrder, inProgress, completedOrder, totalEarnings, notify;
    String lat, lng, strAadhar, earn, strAuth, auth, aadhar, deliveryID;
    Vibrator vibrator;
    private static ActivityHome instance;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

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
        strAadhar = cookie.getString(AN, "");// retrieve aadhaar value stored locally and assign it to String aadhar
        auth = strAuth;
        aadhar = strAadhar;

        //initializing variables
        scrollView = findViewById(R.id.scrollLayout);
        newOrder = findViewById(R.id.new_job);
        inProgress = findViewById(R.id.job_accepted);
        completedOrder = findViewById(R.id.completed_jobs);
        //totalEarnings = findViewById(R.id.earnings);
        notify = findViewById(R.id.notifNo);

        newOrder.setOnClickListener(this);
        inProgress.setOnClickListener(this);
        completedOrder.setOnClickListener(this);
        //totalEarnings.setOnClickListener(this);

        myDialog = new Dialog(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityHome.this);// needed for gsp tracking
        servitorBookingCheck();
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
            infoText.setText(R.string.locked);
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 4) {
            infoText.setText(getString(R.string.delivery_successful, earn));
            retireDelvy();
        }
        if (id == 5) {
            infoText.setText(R.string.delivery_timeout);
            retireDelvy();
        }
        if (id == 6) {
            infoText.setText(R.string.delivery_failed);
            retireDelvy();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
        int id = v.getId();
        if (id == R.id.new_job) {
            //servitorBookingCheck();
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityNewJobList.class);
            startActivity(newOrderIntent);
        } else if (id == R.id.job_accepted) {
            Intent inProgressIntent = new Intent(ActivityHome.this, ActivityAllJobsAccepted.class);
            startActivity(inProgressIntent);
            finish();
        } else if (id == R.id.completed_jobs) {
            Intent inProgressIntent = new Intent(ActivityHome.this, ActivityCompletedJobList.class);
            startActivity(inProgressIntent);
            finish();
        }
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
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void servitorBookingCheck() {

        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST servitor-booking-check");
        UtilityApiRequestPost.doPOST(a, "servitor-booking-check", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void delvyGetInfo() {

        params.put("auth", auth);
        params.put("did", deliveryID);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + auth + " did=" + deliveryID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-delivery-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-delivery-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void retireDelvy() {

        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-retire");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 6);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }


    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        if (id == 2) {
            Log.d(TAG, "RESPONSE of auth-location-update :" + response);
        }

        //response on hitting agent-delivery-get-status API
        if (id == 3) {
           /* try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String did = response.getString("did");
                    SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(DID, did).apply();
                    deliveryID = did;
                    if (status.equals("AS")) {

                       *//* Intent reachClient = new Intent(ActivityHome.this, ActivityOTP.class);
                        startActivity(reachClient);
                        finish();*//*
                    }
                    if (status.equals("ST")) {
                       *//* Intent st = new Intent(ActivityHome.this, ActivityEnroute.class);
                        startActivity(st);*//*

                    }
                    if (status.equals("RC")) {
                        *//*Intent pd = new Intent(ActivityHome.this, ActivityOTP.class);
                        startActivity(pd);*//*

                    }

                } else if (active.equals("false")) {
                    try {
                        String status = response.getString("st");
                        String did = response.getString("did");
                        delvyGetInfo();

                    } catch (Exception e) {
                        e.printStackTrace();
                        servitorBookingCheck();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }*/
        }
        //response on hitting servitor-booking-check API
        if (id == 4) {
            try {
                String count = response.getString("count");
                if (!count.equals("0")) {
                    notify.setVisibility(View.VISIBLE);
                    notify.setText(count);

                    /*Intent newOrderIntent = new Intent(ActivityHome.this, MapsReachClient.class);
                    startActivity(newOrderIntent);
                    finish();*/

                } else {
                    notify.setVisibility(View.VISIBLE);
                    notify.setText("0");
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 45s = 45000ms
                            servitorBookingCheck();
                        }
                    }, 45000);
                }

            } catch (Exception e) {
                notify.setVisibility(View.VISIBLE);
                notify.setText("0");
                e.printStackTrace();
            }
        }
        //response on hitting auth-delivery-get-info API
        if (id == 5) {
            /*String status = response.getString("st");
            //String did = response.getString("did");
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
            if (status.equals("FN")) {
                earn = response.getString("earn");
                ShowPopup(4);
            }*/

        }
        //response on hitting agent-delivery-retire API
        if (id == 6) {
            /*SharedPreferences preferences = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();*/
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(instance, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
