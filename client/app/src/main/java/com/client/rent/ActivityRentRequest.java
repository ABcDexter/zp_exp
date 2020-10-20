package com.client.rent;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRentRequest extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityRentRequest";

    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String COST_DROP = "";
    // public static final String SPEED_DROP = "00";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";

    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String VEHICLE_TYPE = "VehicleType";
    public static final String NO_HOURS = "NoHours";
    public static final String RENT_RIDE = "RentRide";
    public static final String PAYMENT_MODE = "PaymentMode";

    ImageButton next, speedInfo, paymentInfo, PickInfo, DropInfo;
    TextView vSpeed, advPay, pickPlaceInfo, dropPlaceInfo;
    ScrollView scrollView;
    String stringAuth, stringPick, stringDrop;
    ImageView scootyUp, scootyDown;
    SharedPreferences prefAuth;
    ActivityRentRequest a = ActivityRentRequest.this;
    Dialog myDialog;
    String rideInfo, vTypeInfo, pModeInfo, dropInfo, pickInfo, speedDrop, costDrop, hrs;
    Map<String, String> params = new HashMap();
    Animation animMoveL2R, animMoveR2L;

    private static ActivityRentRequest instance;

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-trip-estimate API
        if (id == 1) {
            try {
                // Parsing json object response
                // response will be a json object
                String price = response.getString("price");
                String speed = response.getString("speed");

                vSpeed.setText(speed + " km/hr");
                advPay.setText("₹ " + price);
                /*SharedPreferences sp_cookie = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                sp_cookie.edit().putString(SPEED_DROP, speed).apply();
                sp_cookie.edit().putString(COST_DROP, price).apply();*/

                Log.d(TAG, "price:" + price + " speed:" + speed);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //response on hitting user-trip-request API
        if (id == 2) {
            String tid = response.getString("tid");
            SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(TRIP_ID, tid).apply();
            checkStatus();
        }
        //response on hitting user-trip-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    String rtype = response.getString("rtype");

                    if (rtype.equals("1")) {
                        SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        sp_cookie.edit().putString(TRIP_ID, tid).apply();
                        if (status.equals("RQ")) {
                            Snackbar snackbar = Snackbar
                                    .make(scrollView, R.string.checking_veh_av, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.cancel, new View.OnClickListener() {
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
                            /*String otp = response.getString("otp");
                            //String van = response.getString("vno");
                            Intent as = new Intent(ActivityRentRequest.this, ActivityRentOTP.class);
                            startActivity(as);
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
                            //sp_otp.edit().putString(VAN_PICK, van).apply();
                            Intent as = new Intent(ActivityRentRequest.this, ActivityRentOTP.class);
                            startActivity(as);
                        }
                    }
                } else {
                    Intent homePage = new Intent(ActivityRentRequest.this, ActivityRentHome.class);
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
            Intent home = new Intent(ActivityRentRequest.this, ActivityRentHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rent_request, null, false);
        frameLayout.addView(activityView);

        instance = this;

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        SharedPreferences prefPLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        stringPick = prefPLoc.getString(LOCATION_PICK, "");
        stringDrop = prefPLoc.getString(LOCATION_DROP, "");
        dropInfo = prefPLoc.getString(LOCATION_DROP_ID, "");
        pickInfo = prefPLoc.getString(LOCATION_PICK_ID, "");
        rideInfo = prefPLoc.getString(RENT_RIDE, "");
        vTypeInfo = prefPLoc.getString(VEHICLE_TYPE, "");
        pModeInfo = prefPLoc.getString(PAYMENT_MODE, "");
        hrs = prefPLoc.getString(NO_HOURS, "");
        // speedDrop = prefPLoc.getString(SPEED_DROP, "");
        costDrop = prefPLoc.getString(COST_DROP, "");

        vSpeed = findViewById(R.id.vehicle_speed);
        advPay = findViewById(R.id.adv_payment);
        pickPlaceInfo = findViewById(R.id.pick_hub);
        dropPlaceInfo = findViewById(R.id.drop_hub);
        scrollView = findViewById(R.id.scrollView_rent_request);
        next = findViewById(R.id.confirm_rent_book);
        scootyUp = findViewById(R.id.image_scooty_up);
        scootyDown = findViewById(R.id.image_scooty_below);
        speedInfo = findViewById(R.id.infoSpeed);
        paymentInfo = findViewById(R.id.infoPayment);
        PickInfo = findViewById(R.id.infoPick);
        DropInfo = findViewById(R.id.infoDrop);

        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);
        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);

        try {

            String upToNCharacters = stringPick.substring(0, Math.min(stringPick.length(), 20));
            pickPlaceInfo.setText(upToNCharacters);
            //Log.d(TAG, "qwertyuiop"+upToNCharacters);
        } catch (Exception e) {
            pickPlaceInfo.setText(stringPick);
            e.printStackTrace();
        }

        try {
            String upToNCharacters = stringDrop.substring(0, Math.min(stringDrop.length(), 20));
            dropPlaceInfo.setText(upToNCharacters);
        } catch (Exception e) {
            dropPlaceInfo.setText(stringDrop);
            e.printStackTrace();
        }
        next.setOnClickListener(this);
        paymentInfo.setOnClickListener(this);
        speedInfo.setOnClickListener(this);
        PickInfo.setOnClickListener(this);
        DropInfo.setOnClickListener(this);

        myDialog = new Dialog(this);

        rideEstimate();
        if (!costDrop.equals("")) {
            advPay.setText("₹ " + costDrop);
        }

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

    public void checkStatus() {
        String auth = stringAuth;
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

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText(R.string.max_speed_25);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

            //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            //wmlp.x = 100;   //x position
            wmlp.y = 80;   //y position

        }
        if (id == 2) {
            infoText.setText(R.string.cost_calc_as_per_time);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

            //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            //wmlp.x = 100;   //x position
            wmlp.y = 80;   //y position
        }

        if (id == 3) {
            infoText.setText(stringPick);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

            //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            //wmlp.x = 100;   //x position
            wmlp.y = 76;   //y position
        }
        if (id == 5) {
            infoText.setText(stringDrop);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

            //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            //wmlp.x = 100;   //x position
            wmlp.y = 76;   //y position
        }

        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(true);

    }

    public static ActivityRentRequest getInstance() {
        return instance;
    }

    private void moveit() {

        scootyDown.setVisibility(View.VISIBLE);
        scootyUp.startAnimation(animMoveL2R);
        scootyDown.startAnimation(animMoveR2L);
        /*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(scootyDown, "translationX", 1500, 0f);
        objectAnimator.setDuration(1500);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(scootyUp, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.confirm_rent_book:
                moveit();
                userRequestRide();
                break;
            case R.id.infoPayment:
                ShowPopup(2);
                break;
            case R.id.infoSpeed:
                ShowPopup(1);
                break;
            case R.id.infoPick:
                ShowPopup(3);
                break;
            case R.id.infoDrop:
                ShowPopup(5);
                break;
        }
    }

    private void rideEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srcid", pickInfo);
        params.put("dstid", dropInfo);
        params.put("rtype", rideInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);
        params.put("hrs", hrs);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srcid= " + pickInfo + " dstid= " + dropInfo
                + " rtype= " + rideInfo + " vtype=" + vTypeInfo + " pmode=" + pModeInfo + " hrs= " + hrs);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-trip-estimate");

        UtilityApiRequestPost.doPOST(a, "user-trip-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    protected void userRequestRide() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srcid", pickInfo);
        params.put("dstid", dropInfo);
        params.put("hrs", hrs);
        params.put("rtype", rideInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srcid = " + pickInfo + " dstid = " + dropInfo + " hrs" + hrs +
                " rtype = " + rideInfo + " vtype = " + vTypeInfo + " pmode = " + pModeInfo);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-rent-request");

        UtilityApiRequestPost.doPOST(a, "user-rent-request", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityRentRequest.this, ActivityRentHome.class));
        finish();
    }
}
