package com.client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideRequest extends ActivityDrawer implements View.OnClickListener {

    public static final String SESSION_COOKIE = "com.client.ride.Cookie";

    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static final String TAG = "ActivityRideRequest";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String COST_DROP = "CostDrop";
    public static final String TIME_DROP = "TimeDrop";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    ImageButton next, costInfo, timeInfo;
    TextView /*pick, drop,*/ costEst, timeEst, pickPlaceInfo, dropPlaceInfo, riders;
    ScrollView scrollView;
    String stringAuth;
    ImageView zbeeR, zbeeL;
    SharedPreferences prefAuth;
    ActivityRideRequest a;
    Dialog myDialog;
    String rideInfo, vTypeInfo, noRiderInfo, pModeInfo, dropInfo, pickInfo, pickPlace, dropPlace;
    Map<String, String> params = new HashMap();

    private static ActivityRideRequest instance;

    public void onSuccess(JSONObject response, int id) throws JSONException {

        if (id == 1) {
            Log.d(TAG, "RESPONSE:" + response);

            try {
                // Parsing json object response
                // response will be a json object
                String price = response.getString("price");
                String time = response.getString("time");

                costEst.setText(price);
                timeEst.setText(time);
                SharedPreferences sp_cookie = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                sp_cookie.edit().putString(TIME_DROP, time).apply();
                sp_cookie.edit().putString(COST_DROP, price).apply();

                Log.d(TAG, "price:" + price + " time:" + time);

            } catch (JSONException e) {

                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (id == 2) {
            Log.d(TAG, "RESPONSE:" + response);
            String tid = response.getString("tid");
            SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(TRIP_ID, tid).apply();
            checkStatus();
        }
        if (id == 3) {
            Log.d(TAG, "RESPONSE:" + response);
            try {

                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("RQ")) {
                        Snackbar snackbar = Snackbar
                                .make(scrollView, "WAITING FOR DRIVER", Snackbar.LENGTH_INDEFINITE)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelRequest();

                                    }
                                });
                        snackbar.setActionTextColor(Color.RED);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("02");
                        startService(intent);
                    }
                    if (status.equals("AS")) {
                        String otp = response.getString("otp");
                        String van = response.getString("van");
                        Intent as = new Intent(ActivityRideRequest.this, ActivityRideOTP.class);
                        as.putExtra("OTP", otp);
                        as.putExtra("VAN", van);
                        startActivity(as);
                        SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                        sp_otp.edit().putString(OTP_PICK, otp).apply();
                        sp_otp.edit().putString(VAN_PICK, van).apply();
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideRequest.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (id == 4){
            Intent home = new Intent(ActivityRideRequest.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_request, null, false);
        frameLayout.addView(activityView);

        instance = this;

        SharedPreferences prefPLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = prefPLoc.getString(LOCATION_PICK, "");
        String stringDrop = prefPLoc.getString(LOCATION_DROP, "");

        Intent data = getIntent();
        rideInfo = data.getStringExtra("rtype");
        noRiderInfo = data.getStringExtra("npas");
        pickInfo = data.getStringExtra("srcid");
        dropInfo = data.getStringExtra("dstid");
        vTypeInfo = data.getStringExtra("vtype");
        pModeInfo = data.getStringExtra("pmode");
        pickPlace = data.getStringExtra("pick");
        dropPlace = data.getStringExtra("drop");

        costEst = findViewById(R.id.cost_estimate);
        timeEst = findViewById(R.id.time_estimate);
        pickPlaceInfo = findViewById(R.id.pick_info);
        dropPlaceInfo = findViewById(R.id.drop_info);
        riders = findViewById(R.id.ridersInfo);

        int pickSpace = (stringPick.contains(" ")) ? stringPick.indexOf(" ") : stringPick.length() - 1;
        String pickCutName = stringPick.substring(0, pickSpace);

        pickPlaceInfo.setText(pickCutName);

        int dropSpace = (stringDrop.contains(" ")) ? stringDrop.indexOf(" ") : stringDrop.length() - 1;
        String dropCutName = stringDrop.substring(0, dropSpace);

        dropPlaceInfo.setText(dropCutName);
        riders.setText(noRiderInfo);


        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        a = ActivityRideRequest.this;

        rideEstimate();

        scrollView = findViewById(R.id.scrollView_location_selection);

        next = findViewById(R.id.confirm_ride_book);
        next.setOnClickListener(this);

        zbeeR = findViewById(R.id.image_zbee);
        zbeeL = findViewById(R.id.image_zbee_below);
        costInfo = findViewById(R.id.infoCost);
        costInfo.setOnClickListener(this);
        timeInfo = findViewById(R.id.infoTime);
        timeInfo.setOnClickListener(this);
        myDialog = new Dialog(this);

    }

    private void cancelRequest() {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideRequest a = ActivityRideRequest.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-cancel");
        UtilityApiRequestPost.doPOST(a, "user-ride-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    protected void checkStatus() {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideRequest a = ActivityRideRequest.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        LinearLayout ll = myDialog.findViewById(R.id.layout_btn);
        TextView reject = myDialog.findViewById(R.id.reject_request);
        TextView accept = myDialog.findViewById(R.id.accept_request);
        if (id == 1) {
            infoText.setText("THIS IS ONLY AN APPROXIMATION OF COST. IT MAY CHANGE DEPENDING ON THE TRAFFIC");
        }
        if (id == 2) {
            infoText.setText("THIS IS ONLY AN APPROXIMATION OF TIME. IT MAY CHANGE DEPENDING ON THE TRAFFIC");
        }
        if (id == 3) {
            ll.setVisibility(View.VISIBLE);
            infoText.setText("NOTIFY ME WHEN RIDE IS AVAILABLE");

            reject.setOnClickListener(this);
            accept.setOnClickListener(this);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public static ActivityRideRequest getInstance() {
        return instance;
    }

    private void moveit() {
        zbeeL.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zbeeL, "translationX", 1500, 0f);
        objectAnimator.setDuration(1500);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zbeeR, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reject_request:
                Intent home = new Intent(ActivityRideRequest.this, ActivityRideHome.class);
                startActivity(home);
                finish();
                break;
            case R.id.accept_request:
                //TODO
                break;
            case R.id.confirm_ride_book:
                moveit();
                userRequestRide();
                break;
            case R.id.infoTime:
                ShowPopup(2);
                break;
            case R.id.infoCost:
                ShowPopup(1);
                break;
        }
    }

    private void rideEstimate() {
        params.put("auth", stringAuth);
        params.put("npas", noRiderInfo);
        params.put("dstid", dropInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: npas=" + noRiderInfo + " dstid = " + dropInfo + " vtype = " + vTypeInfo + " pmode = " + pModeInfo);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-ride-estimate");

        UtilityApiRequestPost.doPOST(a, "user-ride-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    protected void userRequestRide() {
        params.put("auth", stringAuth);
        params.put("npas", noRiderInfo);
        params.put("srcid", pickInfo);
        params.put("dstid", dropInfo);
        params.put("rtype", rideInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: npas=" + noRiderInfo + " srcid = " + pickInfo + " dstid = "
                + dropInfo + " rtype = " + rideInfo + " vtype = " + vTypeInfo + " pmode = " + pModeInfo);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-ride-request");

        UtilityApiRequestPost.doPOST(a, "user-ride-request", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }
}
