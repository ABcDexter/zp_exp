package com.clientzp.rent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> dev
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
<<<<<<< HEAD
=======
import android.widget.PopupWindow;
>>>>>>> dev
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.HubList;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;

public class ActivityUpdateInfo extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityUpdateInfo";
    TextView destination;
    ImageButton update;
    ScrollView scrollView;
<<<<<<< HEAD
    // PopupWindow popupWindow;
=======
    PopupWindow popupWindow;
>>>>>>> dev
    String dropID;
    String stringDrop, stringDropID;
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_LOCATIONS = "com.clientzp.ride.Locations";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String NO_HOURS = "NoHours";
    public static final String LOCATION_DROP_ID = "DropLocationID";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    private static ActivityUpdateInfo instance;
    FusedLocationProviderClient mFusedLocationClient;
    Dialog imageDialog2;
    String stringAuthCookie, stringHrs;
    Dialog myDialog;

    public static ActivityUpdateInfo getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
<<<<<<< HEAD
        //Log.d(TAG, "RESPONSE:" + response);
=======
        Log.d(TAG, "RESPONSE:" + response);
>>>>>>> dev

        //response on hitting user-rental-update API
        if (id == 2) {
            Toast.makeText(this, R.string.details_updated, Toast.LENGTH_LONG).show();
            SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(LOCATION_DROP, stringDrop);
            editor.putString(LOCATION_DROP_ID, stringDropID);
            editor.apply();

            Intent goBack = new Intent(ActivityUpdateInfo.this, ActivityRentInProgress.class);
            startActivity(goBack);
            finish();

        }
    }

    public void onFailure(VolleyError error) {
<<<<<<< HEAD
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
=======
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
>>>>>>> dev
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don’t set any content view here, since its already set in ActivityDrawer
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_update_info, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;
        //retrieve locally stored data
        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        stringHrs = pref.getString(NO_HOURS, "");

<<<<<<< HEAD
        //Log.d(TAG, "######stringHrs = " + stringHrs);
        stringDrop = pref.getString(LOCATION_DROP, "");
        stringDropID = pref.getString(LOCATION_DROP_ID, "");
        //SharedPreferences tripPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
=======
        Log.d(TAG, "######stringHrs = " + stringHrs);
        stringDrop = pref.getString(LOCATION_DROP, "");
        stringDropID = pref.getString(LOCATION_DROP_ID, "");
        SharedPreferences tripPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
>>>>>>> dev
        destination = findViewById(R.id.drop_hub);
        update = findViewById(R.id.update_data);
        scrollView = findViewById(R.id.scrollView_rent_progress);
        update.setOnClickListener(this);
        destination.setOnClickListener(this);

        if (stringDrop.isEmpty()) {
            destination.setText(R.string.drop_point);
        } else {
            String upToNCharacters = stringDrop.substring(0, Math.min(stringDrop.length(), 20));
            destination.setText(upToNCharacters);

            dropID = stringDropID;
<<<<<<< HEAD
            //Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
=======
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
>>>>>>> dev
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityUpdateInfo.this);
        imageDialog2 = new Dialog(this);

        myDialog = new Dialog(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.update_data) {
            showProgressIndication();
            userUpdateTrip();
        } else if (id == R.id.drop_hub) {
            Intent drop = new Intent(ActivityUpdateInfo.this, HubList.class);
            drop.putExtra("Request", "destination_rental_in_progress");
<<<<<<< HEAD
            //Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
=======
            Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
>>>>>>> dev
            startActivity(drop);
        }
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

    private void userUpdateTrip() {
        String hour = stringHrs;
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        params.put("dstid", stringDropID);
        params.put("hrs", hour);
        JSONObject param = new JSONObject(params);
        ActivityUpdateInfo a = ActivityUpdateInfo.this;
<<<<<<< HEAD
        /*Log.d(TAG, "Values: auth=" + stringAuth + " dstid=" + dropID + " hrs=" + hour);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rental-update");*/
=======
        Log.d(TAG, "Values: auth=" + stringAuth + " dstid=" + dropID + " hrs=" + hour);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rental-update");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "user-rental-update", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityUpdateInfo.this, ActivityRentInProgress.class));
        finish();
    }
}
