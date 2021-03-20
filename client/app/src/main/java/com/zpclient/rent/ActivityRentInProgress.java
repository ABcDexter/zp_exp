package com.zpclient.rent;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
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
import com.zpclient.R;
import com.zpclient.UtilityApiRequestPost;
import com.zpclient.UtilityPollingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRentInProgress extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRentInProgress";
    TextView emergencyCall, destination, nameD, phone, hours, vNum, remainingHours, shareLocation;
    ImageButton endRent;
    ScrollView scrollView;
    RelativeLayout rlPhone;
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    public static final String NO_HOURS = "NoHours";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static ActivityRentInProgress instance;
    FusedLocationProviderClient mFusedLocationClient;
    Dialog myDialog, colorDialog;
    private static final String PUBLISHABLE_KEY = "shXqLCv6GJVJ9QFgdHb6VL0JzE_7X96YoAX3ZxA919DLWOA1fayXhLg_NguIvRNypeaSpLu4U6JlYiwJahN8pA";
    String deviceId;
    String locationUrl;
    String stringAuthCookie;
    ImageView supPhoto;

    ActivityRentInProgress a = ActivityRentInProgress.this;
    Map<String, String> params = new HashMap();
    public static ActivityRentInProgress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

       /* //response on hitting auth-time-remaining API
        if (id == 1) {
            String time = response.getString("time");
            remainingHours.setText(time);

            int t = Integer.parseInt(time);
            if (time.equals("10")) {
                PopupColor(1);
            }
            if (time.equals("5")) {
                PopupColor(2);
            }
            if (time.equals("0")) {
                ShowPopup(2);
            }
            if (t < 0) {
                t = -t;
                txt.setText(R.string.extended_min);
                String timePositive = Integer.toString(t);
                remainingHours.setText(timePositive);
                if (timePositive.equals("60")) {
                    PopupColor(3);
                }
                if (timePositive.equals("120")) {
                    ShowPopup(3);
                }
                if (timePositive.equals("180")) {
                    ShowPopup(3);
                }
                if (timePositive.equals("240")) {
                    ShowPopup(3);
                }
                if (timePositive.equals("300")) {
                    ShowPopup(3);
                }
            }

        }*/

        //response on hitting user-rent-get-sup API
        if (id == 2) {
            String name = response.getString("name");
            String phn = response.getString("pn");
            String photo = response.getString("photourl");

            nameD.setText(name);
            phone.setText(phn);
            Glide.with(this).load(photo).into(supPhoto);
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
                        String vno = response.getString("vno");
                        vNum.setText(vno);
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("14");
                        startService(intent);
                        try {
                            String time = response.getString("time");
                            remainingHours.setText(time);
                            int t = Integer.parseInt(time);
                            if (time.equals("10")) {
                                PopupColor(1);
                            }
                            if (time.equals("5")) {
                                PopupColor(2);
                            }
                            if (time.equals("0")) {
                                ShowPopup(2);
                            }
                            if (t < 0) {
                                t = -t;
                                txt.setText(R.string.extended_min);
                                String timePositive = Integer.toString(t);
                                remainingHours.setText(timePositive);
                                if (timePositive.equals("60")) {
                                    PopupColor(3);
                                }
                                if (timePositive.equals("120")) {
                                    ShowPopup(3);
                                }
                                if (timePositive.equals("180")) {
                                    ShowPopup(3);
                                }
                                if (timePositive.equals("240")) {
                                    ShowPopup(3);
                                }
                                if (timePositive.equals("300")) {
                                    ShowPopup(3);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    if (status.equals("FN") || status.equals("TR")) {
                        String price = response.getString("price");
                        if (price.equals("0.00")) {
                            Intent rate = new Intent(ActivityRentInProgress.this, ActivityRateRent.class);
                            startActivity(rate);
                            finish();
                        } else {
                            Intent payment = new Intent(ActivityRentInProgress.this, ActivityRentEnded.class);
                            startActivity(payment);
                            finish();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting user-trip-track API
        if (id == 4) {
            try {
                locationUrl = response.getString("hurl");
                String messageBody = "Track my live location here:\n" + locationUrl;
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", messageBody);
                startActivity(sendIntent);

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

    TextView txt;

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
        String stringHrs = pref.getString(NO_HOURS, "");
        String stringVan = pref.getString(VAN_PICK, "");
        SharedPreferences tripPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String stringDName = tripPref.getString(DRIVER_NAME, "");
        String stringDPhn = tripPref.getString(DRIVER_PHN, "");
        shareLocation = findViewById(R.id.share_location);
        shareLocation.setOnClickListener(this);

        supPhoto = findViewById(R.id.photo_sup);
        vNum = findViewById(R.id.v_no);
        hours = findViewById(R.id.hours);
        txt = findViewById(R.id.txt);
        remainingHours = findViewById(R.id.remaining_hrs);
        destination = findViewById(R.id.drop_hub);
        nameD = findViewById(R.id.supervisor_name);
        phone = findViewById(R.id.supervisor_phone);
        emergencyCall = findViewById(R.id.emergency);
        endRent = findViewById(R.id.end_rent);
        scrollView = findViewById(R.id.scrollView_rent_progress);
        rlPhone = findViewById(R.id.rl_p);
        rlPhone.setOnClickListener(this);
        nameD.setText(stringDName);
        phone.setText(stringDPhn);
        emergencyCall.setOnClickListener(this);
        endRent.setOnClickListener(this);
        destination.setOnClickListener(this);
        hours.setOnClickListener(this);
        phone.setOnClickListener(this);

        checkStatus();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRentInProgress.this);
        myDialog = new Dialog(this);
        colorDialog = new Dialog(this);

        getDropSup();
        getDeviceID();
    }
    private void getDeviceID() {
        HyperTrack sdkInstance = HyperTrack
                .getInstance(PUBLISHABLE_KEY);

        //deviceId.setText(sdkInstance.getDeviceID());
        Log.d(TAG, "device id is " + sdkInstance.getDeviceID());
        deviceId = sdkInstance.getDeviceID();
    }
    private void getLocationUrl() {
        String stringAuth = stringAuthCookie;
        params.put("auth", stringAuth);
        params.put("devid", deviceId);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-track");
        UtilityApiRequestPost.doPOST(a, "user-trip-track", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        if (id == 2) {
            infoText.setText(R.string.time_extended);
        }

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(true);
    }


    private void PopupColor(int id) {

        colorDialog.setContentView(R.layout.popup_color);
        TextView infoText = colorDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText(R.string.return_veh);
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(true);

        }
        if (id == 2) {
            infoText.setText(R.string.return_veh);
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(true);

        }
        if (id == 3) {
            infoText.setText(R.string.time_extended_by_hour);
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(true);
        }
        colorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = colorDialog.getWindow().getAttributes();

        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        colorDialog.show();
        Window window = colorDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        colorDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_p) {
            callSuper();
        } else if(id==R.id.supervisor_phone){
            callSuper();
        } else if (id == R.id.emergency) {
            btnSetOnEmergency();
        } else if (id == R.id.share_location) {
            getLocationUrl();
        } else if (id == R.id.drop_hub) {
            Intent drop = new Intent(ActivityRentInProgress.this, ActivityUpdateInfo.class);
            startActivity(drop);
        } else if (id == R.id.hours) {
            Intent hour = new Intent(ActivityRentInProgress.this, ActivityUpdateHours.class);
            startActivity(hour);
        } else if (id == R.id.end_rent) {
            Intent nearest = new Intent(ActivityRentInProgress.this, ActivityNearestHub.class);
            startActivity(nearest);
        }
    }

    private void getDropSup() {
        String stringAuth = stringAuthCookie;
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rent-get-sup");
        UtilityApiRequestPost.doPOST(a, "user-rent-get-sup", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void checkStatus() {
        String auth = stringAuthCookie;
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
        /*Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
        String number = "7777777777";*/
        Uri call = Uri.parse("tel:" + number);
        Intent surf = new Intent(Intent.ACTION_DIAL, call);
        startActivity(surf);
    }

    public void callSuper() {
        /*Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneSuper));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);*/
        String phoneSuper = phone.getText().toString().trim();
        Uri call = Uri.parse("tel:" + phoneSuper);
        Intent surf = new Intent(Intent.ACTION_DIAL, call);
        startActivity(surf);
    }

}
