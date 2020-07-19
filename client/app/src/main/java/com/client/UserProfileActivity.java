package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.client.ride.ActivityRideHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserProfileActivity extends ActivityDrawer {

    private static final String TAG = "UserProfileActivity";
    private Spinner spLanguage;
    Locale myLocale;
    String currentLanguage = "en", currentLang;

    TextView mobiletxt;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String USER_DATA = "com.client.UserData";
    public static final String USER_NAME = "UserName";
    public static final String USER_PHONE = "UserPhone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UtilityInitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_user_profile, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);


        SwitchCompat switchCompat = findViewById(R.id.switchCompat);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            switchCompat.setChecked(true);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UtilityInitApplication.getInstance().setIsNightModeEnabled(true);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);

                } else {
                    UtilityInitApplication.getInstance().setIsNightModeEnabled(false);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            }
        });
        mobiletxt = findViewById(R.id.mobile);
        nameText = findViewById(R.id.user_name);

        SharedPreferences prefPLoc = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
        String stringPhone = prefPLoc.getString(USER_PHONE, "");

        if (stringPhone.isEmpty())
            mobiletxt.setText("XXXXXXXXXX");
        else {
            mobiletxt.setText(stringPhone);
            Log.d(TAG, "phone no:" + stringPhone);

        }

        String stringName = prefPLoc.getString(USER_NAME, "");

        if (stringName.isEmpty())
            nameText.setText("");
        else {
            nameText.setText(stringName);
            Log.d(TAG, "name:" + stringName);

        }

        currentLanguage = getIntent().getStringExtra(currentLang);
        List<String> list = new ArrayList<>();

        list.add("Select");
        list.add("English");
        list.add("Hindi");

        spLanguage = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("en");
                        break;
                    case 2:
                        setLocale("hi");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, ActivityRideHome.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(UserProfileActivity.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
