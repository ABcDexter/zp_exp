package com.client.ride;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ActivityRideOTP extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityRideOTP";

    TextView origin, destination, dName, dPhone, vNum, OTP, costEst, timeEst, trackDriver;
    ImageButton cancel;
    ScrollView scrollView;

    public static final String COST_DROP = "";
    public static final String TIME_DROP = "e";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String VAN_PICK = "com.client.Locations";
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String OTP_PICK = "OTPPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    public static final String DRIVER_MINS = "DriverMins";

    private static ActivityRideOTP instance;
    Dialog myDialog;
    RelativeLayout rlPhone;
    ImageButton costInfo, priceInfo, pickInfo, dropInfo,infoMins;
    String stringAuthCookie, stringPick, stringDrop;
    //Button giveOTP;
    ActivityRideOTP a = ActivityRideOTP.this;
    Map<String, String> params = new HashMap();

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-give-otp API
       /* if (id == 5) {
            //TODO remove later
            Log.d(TAG, "RESPONSE:" + response);
        }*/
        //response on hitting user-ride-get-driver API
        if (id == 1) {
            String pn = response.getString("pn");
            String name = response.getString("name");
            dPhone.setText(pn);
            dName.setText(name);
            SharedPreferences sp_cookie = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(DRIVER_NAME, name).apply();
            sp_cookie.edit().putString(DRIVER_PHN, pn).apply();
        }

        //response on hitting user-trip-cancel API
        if (id == 2) {
            Intent home = new Intent(ActivityRideOTP.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }

        //response on hitting user-trip-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("AS")) {

                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("03");
                        startService(intent);
                    }
                    if (status.equals("ST")) {
                        Intent st = new Intent(ActivityRideOTP.this, ActivityRideInProgress.class);
                        startActivity(st);
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideOTP.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_otp, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");

        SharedPreferences prefPLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        stringPick = prefPLoc.getString(SRC_NAME, "");
        stringDrop = prefPLoc.getString(DST_NAME, "");
        String stringCost = prefPLoc.getString(COST_DROP, "");
        String stringTime = prefPLoc.getString(TIME_DROP, "");
        String str_otp = prefPLoc.getString(OTP_PICK, "");
        String str_van = prefPLoc.getString(VAN_PICK, "");
        String str_min = prefPLoc.getString(DRIVER_MINS, "");

        costEst = findViewById(R.id.cost_estimate_otp);
        costEst.setText("₹ " + stringCost);
        timeEst = findViewById(R.id.ride_estimate_otp);
        timeEst.setText(stringTime + " MINS");
        dName = findViewById(R.id.driver_name);
        dPhone = findViewById(R.id.driver_phone);
        OTP = findViewById(R.id.otp_ride);
        vNum = findViewById(R.id.v_no);
        origin = findViewById(R.id.pick_place);
        destination = findViewById(R.id.drop_place);
        scrollView = findViewById(R.id.scrollView_ride_OTP);
        cancel = findViewById(R.id.cancel_ride_booking);
        costInfo = findViewById(R.id.infoCost);
        costInfo.setOnClickListener(this);
        priceInfo = findViewById(R.id.infoTime);
        priceInfo.setOnClickListener(this);
        cancel.setOnClickListener(this);
        pickInfo = findViewById(R.id.infoPick);
        pickInfo.setOnClickListener(this);
        dropInfo = findViewById(R.id.infoDrop);
        dropInfo.setOnClickListener(this);
        trackDriver = findViewById(R.id.trackDriver);
        rlPhone = findViewById(R.id.rl_p);
        infoMins = findViewById(R.id.infomins);
        infoMins.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        /*giveOTP = findViewById(R.id.give_otp);
        giveOTP.setOnClickListener(this);*/
        //dPhone.setOnClickListener(this);
        myDialog = new Dialog(this);
        OTP.setText(str_otp);
        vNum.setText(str_van);
        driverDetails();
        trackDriver.setText(str_min+" MINS");

        if (stringDrop.isEmpty()) {
            destination.setText("DROP POINT");
        } else {
            try {
                String upToNCharacters = stringDrop.substring(0, Math.min(stringDrop.length(), 25));
                destination.setText(upToNCharacters);
                //destination.setText(dropCutName);
            } catch (Exception e) {
                destination.setText(stringDrop);
            }
        }

        if (stringPick.isEmpty()) {
            origin.setText("PICK UP POINT");
        } else {
            try {
                String upToNCharacters = stringPick.substring(0, Math.min(stringPick.length(), 25));
                origin.setText(upToNCharacters);
                //destination.setText(dropCutName);
            } catch (Exception e) {
                origin.setText(stringPick);
            }
        }

        checkStatus();
    }

    private void driverDetails() {
        String stringAuth = stringAuthCookie;
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-driver");
        UtilityApiRequestPost.doPOST(a, "user-ride-get-driver", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText("This is an approximate cost. May change depending on ride time.");
        }
        if (id == 2) {
            infoText.setText("Approximate time as per Google Maps. May change depending on traffic.");
        }
        if (id == 3) {
            infoText.setText(stringPick);
        }
        if (id == 4) {
            infoText.setText(stringDrop);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }


    public static ActivityRideOTP getInstance() {
        return instance;
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityRideOTP.this);
        alertDialog.setTitle("CANCEL RIDE");
        String[] items = {
                "DRIVER DENIED PICKUP",
                "DRIVER DENIED DESTINATION",
                "EXPECTED A SHORTER WAIT TIME",
                "UNABLE TO CONTACT DRIVER",
                "MY REASON IS NOT LISTED"};
        alertDialog.setCancelable(false);

        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        //Toast.makeText(ActivityRideOTP.this, "Thank you for your feedback.", Toast.LENGTH_LONG).show();
                        Snackbar snackbar = Snackbar
                                .make(scrollView, "Thank you for your feedback.", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        break;
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userCancelTrip();
                Intent intent = new Intent(ActivityRideOTP.this, ActivityRideHome.class);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.setPositiveButton("Don't Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
    }

    private void userCancelTrip() {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-cancel");
        UtilityApiRequestPost.doPOST(a, "user-trip-cancel", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_ride_booking:
                showAlertDialog();
                break;
            case R.id.infoTime:
            case R.id.infomins:
                ShowPopup(2);
                break;
            case R.id.infoCost:
                ShowPopup(1);
                break;
            /*case R.id.give_otp: //TODO remove this later
                giveOtp();
                break;*/
            case R.id.rl_p:
                callDriverPhn();
                break;
            case R.id.infoPick:
                ShowPopup(3);
                break;
            case R.id.infoDrop:
                ShowPopup(4);
                break;

        }
    }

    public void callDriverPhn() {
        String phoneDriver = dPhone.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneDriver));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

   /* private void giveOtp() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("otp", OTP.getText().toString());
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " otp=" + OTP.getText().toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-give-otp", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 5);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }*/

    public void checkStatus() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
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
}
