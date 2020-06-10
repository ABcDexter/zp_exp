package com.example.driver;

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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideAccepted extends ActivityDrawer implements View.OnClickListener {
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    private static final String TAG = "ActivityRideAccepted";
    public static final String TRIP_NAME = "TripName";
    public static final String TRIP_SRC = "TripSrc";
    public static final String TRIP_DST = "TripDst";
    public static final String TRIP_PHN = "TripPhn";
    TextView origin, destination, phone, name;
    EditText OTP;
    Button startRide, cancleRideBtn;
    String authCookie;
    Dialog myDialog;
    private static ActivityRideAccepted instance;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        if (id == 1) {
            Log.d(TAG, "response ");
        }
        if (id == 3) {
            //return to the home activity
            Intent home = new Intent(ActivityRideAccepted.this, ActivityHome.class);
            home.putExtra("CANCEL", "cancel");
            startActivity(home);
            finish();
        }
        if (id == 4) {
            String active = response.getString("active");
            if (active.equals("false")) {
                //return to home activity
                ShowPopup(1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent home = new Intent(ActivityRideAccepted.this, ActivityHome.class);
                        startActivity(home);
                        finish();
                    }
                }, 20000);


            } else if (active.equals("true")) {
                String tid = response.getString("tid");
                String st = response.getString("st");
                if (st.equals("ST")) {
                    //go on to the next activity
                    Intent inProgress = new Intent(ActivityRideAccepted.this, ActivityRideInProgress.class);
                    startActivity(inProgress);
                    finish();
                }
            }
            //start polling for driver-ride-get-status
            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("4");
            startService(i);
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
        View activityView = layoutInflater.inflate(R.layout.activity_ride_accepted, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        instance = this;
        //retrieve data stored locally
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        authCookie = cookie.getString(COOKIE, "");
        SharedPreferences sharedPreferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String picLoc = sharedPreferences.getString(TRIP_SRC, "");
        String dropLoc = sharedPreferences.getString(TRIP_DST, "");
        String strPhone = sharedPreferences.getString(TRIP_PHN, "");
        String strName = sharedPreferences.getString(TRIP_NAME, "");

        //initializing variables
        origin = findViewById(R.id.userSrc);
        destination = findViewById(R.id.userDst);
        phone = findViewById(R.id.userPhone);
        name = findViewById(R.id.userName);
        OTP = findViewById(R.id.otp);
        cancleRideBtn = findViewById(R.id.btn_cancelRide);
        startRide = findViewById(R.id.btn_startRide);
        myDialog = new Dialog(this);

        phone.setText(strPhone);
        name.setText(strName);
        destination.setText(dropLoc);
        origin.setText(picLoc);

        phone.setOnClickListener(this);
        cancleRideBtn.setOnClickListener(this);
        startRide.setOnClickListener(this);
        rideStatus();//method to check the status of current ride

    }

    //method to check the status of current ride
    protected void rideStatus() {
        Map<String, String> params = new HashMap();
        params.put("auth", authCookie);
        JSONObject parameters = new JSONObject(params);
        ActivityRideAccepted a = ActivityRideAccepted.this;

        Log.d(TAG, "Values: auth=" + authCookie);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    public static ActivityRideAccepted getInstance() {
        return instance;
    }

    //method to initiate and populate dialog box
    private void ShowPopup(int id) {
        myDialog.setContentView(R.layout.popup_text);
        TextView reject_rq = myDialog.findViewById(R.id.reject_request);
        TextView accept_rq = myDialog.findViewById(R.id.accept_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        //called when the user cancels the ride
        if (id == 1) {
            infoText.setText("TRIP HAS BEEN CANCELED BY USER ! ");
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(true);
        }
        //called when driver cancels the ride
        if (id == 2) {
            ln.setVisibility(View.VISIBLE);
            infoText.setText("YOU WILL BE MARKED OFFLINE !\nCONTINUE? ");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(false);
        }
    }

    //method to check permissions granted by driver to the app
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_startRide:
                String checkOtp = OTP.getText().toString();
                if (!checkOtp.isEmpty()) {
                    driverStartRide(checkOtp);//method to check if the OTP entered is correct or not
                } else
                    OTP.requestFocus();// if OTP field is empty, then driverStartTrip method will not be called
                break;
            case R.id.btn_cancelRide:
                ShowPopup(2);// dialog box with message to confirm if the driver wants to end ride
                break;
            case R.id.accept_request:
                driverRideCancel();// method to cancel ride by hit driver-cancel-ride API
                break;
            case R.id.reject_request:
                myDialog.dismiss();
                break;
            case R.id.userPhone:
                //method to call the user
                if (hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phone.getText().toString()));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(callIntent);
                }
                break;
        }
    }

    // method to cancel ride by hitting driver-ride-cancel
    private void driverRideCancel() {
        String auth = authCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideAccepted a = ActivityRideAccepted.this;

        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-cancel");
        UtilityApiRequestPost.doPOST(a, "driver-ride-cancel", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 3);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    //method to start ride by hitting driver-ride-start API
    private void driverStartRide(String otp) {

        String auth = authCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("otp", otp);
        JSONObject parameters = new JSONObject(params);
        ActivityRideAccepted a = ActivityRideAccepted.this;

        Log.d(TAG, "Values: auth=" + auth + " otp=" + otp);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-start");
        UtilityApiRequestPost.doPOST(a, "driver-ride-start", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }
}
