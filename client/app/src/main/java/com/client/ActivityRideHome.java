package com.client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ride.HubList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideHome extends ActivityDrawer implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "ActivityRideHome";
    Spinner vehicle, riders;
    String VehicleType, RentRide, RiderNo , PaymentMode;
    ImageButton next;
    ScrollView scrollView;
    TextView reject_rq;
    TextView accept_rq;
    TextView dialog_txt;
    TextView pick;
    TextView drop;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
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
    SharedPreferences prefAuth, prefBuss;
    String stringAuth, stringBuss, bussFlag, stringAN;
    Dialog myDialog;
    private static ActivityRideHome instance;
    String lat, lng;
    Vibrator vibrator;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};
    String Rent, pMode, pickID, dropID, pickPoint, dropPoint;

    public void onSuccess(JSONObject response, int id) throws JSONException {
        if (id == 1) {
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("00");
            startService(i);
            //getAvailableVehicle();
        }
        if (id == 2) {
            Log.d(TAG + "jsArrayRequest", "RESPONSE:" + response.toString());
            prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
            stringBuss = prefBuss.getString(BUSS, "");
            String responseS = response.toString();
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("vehicles");
            if (array.length() == 0) {
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
            } else if(array.length() > 0) {
                ShowPopup(2);
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
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = pref.getString(LOCATION_PICK, "");
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");
        String stringPickID = pref.getString(LOCATION_PICK_ID, "");

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityRideHome.this);
        getLastLocation();
        Log.d(TAG, "below getLastLocation() call");

        scrollView = findViewById(R.id.scrollViewRentRide);
        vehicle = findViewById(R.id.vehicle_type);
        riders = findViewById(R.id.no_riders);
        next = findViewById(R.id.letsGo_ride);
        next.setOnClickListener(this);

        pick = findViewById(R.id.txt_pick_point);
        pick.setOnClickListener(this);
        drop = findViewById(R.id.txt_drop_point);
        drop.setOnClickListener(this);
        Rent = pref.getString(RENT_RIDE, "");
        pMode = pref.getString(PAYMENT_MODE, "");

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
                return position != 0 && position!=3;
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
        getAvailableVehicle();
    }

    public void getLastLocation() {
        Log.d(TAG, "Inside getLastLocation()");
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d(TAG, "inside else of getLastLocation()");
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                Log.d(TAG, "inside else of addOnCompleteListener()");
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
                                sendLocation();
                            }
                        }
                    });
        }
    }

    protected void sendLocation() {

        Log.d(TAG, "inside sendLocation()");
        Map<String, String> params = new HashMap();
        params.put("an", stringAN);
        params.put("auth", stringAuth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);
        ActivityRideHome a = ActivityRideHome.this;

        Log.d(TAG, "auth = " + stringAuth + " lat =" + lat + " lng = " + lng + " an=" + stringAN);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        Log.d(TAG, "inside hasPermission()");
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private void requestNewLocationData() {
        Log.d(TAG, "inside requestNewLocationData()");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "inside LocationResult() call");
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };

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
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 2) {
            //TODO send push notification
            dialog_txt.setText("Rides are available.");
            myDialog.setCanceledOnTouchOutside(true);

        }
    }

    protected void getAvailableVehicle() {
        Map<String, String> params = new HashMap();
        String auth = (stringAuth);
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRideHome a = ActivityRideHome.this;

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
            case R.id.letsGo_ride:
                Log.d(TAG, "button clicked!");
                if (/*RentRide == null ||*/ VehicleType.equals("VEHICLE TYPE")|| RiderNo.equals("NO OF RIDERS ?")|| pick.getText().equals("PICK UP POINT")
                        || drop.getText().equals("DROP POINT")/*|| PaymentMode == null*/) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    Log.d(TAG, "empty field: vehicle:" + VehicleType + " " + "rent ride: " +
                            RentRide + " " + "No of riders: " + RiderNo + " " + "payment Mode: " +
                            PaymentMode);
                } else {
                    Log.d(TAG, "control in letsGo_ride");
                    storeData();
                    Intent rideIntent = new Intent(ActivityRideHome.this, ActivityRideRequest.class);
                    rideIntent.putExtra("rtype", "1");
                    rideIntent.putExtra("npas", RiderNo);
                    rideIntent.putExtra("srcid", pickID);
                    rideIntent.putExtra("dstid", dropID);
                    rideIntent.putExtra("vtype", VehicleType);
                    rideIntent.putExtra("pmode", "1");
                    rideIntent.putExtra("pick", pick.getText().toString());
                    rideIntent.putExtra("drop", drop.getText().toString());

                    Log.d(TAG, "vehicle:" + VehicleType + " " + "rent ride: " +
                            RentRide + " " + "No of riders: " + RiderNo + " " + "payment Mode: " +
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

            case R.id.txt_pick_point:
                Intent pick = new Intent(ActivityRideHome.this, HubList.class);
                pick.putExtra("Request", "origin");
                Log.d(TAG, "control moved to HUBLIST activity with key origin");
                startActivity(pick);
                break;
            case R.id.txt_drop_point:
                Intent drop = new Intent(ActivityRideHome.this, HubList.class);
                drop.putExtra("Request", "destination");
                Log.d(TAG, "control moved to HUBLIST activity with key destination");

                startActivity(drop);
                break;

        }
    }

    private void storeData() {
        SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RENT_RIDE, "1");
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}
