package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideCompleted extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRideCompleted";
    public static final String TRIP_ID = "TripId";
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";
    TextView price;
    Button home;
    String authCookie, paymentMode;
    RadioGroup paymentGrp;
    RadioButton paymentCash, paymentUPI;
    int paymentMethodId = 0;
    String strAuth, strTid;

    ActivityRideCompleted a = ActivityRideCompleted.this;
    Map<String, String> params = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_completed, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
//retrieve locally stored data
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        authCookie = cookie.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        strTid = pref.getString(TID, "");
        getInfo();
        //initializing variables
        price = findViewById(R.id.txt_rideCost);
        home = findViewById(R.id.btn_paymntAccepted);
        paymentGrp = findViewById(R.id.rad_grp_paymentMethod);
        paymentCash = findViewById(R.id.radio_cash);
        paymentUPI = findViewById(R.id.radio_upi);

        paymentCash.setOnClickListener(this);
        paymentUPI.setOnClickListener(this);
        home.setOnClickListener(this);

    }

    //method to tell the server that payment was received by the user
    private void paymentAccepted(int paymentMethod) {
        String amount = price.getText().toString();
        //the driver has to select the mode of payment
        if (!amount.equals("XXX")) {
            if (paymentMethod == 1) {
                paymentMode = "1";// payment mode is cash
            } else if (paymentMethod == 2) {
                paymentMode = "2";//payment mode is UPI
            } else {
                Toast.makeText(this, "PLEASE SELECT PAYMENT METHOD!", Toast.LENGTH_SHORT).show();
            }
            String auth = authCookie;
            params.put("auth", auth);
            JSONObject parameters = new JSONObject(params);

            Log.d(TAG, "Values: auth=" + auth);
            Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-payment-confirm");
            UtilityApiRequestPost.doPOST(a, "driver-payment-confirm", parameters, 30000, 0, response -> {
                try {
                    a.onSuccess(response, 2);// call this method if api was hit successfully
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, a::onFailure);// call this method if api was hit successfully
        } else {
            Toast.makeText(this, "Check your internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    //method to get the actual payment that the user has to make
    /*private void getPrice() {
        SharedPreferences pref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String tid = pref.getString(TRIP_ID, "");
        String auth = authCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tid);
        JSONObject parameters = new JSONObject(params);
        ActivityRideCompleted a = ActivityRideCompleted.this;

        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-actual-price");
        UtilityApiRequestPost.doPOST(a, "auth-ride-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }*/

    public void getInfo() {
        String auth = authCookie;
        params.put("auth", auth);
        params.put("tid", strTid);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " tid=" + strTid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        /*if (id == 1) {
            //get the price and display it in the UI
            String actualPrice = response.getString("price");
            price.setText(actualPrice);
        }*/
        if (id == 2) {
            //payment made, now move to the home page
            Intent home = new Intent(ActivityRideCompleted.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
        //response on hitting auth-trip-get-info API
        if (id == 1) {
            String actualPrice = response.getString("price");
            price.setText(actualPrice);
        }
    }


    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radio_cash:
                paymentMethodId = 1;
                Log.d(TAG, "radio_cash clicked");
                break;
            case R.id.radio_upi:
                paymentMethodId = 2;
                Log.d(TAG, "radio_upi clicked");
                break;
            case R.id.btn_paymntAccepted:
                if (paymentMethodId != 0)
                    paymentAccepted(paymentMethodId);
                else
                    Toast.makeText(ActivityRideCompleted.this, "PLEASE SELECT PAYMENT METHOD!", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
