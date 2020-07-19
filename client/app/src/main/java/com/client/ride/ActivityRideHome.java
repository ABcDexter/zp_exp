package com.client.ride;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ActivityRideHome extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRideHome";
    Button vehicle, riders;
    String VehicleType, RiderNo;
    ImageButton next;
    ScrollView scrollView;
    TextView reject_rq, accept_rq, dialog_txt;
    AutocompleteSupportFragment srcAutocompleteFragment, dstAutocompleteFragment;
    EditText etPlace, etDst;
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String SRC_LNG = "SrcLng";
    public static final String SRC_LAT = "SrcLat";
    public static final String DST_LAT = "DropLat";
    public static final String DST_LNG = "DropLng";
    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String RENT_RIDE = "RentRide";
    public static final String PAYMENT_MODE = "PaymentMode";
    public static final String OTP_PICK = "OTPPick";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    SharedPreferences prefAuth, prefBuss;
    String stringAuth, stringBuss, bussFlag, stringAN;
    Dialog myDialog, imageDialog, imageDialog2;
    private static ActivityRideHome instance;
    Vibrator vibrator;
    ActivityRideHome a = ActivityRideHome.this;
    Map<String, String> params = new HashMap();

    String srcLat, srcLng, dstLat, dstLng;
    String dstName ="";
    String srcName ="";

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting user-is-driver-av API
        if (id == 2) {
            prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
            stringBuss = prefBuss.getString(BUSS, "");
            String count = response.getString("count");

            if (count.equals("0")) {
                next.setEnabled(false);//the user cannot go to the next activity if vehicle not available at the hub
                if (stringBuss.equals("BussMeNot")) {
                    Log.d(TAG, "user not interested in notifications");
                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();
                } else if (stringBuss.equals("BussMe")) {
                    Intent intent = new Intent(this, UtilityPollingService.class);
                    intent.setAction("01");
                    startService(intent);
                } else
                    ShowPopup(1);
            } else {
                //next.setEnabled(true);
                Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                rideIntent.putExtra("npas", RiderNo);
                rideIntent.putExtra("vtype", VehicleType);
                startActivity(rideIntent);
                //ShowPopup(2);
                SharedPreferences pref = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(BUSS);
                editor.apply();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    public static ActivityRideHome getInstance() {
        return instance;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_home, null, false);
        frameLayout.addView(activityView);
        instance = this;
        //initializing views
        scrollView = findViewById(R.id.scrollViewRentRide);
        vehicle = findViewById(R.id.vehicle_type);
        riders = findViewById(R.id.no_riders);
        next = findViewById(R.id.letsGo_ride);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        next.setOnClickListener(this);
        vehicle.setOnClickListener(this);
        riders.setOnClickListener(this);
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);

        myDialog = new Dialog(this);
        imageDialog = new Dialog(this);
        imageDialog2 = new Dialog(this);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyD61UBJv3DR1fcTzHg3U7FgSYFz9vBX3fk");

        // Initialize the AutocompleteSupportFragment.
        srcAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_pick);

        etPlace = (EditText) srcAutocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHint("PICK UP POINT");
        etPlace.setHintTextColor(Color.parseColor("#DFDDDD"));
        etPlace.setTextColor(Color.parseColor("#DFDDDD"));
        etPlace.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etPlace.setPadding(0, 0, 150, 0);

        dstAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_drop);
        etDst = (EditText) dstAutocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etDst.setHint("DROP POINT");
        etDst.setHintTextColor(Color.parseColor("#DFDDDD"));
        etDst.setTextColor(Color.parseColor("#DFDDDD"));
        etDst.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etDst.setPadding(0, 0, 150, 0);

        // Specify the types of place data to return.
        srcAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));
        dstAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        srcAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place srcPlace) {
                srcName = srcPlace.getName();

                Log.i(TAG, "SRC Place: " + srcPlace.getName() + ", " + srcPlace.getLatLng());

                String srcLatLng = Objects.requireNonNull(srcPlace.getLatLng()).toString();
                srcLat = srcLatLng.substring(0, srcLatLng.indexOf(",")).replaceAll("[^0-9.]", "");
                srcLng = srcLatLng.substring(srcLatLng.indexOf(",") + 1).replaceAll("[^0-9.]", "");

                Log.d(TAG, "src lat: " + srcLat);
                Log.d(TAG, "src lng: " + srcLng);

                SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SRC_NAME, srcName);
                editor.putString(SRC_LAT, srcLat);
                editor.putString(SRC_LNG, srcLng);
                editor.apply();

                //storeData();
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        dstAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place dstPlace) {
                dstName = dstPlace.getName();
                Log.i(TAG, "DST Place: " + dstPlace.getName() + ", " + dstPlace.getLatLng());

                String dstLatLng = Objects.requireNonNull(dstPlace.getLatLng()).toString();
                dstLat = dstLatLng.substring(0, dstLatLng.indexOf(",")).replaceAll("[^0-9.]", "");
                dstLng = dstLatLng.substring(dstLatLng.indexOf(",") + 1).replaceAll("[^0-9.]", "");

                Log.d(TAG, "dst lat: " + dstLat);
                Log.d(TAG, "dst lng: " + dstLng);
                //storeData();
                SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(DST_NAME, dstName);
                editor.putString(DST_LAT, dstLat);
                editor.putString(DST_LNG, dstLng);
                editor.apply();
                Log.d(TAG, dstName + dstLat + dstLng);

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
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
            dialog_txt.setText("No drivers currently online.\nNotify me when available.");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 2) {
            ln.setVisibility(View.GONE);
            next.setEnabled(true);
            //TODO send push notification
            dialog_txt.setText("Drivers are available.");
            myDialog.setCanceledOnTouchOutside(true);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private void ImagePopup() {

        imageDialog.setContentView(R.layout.popup_images);
        TextView txt1 = (TextView) imageDialog.findViewById(R.id.txt1);
        TextView txt2 = (TextView) imageDialog.findViewById(R.id.txt2);
        TextView txt3 = (TextView) imageDialog.findViewById(R.id.txt3);
        RelativeLayout rl1 = (RelativeLayout) imageDialog.findViewById(R.id.rl_1);
        RelativeLayout rl2 = (RelativeLayout) imageDialog.findViewById(R.id.rl_2);
        RelativeLayout rl3 = (RelativeLayout) imageDialog.findViewById(R.id.rl_3);

        txt1.setText("E-SCOOTY");
        txt2.setText("E-BIKE");
        txt3.setText("ZBEE");

        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);

        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.show();
        imageDialog.setCanceledOnTouchOutside(true);
    }

    private void ImagePopup2() {

        imageDialog2.setContentView(R.layout.popup_images2);
        TextView txt1 = (TextView) imageDialog2.findViewById(R.id.txt1);
        TextView txt2 = (TextView) imageDialog2.findViewById(R.id.txt2);
        TextView txt3 = (TextView) imageDialog2.findViewById(R.id.txt3);
        RelativeLayout per1 = (RelativeLayout) imageDialog2.findViewById(R.id.per_1);
        RelativeLayout per2 = (RelativeLayout) imageDialog2.findViewById(R.id.per_2);

        if (vehicle.getText().toString().equals("E-BIKE")||vehicle.getText().toString().equals("E-SCOOTY")){
            per2.setVisibility(View.GONE);
        }else {
            per2.setVisibility(View.VISIBLE);
        }
        txt1.setText("1");
        txt2.setText("2");
        txt3.setText("( 1 child upto 5 years rides free )");

        per1.setOnClickListener(this);
        per2.setOnClickListener(this);

        imageDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog2.show();
        imageDialog2.setCanceledOnTouchOutside(true);
    }

    public void isDriverAv() {

        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", srcLat);
        params.put("srclng", srcLng);
        params.put("vtype", VehicleType);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + auth + " srclat" + srcLat + " srclng" + srcLng+" vtype="+VehicleType);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-is-driver-av");
        UtilityApiRequestPost.doPOST(a, "user-is-driver-av", parameters, 30000, 0, response -> {
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
            case R.id.letsGo_ride:
                //Log.d(TAG, "button clicked!");
                /*try {*/
                    if (dstName.equals("") || etDst.getText().toString().equals("") || etDst.getText().toString().equals("DROP POINT") ||
                        srcName.equals("") || etPlace.getText().toString().equals("") || etPlace.getText().toString().equals("PICK UP POINT") ||
                        vehicle.getText().toString().equals("VEHICLE TYPE") ||
                            riders.getText().toString().equals("NO OF RIDERS")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else{
                        storeData();
                        isDriverAv();


                    }
                    /*if (srcName.equals("") || etPlace.getText().toString().equals("") || etPlace.getText().toString().equals("PICK UP POINT")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }*/
                    /*if (vehicle.getText().toString().equals("VEHICLE TYPE")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    if (riders.getText().toString().equals("NO OF RIDERS")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    */
                    /*if (VehicleType.equals("") || RiderNo.equals("")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }*/
                /*} catch (Exception e) {
                    storeData();

                    Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                    rideIntent.putExtra("npas", RiderNo);
                    rideIntent.putExtra("vtype", VehicleType);
                    startActivity(rideIntent);
                    e.printStackTrace();
                }*/
                /*storeData();

                Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                rideIntent.putExtra("npas", RiderNo);
                rideIntent.putExtra("vtype", VehicleType);
                startActivity(rideIntent);*/
               /* if (VehicleType.equals("") || RiderNo.equals("") || srcName.equals("")
                        || dstName.equals("")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else*/ /*{
                    storeData();

                    Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                    rideIntent.putExtra("npas", RiderNo);
                    rideIntent.putExtra("vtype", VehicleType);
                    startActivity(rideIntent);
                }*/
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
                isDriverAv();
                myDialog.dismiss();
                break;
            case R.id.vehicle_type:
                ImagePopup();
                break;
            case R.id.no_riders:
                ImagePopup2();
                break;
            case R.id.rl_1:
                vehicle.setText("E-SCOOTY");
                imageDialog.dismiss();
                VehicleType = "1";
                break;
            case R.id.rl_2:
                vehicle.setText("E-BIKE");
                VehicleType = "2";
                imageDialog.dismiss();
                break;
            case R.id.rl_3:
                vehicle.setText("ZBEE");
                VehicleType = "3";
                imageDialog.dismiss();
                break;
            case R.id.per_1:
                riders.setText("1");
                RiderNo = "1";
                imageDialog2.dismiss();
                break;
            case R.id.per_2:
                riders.setText("2");
                RiderNo = "2";
                imageDialog2.dismiss();
                break;
        }
    }

    private void storeData() {
        SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RENT_RIDE, "0");
        editor.putString(PAYMENT_MODE, "1");
        editor.apply();
    }


}
