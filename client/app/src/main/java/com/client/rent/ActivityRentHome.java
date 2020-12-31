package com.client.rent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
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
    public static final String TRIP_ID = "TripID";
    public static final String VAN_PICK = "VanPick";

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
    Map<String, String> params = new HashMap();
    ActivityRentHome a = ActivityRentHome.this;

    public void onSuccess(JSONObject response, int id) throws JSONException {

        //response on hitting auth-location-update API
        if (id == 0) {
            /*Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("00");
            startService(i);*/

        }
//response on hitting user-trip-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String rtype = response.getString("rtype");
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (rtype.equals("1")) {
                        if (status.equals("RQ")) {
                            Intent rq = new Intent(ActivityRentHome.this, ActivityRentRequest.class);
                            startActivity(rq);
                        }
                        if (status.equals("AS")) {
                            /*String otp = response.getString("otp");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
                            Intent as = new Intent(ActivityRentHome.this, ActivityRentOTP.class);
                            startActivity(as);
                        }
                        if (status.equals("ST")) {
                            String van = response.getString("vno");
                            SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                            sp_otp.edit().putString(VAN_PICK, van).apply();
                            Intent as = new Intent(ActivityRentHome.this, ActivityRentInProgress.class);
                            startActivity(as);
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            //retireTrip();
                            /*Intent fntr = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                            startActivity(fntr);*/
                            Intent fntr = new Intent(ActivityRentHome.this, ActivityRentEnded.class);
                            startActivity(fntr);
                        }

                        if (status.equals("TO")) {
                            ShowPopup(7);
                        }

                    }
                } else {
                    Log.d(TAG, "active=" + active);
                    try {
                        String tid = response.getString("tid");
                        if (!tid.equals("-1")) {
                            Intent rateFirst = new Intent(ActivityRentHome.this, ActivityRateRent.class);
                            startActivity(rateFirst);
                        } /*else {
                            tripInfo(tid);
                        }*/
                    } catch (Exception e) {
                        Log.d(TAG, " tid does not exist");
                        e.printStackTrace();
                        //getAvailableVehicle();
                    }

                    SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    String tripIDExists = prefTripDetails.getString(TRIP_ID, "");
                    if (!tripIDExists.equals("")) {
                        /*Intent homePage = new Intent(ActivityWelcome.this, ActivityRideHome.class);
                        homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homePage);
                        finish();*/
                        tripInfo(tripIDExists);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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
                    Toast.makeText(this, R.string.no_vehicle_av, Toast.LENGTH_LONG).show();
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
                Intent rideIntent = new Intent(ActivityRentHome.this, ActivityRentRequest.class);
                startActivity(rideIntent);

            }
        }

        // response on hitting auth-trip-get-info API
        if (id == 3) {
            String st = response.getString("st");
            String rtype = response.getString("rtype");
            if (rtype.equals("1")) {
                if (st.equals("PD")) {
                    //String price = response.getString("price");

                    /*SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(LOCATION_PICK);
                    editor.remove(LOCATION_DROP);
                    editor.remove(LOCATION_DROP_ID);
                    editor.remove(LOCATION_PICK_ID);
                    editor.apply();

                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();*/
                    retireTrip();
                }
                if (st.equals("FN")) {
                    retireTrip();
                }
                if (st.equals("CN")) {
                    retireTrip();
                }
                retireTrip();

            }
            //retireTrip();

        }

        //response on hitting user-trip-retire API
        if (id == 4) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            SharedPreferences prefLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editorLoc = prefLoc.edit();
            editorLoc.remove(VAN_PICK);
            editorLoc.apply();
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
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
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        scrollView = findViewById(R.id.scrollViewRentRide);
        vehicle = findViewById(R.id.vehicle_type);
        hours = findViewById(R.id.no_hours);
        confirmRentButton = findViewById(R.id.confirm_rent);
        pick = findViewById(R.id.txt_pick_hub);
        drop = findViewById(R.id.txt_drop_point);
        scheduleRent = findViewById(R.id.schedule_rent);

        scheduleRent.setOnClickListener(this);
        vehicle.setOnClickListener(this);
        hours.setOnClickListener(this);
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
        } else {
            Log.d(TAG, "confirmRentButton.setEnabled(true)");
        }
        if (stringPick.isEmpty()) {
            pick.setText(R.string.pick_up_point_z_hub);
            pick.setBackgroundResource(R.drawable.rect_box_outline);
            Log.d(TAG, "Pick Location  is " + stringPick);
        } else {
            pick.setText(stringPick);
            pickPoint = pick.getText().toString();
            pick.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            pickID = stringPickID;
            Log.d(TAG, "Pick Location  is " + stringPick + " ID is " + stringPickID);
        }
        if (stringDrop.isEmpty()) {
            drop.setText(R.string.drop_point_z_hub);
            drop.setBackgroundResource(R.drawable.rect_box_outline);
            Log.d(TAG, "Drop Location  is " + stringDrop);
        } else {
            drop.setText(stringDrop);
            dropPoint = drop.getText().toString();
            drop.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            dropID = stringDropID;
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
        }

        myDialog = new Dialog(this);
        //getAvailableVehicle();

        imageDialog = new Dialog(this);
        imageDialog2 = new Dialog(this);

        sendLocation();

        checkStatus();

    }
    public void sendLocation() {
        Log.d(TAG, "inside sendLocation()");
        params.put("an", stringAN);
        params.put("auth", stringAuth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + stringAuth + " lat =" + lat + " lng = " + lng + " an=" + stringAN);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }
    private void dlAlert() {
        // Create the object of
        // AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRentHome.this);
        // Set the message show for the Alert time
        builder.setMessage(R.string.driving_licence);
        // Set Alert Title
        builder.setTitle(R.string.please_note);
        // Set Cancelable false
        // for when the user clicks on the outside
        // the Dialog Box then it will remain show
        builder.setCancelable(false);
        // Set the positive button with ok name
        // OnClickListener method is use of
        // DialogInterface interface.
        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeData();
                getAvailableVehicle();
                dialog.cancel();
            }
        });

        // Set the Negative button with No name
        // OnClickListener method is use
        // of DialogInterface interface.
        builder.setNegativeButton(R.string.disagree, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user click cancel
                // then user goes to the previous window
                Intent back = new Intent(ActivityRentHome.this, ActivityWelcome.class);
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
            dialog_txt.setText(R.string.coming_soon);

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
        String auth = stringAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);

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
        int id = v.getId();
        if (id == R.id.vehicle_type) {
            ImagePopup();
        } else if (id == R.id.no_hours) {
            ImagePopup2();
        } else if (id == R.id.confirm_rent) {
            Log.d(TAG, "confirm_rent button clicked!");
            if (/*RentRide == null ||*/ VehicleType.equals("") || NoHours.equals("") || pick.getText().equals("PICK UP POINT / Z-HUB")
                    || drop.getText().equals("DROP POINT / Z-HUB")/*|| PaymentMode == null*/) {
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
                dlAlert();//method to alert user to provide valid driving licence at the hub before picking up the vehicle
                    /*storeData();
                    getAvailableVehicle();*/
                    /*Intent rideIntent = new Intent(ActivityRentHome.this, ActivityRentRequest.class);
                    startActivity(rideIntent);*/
                Log.d(TAG, "vehicle:" + VehicleType + " " + "rent ride: " +
                        RentRide + " " + "No of riders: " + NoHours + " " + "payment Mode: " +
                        PaymentMode + "srcid:" + pickID + "dstid:" + dropID);
            }
        } else if (id == R.id.reject_request) {
            bussFlag = "BussMeNot";
            prefBuss.edit().putString(BUSS, bussFlag).apply();
            Log.d(TAG, "User not interested in a buss");
            myDialog.dismiss();
        } else if (id == R.id.accept_request) {
            bussFlag = "BussMe";
            prefBuss.edit().putString(BUSS, bussFlag).apply();
            getAvailableVehicle();
            myDialog.dismiss();
        } else if (id == R.id.schedule_rent) {
            ShowPopup(3);
        } else if (id == R.id.txt_pick_hub) {
            Intent pick = new Intent(ActivityRentHome.this, HubList.class);
            pick.putExtra("Request", "pick_rent");
            Log.d(TAG, "control moved to HUBLIST activity with key pick_rent");
            startActivity(pick);
        } else if (id == R.id.txt_drop_point) {
            Intent drop = new Intent(ActivityRentHome.this, HubList.class);
            drop.putExtra("Request", "destination_rental");
            Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
            startActivity(drop);
                /*case R.id.txt1:
                NoHours = "1";
                imageDialog2.dismiss();
                hours.setText("1 hr (60 mins) @ ₹ 1.00 / min");
                break;*/
        } else if (id == R.id.txt2) {
            NoHours = "2";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._2_hr);
        } else if (id == R.id.txt3) {
            NoHours = "3";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._3_hr);
        } else if (id == R.id.txt4) {
            NoHours = "4";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._4_hr);
        } else if (id == R.id.txt5) {
            NoHours = "5";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._5_hr);
        } else if (id == R.id.txt6) {
            NoHours = "6";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._6_hr);
        } else if (id == R.id.txt7) {
            NoHours = "7";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._7_hr);
        } else if (id == R.id.txt8) {
            NoHours = "8";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._8_hr);
        } else if (id == R.id.txt9) {
            NoHours = "9";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._9_hr);
        } else if (id == R.id.txt10) {
            NoHours = "10";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._10_hr);
        } else if (id == R.id.txt11) {
            NoHours = "11";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._11_hr);
        } else if (id == R.id.txt12) {
            NoHours = "12";
            imageDialog2.dismiss();
            hours.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            hours.setText(R.string._12_hr);
        } else if (id == R.id.rent_rl_1) {
            VehicleType = "0";
            imageDialog.dismiss();
            vehicle.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            vehicle.setText(R.string.e_cycle);
        } else if (id == R.id.rent_rl_2) {
            VehicleType = "1";
            imageDialog.dismiss();
            vehicle.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            vehicle.setText(R.string.e_scooty);
        } else if (id == R.id.rent_rl_3) {
            VehicleType = "2";
            imageDialog.dismiss();
            vehicle.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            vehicle.setText(R.string.e_bike);
        }
    }

    public void checkStatus() {
        String auth = stringAuth;
        params.put("auth", stringAuth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void tripInfo(String tripID) {

        params.put("auth", stringAuth);
        params.put("tid", tripID);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void retireTrip() {

        params.put("auth", stringAuth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-retire");
        UtilityApiRequestPost.doPOST(a, "user-trip-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
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

        txt1.setText(R.string.e_cycle);
        txt2.setText(R.string.e_scooty);
        txt3.setText(R.string.e_bike);

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
        //TextView txt1 = (TextView) imageDialog2.findViewById(R.id.txt1);
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
        //txt1.setOnClickListener(this);
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

