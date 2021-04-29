package com.clientzp.ride;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.clientzp.UtilityPollingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;

public class ActivitySearchingDriver extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivitySearchingDriver";
    public static final String COST_DROP = "";
    public static final String PREFS_LOCATIONS = "com.clientzp.ride.Locations";
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
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";

    ImageButton btnCancel, costInfo, pickInfo, dropInfo;
    TextView costEst, pickPlaceInfo, dropPlaceInfo;
    String stringAuth, stringPick, stringDrop;
    ImageView zbeeR, zbeeL, scooty_up, scooty_down;
    SharedPreferences prefAuth;
    Dialog myDialog;
    String rideInfo, pModeInfo, srcLat, srcLng, dstLat, dstLng, cost;
    Map<String, String> params = new HashMap();
    ActivitySearchingDriver a = ActivitySearchingDriver.this;

    private static ActivitySearchingDriver instance;
    Animation animMoveL2R, animMoveR2L;

    public void onSuccess(JSONObject response, int id) throws JSONException {
        //Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-trip-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();

                    if (status.equals("RQ")) {
                        moveit();
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("002");
                        startService(intent);
                    }
                    if (status.equals("AS")) {

                        Intent as = new Intent(ActivitySearchingDriver.this, ActivityRideOTP.class);
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
                            Intent homePage = new Intent(ActivitySearchingDriver.this, ActivityWelcome.class);
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
        if (id == 2) {
            Intent home = new Intent(ActivitySearchingDriver.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_searching_driver, null, false);
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
        String Cost = prefPLoc.getString(COST_DROP, "");

        srcLat = SrcLat;
        srcLng = SrcLng;
        dstLat = DstLat;
        dstLng = DstLng;
        pModeInfo = PModeInfo;
        rideInfo = RideInfo;
        cost = Cost;

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        costEst = findViewById(R.id.cost_estimate);
        pickPlaceInfo = findViewById(R.id.pick_info);
        dropPlaceInfo = findViewById(R.id.drop_info);
        btnCancel = findViewById(R.id.cancel_ride_request);
        zbeeR = findViewById(R.id.image_zbee);
        zbeeL = findViewById(R.id.image_zbee_below);
        costInfo = findViewById(R.id.infoCost);
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

        btnCancel.setOnClickListener(this);
        costInfo.setOnClickListener(this);
        myDialog = new Dialog(this);
        checkStatus();

        if (!cost.equals("")) {
            costEst.setText(getString(R.string.message_rs, cost));
        }
        moveit();
    }

    private void cancelRequest() {
        String auth = stringAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-cancel");*/
        UtilityApiRequestPost.doPOST(a, "user-trip-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
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
        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");*/
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
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

    public static ActivitySearchingDriver getInstance() {
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
        if (id == R.id.cancel_ride_request) {
            showProgressIndication();
            cancelRequest();
        } else if (id == R.id.infoCost) {
            ShowPopup(1);
        } else if (id == R.id.infoPick) {
            ShowPopup(3);
        } else if (id == R.id.infoDrop) {
            ShowPopup(4);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivitySearchingDriver.this, ActivityRideHome.class));
        finish();
    }
}
