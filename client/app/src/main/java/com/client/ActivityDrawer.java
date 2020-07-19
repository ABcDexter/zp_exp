package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.client.deliver.ActivityDeliveryOrders;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ActivityDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    NavigationView nv;
    Toolbar toolbar;
    ImageView menuBtn, backBtn;
    TextView nameText;
    public static final String NAME_KEY = "NameKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";

    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        String stringName = prefPLoc.getString(NAME_KEY, "");

        int firstSpace = (stringName.contains(" ")) ? stringName.indexOf(" ") : stringName.length() - 1;
        String name = stringName.substring(0, firstSpace);

        nameText = findViewById(R.id.nameFrmServer);

        if (name.isEmpty())
            nameText.setText("");
        else {
            nameText.setText(getString(R.string.hi) + name);
            Log.d("USER_NAME", "name:" + name);

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
                Log.d("ACTION", "menuButton clicked");
                if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))mDrawerLayout.openDrawer(Gravity.RIGHT);
                else mDrawerLayout.closeDrawer(Gravity.LEFT);
                //setNavigationDrawer();
                Log.d("ACTION", "setNavigationDrawer() method called");

            }
        });
        backBtn = findViewById(R.id.toolbar_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    /*private void setNavigationDrawer() {
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(ActivityDrawer.this, ActivityMain.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        SharedPreferences authPref = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorAuth = authPref.edit();
                        editorAuth.clear();
                        editorAuth.apply();
                        break;

                    case R.id.nav_profile:
                        Intent lang = new Intent(ActivityDrawer.this, UserProfileActivity.class);
                        startActivity(lang);
                        break;
                    case R.id.nav_home:
                        Intent home = new Intent(ActivityDrawer.this, ActivityWelcome.class);
                        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(home);
                        break;
                    */
    /*case R.id.nav_delivery_orders:
                        Intent deliveryOrders = new Intent(ActivityDrawer.this, ActivityDeliveryOrders.class);
                        deliveryOrders.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(deliveryOrders);
                        break;*//*

                    case R.id.nav_ride_history:
                        Intent deliveryOrders = new Intent(ActivityDrawer.this, ActivityDeliveryOrders.class);
                        deliveryOrders.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(deliveryOrders);
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }
*/
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDrawer.this, ActivityWelcome.class));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(ActivityDrawer.this, ActivityMain.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            case R.id.nav_profile:
                Intent lang = new Intent(ActivityDrawer.this, UserProfileActivity.class);
                startActivity(lang);
                break;
            case R.id.nav_home:
                Intent home = new Intent(ActivityDrawer.this, ActivityWelcome.class);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                break;
            case R.id.nav_ride_history:
                /*Intent deliveryOrders = new Intent(ActivityDrawer.this, ActivityDeliveryOrders.class);
                deliveryOrders.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(deliveryOrders);*/
                Toast.makeText(this, "COMING SOON", Toast.LENGTH_LONG).show();
                break;

            default:
                return true;
        }
        return true;
    }
        //return false;
    }