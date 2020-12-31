package com.deliverpartner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

//this activity acts like the base activity for the application. It is responsible for displaying the menu button on top of each activity
public class ActivityDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    NavigationView nv;
    Toolbar toolbar;
    ImageView menuBtn;
    TextView nameText;

    public static final String USER_DATA = "com.client.UserData";
    public static final String USER_NAME = "UserName";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //application will always run in portrait mode
        SharedPreferences prefPLoc = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
        String stringName = prefPLoc.getString(USER_NAME, "");

        nameText = findViewById(R.id.nameFrmServer);

        if (stringName.isEmpty())
            nameText.setText("");

        else {
            nameText.setText(stringName);

            Log.d("USER_NAME", "name:" + stringName);

        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        menuBtn = findViewById(R.id.toolbar_menu);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ACTION", "menuButton clicked");
                // If the navigation drawer is not open then open it, if its already open then close it.
                if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                     mDrawerLayout.openDrawer(Gravity.LEFT);
                else mDrawerLayout.closeDrawer(Gravity.RIGHT);
                //setNavigationDrawer();
                Log.d("ACTION", "setNavigationDrawer() method called");

            }
        });

        /*backBtn = findViewById(R.id.toolbar_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

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
        //overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        //overridePendingTransitionEnter();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransitionExit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(ActivityDrawer.this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            /*case R.id.nav_partner:toolbar_back
             *//*Intent partner_intent = new Intent(ExperienceHomeActivity.this, BecomePartner.class);
                        startActivity(partner_intent);*//*
                        break;*/
        } else if (id == R.id.nav_profile) {
            Intent profile = new Intent(ActivityDrawer.this, ActivityUserProfile.class);
            startActivity(profile);
        } else if (id == R.id.nav_home) {
            Intent home = new Intent(ActivityDrawer.this, ActivityHome.class);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        } else {
            return true;
        }
        return true;
    }
}