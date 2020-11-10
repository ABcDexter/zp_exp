package com.example.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRateClient extends ActivityDrawer implements View.OnClickListener {

    ImageButton happy,sad;
    ScrollView scrollView;
    Dialog myDialog,checkDialog;

    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    String lat, lng, str_phone, aadhar;
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String FIRST_LAUNCH = "FirstLaunch";
    public static final String DRIVER_STATUS = "DriverStatus";
    public static final String STATUS = "Status";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";
    public static final String MY_LAT = "MYSrcLAT";
    public static final String MY_LNG = "MYSrcLng";
    private static final String TAG = "ActivityRateZippe";
    String stringAuthCookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rate, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        /*Objects.requireNonNull(getSupportActionBar()).setTitle("fgh");*/
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = cookie.getString(AUTH_KEY, "");

        happy = findViewById(R.id.satisfied);
        happy.setOnClickListener(this);
        sad = findViewById(R.id.notSatisfied);
        sad.setOnClickListener(this);

        scrollView = findViewById(R.id.scrollViewRateActivity);
        myDialog = new Dialog(this);
        checkDialog = new Dialog(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.satisfied:
                ShowPopup();
                break;
            case R.id.notSatisfied:
                CheckPopup();
                break;

        }
    }
    private void ShowPopup() {
        rateTrip("", "1");
        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(R.string.thanks_from_zippe);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myDialog.dismiss();
                Intent finishIntent = new Intent(ActivityRateClient.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
            }
        }, 5000);
    }

    private void CheckPopup() {

        checkDialog.setContentView(R.layout.popup_checkbox);
        TextView cancel = (TextView) checkDialog.findViewById(R.id.cancel);
        TextView submit = (TextView) checkDialog.findViewById(R.id.submit);
        EditText specify = checkDialog.findViewById(R.id.specify);

        CheckBox chk1 = checkDialog.findViewById(R.id.attitude);
        CheckBox chk2 = checkDialog.findViewById(R.id.condition);
        CheckBox chk3 = checkDialog.findViewById(R.id.clean);
        CheckBox chk4 = checkDialog.findViewById(R.id.other);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });
        chk1.setText(R.string.att_client);
        chk2.setText(R.string.client_punctuality);
        chk3.setText(R.string.cleanliness);
        chk4.setText(R.string.any_other);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopup1();
                String str1="",str2="",str3="",str4="";
                checkDialog.dismiss();
                if (chk1.isChecked()) {
                    str1 = "Attitude of client";
                }else if (!chk1.isChecked()){
                    str1="";
                }
                if (chk2.isChecked()){
                    str2 = "Client Punctuality";
                }else if (!chk2.isChecked()){
                    str2="";
                }
                if (chk3.isChecked()){
                    str3 = "Client Cleanliness";
                }else if (!chk3.isChecked()){
                    str3="";
                }
                if (chk4.isChecked()){
                    str4 = specify.getText().toString();
                }else if (!chk4.isChecked()){
                    str4="";
                }
                rateTrip(str1+str2+str3+str4, "0");
            }
        });

        checkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkDialog.show();
        checkDialog.setCanceledOnTouchOutside(false);
    }

    private void ShowPopup1() {

        myDialog.setContentView(R.layout.popup_color);

        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(R.string.thanks_for_feedback);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);

    }

    public void rateTrip(String rev, String i) {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("rate", i);
        params.put("rev", rev);
        JSONObject parameters = new JSONObject(params);
        ActivityRateClient a = ActivityRateClient.this;
        Log.d(TAG, "Values: auth=" + auth+ " rate="+i+" rev="+rev);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-rate");
        UtilityApiRequestPost.doPOST(a, "auth-trip-rate", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting auth-trip-rate API
        if (id == 1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myDialog.dismiss();
                    Intent finishIntent = new Intent(ActivityRateClient.this, ActivityHome.class);
                    startActivity(finishIntent);
                    finish();
                }
            }, 5000);

        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

}
