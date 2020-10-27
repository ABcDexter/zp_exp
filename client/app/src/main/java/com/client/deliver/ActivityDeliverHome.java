package com.client.deliver;

import android.animation.ArgbEvaluator;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
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

public class ActivityDeliverHome extends ActivityDrawer implements View.OnClickListener {
    TextView pickAddress, dropAddress;
    ImageButton confirm;
    Button content, size;
    String ContentType = "";
    String ContentSize = "";
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
    String lat, lng, stringAuth, stringAN, addPick, addDrop, pickLat, pickLng,
            dropLat, dropLng, pickLand, dropLand, pickPin, dropPin, pickMobile, dropMobile;
    SharedPreferences prefAuth;
    ScrollView scrollView;
TextView disclaimer;
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
        content.setOnClickListener(this);
        size = findViewById(R.id.content_size);
        size.setOnClickListener(this);
        confirm.setOnClickListener(this);
        pickAddress.setOnClickListener(this);
        dropAddress.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollViewDelivery);
disclaimer = findViewById(R.id.disclaimer);
disclaimer.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent exp = new Intent(ActivityDeliverHome.this, ZExp.class);
        startActivity(exp);
    }
});
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
            /*int pickSpace = (addPick.contains(" ")) ? addPick.indexOf(",") : addPick.length() - 1;
            String pickCutName = addPick.substring(0, pickSpace);
            pickAddress.setText(pickCutName);*/
            String upToNCharacters = addPick.substring(0, Math.min(addPick.length(), 25));
            pickAddress.setText(upToNCharacters);
        }
        if (!addDrop.equals("")) {
            //dropAddress.setText(addDrop + ", " + dropLand);
            /*String nameLandmark = addDrop + dropLand;
            String displayAdd = nameLandmark.substring(0, Math.min(nameLandmark.length(), 20));
            dropAddress.setText(displayAdd);*/
            String upToNCharacters = addDrop.substring(0, Math.min(addDrop.length(), 25));
            dropAddress.setText(upToNCharacters);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityDeliverHome.this);
        getLastLocation();

        models = new ArrayList<>();
        models.add(new Model(R.drawable.delivery_man, "Your safety is important. We ensure contactless delivery."));
        models.add(new Model(R.drawable.packed_parcel, "Please keep the items ready before the agent arrives for pickup."));

        adapterSlider = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapterSlider);
        viewPager.setPadding(5, 0, 5, 0);

        imageDialog = new Dialog(this);
        imageDialog2 = new Dialog(this);
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

                if (ContentType.equals("") || ContentSize.equals("") ||
                        dropAddress.getText().equals("DROP DETAILS") || pickAddress.getText().equals("PICK UP DETAILS")
                        || content.getText().toString().equals("PACKAGE CONTENTS") || size.getText().toString().equals("PACKAGE SIZE")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();

                } else {
                    storeData();
                    Intent confirmIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverItemDetails.class);
                    startActivity(confirmIntent);
                }
                break;
            case R.id.content_type:
                PopupContent();
                break;
            case R.id.ride_rl_1:
                content.setText("Documents / Books");
                imageDialog.dismiss();
                ContentType = "DOC";
                break;
            case R.id.ride_rl_2:
                content.setText("Clothes / Accessories");
                imageDialog.dismiss();
                ContentType = "CLO";
                break;
            case R.id.ride_rl_3:
                content.setText("Household Items");
                imageDialog.dismiss();
                ContentType = "HOU";
                break;
            case R.id.rl_4:
                content.setText("Food Items");
                imageDialog.dismiss();
                ContentType = "FOO";
                break;
            case R.id.rl_5:
                content.setText("Electronics / electrical");
                imageDialog.dismiss();
                ContentType = "ELE";
                break;
            case R.id.rl_6:
                content.setText("Any Other");
                imageDialog.dismiss();
                ContentType = "OTH";
                break;
            case R.id.content_size:
                PopupSize();
                break;
            case R.id.s_1:
                size.setText("Small");
                ContentSize = "S";
                imageDialog2.dismiss();
                break;
            case R.id.s_2:
                size.setText("Medium");
                imageDialog2.dismiss();
                ContentSize = "M";
                break;
            case R.id.s_3:
                size.setText("Large");
                ContentSize = "L";
                imageDialog2.dismiss();
                break;
            case R.id.s_4:
                size.setText("X-Large");
                ContentSize = "XL";
                imageDialog2.dismiss();
                break;
            case R.id.s_5:
                size.setText("XX-Large");
                ContentSize = "XXL";
                imageDialog2.dismiss();
                break;
        }
    }

    Dialog myDialog, imageDialog, imageDialog2;

    private void PopupContent() {

        imageDialog.setContentView(R.layout.popup_content_type);
        RelativeLayout rl1 = (RelativeLayout) imageDialog.findViewById(R.id.ride_rl_1);
        RelativeLayout rl2 = (RelativeLayout) imageDialog.findViewById(R.id.ride_rl_2);
        RelativeLayout rl3 = (RelativeLayout) imageDialog.findViewById(R.id.ride_rl_3);
        RelativeLayout rl4 = (RelativeLayout) imageDialog.findViewById(R.id.rl_4);
        RelativeLayout rl5 = (RelativeLayout) imageDialog.findViewById(R.id.rl_5);
        RelativeLayout rl6 = (RelativeLayout) imageDialog.findViewById(R.id.rl_6);


        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);
        rl4.setOnClickListener(this);
        rl5.setOnClickListener(this);
        rl6.setOnClickListener(this);

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

    private void PopupSize() {

        imageDialog2.setContentView(R.layout.popup_content_size);
        RelativeLayout s1 = (RelativeLayout) imageDialog2.findViewById(R.id.s_1);
        RelativeLayout s2 = (RelativeLayout) imageDialog2.findViewById(R.id.s_2);
        RelativeLayout s3 = (RelativeLayout) imageDialog2.findViewById(R.id.s_3);
        RelativeLayout s4 = (RelativeLayout) imageDialog2.findViewById(R.id.s_4);
        RelativeLayout s5 = (RelativeLayout) imageDialog2.findViewById(R.id.s_5);

        TextView t1 = (TextView) imageDialog2.findViewById(R.id.size1);
        TextView t2 = (TextView) imageDialog2.findViewById(R.id.size2);
        TextView t3 = (TextView) imageDialog2.findViewById(R.id.size3);
        TextView t4 = (TextView) imageDialog2.findViewById(R.id.size4);
        TextView t5 = (TextView) imageDialog2.findViewById(R.id.size5);
        Switch switchUnit = (Switch) imageDialog2.findViewById(R.id.size_switch);
        switchUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    t1.setText("(14 x 10 x 5 inch)");
                    t2.setText("(28 x 20 x 10 inch)");
                    t3.setText("(42 x 30 x 15 inch)");
                    t4.setText("(42 x 40 x 20 inch)");
                    t5.setText("(69 x 50 x 25 inch)");
                } else {
                    t1.setText("(35 x 25 x 13 cm)");
                    t2.setText("(70 x 50 x 26 cm)");
                    t3.setText("(105 x 75 x 39 cm)");
                    t4.setText("(104 x 100 x 52 cm)");
                    t5.setText("(175 x 125 x 65 cm)");

                }
            }
        });

        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);
        s5.setOnClickListener(this);

        imageDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = imageDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        imageDialog2.show();
        Window window = imageDialog2.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        imageDialog2.setCanceledOnTouchOutside(true);
    }

    private void storeData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTENT_TYPE, ContentType);
        editor.putString(CONTENT_DIM, ContentSize);
        editor.apply();
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
