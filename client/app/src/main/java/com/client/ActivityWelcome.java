package com.client;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.deliver.ActivityPackageDetails;
import com.client.rent.ActivityRentHome;
import com.client.ride.ActivityRideEnded;
import com.client.ride.ActivityRideHome;
import com.client.ride.ActivityRideInProgress;
import com.client.ride.ActivityRideOTP;
import com.client.ride.ActivityRideRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityWelcome extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityWelcome";
    ImageView zippe_iv, zippe_iv_below, scooty_up, scooty_down;
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

    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    SharedPreferences prefAuth;
    String stringAuth;
    ImageButton btnRent, btnRide, btnDeliver, btnShop, btnConnect;
    ActivityWelcome a = ActivityWelcome.this;
    Map<String, String> params = new HashMap();
    String auth;
    private static ActivityWelcome instance;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};
    String lat, lng, stringAN;
    private TextView mOutputText;

    Animation animMoveL2R, animMoveR2L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        instance = this;

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");

        btnRent = findViewById(R.id.btn_rent);
        btnRent.setOnClickListener(this);
        btnRide = findViewById(R.id.btn_ride);
        btnRide.setOnClickListener(this);
        btnDeliver = findViewById(R.id.btn_deliver);
        btnShop = findViewById(R.id.btn_shop);
        btnConnect = findViewById(R.id.btn_connect);
        btnDeliver.setOnClickListener(this);
        btnShop.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        auth = stringAuth;
        if (auth.equals("")) {
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityRegistration.class);
            startActivity(registerUser);
            finish();
        }
        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);
        scooty_up = findViewById(R.id.scooty_up);
        scooty_down = findViewById(R.id.scooty_down);
        //moveZbee();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityWelcome.this);
        getLastLocation();
        checkStatus();
        myDialog = new Dialog(this);

        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);
        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);
        //animMoveL2R_scooty = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r_scooty);
        //zippe_iv.startAnimation(animMoveL2R);
        //animMoveR2L_zbee = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l_zbee);
        //scooty_down.startAnimation(animMoveR2L);
        zippe_iv.startAnimation(animMoveL2R);
        scooty_down.startAnimation(animMoveR2L);

    }

    public static ActivityWelcome getInstance() {
        return instance;
    }

    private void moveZbee() {
        scooty_up.setVisibility(View.GONE);
        scooty_down.setVisibility(View.VISIBLE);
        zippe_iv_below.setVisibility(View.GONE);
        zippe_iv.setVisibility(View.VISIBLE);

        zippe_iv.startAnimation(animMoveL2R);
        scooty_down.startAnimation(animMoveR2L);
        /*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(scooty_down, "translationX", 1800, 0f);
        objectAnimator.setDuration(1700);
        objectAnimator.start();

        *//*objectAnimator.setRepeatCount(ValueAnimator.INFINITE);*//*

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zippe_iv, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();*/
        /*objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*/

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveScooty();
            }
        }, 1100);*/
    }

    /*private void moveScooty() {
        scooty_up.setVisibility(View.VISIBLE);
        scooty_down.setVisibility(View.GONE);
        zippe_iv_below.setVisibility(View.VISIBLE);
        zippe_iv.setVisibility(View.GONE);

        zippe_iv_below.startAnimation(animMoveR2L_zbee);
        scooty_up.startAnimation(animMoveL2R_scooty);
        *//*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zippe_iv_below, "translationX", 1800, 0f);
        objectAnimator.setDuration(1700);
        objectAnimator.start();
        *//**//*objectAnimator.setRepeatCount(ValueAnimator.INFINITE);*//**//*

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(scooty_up, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();*//*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveZbee();
            }
        }, 1100);
        *//* objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*//*
    }*/

    public void sendLocation() {
        Log.d(TAG, "inside sendLocation()");
        params.put("an", stringAN);
        params.put("auth", stringAuth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + stringAuth + " lat =" + lat + " lng = " + lng + " an=" + stringAN);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

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

    Dialog myDialog;
    TextView dialog_txt;

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_color);
        dialog_txt = myDialog.findViewById(R.id.info_text);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            dialog_txt.setText("No ride available currently.\nNotify me when available.");
        }
        if (id == 2) {
            dialog_txt.setText(R.string.drivers_available);
        }
        if (id == 3) {
            dialog_txt.setText(R.string.ride_cancelled_by_you);
        }
        if (id == 4) {
            dialog_txt.setText(R.string.ride_timed_out);
        }
        if (id == 5) {
            dialog_txt.setText(R.string.unable_to_complete_your_ride);
        }
        if (id == 6) {
            dialog_txt.setText(R.string.ride_canceled_by_you);
        }
        if (id == 7) {
            dialog_txt.setText(R.string.vehicles_rented_out);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    private void ShowPopup1(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        dialog_txt = myDialog.findViewById(R.id.info_text);

        if (id == 2) {
            dialog_txt.setText(R.string.drivers_available);
        }

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void getLastLocation() {
        Log.d(TAG, "Inside getLastLocation()");
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d(TAG, "inside else of getLastLocation()");
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                Log.d(TAG, "inside else of addOnCompleteListener()");
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
                                sendLocation();
                            }
                        }
                    });
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        Log.d(TAG, "inside hasPermission()");
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private void requestNewLocationData() {
        Log.d(TAG, "inside requestNewLocationData()");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "inside LocationResult() call");
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };


    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
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
                        if (status.equals("CN")) {
                            Log.d(TAG, "trip cancelled");
                        }
                        if (status.equals("TO") || status.equals("DN") || status.equals("FL")) {

                            Log.d(TAG, "error");
                        }
                    }

                    /*if (rtype.equals("1")) {
                        if (status.equals("RQ")) {
                            Intent rq = new Intent(ActivityWelcome.this, ActivityRentRequest.class);
                            startActivity(rq);
                        }
                        if (status.equals("AS")) {
                            */
                    /*String otp = response.getString("otp");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
                    /*
                            Intent as = new Intent(ActivityWelcome.this, ActivityRentOTP.class);
                            startActivity(as);
                        }
                        if (status.equals("ST")) {
                            String van = response.getString("vno");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(VAN_PICK, van).apply();
                            Intent as = new Intent(ActivityWelcome.this, ActivityRentInProgress.class);
                            startActivity(as);
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            //retireTrip();
                            */
                    /*Intent fntr = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                            startActivity(fntr);*/
                    /*
                            Intent fntr = new Intent(ActivityWelcome.this, ActivityRateZippe.class);
                            startActivity(fntr);
                        }

                        if (status.equals("TO")) {
                            ShowPopup(7);
                        }

                    }*/
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
            }
        }

        // response on hitting auth-trip-get-info API
        if (id == 2) {
            String st = response.getString("st");
            String rtype = response.getString("rtype");
            if (rtype.equals("0")) {
                if (st.equals("PD")) {
                    String price = response.getString("price");
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(PREFS_LOCATIONS);
                    editor.apply();

                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();


                }

                if (st.equals("DN")) {
                    ShowPopup(3);
                }
                if (st.equals("TO")) {
                    ShowPopup(4);
                }
                if (st.equals("FL")) {
                    ShowPopup(5);
                }
                if (st.equals("CN")) {
                    ShowPopup(6);
                }
                retireTrip();
            }
            if (rtype.equals("1")) {
                if (st.equals("PD")) {
                    //String price = response.getString("price");

                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(LOCATION_PICK);
                    editor.remove(LOCATION_DROP);
                    editor.remove(LOCATION_DROP_ID);
                    editor.remove(LOCATION_PICK_ID);
                    editor.apply();

                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();
                    retireTrip();
                }
                if (st.equals("FN")) {
                    retireTrip();
                }
                if (st.equals("CN")) {
                    retireTrip();
                }
                retireTrip();

            }
            //retireTrip();

        }
        //response on hitting user-trip-retire API
        if (id == 3) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();


            Log.d(TAG, "tripID= " + TRIP_ID + DRIVER_NAME + DRIVER_PHN);

            SharedPreferences prefLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editorLoc = prefLoc.edit();
            editorLoc.remove(VAN_PICK);
            editorLoc.remove(DST_NAME);
            editorLoc.remove(SRC_NAME);
            editorLoc.remove(OTP_PICK);
            editorLoc.apply();

            sendLocation();
        }

        //response on hitting auth-location-update API
        if (id == 4) {
            /*Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("00");
            startService(i);*/

        }
    }

    private void retireTrip() {

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
        Toast.makeText(this, "Something went wrong! Please try again later.", Toast.LENGTH_LONG).show();

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
                Intent deliverIntent = new Intent(ActivityWelcome.this, ActivityPackageDetails.class);
                startActivity(deliverIntent);
                break;
            case R.id.btn_shop:
                String shopUrl = "https://zippe.in/en/shop-by-category/";
                Intent shopIntent = new Intent(Intent.ACTION_VIEW);
                shopIntent.setData(Uri.parse(shopUrl));
                startActivity(shopIntent);
                break;
            case R.id.btn_connect:
                String connectUrl = "https://zippe.in/en/service-categories/";
                Intent connectIntent = new Intent(Intent.ACTION_VIEW);
                connectIntent.setData(Uri.parse(connectUrl));
                startActivity(connectIntent);
                break;
        }
    }
}
