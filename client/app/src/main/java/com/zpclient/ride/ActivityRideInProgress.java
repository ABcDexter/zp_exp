package com.zpclient.ride;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.zpclient.ActivityDrawer;
import com.zpclient.ActivityWelcome;
import com.zpclient.R;
import com.zpclient.UtilityApiRequestPost;
import com.zpclient.UtilityPollingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;


public class ActivityRideInProgress extends ActivityDrawer implements View.OnClickListener {

    TextView shareDetails, emergencyCall, trackLocation, nameD, phone, vNum;
    ImageButton endRide;
    ImageView driverPhoto;
    ScrollView scrollView;
    private static final String TAG = "ActivityRideInProgress";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String VAN_PICK = "com.client.Locations";
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";

    private static ActivityRideInProgress instance;
    FusedLocationProviderClient mFusedLocationClient;
    ActivityRideInProgress a = ActivityRideInProgress.this;
    Map<String, String> params = new HashMap();
    Dialog imageDialog;
    String stringAuthCookie,tid;
    private static final String PUBLISHABLE_KEY = "shXqLCv6GJVJ9QFgdHb6VL0JzE_7X96YoAX3ZxA919DLWOA1fayXhLg_NguIvRNypeaSpLu4U6JlYiwJahN8pA";
    String deviceId;
    String locationUrl;

    public static ActivityRideInProgress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting user-trip-track API
        if (id == 1) {
            try {
                locationUrl = response.getString("hurl");
                String messageBody = "Hi! I am riding ZIPP-E with\n\nDriver Name: " + nameD.getText().toString()
                        + "\nMobile Number: " + phone.getText().toString() + "\nTrack my live location here:\n" + locationUrl;
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", messageBody);
                startActivity(sendIntent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//response on hitting user-trip-cancel API
        if (id == 2) {
            checkStatus();
        }
//response on hitting user-trip-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    tid = response.getString("tid");
                    String photo = response.getString("photourl");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("ST")) {

                        Glide.with(this).load(photo).into(driverPhoto);
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("04");
                        startService(intent);
                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        Intent payment = new Intent(ActivityRideInProgress.this, ActivityRideEnded.class);
                        startActivity(payment);
                        finish();
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideInProgress.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_in_progress, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringVan = pref.getString(VAN_PICK, "");
        String stringDName = pref.getString(DRIVER_NAME, "");
        String stringDPhn = pref.getString(DRIVER_PHN, "");
        imageDialog = new Dialog(this);
        vNum = findViewById(R.id.v_no);
        vNum.setText(stringVan);
        nameD = findViewById(R.id.driver_name);
        nameD.setText(stringDName);
        phone = findViewById(R.id.driver_phone);
        phone.setText(stringDPhn);
        shareDetails = findViewById(R.id.share_ride_details);
        shareDetails.setOnClickListener(this);
        emergencyCall = findViewById(R.id.emergency);
        emergencyCall.setOnClickListener(this);
        trackLocation = findViewById(R.id.track_your_location);
        trackLocation.setOnClickListener(this);
        endRide = findViewById(R.id.end_ride);
        endRide.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollView_ride_OTP);
        driverPhoto = findViewById(R.id.photo_driver);

        checkStatus();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRideInProgress.this);
        getDeviceID();
    }

    private void getDeviceID() {
        HyperTrack sdkInstance = HyperTrack
                .getInstance(PUBLISHABLE_KEY);
        Log.d(TAG, "device id is " + sdkInstance.getDeviceID());
        deviceId = sdkInstance.getDeviceID();
    }

    private void getLocationUrl() {
        String stringAuth = stringAuthCookie;
        params.put("auth", stringAuth);
        params.put("devid", deviceId);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-track");
        UtilityApiRequestPost.doPOST(a, "user-trip-track", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }


    public void trackLocation() {
        Intent intent = new Intent(ActivityRideInProgress.this, MapsActivity2.class);
        startActivity(intent);

    }

    private void userCancelTrip() {
        String stringAuth = stringAuthCookie;
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-cancel");
        UtilityApiRequestPost.doPOST(a, "user-trip-cancel", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void alertDialog() {
        Log.d(TAG, " alert Dialog opened");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setMessage(R.string.ride_not_complete);

        dialog.setTitle(R.string.please_note);
        dialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        showProgressIndication();
                        userCancelTrip();
                        Log.d(TAG, "userCancelTrip() invoked");
                    }
                });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), R.string.ride_not_ended, Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC7721")));

    }
    private void showProgressIndication() {
        SquareProgressBar squareProgressBar = findViewById(R.id.sprogressbar);
        squareProgressBar.setImage(R.drawable.btn_bkg);
        squareProgressBar.setVisibility(View.VISIBLE);
        squareProgressBar.setProgress(50.0);
        squareProgressBar.setWidth(10);
        squareProgressBar.setIndeterminate(true);
        squareProgressBar.setColor("#EC7721");
    }
    public void checkStatus() {
        String auth = stringAuthCookie;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void btnSetOnEmergency() {
        String number = "7060743705";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.share_ride_details) {
            getLocationUrl();
        } else if (id == R.id.emergency) {
            btnSetOnEmergency();
        } else if (id == R.id.end_ride) {
            alertDialog();
        } else if (id == R.id.track_your_location) {
            Log.d(TAG, "tid="+tid);
            trackLocation();

        }
    }
}
