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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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


public class ActivityRideHome extends ActivityDrawer implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "ActivityRideHome";
    Spinner vehicle, riders;
    String VehicleType, RiderNo;
    ImageButton next;
    ScrollView scrollView;
    TextView reject_rq, accept_rq, dialog_txt;

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
    Dialog myDialog;
    private static ActivityRideHome instance;
    Vibrator vibrator;
    ActivityRideHome a = ActivityRideHome.this;
    Map<String, String> params = new HashMap();

    String srcName, srcLat, srcLng, dstName, dstLat, dstLng;

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
                next.setEnabled(true);
                ShowPopup(2);
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

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.vehicle_array_sans_cycle)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.WHITE);
                    tv.setBackgroundColor(Color.DKGRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item_orange);
        vehicle.setAdapter(adapter);
        vehicle.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapterNoRiders = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.rider_no_array)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0 && position != 3;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0 || position == 3) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.WHITE);
                    tv.setBackgroundColor(Color.DKGRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        adapterNoRiders.setDropDownViewResource(R.layout.spinner_item_blue);
        riders.setAdapter(adapterNoRiders);
        riders.setOnItemSelectedListener(this);

        myDialog = new Dialog(this);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyD61UBJv3DR1fcTzHg3U7FgSYFz9vBX3fk");

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment srcAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_pick);
        srcAutocompleteFragment.setHint("PICK UP POINT");

        AutocompleteSupportFragment dstAutocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_drop);
        dstAutocompleteFragment.setHint("DROP POINT");

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

                isDriverAv();
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
            dialog_txt.setText("no ride available currently.\nNotify me when available.");
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

    public void isDriverAv() {

        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", srcLat);
        params.put("srclng", srcLng);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + auth + " srclat" + srcLat + " srclng" + srcLng);
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
                Log.d(TAG, "button clicked!");
                if (VehicleType.equals("VEHICLE TYPE") || RiderNo.equals("NO OF RIDERS ?") || srcName.equals("PICK UP POINT")
                        || dstName.equals("DROP POINT")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    storeData();

                    Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                    rideIntent.putExtra("npas", RiderNo);
                    rideIntent.putExtra("vtype", VehicleType);
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
                isDriverAv();
                myDialog.dismiss();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.vehicle_type:
                VehicleType = vehicle.getItemAtPosition(position).toString();
                switch (VehicleType) {
                    case "E-SCOOTY":
                        VehicleType = "1";
                        break;
                    case "E-BIKE":
                        VehicleType = "2";
                        break;
                    case "ZBEE":
                        VehicleType = "3";
                        break;
                }
                break;
            case R.id.no_riders:
                RiderNo = riders.getItemAtPosition(position).toString();
                switch (RiderNo) {
                    case "1":
                        RiderNo = "1";
                        break;
                    case "2":
                        RiderNo = "2";
                        break;
                }
                break;
        }
    }

    //auto generated method for AdapterView.OnItemSelectedListener
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
