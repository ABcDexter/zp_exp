package com.zpclient.ride;

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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.zpclient.ActivityDrawer;
import com.zpclient.ActivityWelcome;
import com.zpclient.R;
import com.zpclient.UtilityApiRequestPost;
import com.zpclient.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;


public class ActivityRideOTP extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityRideOTP";

    TextView origin, destination, dName, dPhone, vNum, OTP, costEst, timeEst, trackDriver;
    ImageButton cancel;
    ScrollView scrollView;
    ImageView driverPhoto;
    public static final String COST_DROP = "";
    public static final String TIME_DROP = "e";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String VAN_PICK = "com.client.Locations";
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    //public static final String OTP_PICK = "OTPPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    //public static final String DRIVER_MINS = "DriverMins";

    private static ActivityRideOTP instance;
    Dialog myDialog;
    RelativeLayout rlPhone;
    ImageButton costInfo, priceInfo, pickInfo, dropInfo, infoMins;
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

                    String photo = response.getString("photourl");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("AS")) {
                        String vno = response.getString("vno");
                        String otp = response.getString("otp");
                        String time = response.getString("time");
                        vNum.setText(vno);
                        OTP.setText(otp);
                        trackDriver.setText(time + " MINS");
                        Glide.with(this).load(photo).into(driverPhoto);
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("03");
                        startService(intent);

                        SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                        sp_otp.edit().putString(VAN_PICK, vno).apply();
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
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
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
        //String str_otp = prefPLoc.getString(OTP_PICK, "");
        //String str_van = prefPLoc.getString(VAN_PICK, "");
        //String str_min = prefPLoc.getString(DRIVER_MINS, "");
        driverPhoto = findViewById(R.id.photo_driver);
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
        //OTP.setText(str_otp);
        //vNum.setText(str_van);
        driverDetails();
        //trackDriver.setText(str_min + " MINS");
        if (stringDrop.isEmpty()) {
            destination.setText(R.string.drop_point);
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
            origin.setText(R.string.pick_point);
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
            String part1 = getString(R.string.google_part1);
            String part2 = getString(R.string.google_part2);
            String part3 = getString(R.string.google_part3);

            String sourceString = part1 + "<b>" + part2 + "</b> " + part3;
            infoText.setText(Html.fromHtml(sourceString));
            //infoText.setText("This is an approximate cost. May change depending on ride time.");
        }
        if (id == 2) {
            String part1 = getString(R.string.google_time_part1);
            String part2 = getString(R.string.google_time_part2);
            String part3 = getString(R.string.google_time_part3);

            String sourceString = part1 + "<b>" + part2 + "</b> " + part3;
            infoText.setText(Html.fromHtml(sourceString));

            //infoText.setText("Approximate time as per Google Maps. May change depending on traffic.");
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
        alertDialog.setTitle(R.string.cancel_ride);
        String[] items = {
                getString(R.string.driver_denied),
                getString(R.string.driver_denied_destination),
                getString(R.string.wait_time_too_long),
                getString(R.string.driver_no_contact),
                getString(R.string.reason_not_listed)};
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
                                .make(scrollView, R.string.thanks_for_feedback, Snackbar.LENGTH_LONG);
                        snackbar.show();

                        break;
                }
            }
        });

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressIndication();
                userCancelTrip();
            }
        });

        alertDialog.setPositiveButton(R.string.dont_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC7721")));
    }
    private void showProgressIndication() {
        SquareProgressBar squareProgressBar = findViewById(R.id.sprogressbar);
        squareProgressBar.setImage(R.drawable.btn_bkg);
        squareProgressBar.setVisibility(View.VISIBLE);
        squareProgressBar.setProgress(50.0);
        squareProgressBar.setWidth(10);
        squareProgressBar.setIndeterminate(true);
        squareProgressBar.setColor("#EC7721");
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
        int id = v.getId();
        if (id == R.id.cancel_ride_booking) {
            showAlertDialog();
        } else if (id == R.id.infoTime || id == R.id.infomins) {
            ShowPopup(2);
        } else if (id == R.id.infoCost) {
            ShowPopup(1);
                /*case R.id.give_otp: //TODO remove this later
                giveOtp();
                break;*/
        } else if (id == R.id.rl_p) {
            callDriverPhn();
        } else if (id == R.id.infoPick) {
            ShowPopup(3);
        } else if (id == R.id.infoDrop) {
            ShowPopup(4);
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
