package com.clientzp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
<<<<<<< HEAD
import com.clientzp.deliver.ActivityPackageDetails;
import com.clientzp.rent.ActivityRateRent;
import com.clientzp.rent.ActivityRentEnded;
import com.clientzp.rent.ActivityRentHome;
import com.clientzp.rent.ActivityRentOTP;
import com.clientzp.ride.ActivityRideEnded;
import com.clientzp.ride.ActivityRideHome;
import com.clientzp.ride.ActivityRideInProgress;
import com.clientzp.ride.ActivityRideOTP;
import com.clientzp.ride.ActivitySearchingDriver;
=======
import com.clientzp.rent.ActivityRentOTP;
>>>>>>> dev
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
<<<<<<< HEAD
=======
import com.clientzp.deliver.ActivityPackageDetails;
import com.clientzp.rent.ActivityRateRent;
import com.clientzp.rent.ActivityRentEnded;
import com.clientzp.rent.ActivityRentHome;
import com.clientzp.ride.ActivityRideEnded;
import com.clientzp.ride.ActivityRideHome;
import com.clientzp.ride.ActivityRideInProgress;
import com.clientzp.ride.ActivityRideOTP;
import com.clientzp.ride.ActivitySearchingDriver;
>>>>>>> dev

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityWelcome extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityWelcome";

    public static final String PREFS_LOCATIONS = "com.clientzp.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    public static final String SRC_NAME = "PICK UP POINT";
    public static final String DST_NAME = "DROP POINT";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";

    private static ActivityWelcome instance;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    ActivityWelcome a = ActivityWelcome.this;
    Map<String, String> params = new HashMap();

    SharedPreferences prefAuth, sharedPreferences1;
    FusedLocationProviderClient mFusedLocationClient;
    String lat, lng, stringAN, stringAuth, auth;
    ImageButton btnRent, btnRide, btnDeliver, /*btnShop, btnConnect,*/
            infoRide, infoRent, infoDelivery/*, infoShop, infoService*/;
    ImageView zippe_iv, zippe_iv_below, scooty_up, scooty_down;
    Animation animMoveL2R, animMoveR2L;
    private TextView textHelp, dialog_txt;
    private RelativeLayout rlOverlay, rlTopLayout;
    private LinearLayout llRide, llRent, llDelivery, /*llShop, llServices,*/
            llInfo;
    SharedPreferences.Editor sharedEditor1;
    public static boolean isAppRunning;
    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // the method is responsible for populating the activity.
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_welcome, null, false);
        frameLayout.addView(activityView);
        instance = this;
        //retrieve value of auth stored locally
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        //initializing all variables
        btnRent = findViewById(R.id.btn_rent);
        btnRide = findViewById(R.id.btn_ride);
        btnDeliver = findViewById(R.id.btn_deliver);
        /*btnShop = findViewById(R.id.btn_shop);
        btnConnect = findViewById(R.id.btn_connect);*/
        textHelp = findViewById(R.id.textHelp);
        rlOverlay = findViewById(R.id.rlOverlay);
        rlTopLayout = findViewById(R.id.rlTopLayout);
        llRide = findViewById(R.id.llRide);
        llRent = findViewById(R.id.llRent);
        llDelivery = findViewById(R.id.llDelivery);
        /*llShop = findViewById(R.id.llShop);
        llServices = findViewById(R.id.llServices);*/
        llInfo = findViewById(R.id.llInfo);
        infoRide = findViewById(R.id.infoRideBtn);
        infoRent = findViewById(R.id.infoRentBtn);
        infoDelivery = findViewById(R.id.infoDeliveyBtn);
        /*infoShop = findViewById(R.id.infoShopBtn);
        infoService = findViewById(R.id.infoServiceBtn);*/
        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);
        scooty_up = findViewById(R.id.scooty_up);
        scooty_down = findViewById(R.id.scooty_down);

        //making variables clickable
        btnRent.setOnClickListener(this);
        btnRide.setOnClickListener(this);
        btnDeliver.setOnClickListener(this);
        /*btnShop.setOnClickListener(this);
        btnConnect.setOnClickListener(this);*/
        textHelp.setOnClickListener(this);
        infoRide.setOnClickListener(this);
        infoRent.setOnClickListener(this);
        infoDelivery.setOnClickListener(this);
        /*infoShop.setOnClickListener(this);
        infoService.setOnClickListener(this);*/

        auth = stringAuth;
        //checking if auth is stored locally or not
<<<<<<< HEAD
        /*if (auth.equals("")) {
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityLoginKey.class);
            startActivity(registerUser);
            finish();
        }*/if (auth.equals("")) {
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityRegistration.class);
            startActivity(registerUser);
            finish();
        }
