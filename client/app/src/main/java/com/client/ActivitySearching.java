package com.client;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.client.rent.ActivityRentOTP;
import com.client.rent.ActivityRentRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivitySearching extends AppCompatActivity {
    private static final String TAG = "ActivitySearching";

    ImageView zippe_iv, zippe_iv_below, scooty_up, scooty_down;
    String auth;
    ActivitySearching a = ActivitySearching.this;
    Map<String, String> params = new HashMap();


    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";

    SharedPreferences prefAuth;
    String stringAuth;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);
        scooty_up = findViewById(R.id.scooty_up);
        scooty_down = findViewById(R.id.scooty_down);
        scrollView = findViewById(R.id.scrollView);
        moveZbee();

        checkStatus();

    }

    private void moveZbee() {
        scooty_up.setVisibility(View.GONE);
        scooty_down.setVisibility(View.VISIBLE);
        zippe_iv_below.setVisibility(View.GONE);
        zippe_iv.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(scooty_down, "translationX", 1800, 0f);
        objectAnimator.setDuration(1700);
        objectAnimator.start();

        /*objectAnimator.setRepeatCount(ValueAnimator.INFINITE);*/

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zippe_iv, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();
        /*objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveScooty();
            }
        }, 1600);
    }

    private void moveScooty() {
        scooty_up.setVisibility(View.VISIBLE);
        scooty_down.setVisibility(View.GONE);
        zippe_iv_below.setVisibility(View.VISIBLE);
        zippe_iv.setVisibility(View.GONE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zippe_iv_below, "translationX", 1800, 0f);
        objectAnimator.setDuration(1700);
        objectAnimator.start();
        /*objectAnimator.setRepeatCount(ValueAnimator.INFINITE);*/

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(scooty_up, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveZbee();
            }
        }, 1600);
        /* objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*/
    }

    public void checkStatus() {

        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
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

    private void cancelRequest() {
        String auth = stringAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-cancel");
        UtilityApiRequestPost.doPOST(a, "user-trip-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting user-trip-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    String rtype = response.getString("rtype");
                    /*if (rtype.equals("0")) {
                        Intent ride = new Intent(ActivityRentRequest.this, ActivityWelcome.class);
                        startActivity(ride);
                        finish();
                    } else*/
                    if (rtype.equals("1")) {
                        SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        sp_cookie.edit().putString(TRIP_ID, tid).apply();
                        if (status.equals("RQ")) {
                            Snackbar snackbar = Snackbar
                                    .make(scrollView, "CHECKING VEHICLE AVAILABILITY...", Snackbar.LENGTH_INDEFINITE)
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
                            intent.setAction("12");
                            startService(intent);
                        }
                        if (status.equals("AS")) {
                            String otp = response.getString("otp");
                            String van = response.getString("vno");
                            Intent as = new Intent(ActivitySearching.this, ActivityRentOTP.class);
                            startActivity(as);
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();
                            sp_otp.edit().putString(VAN_PICK, van).apply();
                        }
                    }
                } else {
                    Intent homePage = new Intent(ActivitySearching.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        //response on hitting user-trip-cancel API
        if (id == 4) {
            Intent home = new Intent(ActivitySearching.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
}
