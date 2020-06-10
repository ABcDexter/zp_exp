package com.client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.client.rental.RentalHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityWelcome extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityWelcome";
    ImageView zippe_iv, zippe_iv_below;
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";

    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    SharedPreferences prefAuth;
    String stringAuth;
    Button btnRent, btnRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page_anim);

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        btnRent = findViewById(R.id.btn_rent);
        btnRent.setOnClickListener(this);
        btnRide = findViewById(R.id.btn_ride);
        btnRide.setOnClickListener(this);

        String auth = stringAuth;
        if (auth.equals("")) {
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityMain.class);
            startActivity(registerUser);
            finish();
        }
        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);
        checkStatus();
        moveIt();
    }

    private void moveIt() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zippe_iv_below, "translationX", 1500, 0f);
        objectAnimator.setDuration(1600);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zippe_iv, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);
    }

    public void checkStatus() {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityWelcome a = ActivityWelcome.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        //SuccessMethod onSuccess = SuccessMethod.getInstance();

        if (id == 1) {
            Log.d(TAG, "RESPONSE:" + response);
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("RQ")) {
                        /*Snackbar snackbar = Snackbar
                                .make(scrollView, "WAITING FOR DRIVER", Snackbar.LENGTH_INDEFINITE)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelRequest();

                                    }
                                });*/
                        Intent rq = new Intent(ActivityWelcome.this, ActivityRideRequest.class);
                        rq.putExtra("st", "RQ");
                        startActivity(rq);
                    }
                    if (status.equals("AS")) {
                        String otp = response.getString("otp");
                        String van = response.getString("van");
                        SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                        sp_otp.edit().putString(OTP_PICK, otp).apply();
                        sp_otp.edit().putString(VAN_PICK, van).apply();
                        Intent as = new Intent(ActivityWelcome.this, ActivityRideOTP.class);
                        as.putExtra("OTP", otp);
                        as.putExtra("VAN", van);
                        startActivity(as);
                    }
                    if (status.equals("ST")) {

                        Intent as = new Intent(ActivityWelcome.this, ActivityRideInProgress.class);
                        startActivity(as);
                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        Intent fntr = new Intent(ActivityWelcome.this, ActivityRideEnded.class);
                        startActivity(fntr);
                    }
                } else {
                    Log.d(TAG, "active=" + active);

                    SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    String tripIDExists = prefTripDetails.getString(TRIP_ID, "");
                    if (tripIDExists.equals("")) {
                        Intent homePage = new Intent(ActivityWelcome.this, ActivityRideHome.class);
                        homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homePage);
                        finish();
                    } else {
                        rideInfo(tripIDExists);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (id == 2) {
            String st = response.getString("st");
            if (st.equals("PD")) {
                String price = response.getString("price");
                String time = response.getString("time");
                String dist = response.getString("dist");
                String speed = response.getString("speed");
                SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(PREFS_LOCATIONS);
                editor.apply();

                SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                SharedPreferences.Editor editor1 = prefBuzz.edit();
                editor1.remove(BUSS_FLAG);
                editor1.apply();
                /*Intent summary = new Intent(ActivityWelcome.this, ActivityTripSummary.class);
                summary.putExtra("PRICE", price);
                summary.putExtra("TIME", time);
                summary.putExtra("DIST", dist);
                summary.putExtra("SPEED", speed);
                startActivity(summary);
                finish();*/
            }
            if (st.equals("FL")) {
                Toast.makeText(this, "RIDE FAILED ! \nSORRY FOR THE INCONVENIENCE CAUSED !", Toast.LENGTH_LONG).show();
            }
            if (st.equals("CN")) {
                Toast.makeText(this, "RIDE WAS CANCELED BY YOU !", Toast.LENGTH_LONG).show();
            }
            if (st.equals("DN")) {
                Toast.makeText(this, "DRIVER DENIED PICKUP", Toast.LENGTH_LONG).show();
            }
            if (st.equals("TO")) {
                Toast.makeText(this, "RIDE TIMED OUT ", Toast.LENGTH_LONG).show();
            }
            retireTrip();

        }
        if (id == 3) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(TRIP_ID);
            editor.apply();

            Log.d(TAG, "tripID= " + TRIP_ID);
            checkStatus();
        }
    }

    private void retireTrip() {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityWelcome a = ActivityWelcome.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-ride-retire");
        UtilityApiRequestPost.doPOST(a, "auth-ride-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void rideInfo(String tripID) {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tripID);
        JSONObject parameters = new JSONObject(params);
        ActivityWelcome a = ActivityWelcome.this;
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-ride-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-ride-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rent:
                Intent rentIntent = new Intent(ActivityWelcome.this, RentalHome.class);
                startActivity(rentIntent);
                break;
            case R.id.btn_ride:
                Intent rideIntent = new Intent(ActivityWelcome.this, ActivityRideHome.class);
                startActivity(rideIntent);
                break;
        }
    }
}
