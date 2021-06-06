package com.clientzp.rent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.clientzp.ActivityDrawer.SESSION_COOKIE;
import static com.clientzp.ActivityDrawer.TRIP_DETAILS;

public class InBetweenActivity extends AppCompatActivity {
    private static final String TAG = "InBetweenActivity";
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    TextView rentHistory;
    String stringAuth;
    SharedPreferences prefAuth;
    Map<String, String> params = new HashMap();
    InBetweenActivity a = InBetweenActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_between);
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        /*rentHistory = findViewById(R.id.view_rent);
        rentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "going to ActivityRentHistory");
                Intent rateFirst = new Intent(InBetweenActivity.this, ActivityRentHistory.class);
                startActivity(rateFirst);
                finish();
            }
        });*/

        checkStatus();
    }

    public void checkStatus() {
        Log.d(TAG,"inside checkStatus");

        //String auth = stringAuth;
        params.put("auth", stringAuth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {

//response on hitting user-trip-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String rtype = response.getString("rtype");
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    Log.d(TAG,"status = "+status);
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (rtype.equals("1")) {
                        /* if (status.equals("SC")){
                         *//* Intent intent = new Intent(this, UtilityPollingService.class);
                            intent.setAction("16");
                            startService(intent);*//*
                            new Handler().postDelayed(this::checkStatus, 45000);
                            Log.d(TAG, "Handler initiated");
                        }*/
                        /*if (status.equals("RQ")) {
                            Intent rq = new Intent(InBetweenActivity.this, ActivityRentRequest.class);
                            startActivity(rq);
                        }*/
                        if (status.equals("AS")) {
                            /*String otp = response.getString("otp");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
                            Intent as = new Intent(InBetweenActivity.this, ActivityRentOTP.class);
                            startActivity(as);
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            //retireTrip();
                            /*Intent fntr = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                            startActivity(fntr);*/
                            Intent fntr = new Intent(InBetweenActivity.this, ActivityRentEnded.class);
                            startActivity(fntr);
                        }

                        /*if (status.equals("TO")) {
                            ShowPopup(2);
                        }*/

                    }
                } else {
                    Log.d(TAG, "active=" + active);
                    try {
                        String tid = response.getString("tid");
                        if (!tid.equals("-1")) {
                            Intent rateFirst = new Intent(InBetweenActivity.this, ActivityRateRent.class);
                            startActivity(rateFirst);
                        } /*else {
                            tripInfo(tid);
                        }*/
                        new Handler().postDelayed(() -> {
                            //ShowPopup(3);
                            checkStatus();
                        }, 45000);
                        Log.d(TAG, "Handler initiated");
                    } catch (Exception e) {
                        Log.d(TAG, " tid does not exist");
                        e.printStackTrace();
                        //getAvailableVehicle();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void onFailure(VolleyError error) {
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
}