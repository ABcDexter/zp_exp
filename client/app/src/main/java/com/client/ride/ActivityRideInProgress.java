package com.client.ride;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityTrackYourProgress;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideInProgress extends ActivityDrawer implements View.OnClickListener {

    TextView shareDetails, emergencyCall, trackLocation, origin, destination, nameD, phone, otp, vNum;
    ImageButton endRide;
    ScrollView scrollView;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    private static final String TAG = "ActivityRideInProgress";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    String lat, lng;
    private static ActivityRideInProgress instance;
    FusedLocationProviderClient mFusedLocationClient;
    ActivityRideInProgress a = ActivityRideInProgress.this;

    String stringAuthCookie;

    public static ActivityRideInProgress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws Exception {
        Log.d(TAG, "RESPONSE:" + response);
//response on hitting user-trip-cancel API
        if (id == 2) {
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
                        intent.setAction("04");
                        startService(intent);
                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        Intent payment = new Intent(ActivityRideInProgress.this, ActivityRideEnded.class);
                        startActivity(payment);
                        finish();
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideInProgress.this, ActivityWelcome.class);
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_in_progress, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;


        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = pref.getString(LOCATION_PICK, "");
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringOtp = pref.getString(OTP_PICK, "");
        String stringVan = pref.getString(VAN_PICK, "");
        String stringDName = pref.getString(DRIVER_NAME, "");
        String stringDPhn = pref.getString(DRIVER_PHN, "");

        origin = findViewById(R.id.pick_place);
        vNum = findViewById(R.id.v_no);
        vNum.setText(stringVan);
        destination = findViewById(R.id.drop_place);
        nameD = findViewById(R.id.driver_name);
        nameD.setText(stringDName);
        phone = findViewById(R.id.driver_phone);
        phone.setText(stringDPhn);
        otp = findViewById(R.id.otp_ride);
        otp.setText(stringOtp);
        shareDetails = findViewById(R.id.share_ride_details);
        shareDetails.setOnClickListener(this);
        emergencyCall = findViewById(R.id.emergency);
        emergencyCall.setOnClickListener(this);
        trackLocation = findViewById(R.id.track_your_location);
        trackLocation.setOnClickListener(this);
        endRide = findViewById(R.id.end_ride);
        endRide.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollView_ride_OTP);

        if (stringDrop.isEmpty()) {
            destination.setText("DROP POINT");
        } else {
            int dropSpace = (stringDrop.contains(" ")) ? stringDrop.indexOf(" ") : stringDrop.length() - 1;
            String dropCutName = stringDrop.substring(0, dropSpace);
            destination.setText(dropCutName);
        }

        if (stringPick.isEmpty()) {
            origin.setText("PICK UP");
        } else {
            int pickSpace = (stringPick.contains(" ")) ? stringPick.indexOf(" ") : stringPick.length() - 1;
            String pickCutName = stringPick.substring(0, pickSpace);
            origin.setText(pickCutName);
        }
        checkStatus();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRideInProgress.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_ride_details:
                selectAction(ActivityRideInProgress.this);
                break;

            case R.id.emergency:
                btnSetOnEmergency();
                break;
            case R.id.end_ride:
                alertDialog();
                break;
            case R.id.track_your_location:
                trackYourLocation();
                break;
        }
    }

    private void trackYourLocation() {
        Intent progress = new Intent(ActivityRideInProgress.this, ActivityTrackYourProgress.class);
        startActivity(progress);
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

    private void alertDialog() {
        Log.d(TAG, " alert Dialog opened");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("YOU MAY BE CHARGED FOR CANCELLING THE RIDE. \nARE YOU SURE YOU WANT TO END RIDE?");
        dialog.setTitle("END RIDE");
        dialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        userCancelTrip();
                        Log.d(TAG, "checkStatus invoked");
                    }
                });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "RIDE NOT ENDED", Toast.LENGTH_LONG).show();
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
