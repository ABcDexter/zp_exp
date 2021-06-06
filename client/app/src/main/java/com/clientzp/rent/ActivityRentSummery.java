package com.clientzp.rent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRentSummery extends ActivityDrawer implements OnMapReadyCallback/*, TaskLoadedCallback*/ {
    private static final String TAG = "ActivityRideSummery";
    String stringAuthKey, stringSCID;
    //TextView dialog_txt;
    SwipeRefreshLayout swipeRefresh;
    ScrollView scrollView;
    ActivityRentSummery a = ActivityRentSummery.this;
    Map<String, String> params = new HashMap();
    public static final String AUTH_KEY = "AuthKey";
    TextView rideRate, ridePrice, rideTax, rideTotal, rideVehicle, rideTime, rideDate, txtSummery;
    private GoogleMap mMap;
    private MarkerOptions src, dst;
    private Polyline currentPolyline;
    double srcLat, srcLng, dstLat, dstLng;
    String rate, price, tax, total, vtype, time, date, sLat, sLng, dLat, dLng, srcname, dstname, sc, canl;
    ImageButton cancelButton;
    public static final String TRIP_ID = "TripID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_summery, null, false);
        frameLayout.addView(activityView);
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        Intent intent = getIntent();
        stringSCID = intent.getStringExtra("TID");
        //Log.d(TAG, "TID" + stringSCID);
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        rideRate = findViewById(R.id.ride_rate);
        ridePrice = findViewById(R.id.ride_price);
        rideTax = findViewById(R.id.ride_tax);
        rideTotal = findViewById(R.id.ride_total);
        rideVehicle = findViewById(R.id.ride_vehicle);
        rideTime = findViewById(R.id.ride_time);
        rideDate = findViewById(R.id.ride_date);
        txtSummery = findViewById(R.id.txtSummery);
        cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setVisibility(View.GONE);
        txtSummery.setText(R.string.rent_summary);
        authTripData();

        swipeRefresh = findViewById(R.id.swipeRefresh);

        //getInfo();
        swipeRefresh.setOnRefreshListener(() -> {
            recreate();//this will recreate or reload the activity when swiped down
            swipeRefresh.setRefreshing(false);
        });
    }

    protected void authTripData() {
        String auth = stringAuthKey;
        String scid = stringSCID;
        params.put("auth", auth);
        params.put("tid", scid);

        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-trip-data");
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);*/

        UtilityApiRequestPost.doPOST(a, "auth-trip-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    protected void cancelRequest() {
        String auth = stringAuthKey;
        String scid = stringSCID;
        params.put("auth", auth);
        params.put("tid", scid);

        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-trip-data");
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);*/

        UtilityApiRequestPost.doPOST(a, "user-rent-cancel", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    public void checkStatus() {
        Log.d(TAG,"inside checkStatus");
        String auth = stringAuthKey;
        //String auth = stringAuth;
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
    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE`1:" + response);
        //response on hitting user-rent-cancel API
        if (id == 1) {
            Intent home = new Intent(ActivityRentSummery.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }
        //response on hitting auth-trip-data API
        if (id == 2) {

            vtype = response.getString("rvtype");
            rate = response.getString("rate");
            price = response.getString("price");
            tax = response.getString("tax");
            total = response.getString("total");
            time = response.getString("time");
            date = response.getString("sdate");
            sLat = response.getString("srclat");
            sLng = response.getString("srclng");
            dLat = response.getString("dstlat");
            dLng = response.getString("dstlng");
            srcname = response.getString("srchub");
            dstname = response.getString("dsthub");
            sc = response.getString("st");
            canl = response.getString("cancel");

            rideRate.setText(getString(R.string.message_rs, rate));
            ridePrice.setText(getString(R.string.message_rs, price));
            rideTax.setText(getString(R.string.message_rs, tax));
            rideTotal.setText(getString(R.string.message_rs, total));
            rideVehicle.setText(vtype);
            rideTime.setText(getString(R.string.message_min, time));
            //rideTime.setText(time + R.string.mins);
            rideDate.setText(date);
            if (canl.equals("1")) {
                cancelButton.setVisibility(View.VISIBLE);
                checkStatus();
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelRequest();
                    }
                });
            } else if (canl.equals("0")) {
                cancelButton.setVisibility(View.GONE);
            }
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
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsHistory);
            mapFragment.getMapAsync(this);

            srcLat = Double.parseDouble(sLat);
            srcLng = Double.parseDouble(sLng);
            dstLat = Double.parseDouble(dLat);
            dstLng = Double.parseDouble(dLng);

            src = new MarkerOptions().position(new LatLng(srcLat, srcLng)).title(srcname);
            dst = new MarkerOptions().position(new LatLng(dstLat, dstLng)).title(dstname);

            //new FetchURL(ActivityRentSummery.this).execute(getUrl(src.getPosition(), dst.getPosition(), "driving"), "driving")
        }

        //response on hitting user-trip-get-status API
        if (id == 3) {
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
                            Log.d(TAG,"inside if when status = AS");
                            /*String otp = response.getString("otp");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
                            Intent as = new Intent(ActivityRentSummery.this, ActivityRentOTP.class);
                            startActivity(as);
                            finish();
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            //retireTrip();
                            /*Intent fntr = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                            startActivity(fntr);*/
                            Intent fntr = new Intent(ActivityRentSummery.this, ActivityRentEnded.class);
                            startActivity(fntr);
                            finish();
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
                            Intent rateFirst = new Intent(ActivityRentSummery.this, ActivityRateRent.class);
                            startActivity(rateFirst);
                            finish();
                        } /*else {
                            tripInfo(tid);
                        }*/
                        new Handler().postDelayed(() -> {
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
        /*Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityRentSummery.this, ActivityRentHistory.class));
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Log.d(TAG, "Added Markers");
        mMap.addMarker(src).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mMap.addMarker(dst).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(srcLat, srcLng))
                .zoom(12)//zoom level
                .bearing(0)
                .tilt(45)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 5000, null);
    }
}
