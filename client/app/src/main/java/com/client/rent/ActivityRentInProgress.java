package com.client.rent;

import android.Manifest;
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
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.client.HubList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRentInProgress extends ActivityDrawer implements View.OnClickListener {

    TextView shareDetails, emergencyCall, destination, nameD, phone, hours, vNum, remainingHours;
    Button update;
    ScrollView scrollView;
    PopupWindow popupWindow;
    String dropID;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    private static final String TAG = "ActivityRentInProgress";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    public static final String NO_HOURS = "NoHours";
    public static final String LOCATION_DROP_ID = "DropLocationID";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static ActivityRentInProgress instance;
    FusedLocationProviderClient mFusedLocationClient;

    String stringAuthCookie;

    public static ActivityRentInProgress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-time-remaining API
        if (id == 1) {
            String time = response.getString("time");
            remainingHours.setText(time);
        }
        //response on hitting user-rental-update API
        if (id == 2) {
            Toast.makeText(this, "Details Updated Successfully!", Toast.LENGTH_LONG).show();
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
                    if (status.equals("ST")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("14");
                        startService(intent);
                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        String price = response.getString("price");

                        Intent payment = new Intent(ActivityRentInProgress.this, ActivityRentEnded.class);
                        startActivity(payment);
                        finish();
                    }
                } else {
                    Intent homePage = new Intent(ActivityRentInProgress.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, "Updated Unsuccessful!", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rent_in_progress, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;
        //retrieve locally stored data
        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringVan = pref.getString(VAN_PICK, "");
        String stringDName = pref.getString(DRIVER_NAME, "");
        String stringDPhn = pref.getString(DRIVER_PHN, "");
        String stringHrs = pref.getString(NO_HOURS, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");

        vNum = findViewById(R.id.v_no);
        hours = findViewById(R.id.hours);
        remainingHours = findViewById(R.id.remaining_hrs);
        destination = findViewById(R.id.drop_hub);
        nameD = findViewById(R.id.supervisor_name);
        phone = findViewById(R.id.supervisor_phone);
        shareDetails = findViewById(R.id.share_ride_details);
        emergencyCall = findViewById(R.id.emergency);
        update = findViewById(R.id.update_data);
        scrollView = findViewById(R.id.scrollView_rent_progress);

        vNum.setText(stringVan);
        nameD.setText(stringDName);
        phone.setText(stringDPhn);
        hours.setText(stringHrs);

        shareDetails.setOnClickListener(this);
        emergencyCall.setOnClickListener(this);
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

        checkStatus();
        timeRemaining();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRentInProgress.this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_ride_details:
                selectAction(ActivityRentInProgress.this);
                break;

            case R.id.emergency:
                btnSetOnEmergency();
                break;
            case R.id.update_data:
                alertDialog();
                break;
            case R.id.drop_hub:
                Intent drop = new Intent(ActivityRentInProgress.this, HubList.class);
                drop.putExtra("Request", "destination_rental_in_progress");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(drop);
                break;
            case R.id.hours:
                LayoutInflater layoutInflater = (LayoutInflater) ActivityRentInProgress.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private void timeRemaining() {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        ActivityRentInProgress a = ActivityRentInProgress.this;
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-time-remaining");
        UtilityApiRequestPost.doPOST(a, "auth-time-remaining", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void userUpdateTrip() {
        String hour = hours.getText().toString();
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        params.put("dstid", dropID);
        params.put("hrs", hour);
        JSONObject param = new JSONObject(params);
        ActivityRentInProgress a = ActivityRentInProgress.this;
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

    public void checkStatus() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRentInProgress a = ActivityRentInProgress.this;
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

    public void btnSetOnEmergency() {
        String number = "7060743705";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private void selectAction(Context context) {
        final CharSequence[] options = {"SEND SMS", "VIDEO CALL", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("SEND SMS")) {
                    String messageBody = "TRACK MY RIDE HERE";
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setData(Uri.parse("sms:"));
                    sendIntent.putExtra("sms_body", messageBody);
                    startActivity(sendIntent);

                } else if (options[item].equals("VIDEO CALL")) {
                    Intent whatsappLaunch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    startActivity(whatsappLaunch);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

}
