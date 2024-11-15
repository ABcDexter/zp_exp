package com.clientzp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> dev
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clientzp.deliver.ActivityDeliveryHistoryList;
import com.clientzp.rent.ActivityRentHistory;
import com.clientzp.ride.ActivityRideHistory;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;

import java.util.Objects;

public class ActivityDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TrackingStateObserver.OnTrackingStateChangeListener {
    private static final String TAG = "ActivityDrawer";

    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    NavigationView nv;
    Toolbar toolbar;
    ImageView menuBtn, backBtn;
    TextView nameText;
    public static final String NAME_KEY = "NameKey";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    private static final String PUBLISHABLE_KEY = "shXqLCv6GJVJ9QFgdHb6VL0JzE_7X96YoAX3ZxA919DLWOA1fayXhLg_NguIvRNypeaSpLu4U6JlYiwJahN8pA";
    private HyperTrack sdkInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        String stringName = prefPLoc.getString(NAME_KEY, "");
        nameText = findViewById(R.id.nameFrmServer);

        /*try {
            int firstSpace = (stringName.contains(" ")) ? stringName.indexOf(" ") : stringName.length() - 1;
            String name = stringName.substring(0, firstSpace);


            if (name.isEmpty())
                nameText.setText("");
            else {
                nameText.setText(getString(R.string.hi, name));
                Log.d("USER_NAME", "name:" + name);

            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "error" + e);
        }*/

        String firstWord = stringName;
<<<<<<< HEAD
        if (firstWord.contains(" ")) {
            firstWord = firstWord.substring(0, firstWord.indexOf(" "));
            System.out.println(firstWord);
            nameText.setText(getString(R.string.hi, firstWord));
            //Log.d("USER_NAME", "name:" + firstWord);
        } else {
            nameText.setText(getString(R.string.hi, firstWord));
            //Log.d("USER_NAME", "name:" + firstWord);
=======
        if(firstWord.contains(" ")){
            firstWord= firstWord.substring(0, firstWord.indexOf(" "));
            System.out.println(firstWord);
            nameText.setText(getString(R.string.hi, firstWord));
            Log.d("USER_NAME", "name:" + firstWord);
        }else {
            nameText.setText(getString(R.string.hi, firstWord));
            Log.d("USER_NAME", "name:" + firstWord);
>>>>>>> dev
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        menuBtn = findViewById(R.id.toolbar_menu);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< HEAD
                //Log.d("ACTION", "menuButton clicked");
=======
                Log.d("ACTION", "menuButton clicked");
>>>>>>> dev
                if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                else mDrawerLayout.closeDrawer(Gravity.LEFT);
                //setNavigationDrawer();
<<<<<<< HEAD
                //Log.d("ACTION", "setNavigationDrawer() method called");
=======
                Log.d("ACTION", "setNavigationDrawer() method called");
>>>>>>> dev

            }
        });
        backBtn = findViewById(R.id.toolbar_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        hyperTrack();
    }

    //method to use HyperTrack sdk for tracking location of user
    private void hyperTrack() {
        HyperTrack.enableDebugLogging();
        sdkInstance = HyperTrack
                .getInstance(PUBLISHABLE_KEY)
                .addTrackingListener(this);

<<<<<<< HEAD
        //Log.d(TAG, "device id is " + sdkInstance.getDeviceID());
=======
        Log.d(TAG, "device id is " + sdkInstance.getDeviceID());
>>>>>>> dev

    }

    @Override
    protected void onResume() {
        super.onResume();
<<<<<<< HEAD
        //Log.d(TAG, "onResume");
=======
        Log.d(TAG, "onResume");
>>>>>>> dev
        if (sdkInstance.isRunning()) {
            onTrackingStart();
        } else {
            onTrackingStop();
        }

        sdkInstance.requestPermissionsIfNecessary();
    }

    // TrackingStateObserver.OnTrackingStateChangeListener interface methods
    @Override
    public void onError(TrackingError trackingError) {
<<<<<<< HEAD
        //Log.d(TAG, "onError: " + trackingError.message);
        if (trackingError.code == TrackingError.INVALID_PUBLISHABLE_KEY_ERROR || trackingError.code == TrackingError.AUTHORIZATION_ERROR) {
            //Log.d(TAG, "check your publishable key");
        } else if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            //Log.d(TAG, "Enable location data access");
        } else if (trackingError.code == TrackingError.PERMISSION_DENIED_ERROR) {
            //Log.d(TAG, "data access permissions were not granted");
        } else {
            //Log.d(TAG, "can't start tracking");
=======
        Log.d(TAG, "onError: " + trackingError.message);
        if (trackingError.code == TrackingError.INVALID_PUBLISHABLE_KEY_ERROR || trackingError.code == TrackingError.AUTHORIZATION_ERROR) {
            Log.d(TAG, "check your publishable key");
        } else if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            Log.d(TAG, "Enable location data access");
        } else if (trackingError.code == TrackingError.PERMISSION_DENIED_ERROR) {
            Log.d(TAG, "data access permissions were not granted");
        } else {
            Log.d(TAG, "can't start tracking");
>>>>>>> dev
        }
    }

    @Override
    public void onTrackingStart() {
<<<<<<< HEAD
        //Log.d(TAG, "tracking");
=======
        Log.d(TAG, "tracking");
>>>>>>> dev
    }

    @Override
    public void onTrackingStop() {
<<<<<<< HEAD
        //Log.d(TAG, "not tracking");
=======
        Log.d(TAG, "not tracking");
>>>>>>> dev
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkInstance.removeTrackingListener(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDrawer.this, ActivityWelcome.class));
        finish();
    }
    /*@Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();

<<<<<<< HEAD
            Intent intent = new Intent(ActivityDrawer.this, ActivityRegistration.class);
=======
            Intent intent = new Intent(ActivityDrawer.this, ActivityLoginKey.class);
>>>>>>> dev
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent lang = new Intent(ActivityDrawer.this, UserProfileActivity.class);
            startActivity(lang);
        } else if (id == R.id.nav_home) {
            Intent home = new Intent(ActivityDrawer.this, ActivityWelcome.class);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        } else if (id == R.id.nav_ride_history) {
            Intent rideHistory = new Intent(ActivityDrawer.this, ActivityRideHistory.class);
            rideHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(rideHistory);
        } else if (id == R.id.nav_rent_history) {
            Intent rentHistory = new Intent(ActivityDrawer.this, ActivityRentHistory.class);
            rentHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(rentHistory);
        } else if (id == R.id.nav_delivery_orders) {
            Intent deliveryOrders = new Intent(ActivityDrawer.this, ActivityDeliveryHistoryList.class);
            deliveryOrders.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(deliveryOrders);
        }/*else if (id == R.id.nav_shop_history) {
            //take user to https://zippe.in/en/my-account/orders/ url
            String connectUrl = "https://zippe.in/en/my-account/orders/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);
            *//*Intent shopHistory = new Intent(ActivityDrawer.this, ActivityShopHistoryList.class);
            shopHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(shopHistory);*//*
        }else if (id == R.id.nav_service_history) {
            //take user to https://zippe.in/en/my-account/orders/ url
            String connectUrl = "https://zippe.in/en/my-account/orders/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);
            *//*Intent serviceHistory = new Intent(ActivityDrawer.this, ActivityServiceHistoryList.class);
            serviceHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(serviceHistory);*//*
        }*/ else {
            return true;
        }
        return true;
    }
}