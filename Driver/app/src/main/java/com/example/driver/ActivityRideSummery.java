package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideSummery extends ActivityDrawer {
    private static final String TAG = "ActivityRideSummery";
    String stringAuthKey, stringTID;
    SwipeRefreshLayout swipeRefresh;
    ScrollView scrollView;
    ActivityRideSummery a = ActivityRideSummery.this;
    Map<String, String> params = new HashMap();
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    TextView ridePickup, rideVehicle, rideTime, rideDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_summery, null, false);
        frameLayout.addView(activityView);
        SharedPreferences prefCookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        Intent intent = getIntent();
        stringTID = intent.getStringExtra("TID");
        Log.d(TAG, "TID" + stringTID);
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        ridePickup = findViewById(R.id.ride_pick_up);
        rideVehicle = findViewById(R.id.ride_vehicle);
        rideTime = findViewById(R.id.ride_time);
        rideDate = findViewById(R.id.ride_date);

        userDeliverySummery();

        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            recreate();//this will recreate or reload the activity when swiped down
            swipeRefresh.setRefreshing(false);
        });
    }

    protected void userDeliverySummery() {
        String auth = stringAuthKey;
        String tid = stringTID;
        params.put("auth", auth);
        params.put("tid", tid);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-delivery-data");
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);

        UtilityApiRequestPost.doPOST(a, "auth-trip-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    String rate, price, tax, total, vtype, time, date, sLat, sLng, dLat, dLng;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-trip-data API
        if (id == 2) {

            vtype = response.getString("rvtype");
            time = response.getString("time");
            date = response.getString("sdate");

            //ridePickup.setText(getString(R.string.message_rs, rate));
            rideVehicle.setText(vtype);
            rideTime.setText(getString(R.string.message_min, time));
            rideDate.setText(date);

            switch (vtype) {
                case "0":
                    rideVehicle.setText(R.string.e_cycle);
                    break;
                case "1":
                    rideVehicle.setText(R.string.e_scooty);
                    break;
                case "2":
                    rideVehicle.setText(R.string.e_bike);
                    break;
                case "3":
                    rideVehicle.setText(R.string.zbee);
                    break;
                default:
                    rideVehicle.setText(vtype);
                    break;
            }

        }

    }

    public void onFailure(VolleyError error) {
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityRideSummery.this, ActivityHome.class));
        finish();
    }

}