=======
        if (auth.equals("")) {
            Intent registerUser = new Intent( ActivityWelcome.this, ActivityLoginKey.class);
            startActivity(registerUser);
            finish();
        }/*if (auth.equals("")) {
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityRegistration.class);
            startActivity(registerUser);
            finish();
        }*/
>>>>>>> dev
        sharedPreferences1 = getPreferences(Context.MODE_PRIVATE);
        sharedEditor1 = sharedPreferences1.edit();
        //checking if app is being run for the 1st time or not
        if (isItFirstTime()) {
<<<<<<< HEAD
            //Log.d(TAG, "First Time");
=======
            Log.d(TAG, "First Time");
>>>>>>> dev
            rlOverlay.setVisibility(View.VISIBLE);

        } else {
            rlOverlay.setVisibility(View.GONE);
<<<<<<< HEAD
            //Log.d(TAG, "Not a First Time");
=======
            Log.d(TAG, "Not a First Time");
>>>>>>> dev
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityWelcome.this);
        getLastLocation();// get the last location coordinates
        checkStatus(); // hitting API user-trip-get-status
        myDialog = new Dialog(this); //initializing dialog to be populated when required

        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);
        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);

        zippe_iv.startAnimation(animMoveL2R);
        scooty_down.startAnimation(animMoveR2L);
        //getting firebase instance ID. This is required for push notifications
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(ActivityWelcome.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken", newToken);
            }
        });

    }

    /**
     * this method is used to store boolean value for judging if the app is being run for the first time or not.
     */
    public boolean isItFirstTime() {
        if (sharedPreferences1.getBoolean("firstTime", true)) {
            sharedEditor1.putBoolean("firstTime", false);
            sharedEditor1.commit();
            sharedEditor1.apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This is creating an instance of ActivityWelcome class
     */
    public static ActivityWelcome getInstance() {
        return instance;
    }

    /**
     * This method is used for hitting auth-location-update API on the server
     * It sends 1) an: Aadhaar number for User
     * 2,3) lat,lng: location
     * 4) auth: user auth token as parameters
     * It receives empty string {} as response.
     */
    public void sendLocation() {
<<<<<<< HEAD
        //Log.d(TAG, "inside sendLocation()");
=======
        Log.d(TAG, "inside sendLocation()");
>>>>>>> dev
        params.put("an", stringAN);
        params.put("auth", stringAuth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);

<<<<<<< HEAD
        /*Log.d(TAG, "auth = " + stringAuth + " lat =" + lat + " lng = " + lng + " an=" + stringAN);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");*/
=======
        Log.d(TAG, "auth = " + stringAuth + " lat =" + lat + " lng = " + lng + " an=" + stringAN);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-location-update");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "auth-location-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    /**
     * This method is used for hitting user-trip-get-status API on the server
     * Sends  1) auth: user auth token as parameters
     * This must be polled continuously by the user app to detect any state change
     * after a ride request is made
     * Receives:
     * active(bool): Whether a trip is in progress
     * status(str): Trip status
     * tid(str): trip ID
     * For each of the following statuses, additional data is returned:
     * AS: otp, dan, van
     * ST: progress (percent)
     * TR, FN: price, time (seconds), dist (meters), speed (m/s average)
     * Note: If active is false, no other data is returned
     */
    public void checkStatus() {

        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
<<<<<<< HEAD
        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");*/
=======
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    /**
     * This shows a popup with respective information when called
     */
    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_color);
        dialog_txt = myDialog.findViewById(R.id.info_text);
        if (id == 1) {
            dialog_txt.setText("No ride available currently.\nNotify me when available.");
        }
        if (id == 2) {
            dialog_txt.setText(R.string.drivers_available);
        }
        if (id == 3) {
            dialog_txt.setText(R.string.ride_cancelled_by_you);
        }
        if (id == 4) {
            dialog_txt.setText(R.string.ride_timed_out);

        }
        if (id == 5) {
            dialog_txt.setText(R.string.unable_to_complete_your_ride);
        }
        if (id == 6) {
            dialog_txt.setText(R.string.ride_canceled_by_you);
        }
        if (id == 7) {
            dialog_txt.setText(R.string.vehicles_rented_out);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * This method is called when the app is installed for the 1st time on a phone.
     * This is responsible for showing a tutorial to the user.
     * i.e. what happens on click each button. And how to navigate the app.
     */

    private void ShowInfo(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        LinearLayout ll = myDialog.findViewById(R.id.layout_btn);
        ll.setVisibility(View.GONE);
        if (id == 1) {
            infoText.setText(R.string.to_ride_click);
        }
        if (id == 2) {
            infoText.setText(R.string.to_rent_click);
        }
        if (id == 3) {
            infoText.setText(R.string.to_deliver_click);
        }
        /*if (id == 4) {
            infoText.setText(R.string.to_shop_click);
        }
        if (id == 5) {
            infoText.setText(R.string.to_service_click);
        }*/

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * This method checks 1) if the user has given location permission or not
     * 2) gets the lng and lat of current location
     * 3) requestNewLocationData()
     * 4) for calling sendLocation()
     * if there is no change in location then sendLocation() is called else requestNewLocationData() is called
     */
    public void getLastLocation() {
<<<<<<< HEAD
        //Log.d(TAG, "Inside getLastLocation()");
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            //Log.d(TAG, "inside else of getLastLocation()");
=======
        Log.d(TAG, "Inside getLastLocation()");
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d(TAG, "inside else of getLastLocation()");
>>>>>>> dev
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
<<<<<<< HEAD
                                //Log.d(TAG, "inside else of addOnCompleteListener()");
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                //Log.d(TAG, "lat = " + lat + " lng = " + lng);
=======
                                Log.d(TAG, "inside else of addOnCompleteListener()");
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
>>>>>>> dev
                                sendLocation();
                            }
                        }
                    });
        }
    }

    /**
     * Checks if all permissions
     * 1) Location
     * 2) Phone Call have be granted by user or not
     */
    public static boolean hasPermissions(Context context, String... permissions) {
<<<<<<< HEAD
        //Log.d(TAG, "inside hasPermission()");
=======
        Log.d(TAG, "inside hasPermission()");
>>>>>>> dev
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if location permissions have been granted by user
     * calls LocationRequest()
     */
    private void requestNewLocationData() {
<<<<<<< HEAD
        //Log.d(TAG, "inside requestNewLocationData()");
=======
        Log.d(TAG, "inside requestNewLocationData()");
>>>>>>> dev
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    /**
     * Returns lat lng of the device
     */
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
<<<<<<< HEAD
            //Log.d(TAG, "inside LocationResult() call");
=======
            Log.d(TAG, "inside LocationResult() call");
>>>>>>> dev
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };

    /**
     * Receives the response from the server
     */
    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
<<<<<<< HEAD
        //Log.d(TAG, "RESPONSE:" + response);
=======
        Log.d(TAG, "RESPONSE:" + response);
>>>>>>> dev
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
                    if (rtype.equals("0")) {
                        if (status.equals("RQ")) {
                            //Intent rq = new Intent(ActivityWelcome.this, ActivityRideRequest.class);
                            Intent rq = new Intent(ActivityWelcome.this, ActivitySearchingDriver.class);
                            //rq.putExtra("st", "RQ");
                            startActivity(rq);
                        }
                        if (status.equals("AS")) {

                            Intent as = new Intent(ActivityWelcome.this, ActivityRideOTP.class);
                            startActivity(as);
                        }
                        if (status.equals("ST")) {
                            Intent as = new Intent(ActivityWelcome.this, ActivityRideInProgress.class);
                            startActivity(as);
                        }
                        if (status.equals("FN") || status.equals("TR")) {

                            Intent fntr = new Intent(ActivityWelcome.this, ActivityRideEnded.class);
                            startActivity(fntr);
                        }
                        if (status.equals("CN")) {
<<<<<<< HEAD
                            //Log.d(TAG, "trip cancelled");
                        }
                        if (status.equals("TO") || status.equals("DN") || status.equals("FL")) {
                            //Log.d(TAG, "error");
=======
                            Log.d(TAG, "trip cancelled");
                        }
                        if (status.equals("TO") || status.equals("DN") || status.equals("FL")) {
                            Log.d(TAG, "error");
>>>>>>> dev
                            //retireTrip();
                        }
                    }

                    if (rtype.equals("1")) {
                        if (status.equals("RQ")) {
                            //Intent rq = new Intent(ActivityWelcome.this, ActivityRideRequest.class);
                            Intent rq = new Intent(ActivityWelcome.this, ActivityRentOTP.class);
                            //rq.putExtra("st", "RQ");
                            startActivity(rq);
                        }
                        if (status.equals("FN") || status.equals("TR")) {
                            String price = response.getString("price");
                            if (price.equals("0.00")) {
                                Intent rate = new Intent(ActivityWelcome.this, ActivityRateRent.class);
                                startActivity(rate);
                                finish();
                            } else {
                                Intent payment = new Intent(ActivityWelcome.this, ActivityRentEnded.class);
                                startActivity(payment);
                                finish();
                            }
                        }
                    }

                } else if (active.equals("false")) {
                    String tid = response.getString("tid");
<<<<<<< HEAD
                    //Log.d(TAG, "active=" + active + " tid=" + tid);
=======
                    Log.d(TAG, "active=" + active + " tid=" + tid);
>>>>>>> dev


                    if (!tid.equals("-1")) {
                        tripInfo(tid);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // response on hitting auth-trip-get-info API
        if (id == 2) {
            String st = response.getString("st");
            String rtype = response.getString("rtype");
            if (rtype.equals("0")) {
                if (st.equals("PD")) {
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(PREFS_LOCATIONS);
                    editor.apply();
                    Intent home = new Intent(ActivityWelcome.this, ActivityRateZippe.class);
                    startActivity(home);
                    finish();
                }

                if (st.equals("DN")) {
                    ShowPopup(3);
                    retireTrip();
                }
                if (st.equals("TO")) {
                    ShowPopup(4);
                    retireTrip();
                }
                if (st.equals("FL")) {
                    ShowPopup(5);
                    retireTrip();
                }
                if (st.equals("CN")) {
                    ShowPopup(6);
                    retireTrip();
                }
                //retireTrip();
            }
            if (rtype.equals("1")) {
                if (st.equals("PD")) {
                    //String price = response.getString("price");

                    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(LOCATION_PICK);
                    editor.remove(LOCATION_DROP);
                    editor.remove(LOCATION_DROP_ID);
                    editor.remove(LOCATION_PICK_ID);
                    editor.apply();

                    retireTrip();
                }
                if (st.equals("FN")) {
                    retireTrip();
                }
                if (st.equals("CN")) {
                    retireTrip();
                }
                //retireTrip();

            }
            //retireTrip();

        }
        //response on hitting user-trip-retire API
        if (id == 3) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

<<<<<<< HEAD
            //Log.d(TAG, "tripID= " + TRIP_ID + DRIVER_NAME + DRIVER_PHN);

            SharedPreferences prefLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editorLoc = prefLoc.edit();
=======
            Log.d(TAG, "tripID= " + TRIP_ID + DRIVER_NAME + DRIVER_PHN);

            SharedPreferences prefLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editorLoc = prefLoc.edit();
            //editorLoc.remove(VAN_PICK);
>>>>>>> dev
            editorLoc.remove(DST_NAME);
            editorLoc.remove(SRC_NAME);
            //editorLoc.remove(OTP_PICK);
            editorLoc.apply();

            sendLocation();
        }

        //response on hitting auth-location-update API
        if (id == 4) {
            //empty response from server
        }
    }

    /**
     * Called when there is error in response from server
     */
    public void onFailure(VolleyError error) {
<<<<<<< HEAD
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
=======
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
>>>>>>> dev
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    /**
     * Used for hitting user-trip-retire API on the server
     * Sends 1)  auth: user auth token as parameters
     * The response Resets users active trip
     * This is called when the user has seen the message pertaining to trip end for these states:
     * 'TO', 'DN', 'PD',
     * Following states do not need user to retire trip
     * CN : user has already retired in userRideCancel()
     * FL : admin retires this via adminHandleFailedTrip()
     * TR/FN : Driver will retire via driverConfirmPayment() after user pays money
     */
    private void retireTrip() {

        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
<<<<<<< HEAD
        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-retire");*/
=======
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-retire");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "user-trip-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    /**
     * Used for hitting auth-trip-get-info API on the server
     * Sends 1) auth: user auth token as parameters
     * 2) tid: trip id for user
     * Response: trip info for this user for any past or current trip
     */
    private void tripInfo(String tripID) {

        params.put("auth", auth);
        params.put("tid", tripID);
        JSONObject parameters = new JSONObject(params);
<<<<<<< HEAD
        /*Log.d(TAG, "Values: auth=" + auth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");*/
=======
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tripID);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }


    int counter = 1;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_rent) {
            //take user to ActivityRentHome activity
            Intent rentIntent = new Intent(ActivityWelcome.this, ActivityRentHome.class);
            startActivity(rentIntent);
        } else if (id == R.id.btn_ride) {
            //take user to ActivityRideHome activity
            Intent rideIntent = new Intent(ActivityWelcome.this, ActivityRideHome.class);
            startActivity(rideIntent);
        } else if (id == R.id.btn_deliver) {
            //take user to ActivityPackageDetails activity
            Intent deliverIntent = new Intent(ActivityWelcome.this, ActivityPackageDetails.class);
            startActivity(deliverIntent);
        } /*else if (id == R.id.btn_shop) {
            //take user to https://zippe.in/en/shop-by-category/ url
           *//* String shopUrl = "https://zippe.in/en/shop-by-category/";
            Intent shopIntent = new Intent(Intent.ACTION_VIEW);
            shopIntent.setData(Uri.parse(shopUrl));
            startActivity(shopIntent);*//*
            Intent shop = new Intent(ActivityWelcome.this, ActivityShopHome.class);
            startActivity(shop);

        } else if (id == R.id.btn_connect) {
            //take user to https://zippe.in/en/zippe-connect/ url
            //String connectUrl = "https://zippe.in/en/zippe-connect/";
           *//* String connectUrl = "https://zippe.in/service-categories/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);*//*
            Intent service = new Intent(ActivityWelcome.this, ActivityServicesHome.class);
            startActivity(service);
        }*/ else if (id == R.id.textHelp) {
            //this displays the instructions for fist time users.
<<<<<<< HEAD
            //Log.d(TAG, "counter = " + counter);
=======
            Log.d(TAG, "counter = " + counter);
>>>>>>> dev
            //counter++;
            if (counter == 1) {
                llRide.setVisibility(View.INVISIBLE);
                llRent.setVisibility(View.VISIBLE);
                llDelivery.setVisibility(View.INVISIBLE);
                /*llShop.setVisibility(View.INVISIBLE);
                llServices.setVisibility(View.INVISIBLE);*/
                llInfo.setVisibility(View.INVISIBLE);
                rlOverlay.setVisibility(View.VISIBLE);

                textHelp.setText(R.string.next);
                counter = counter + 1;
<<<<<<< HEAD
                //Log.d(TAG, "counter = " + counter);
=======
                Log.d(TAG, "counter = " + counter);
>>>>>>> dev

            } else if (counter == 2) {
                llRide.setVisibility(View.INVISIBLE);
                llRent.setVisibility(View.INVISIBLE);
                llDelivery.setVisibility(View.VISIBLE);
                /*llShop.setVisibility(View.INVISIBLE);
                llServices.setVisibility(View.INVISIBLE);*/
                llInfo.setVisibility(View.INVISIBLE);
                rlOverlay.setVisibility(View.VISIBLE);

                textHelp.setText(R.string.next);
                counter = counter + 1;
            } else if (counter == 3) {
                llRide.setVisibility(View.INVISIBLE);
                llRent.setVisibility(View.INVISIBLE);
                llDelivery.setVisibility(View.INVISIBLE);
                /*llShop.setVisibility(View.VISIBLE);
                llServices.setVisibility(View.INVISIBLE);*/
                llInfo.setVisibility(View.INVISIBLE);
                rlOverlay.setVisibility(View.VISIBLE);

                textHelp.setText(R.string.next);
                counter = counter + 1;
            }/* else if (counter == 4) {
                llRide.setVisibility(View.INVISIBLE);
                llRent.setVisibility(View.INVISIBLE);
                llDelivery.setVisibility(View.INVISIBLE);
                llShop.setVisibility(View.INVISIBLE);
                llServices.setVisibility(View.VISIBLE);
                llInfo.setVisibility(View.INVISIBLE);
                rlOverlay.setVisibility(View.VISIBLE);

                textHelp.setText(R.string.next);
                counter = counter + 1;
            } else if (counter == 5) {
                llRide.setVisibility(View.INVISIBLE);
                llRent.setVisibility(View.INVISIBLE);
                llDelivery.setVisibility(View.INVISIBLE);
                llShop.setVisibility(View.INVISIBLE);
                llServices.setVisibility(View.INVISIBLE);
                llInfo.setVisibility(View.VISIBLE);
                rlOverlay.setVisibility(View.VISIBLE);

                textHelp.setText(R.string.got_it);
                counter = counter + 1;
            } else if (counter == 6)*/ else if (counter == 4) {
                rlOverlay.setVisibility(View.GONE);
            }
        } else if (id == R.id.infoRideBtn) {
            //call ShowInfo()
            ShowInfo(1);
        } else if (id == R.id.infoRentBtn) {
            //call ShowInfo()
            ShowInfo(2);
        } else if (id == R.id.infoDeliveyBtn) {
            //call ShowInfo()
            ShowInfo(3);
        }/* else if (id == R.id.infoShopBtn) {
            //call ShowInfo()
            ShowInfo(4);
        } else if (id == R.id.infoServiceBtn) {
            //call ShowInfo()
            ShowInfo(5);
        }*/
    }

    /**
     * when app is killed then this is called
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
    }
}