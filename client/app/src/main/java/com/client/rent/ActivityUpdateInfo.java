package com.client.rent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.HubList;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityUpdateInfo extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityUpdateInfo";
    TextView destination, hours;
    Button update;
    ScrollView scrollView;
    PopupWindow popupWindow;
    String dropID;
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String NO_HOURS = "NoHours";
    public static final String LOCATION_DROP_ID = "DropLocationID";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static ActivityUpdateInfo instance;
    FusedLocationProviderClient mFusedLocationClient;

    String stringAuthCookie;

    public static ActivityUpdateInfo getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-rental-update API
        if (id == 2) {
            Toast.makeText(this, "Details Updated Successfully!", Toast.LENGTH_LONG).show();
            SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(NO_HOURS, hours.getText().toString());
            editor.apply();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, "Updated Unsuccessful!", Toast.LENGTH_LONG).show();
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
        String stringHrs = pref.getString(NO_HOURS, "");
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");
        SharedPreferences tripPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        hours = findViewById(R.id.hours);
        destination = findViewById(R.id.drop_hub);
        update = findViewById(R.id.update_data);
        scrollView = findViewById(R.id.scrollView_rent_progress);
        hours.setText(stringHrs);

        update.setOnClickListener(this);
        destination.setOnClickListener(this);
        hours.setOnClickListener(this);

        if (stringDrop.isEmpty()) {
            destination.setText("DROP POINT");
        } else {
            int dropSpace = (stringDrop.contains(" ")) ? stringDrop.indexOf(" ") : stringDrop.length() - 1;
            String dropCutName = stringDrop.substring(0, dropSpace);
            destination.setText(dropCutName);
            dropID = stringDropID;
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityUpdateInfo.this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.update_data:
                alertDialog();
                break;
            case R.id.drop_hub:
                Intent drop = new Intent(ActivityUpdateInfo.this, HubList.class);
                drop.putExtra("Request", "destination_rental_in_progress");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(drop);
                break;
            case R.id.hours:
                LayoutInflater layoutInflater = (LayoutInflater) ActivityUpdateInfo.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert layoutInflater != null;
                View customView = layoutInflater.inflate(R.layout.hrs_popup, null);

                TextView hs2 = customView.findViewById(R.id.hrs2);
                TextView hs4 = customView.findViewById(R.id.hrs4);
                TextView hs8 = customView.findViewById(R.id.hrs8);
                TextView hs12 = customView.findViewById(R.id.hrs12);

                //instantiate popup window
                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                //display the popup window
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                popupWindow.showAtLocation(scrollView, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(false);
                //close the popup window on button click
                hs2.setOnClickListener(this);
                hs4.setOnClickListener(this);
                hs8.setOnClickListener(this);
                hs12.setOnClickListener(this);
                break;

            case R.id.hrs2:
                popupWindow.dismiss();
                hours.setText("2");
                break;
            case R.id.hrs4:
                popupWindow.dismiss();
                hours.setText("4");
                break;
            case R.id.hrs8:
                popupWindow.dismiss();
                hours.setText("8");
                break;
            case R.id.hrs12:
                popupWindow.dismiss();
                hours.setText("12");
                break;
        }
    }


    private void userUpdateTrip() {
        String hour = hours.getText().toString();
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        params.put("dstid", dropID);
        params.put("hrs", hour);
        JSONObject param = new JSONObject(params);
        ActivityUpdateInfo a = ActivityUpdateInfo.this;
        Log.d(TAG, "Values: auth=" + stringAuth + " dstid=" + dropID + " hrs=" + hour);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rental-update");
        UtilityApiRequestPost.doPOST(a, "user-rental-update", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void alertDialog() {
        Log.d(TAG, " alert Dialog opened");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("YOU MAY BE CHARGED EXTRA FOR CHANGING THE DETAILS. \nARE YOU SURE YOU WANT TO CHANGE DETAILS?");
        dialog.setTitle("UPDATE");
        dialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        userUpdateTrip();
                        Log.d(TAG, "checkStatus invoked");
                    }
                });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "DETAILS NOT UPDATED", Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));

    }

}
