package com.deliverpartner;

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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityHome extends ActivityDrawer implements View.OnClickListener {
    Dialog myDialog;
    Switch status_duty;
    String driverStatus = "";
    private static final String TAG = "ActivityHome";
    ScrollView scrollView;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    FusedLocationProviderClient mFusedLocationClient;
    TextView newOrder, inProgress, completedOrder, totalEarnings;
    String lat, lng, str_phone, aadhar;
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String FIRST_LAUNCH = "FirstLaunch";
    public static final String DRIVER_STATUS = "DriverStatus";
    public static final String STATUS = "Status";

    String stringBuss, bussFlag;
    SharedPreferences prefBuss;
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    Vibrator vibrator;

    private static ActivityHome instance;
    String auth;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_home, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        instance = this;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);// initializing vibration service for this activity

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        auth = cookie.getString(COOKIE, ""); // retrieve auth value stored locally and assign it to String auth
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
        stringBuss = prefBuss.getString(BUSS, ""); // retrieve buss value stored locally and assign it to String stringBuss
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        aadhar = sharedPreferences.getString(AADHAR, "");// retrieve aadhaar value stored locally and assign it to String aadhar

        //initializing variables
        status_duty = findViewById(R.id.dutyStatus);
        scrollView = findViewById(R.id.scrollLayout);
        newOrder = findViewById(R.id.new_order);
        inProgress = findViewById(R.id.order_in_progress);
        completedOrder = findViewById(R.id.completed_orders);
        totalEarnings = findViewById(R.id.earnings);

        newOrder.setOnClickListener(this);
        inProgress.setOnClickListener(this);
        completedOrder.setOnClickListener(this);
        totalEarnings.setOnClickListener(this);

        myDialog = new Dialog(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityHome.this);// needed for gsp tracking

        status_duty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    driverStatus = "OnDuty";
                    //store the value of driver status (AV) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply(); // storing driver status locally for later use
                    Log.d(TAG, "driverStatus = " + driverStatus);
                    getLastLocation();// method for getting the last know latitude and longitude of driver
                } else {
                    driverStatus = "OffDuty";
                    //store the value of driver status (OF) in Shared Preference
                    SharedPreferences sp_cookie = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(STATUS, driverStatus).apply();
                    ShowPopup(2);
                    Log.d(TAG, "driverStatus = " + driverStatus);
                }
                //driverSetMode();// method fro hitting driver-set-mode API
            }
        });
        firstLaunch(); // method to check id the app is launched for the first time in the day
        Snackbar snackbar = Snackbar
                .make(scrollView, "NO NEW RIDE CURRENTLY AVAILABLE !", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        ShowPopup(1);
        //enter this only if this activity is called from ActivityRideAccepted
        try {
            //this means that the driver canceled ride after initially accepting the ride
            Intent rideAccept = getIntent();
            String cancel = rideAccept.getStringExtra("CANCEL");
            assert cancel != null;
            if (!cancel.isEmpty())
                status_duty.setChecked(false);
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

    }

    //method to check if the app is launched for the first time in the day
    private void firstLaunch() {
        SharedPreferences sharedPref = getSharedPreferences(FIRST_LAUNCH, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDate = sdf.format(new Date());
        if (sharedPref.getString("LAST_LAUNCH_DATE", "nodate").contains(currentDate)) {
            // Date matches. User has already Launched the app once today.
            SharedPreferences pref = getSharedPreferences(DRIVER_STATUS, Context.MODE_PRIVATE);
            String stringStatus = pref.getString(STATUS, "");
            if (stringStatus.equals("OnDuty")) {
                status_duty.setChecked(true);
            }
            if (!stringStatus.equals("OnDuty")) {
                status_duty.setChecked(false);
            }

        } else {
            status_duty.setChecked(false);
            // Set the last Launched date to today.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("LAST_LAUNCH_DATE", currentDate);
            editor.apply();
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor1 = preferences.edit();
            editor1.clear();
            editor1.apply();
        }
    }

    //method to initiate and populate dialog box
    private void ShowPopup(int id) {
        myDialog.setContentView(R.layout.popup_text);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        TextView reject_rq = myDialog.findViewById(R.id.reject_request);
        TextView accept_rq = myDialog.findViewById(R.id.accept_request);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            //vibrate the device for 1000 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
            ln.setVisibility(View.VISIBLE);
            infoText.setText("no ride available currently.\nNotify me when available.");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);// dialog box will not be dismissed if screen outside the box is touched

        }
        if (id == 2) {
            infoText.setText("YOU ARE OFFLINE ! ");
        }
        if (id == 3) {
            infoText.setText("YOU ARE CURRENTLY LOCKED ! \nCONTACT ADMIN");
        }
        if (id == 4) {
            infoText.setText("TRIP FAILED !");
        }
        if (id == 6) {
            infoText.setText("Rides are Available");
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);// dialog box will be dismissed if screen outside the box is touched
    }

    public static ActivityHome getInstance() {
        return instance;
    }

    //method to check if permissions are granted to the app by the driver
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

    //method to check and record the last know location of the driver
    public void getLastLocation() {
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
                                //sendLocation();// method to hit auth-location-update API
                            }
                        }
                    });
        }
    }

    //gps tracking with high accuracy. This means that the location is checked every 1 millisecond
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    // with every change in location this method is called
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else
            getLastLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reject_request:
                bussFlag = "BussMeNot";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                Log.d(TAG, "User not interested in a buss");
                //showAlertDialog();
                myDialog.dismiss();
                break;
            case R.id.accept_request:
                bussFlag = "BussMe";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                myDialog.dismiss();
                //driverRideCheck();
                break;
            case R.id.new_order:
                Intent newOrderIntent = new Intent(ActivityHome.this, ActivityNewOrders.class);
                startActivity(newOrderIntent);
                break;
            case R.id.order_in_progress:
                Intent inProgressIntent = new Intent(ActivityHome.this, ActivityInProgress.class);
                startActivity(inProgressIntent);
                break;
            case R.id.completed_orders:
                Intent completedOrderIntent = new Intent(ActivityHome.this, ActivityCompletedOrders.class);
                startActivity(completedOrderIntent);
                break;
            case R.id.earnings:
                Intent earningsIntent = new Intent(ActivityHome.this, ActivityTotalEarnings.class);
                startActivity(earningsIntent);
                break;
        }
    }
}
