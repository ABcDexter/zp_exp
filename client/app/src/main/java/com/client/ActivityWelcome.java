package com.client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.client.deliver.ActivityDeliverHome;
import com.client.rent.ActivityRentEnded;
import com.client.rent.ActivityRentHome;
import com.client.rent.ActivityRentInProgress;
import com.client.rent.ActivityRentOTP;
import com.client.rent.ActivityRentRequest;
import com.client.ride.ActivityRideEnded;
import com.client.ride.ActivityRideHome;
import com.client.ride.ActivityRideInProgress;
import com.client.ride.ActivityRideOTP;
import com.client.ride.ActivityRideRequest;
import com.client.ride.MapsActivity2;

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
    public static final String PRICE_RENT = "PriceRent";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";

    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    SharedPreferences prefAuth;
    String stringAuth;
    ImageButton btnRent, btnRide, btnDeliver;
    ActivityWelcome a = ActivityWelcome.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        btnRent = findViewById(R.id.btn_rent);
        btnRent.setOnClickListener(this);
        btnRide = findViewById(R.id.btn_ride);
        btnRide.setOnClickListener(this);
        btnDeliver = findViewById(R.id.btn_deliver);
        btnDeliver.setOnClickListener(this);
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

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        //SuccessMethod onSuccess = SuccessMethod.getInstance();
        Log.d(TAG, "RESPONSE:" + response);
//response on hitting user-trip-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String rtype = response.getString("rtype");
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (rtype.equals("0")) {
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
                            String van = response.getString("vno");
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
                    }
                    if (rtype.equals("1")) {
                        if (status.equals("RQ")) {
                        /*Snackbar snackbar = Snackbar
                                .make(scrollView, "WAITING FOR DRIVER", Snackbar.LENGTH_INDEFINITE)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelRequest();

                                    }
                                });*/
                            Intent rq = new Intent(ActivityWelcome.this, ActivityRentRequest.class);
                            /*rq.putExtra("st", "RQ");*/
                            startActivity(rq);
                        }
                        if (status.equals("AS")) {
                            String otp = response.getString("otp");
                            String van = response.getString("vno");
                            //String price = response.getString("price");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();
                            sp_otp.edit().putString(VAN_PICK, van).apply();
                            //sp_otp.edit().putString(PRICE_RENT,price).apply();
                            Intent as = new Intent(ActivityWelcome.this, ActivityRentOTP.class);
                            startActivity(as);
                        }
                        if (status.equals("ST")) {
                            Intent as = new Intent(ActivityWelcome.this, ActivityRentInProgress.class);
                            startActivity(as);
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            Intent fntr = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                            startActivity(fntr);
                        }

                    }
                } else {
                    Log.d(TAG, "active=" + active);

                    SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    String tripIDExists = prefTripDetails.getString(TRIP_ID, "");
                    if (!tripIDExists.equals("")) {
                        /*Intent homePage = new Intent(ActivityWelcome.this, ActivityRideHome.class);
                        homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homePage);
                        finish();*/
                        tripInfo(tripIDExists);
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
        //response on hitting user-trip-retire API
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
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-retire");
        UtilityApiRequestPost.doPOST(a, "user-trip-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void tripInfo(String tripID) {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tripID);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 20000, 0, response -> {
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
                Intent rentIntent = new Intent(ActivityWelcome.this, ActivityRentHome.class);
                startActivity(rentIntent);
                break;
            case R.id.btn_ride:
                Intent rideIntent = new Intent(ActivityWelcome.this, ActivityRideHome.class);
                startActivity(rideIntent);
                break;
            case R.id.btn_deliver:
                Intent deliverIntent = new Intent(ActivityWelcome.this, ActivityDeliverHome.class);
                startActivity(deliverIntent);
        }
    }

    public void nextActivity(View view) {
        Intent intent = new Intent(ActivityWelcome.this, MapsActivity2.class);
        startActivity(intent);

    }
}
