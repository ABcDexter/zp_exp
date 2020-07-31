package com.client.rent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.HubList;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRentHome extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityRentHome";
    Button vehicle, hours;
    ImageButton confirmRentButton;
    TextView reject_rq, accept_rq, dialog_txt, pick, drop, scheduleRent;
    Dialog myDialog, imageDialog, imageDialog2;

    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String RENT_RIDE = "RentRide";
    public static final String PAYMENT_MODE = "PaymentMode";
    public static final String VEHICLE_TYPE = "VehicleType";
    public static final String NO_HOURS = "NoHours";

    SharedPreferences prefAuth, prefBuss;
    ScrollView scrollView;
    private static ActivityRentHome instance;
    Vibrator vibrator;

    String RentRide, PaymentMode;
    String NoHours = "";
    String VehicleType = "";
    String lat, lng;
    String stringAuth, stringBuss, bussFlag, stringAN;
    String Rent, pMode, pickID, dropID, pickPoint, dropPoint;
    String imgBtnConfirm = "";

    public void onSuccess(JSONObject response, int id) throws JSONException {

        if (id == 2) {
            Log.d(TAG + "jsArrayRequest", "RESPONSE:" + response.toString());
            prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
            stringBuss = prefBuss.getString(BUSS, "");
            String responseS = response.toString();
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("vehicles");
            if (array.length() == 0) {
                confirmRentButton.setEnabled(false);//the user cannot go to the next activity if vehicle not available at the hub
                imgBtnConfirm = "false";
                Log.d(TAG, "confirmRentButton.setEnabled(false)");
                if (stringBuss.equals("BussMeNot")) {
                    Log.d(TAG, "user not interested in notifications");
                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();
                    Toast.makeText(this, "No Vehicles Available Currently!", Toast.LENGTH_LONG).show();
                } else if (stringBuss.equals("BussMe")) {
                    Intent intent = new Intent(this, UtilityPollingService.class);
                    intent.setAction("11");
                    startService(intent);
                } else
                    ShowPopup(1);
            } else if (array.length() > 0) {
                //ShowPopup(2);
                SharedPreferences pref = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(BUSS);
                editor.apply();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject vehicle = array.getJSONObject(i);
                    String an = vehicle.getString("an");
                }
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    public static ActivityRentHome getInstance() {
        return instance;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rent_home, null, false);
        frameLayout.addView(activityView);
        instance = this;
        //initializing views
        scrollView = findViewById(R.id.scrollViewRentRide);
        vehicle = findViewById(R.id.vehicle_type);
        vehicle.setOnClickListener(this);
        hours = findViewById(R.id.no_hours);
        hours.setOnClickListener(this);
        confirmRentButton = findViewById(R.id.confirm_rent);
        pick = findViewById(R.id.txt_pick_hub);
        drop = findViewById(R.id.txt_drop_point);
        scheduleRent = findViewById(R.id.schedule_rent);
        scheduleRent.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        confirmRentButton.setOnClickListener(this);
        pick.setOnClickListener(this);
        drop.setOnClickListener(this);
        //retrieving locally stored data
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = pref.getString(LOCATION_PICK, "");
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");
        String stringPickID = pref.getString(LOCATION_PICK_ID, "");
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
        pMode = pref.getString(PAYMENT_MODE, "");

        if (imgBtnConfirm.equals("false")) {
            Log.d(TAG, "confirmRentButton.setEnabled(false)");
        } else
            Log.d(TAG, "confirmRentButton.setEnabled(true)");
        if (stringPick.isEmpty()) {
            pick.setText("PICK UP POINT");
            Log.d(TAG, "Pick Location  is " + stringPick);
        } else {
            pick.setText(stringPick);
            pickPoint = pick.getText().toString();
            pickID = stringPickID;
            Log.d(TAG, "Pick Location  is " + stringPick + " ID is " + stringPickID);
        }
        if (stringDrop.isEmpty()) {
            drop.setText("DROP POINT");
            Log.d(TAG, "Drop Location  is " + stringDrop);
        } else {
            drop.setText(stringDrop);
            dropPoint = drop.getText().toString();
            dropID = stringDropID;
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
        }

        myDialog = new Dialog(this);
        getAvailableVehicle();

        imageDialog = new Dialog(this);
        imageDialog2 = new Dialog(this);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        reject_rq = myDialog.findViewById(R.id.reject_request);
        accept_rq = myDialog.findViewById(R.id.accept_request);
        dialog_txt = myDialog.findViewById(R.id.info_text);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
            ln.setVisibility(View.VISIBLE);
            dialog_txt.setText("no ride available currently.\nNotify me when available.");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 2) {
            //TODO send push notification
            //dialog_txt.setText("Rides are available.");

        }
        if (id == 3) {
            dialog_txt.setText("This feature shall be active soon.");

            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

            //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            //wmlp.x = 100;   //x position
            wmlp.y = 77;   //y position
            myDialog.show();
            Window window = myDialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            myDialog.setCanceledOnTouchOutside(true);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void getAvailableVehicle() {
        Map<String, String> params = new HashMap();
        String auth = stringAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRentHome a = ActivityRentHome.this;

        Log.d(TAG, "Values: auth=" + params);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-vehicle-get-avail");
        UtilityApiRequestPost.doPOST(a, "auth-vehicle-get-avail", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vehicle_type:
                ImagePopup();
                break;
            case R.id.no_hours:
                ImagePopup2();
                break;
            case R.id.confirm_rent:
                Log.d(TAG, "confirm_rent button clicked!");
                if (/*RentRide == null ||*/ VehicleType.equals("") || NoHours.equals("") || pick.getText().equals("PICK UP POINT")
                        || drop.getText().equals("DROP POINT")/*|| PaymentMode == null*/) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    Log.d(TAG, "empty field: vehicle:" + VehicleType + " " + "rent ride: " +
                            RentRide + " " + "No of riders: " + NoHours + " " + "payment Mode: " +
                            PaymentMode);
                } else {
                    storeData();
                    Intent rideIntent = new Intent(ActivityRentHome.this, ActivityRentRequest.class);

                    Log.d(TAG, "vehicle:" + VehicleType + " " + "rent ride: " +
                            RentRide + " " + "No of riders: " + NoHours + " " + "payment Mode: " +
                            PaymentMode + "srcid:" + pickID + "dstid:" + dropID);
                    startActivity(rideIntent);
                }
                break;

            case R.id.reject_request:
                bussFlag = "BussMeNot";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                Log.d(TAG, "User not interested in a buss");
                myDialog.dismiss();
                break;
            case R.id.accept_request:
                bussFlag = "BussMe";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                getAvailableVehicle();
                myDialog.dismiss();
                break;
            case R.id.schedule_rent:
                ShowPopup(3);
                break;
            case R.id.txt_pick_hub:
                Intent pick = new Intent(ActivityRentHome.this, HubList.class);
                pick.putExtra("Request", "pick_rent");
                Log.d(TAG, "control moved to HUBLIST activity with key pick_rent");
                startActivity(pick);
                break;
            case R.id.txt_drop_point:
                Intent drop = new Intent(ActivityRentHome.this, HubList.class);
                drop.putExtra("Request", "destination_rental");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(drop);
                break;
            case R.id.txt1:
                NoHours = "1";
                imageDialog2.dismiss();
                hours.setText("1 hr (60 mins) @ ₹ 1.00 / min");
                break;
            case R.id.txt2:
                NoHours = "2";
                imageDialog2.dismiss();
                hours.setText("2 hrs (120 mins) @ ₹ 0.95 / min");
                break;
            case R.id.txt3:
                NoHours = "3";
                imageDialog2.dismiss();
                hours.setText("3 hrs (180 mins) @ ₹ 0.90 / min");
                break;
            case R.id.txt4:
                NoHours = "4";
                imageDialog2.dismiss();
                hours.setText("4 hrs (240 mins) @ ₹ 0.85 / min");
                break;
            case R.id.txt5:
                NoHours = "5";
                imageDialog2.dismiss();
                hours.setText("5 hrs (300 mins) @ ₹ 0.80 / min");
                break;
            case R.id.txt6:
                NoHours = "6";
                imageDialog2.dismiss();
                hours.setText("6 hrs (360 mins) @ ₹ 0.75 / min");
                break;
            case R.id.txt7:
                NoHours = "7";
                imageDialog2.dismiss();
                hours.setText("7 hrs (420 mins) @ ₹ 0.70 / min");
                break;
            case R.id.txt8:
                NoHours = "8";
                imageDialog2.dismiss();
                hours.setText("8 hrs (480 mins) @ ₹ 0.65 / min");
                break;
            case R.id.txt9:
                NoHours = "9";
                imageDialog2.dismiss();
                hours.setText("9 hrs (540 mins) @ ₹ 0.60 / min");
                break;
            case R.id.txt10:
                NoHours = "10";
                imageDialog2.dismiss();
                hours.setText("10 hrs (600 mins) @ ₹ 0.55 / min");
                break;
            case R.id.txt11:
                NoHours = "12";
                imageDialog2.dismiss();
                hours.setText("11 hrs (660 mins) @ ₹ 0.50 / min");
                break;
            case R.id.rent_rl_1:
                VehicleType = "0";
                imageDialog.dismiss();
                vehicle.setText("E-CYCLE");
                break;
            case R.id.rent_rl_2:
                VehicleType = "1";
                imageDialog.dismiss();
                vehicle.setText("E-SCOOTY");
                break;
            case R.id.rent_rl_3:
                VehicleType = "2";
                imageDialog.dismiss();
                vehicle.setText("E-BIKE");
                break;
        }
    }

    private void storeData() {
        SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RENT_RIDE, "1");
        editor.putString(PAYMENT_MODE, "1");
        editor.putString(NO_HOURS, NoHours);
        editor.putString(VEHICLE_TYPE, VehicleType);
        editor.apply();
        Log.d(TAG, "vehicle:" + VehicleType + " " + "rent ride: " +
                RentRide + " " + "No of riders: " + NoHours + " " + "payment Mode: " +
                PaymentMode + "srcid:" + pickID + "dstid:" + dropID);
    }

    private void ImagePopup() {

        imageDialog.setContentView(R.layout.popup_rent_vehicles);
        TextView txt1 = (TextView) imageDialog.findViewById(R.id.txt1);
        TextView txt2 = (TextView) imageDialog.findViewById(R.id.txt2);
        TextView txt3 = (TextView) imageDialog.findViewById(R.id.txt3);
        RelativeLayout rl1 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_1);
        RelativeLayout rl2 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_2);
        RelativeLayout rl3 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_3);

        txt1.setText("E-CYCLE");
        txt2.setText("E-SCOOTY");
        txt3.setText("E-BIKE");

        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);

        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = imageDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        imageDialog.show();
        Window window = imageDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        imageDialog.setCanceledOnTouchOutside(true);
    }

    SpannableStringBuilder spannableStringBuilder;
    String txt_3;

    private void ImagePopup2() {

        imageDialog2.setContentView(R.layout.popup_hours);
        TextView txt1 = (TextView) imageDialog2.findViewById(R.id.txt1);
        TextView txt2 = (TextView) imageDialog2.findViewById(R.id.txt2);
        TextView txt3 = (TextView) imageDialog2.findViewById(R.id.txt3);
        TextView txt4 = (TextView) imageDialog2.findViewById(R.id.txt4);
        TextView txt5 = (TextView) imageDialog2.findViewById(R.id.txt5);
        TextView txt6 = (TextView) imageDialog2.findViewById(R.id.txt6);
        TextView txt7 = (TextView) imageDialog2.findViewById(R.id.txt7);
        TextView txt8 = (TextView) imageDialog2.findViewById(R.id.txt8);
        TextView txt9 = (TextView) imageDialog2.findViewById(R.id.txt9);
        TextView txt10 = (TextView) imageDialog2.findViewById(R.id.txt10);
        TextView txt11 = (TextView) imageDialog2.findViewById(R.id.txt11);
        TextView txt12 = (TextView) imageDialog2.findViewById(R.id.txt12);

       // txt1.setText(getText(R.string._1_hr));
        txt1.setOnClickListener(this);
        txt2.setOnClickListener(this);
        txt3.setOnClickListener(this);
        txt4.setOnClickListener(this);
        txt5.setOnClickListener(this);
        txt6.setOnClickListener(this);
        txt7.setOnClickListener(this);
        txt8.setOnClickListener(this);
        txt9.setOnClickListener(this);
        txt10.setOnClickListener(this);
        txt11.setOnClickListener(this);
        txt12.setOnClickListener(this);

        imageDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = imageDialog2.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 0;   //y position

        imageDialog2.show();
        Window window = imageDialog2.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        imageDialog2.setCanceledOnTouchOutside(true);
    }

}

