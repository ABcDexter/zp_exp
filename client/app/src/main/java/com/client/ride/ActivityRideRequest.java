package com.client.ride;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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

import ch.halcyon.squareprogressbar.SquareProgressBar;

public class ActivityRideRequest extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRideRequest";
    public static final String COST_DROP = "";
    public static final String TIME_DROP = "e";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String SRC_LNG = "SrcLng";
    public static final String SRC_LAT = "SrcLat";
    public static final String DST_LAT = "DropLat";
    public static final String DST_LNG = "DropLng";
    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String RENT_RIDE = "RentRide";
    public static final String PAYMENT_MODE = "PaymentMode";
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";

    ImageButton next, costInfo, timeInfo, pickInfo, dropInfo;
    TextView costEst, timeEst, pickPlaceInfo, dropPlaceInfo;
    ScrollView scrollView;
    String stringAuth, stringPick, stringDrop;
    ImageView zbeeR, zbeeL, scooty_up, scooty_down;
    SharedPreferences prefAuth;
    Dialog myDialog;
    String rideInfo, vTypeInfo, noRiderInfo, pModeInfo, srcLat, srcLng, dstLat, dstLng, time, cost;
    Map<String, String> params = new HashMap();
    ActivityRideRequest a = ActivityRideRequest.this;

    private static ActivityRideRequest instance;
    Animation animMoveL2R, animMoveR2L;

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-ride-estimate API
        if (id == 1) {
            try {
                // Parsing json object response
                // response will be a json object
                String price = response.getString("price");
                String time = response.getString("time");
                costEst.setText(getString(R.string.message_rs, price));
                timeEst.setText(getString(R.string.message_min, time));
                SharedPreferences sp_cookie = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                sp_cookie.edit().putString(TIME_DROP, time).apply();
                sp_cookie.edit().putString(COST_DROP, price).apply();

                Log.d(TAG, "price:" + price + " time:" + time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //checkStatus();
        }
        //response on hitting user-ride-request API
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
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();

                    if (status.equals("RQ")) {
                        Snackbar snackbar = Snackbar
                                .make(scrollView, R.string.searching_for_ride, Snackbar.LENGTH_INDEFINITE)
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
                        intent.setAction("02");
                        startService(intent);
                    }
                    if (status.equals("AS")) {

                        Intent as = new Intent(ActivityRideRequest.this, ActivityRideOTP.class);
                        startActivity(as);

                    }
                } else {
                    myDialog.setContentView(R.layout.popup_new_request);
                    TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

                    infoText.setText(R.string.no_driver_av);

                    myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    myDialog.show();
                    myDialog.setCanceledOnTouchOutside(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myDialog.dismiss();
                            Intent homePage = new Intent(ActivityRideRequest.this, ActivityWelcome.class);
                            homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(homePage);
                            finish();
                        }
                    }, 5000);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //response on hitting user-trip-cancel API
        if (id == 4) {
            Intent home = new Intent(ActivityRideRequest.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_request, null, false);
        frameLayout.addView(activityView);

        instance = this;

        SharedPreferences prefPLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        stringPick = prefPLoc.getString(SRC_NAME, "");
        stringDrop = prefPLoc.getString(DST_NAME, "");
        String SrcLat = prefPLoc.getString(SRC_LAT, "");
        String SrcLng = prefPLoc.getString(SRC_LNG, "");
        String DstLng = prefPLoc.getString(DST_LNG, "");
        String DstLat = prefPLoc.getString(DST_LAT, "");
        String PModeInfo = prefPLoc.getString(PAYMENT_MODE, "");
        String RideInfo = prefPLoc.getString(RENT_RIDE, "");
        String Time = prefPLoc.getString(TIME_DROP, "");
        String Cost = prefPLoc.getString(COST_DROP, "");

        srcLat = SrcLat;
        srcLng = SrcLng;
        dstLat = DstLat;
        dstLng = DstLng;
        pModeInfo = PModeInfo;
        rideInfo = RideInfo;
        time = Time;
        cost = Cost;

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Intent data = getIntent();
        noRiderInfo = data.getStringExtra("npas");
        vTypeInfo = data.getStringExtra("vtype");


        costEst = findViewById(R.id.cost_estimate);
        timeEst = findViewById(R.id.time_estimate);
        pickPlaceInfo = findViewById(R.id.pick_info);
        dropPlaceInfo = findViewById(R.id.drop_info);
        scrollView = findViewById(R.id.scrollView_location_selection);
        next = findViewById(R.id.confirm_ride_book);
        zbeeR = findViewById(R.id.image_zbee);
        zbeeL = findViewById(R.id.image_zbee_below);
        costInfo = findViewById(R.id.infoCost);
        timeInfo = findViewById(R.id.infoTime);
        pickInfo = findViewById(R.id.infoPick);
        dropInfo = findViewById(R.id.infoDrop);
        scooty_up = findViewById(R.id.scooty_up);
        scooty_down = findViewById(R.id.scooty_down);

        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);

        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);

        dropInfo.setOnClickListener(this);
        pickInfo.setOnClickListener(this);
        try {
            String upToNCharacters = stringPick.substring(0, Math.min(stringPick.length(), 20));
            pickPlaceInfo.setText(upToNCharacters);
        } catch (Exception e) {
            pickPlaceInfo.setText(stringPick);
            e.printStackTrace();
        }

        try {
            String upTo16Characters = stringDrop.substring(0, Math.min(stringDrop.length(), 20));
            dropPlaceInfo.setText(upTo16Characters);
        } catch (Exception e) {
            dropPlaceInfo.setText(stringDrop);
            e.printStackTrace();
        }

        next.setOnClickListener(this);
        costInfo.setOnClickListener(this);
        timeInfo.setOnClickListener(this);
        myDialog = new Dialog(this);
        rideEstimate();

        if (!time.equals("")) {
            timeEst.setText(getString(R.string.message_min, time));
        }
        if (!cost.equals("")) {
            costEst.setText(getString(R.string.message_rs, cost));
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
        LinearLayout ll = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            ll.setVisibility(View.GONE);
            String part1 = getString(R.string.google_part1);
            String part2 = getString(R.string.google_part2);
            String part3 = getString(R.string.google_part3);

            String sourceString = part1 + "<b>" + part2 + "</b> " + part3;
            infoText.setText(Html.fromHtml(sourceString));
        }
        if (id == 2) {
            ll.setVisibility(View.GONE);
            String part1 = getString(R.string.google_time_part1);
            String part2 = getString(R.string.google_time_part2);
            String part3 = getString(R.string.google_time_part3);

            String sourceString = part1 + "<b>" + part2 + "</b> " + part3;
            infoText.setText(Html.fromHtml(sourceString));

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

    public static ActivityRideRequest getInstance() {
        return instance;
    }

    private void moveit() {
        scooty_down.setVisibility(View.VISIBLE);
        zbeeR.startAnimation(animMoveL2R);
        scooty_down.startAnimation(animMoveR2L);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.reject_request) {
            Intent home = new Intent(ActivityRideRequest.this, ActivityRideHome.class);
            startActivity(home);
            finish();
        } else if (id == R.id.accept_request) {//TODO
        } else if (id == R.id.confirm_ride_book) {
            alertBox(); //alert dialog box to tell the user that if he wishes to end the ride before reaching the destination, he will be charged as per the destination chosen at the time of booking.
        } else if (id == R.id.infoTime) {
            ShowPopup(2);
        } else if (id == R.id.infoCost) {
            ShowPopup(1);
        } else if (id == R.id.infoPick) {
            ShowPopup(3);
        } else if (id == R.id.infoDrop) {
            ShowPopup(4);
        }
    }

    private void alertBox() {
        // Create the object of
        // AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRideRequest.this);
        // Set the message show for the Alert time
        builder.setMessage(R.string.end_ride_before_destination);
        // Set Alert Title
        builder.setTitle(R.string.please_note);
        // Set Cancelable false
        // for when the user clicks on the outside
        // the Dialog Box then it will remain show
        builder.setCancelable(false);
        // Set the positive button with ok name
        // OnClickListener method is use of
        // DialogInterface interface.
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // When the user click ok button
                // then app will close
                moveit();
                showProgressIndication();
                userRequestRide();
            }
        });

        // Set the Negative button with No name
        // OnClickListener method is use
        // of DialogInterface interface.
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user click cancel
                // then user goes to the previous window
                Intent back = new Intent(ActivityRideRequest.this, ActivityRideHome.class);
                startActivity(back);
                finish();
            }
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC7721")));
        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonNegative.setTextColor(ContextCompat.getColor(this, R.color.Black));
    }
    private void showProgressIndication() {
        SquareProgressBar squareProgressBar = findViewById(R.id.sprogressbar);
        squareProgressBar.setImage(R.drawable.btn_bkg);
        squareProgressBar.setVisibility(View.VISIBLE);
        squareProgressBar.setProgress(50.0);
        squareProgressBar.setWidth(10);
        squareProgressBar.setIndeterminate(true);
        squareProgressBar.setColor("#D7FB05");
    }
    private void rideEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", srcLat);
        params.put("srclng", srcLng);
        params.put("dstlat", dstLat);
        params.put("dstlng", dstLng);
        params.put("rtype", rideInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat=" + srcLat + " srclng=" + srcLng
                + " dstlat=" + dstLat + " dstlng=" + dstLng + " vtype=" + vTypeInfo + " pmode=" + pModeInfo + " rtype=" + rideInfo);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-ride-estimate");

        UtilityApiRequestPost.doPOST(a, "user-ride-estimate", parameters, 2000, 0, response -> {
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
        params.put("srclat", srcLat);
        params.put("srclng", srcLng);
        params.put("dstlat", dstLat);
        params.put("dstlng", dstLng);
        params.put("rtype", rideInfo);
        params.put("vtype", vTypeInfo);
        params.put("pmode", pModeInfo);
        params.put("npas", noRiderInfo);
        params.put("srcname", stringPick);
        params.put("dstname", stringDrop);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat=" + srcLat + " srclng=" + srcLng
                + " dstlat=" + dstLat + " dstlng=" + dstLng + " vtype=" + vTypeInfo + " pmode="
                + pModeInfo + " rtype=" + rideInfo + " npas=" + noRiderInfo + " srcname=" + stringPick + " dstname=" + stringDrop);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-ride-request");

        UtilityApiRequestPost.doPOST(a, "user-ride-request", parameters, 2000, 0, response -> {
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
        startActivity(new Intent(ActivityRideRequest.this, ActivityRideHome.class));
        finish();
    }
}
