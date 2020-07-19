package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityInProgress extends AppCompatActivity implements View.OnClickListener {

    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityInProgress";

    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";
    public static final String SRC_PER = "SrcPer";
    public static final String SRC_PHN = "SrcPhn";
    String strAuth;
    ActivityInProgress a = ActivityInProgress.this;
    Map<String, String> params = new HashMap();

    TextView person, phone;
    String lat, lng;
    Button yes, no, map;
    EditText otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_progress);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        person = findViewById(R.id.src_per);
        phone = findViewById(R.id.src_phone);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        otp = findViewById(R.id.enter_otp);
        map = findViewById(R.id.map);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        map.setOnClickListener(this);
        getStatus();
    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void rideCancel() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-cancel");
        UtilityApiRequestPost.doPOST(a, "driver-ride-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void rideStart() {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("otp", otp.getText().toString().trim());
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " otp=" + otp.getText().toString().trim());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-start");
        UtilityApiRequestPost.doPOST(a, "driver-ride-start", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting driver-ride-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String did = response.getString("did");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TID, did).apply();

                    if (status.equals("AS")) {
                        String srcLat = response.getString("srclat");
                        String srcLng = response.getString("srclng");
                        String dstLat = response.getString("dstlat");
                        String dstLng = response.getString("dstlng");
                        String phn = response.getString("uphone");
                        String name = response.getString("uname");

                        person.setText(name);
                        phone.setText(phn);

                        SharedPreferences delvyPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(SRC_PER, name).apply();
                        delvyPref.edit().putString(SRC_PHN, phn).apply();
                        delvyPref.edit().putString(SRCLAT, srcLat).apply();
                        delvyPref.edit().putString(SRCLNG, srcLng).apply();
                        delvyPref.edit().putString(DSTLAT, dstLat).apply();
                        delvyPref.edit().putString(DSTLNG, dstLng).apply();
                        Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                        startActivity(home);
                        finish();
                    }
                    Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                } else if (active.equals("false")) {
                    Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting driver-ride-cancel API
        if (id == 2) {
            Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
            startActivity(home);
            finish();
        }

        //response on hitting driver-ride-start API
        if (id == 3) {
            getStatus();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                rideStart();

                break;
            case R.id.no:
                rideCancel();
                break;
            case R.id.map:
                /*Intent map = new Intent(ActivityInProgress.this, ActivityMap.class);
                startActivity(map);*/
                Toast.makeText(this, "Map will open", Toast.LENGTH_LONG).show();
                Intent map = new Intent(ActivityInProgress.this, MapsActivity2.class);
                startActivity(map);
                break;
        }
    }
}
