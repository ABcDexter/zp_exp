package com.client.deliver;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class ActivityDeliverHome extends ActivityDrawer implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    TextView pickAddress, dropAddress;
    ImageButton confirm;
    Spinner content, size;
    String ContentType, ContentSize;
    private static final String TAG = "ActivityDeliverHome";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String PICK_NAME = "com.client.ride.PickName";
    public static final String DROP_NAME = "com.client.ride.DropName";
    Vibrator vibrator;

    ViewPager viewPager;
    Adapter adapterSlider;
    List<Model> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};
    String lat, lng, stringAuth, stringAN,addPick,addDrop,pickLat,pickLng,
            dropLat,dropLng,pickLand,dropLand,pickPin,dropPin,pickMobile,dropMobile;
    SharedPreferences prefAuth;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_deliver_home, null, false);
        frameLayout.addView(activityView);

        //initializing vies
        pickAddress = findViewById(R.id.txt_pick_address);
        dropAddress = findViewById(R.id.txt_drop_address);
        confirm = findViewById(R.id.next_deliver);
        content = findViewById(R.id.content_type);
        size = findViewById(R.id.content_size);
        confirm.setOnClickListener(this);
        pickAddress.setOnClickListener(this);
        dropAddress.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollViewRentRide);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //retrieving locally stored data
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
         addPick = pref.getString(ADDRESS_PICK, "");
         pickLat = pref.getString(PICK_LAT, "");
         pickLng = pref.getString(PICK_LNG, "");
         addDrop = pref.getString(ADDRESS_DROP, "");
         dropLat = pref.getString(DROP_LAT, "");
         dropLng = pref.getString(DROP_LNG, "");
         pickLand = pref.getString(PICK_LANDMARK, "");
         dropLand = pref.getString(DROP_LANDMARK, "");
         pickPin = pref.getString(PICK_PIN, "");
         dropPin = pref.getString(DROP_PIN, "");
         pickMobile = pref.getString(PICK_MOBILE, "");
         dropMobile = pref.getString(DROP_MOBILE, "");

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");

        if (!addPick.equals("")) {
            //String nameLandmarkPick = addPick+pickLand;
            //String displayPickAdd = nameLandmarkPick.substring(0, Math.min(nameLandmarkPick.length(), 13));
            int pickSpace = (addPick.contains(" ")) ? addPick.indexOf(",") : addPick.length() - 1;
            String pickCutName = addPick.substring(0, pickSpace);
            pickAddress.setText(pickCutName);

        }
        if (!addDrop.equals("")) {
            //dropAddress.setText(addDrop + ", " + dropLand);
            String nameLandmark = addDrop+dropLand;
            String displayAdd = nameLandmark.substring(0, Math.min(nameLandmark.length(), 13));
            dropAddress.setText(displayAdd);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityDeliverHome.this);
        getLastLocation();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.content_array)) {
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
        content.setAdapter(adapter);
        content.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapterNoRiders = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.size_array)) {
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
        adapterNoRiders.setDropDownViewResource(R.layout.spinner_item_blue);
        size.setAdapter(adapterNoRiders);
        size.setOnItemSelectedListener(this);

        models = new ArrayList<>();
        models.add(new Model(R.drawable.delivery_man, "Your safety is very important to us. We promote contactless delivery"));
        models.add(new Model(R.drawable.logistics, "This is not express delivery. Please do not request delivery for perishable items."));
        models.add(new Model(R.drawable.no_shopping, "No Purchases will be done by our partners."));
        models.add(new Model(R.drawable.packed_parcel, "Please keep the items ready before the partner arrives for pickup."));

        adapterSlider = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapterSlider);
        viewPager.setPadding(30, 0, 30, 0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_pick_address:
                Intent pickIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverFillAddress.class);
                pickIntent.putExtra("FillPick", "pick");
                startActivity(pickIntent);
                break;
            case R.id.txt_drop_address:
                Intent dropIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverFillAddress.class);
                dropIntent.putExtra("FillPick", "drop");
                startActivity(dropIntent);
                break;
            case R.id.next_deliver:
                if (ContentType.equals("PACKAGE CONTENTS") || ContentSize.equals("PACKAGE SIZE") ||
                        dropAddress.getText().equals("DROP POINT") || pickAddress.getText().equals("PICK UP POINT")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }else {
                storeData();
                Intent confirmIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverItemDetails.class);
                startActivity(confirmIntent);}
                break;
        }
    }

    private void storeData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTENT_TYPE, ContentType);
        editor.putString(CONTENT_DIM, ContentSize);
        editor.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.content_type:
                ContentType = content.getItemAtPosition(position).toString();
                switch (ContentType) {
                    case "DOCUMENTS / BOOKS":
                        ContentType = "DOC";
                        break;
                    case "CLOTHES / ACCESSORIES":
                        ContentType = "CLO";
                        break;
                    case "FOOD":
                        ContentType = "FOO";
                        break;
                    case "HOUSEHOLD":
                        ContentType = "HOU";
                        break;
                    case "ELECTRONICS / ELECTRICAL ITEMS":
                        ContentType = "ELE";
                        break;
                    case "OTHER":
                        ContentType = "OTH";
                        break;

                }
                break;
            case R.id.content_size:
                ContentSize = size.getItemAtPosition(position).toString();
                switch (ContentSize) {
                    case "S (35 x 25 x 13 cm)":
                        ContentSize = "S";
                        break;
                    case "M (70 x 50 x 26 cm)":
                        ContentSize = "M";
                        break;
                    case "L (105 x 75 x 39 cm)":
                        ContentSize = "L";
                        break;
                    case "XL (104 x 100 x 52 cm)":
                        ContentSize = "XL";
                        break;
                    case "XXL (175 x 125 x 65 cm)":
                        ContentSize = "XXL";
                        break;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    public void sendLocation() {

        Log.d(TAG, "inside sendLocation()");
        Map<String, String> params = new HashMap();
        params.put("an", stringAN);
        params.put("auth", stringAuth);
        params.put("lat", lat);
        params.put("lng", lng);
        JSONObject parameters = new JSONObject(params);
        ActivityDeliverHome a = ActivityDeliverHome.this;

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

    public void onSuccess(JSONObject response, int id) {
        if (id == 1) {
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

}
