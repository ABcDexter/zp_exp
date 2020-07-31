package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityNewRide extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityNewRide";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";

    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";
    public static final String SRC_PER = "SrcPer";
    public static final String SRC_PHN = "SrcPhn";
    String strAuth, strTid, strSrcLat, strSrcLng;
    ActivityNewRide a = ActivityNewRide.this;
    Map<String, String> params = new HashMap();

    TextView src, dst, yes, no, info;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ride);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        SharedPreferences delPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        strTid = delPref.getString(TID, "");
        strSrcLat = delPref.getString(SRCLAT, "");
        strSrcLng = delPref.getString(SRCLNG, "");

        info = findViewById(R.id.info_text);
        src = findViewById(R.id.srcLnd);
        dst = findViewById(R.id.dstLnd);
        yes = findViewById(R.id.accept_request);
        no = findViewById(R.id.reject_request);
        relativeLayout = findViewById(R.id.rl_request);
        src.setText(strSrcLat);
        dst.setText(strSrcLng);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        if (strTid.isEmpty()) {
            info.setText("NO new delivery");
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    public void rideAccept() {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("tid", strTid);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth= " + auth + " did= " + strTid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST adriver-ride-accept");
        UtilityApiRequestPost.doPOST(a, "driver-ride-accept", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accept_request:
                rideAccept();
                break;
            case R.id.reject_request:
                Intent home = new Intent(ActivityNewRide.this, ActivityHome.class);
                startActivity(home);
                finish();
                break;
        }
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
//response on hitting driver-ride-accept API
        if (id == 1) {
            Log.d(TAG, "RESPONSE:" + response);
            Intent home = new Intent(ActivityNewRide.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
}
