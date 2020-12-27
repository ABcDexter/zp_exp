package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.android.volley.VolleyError;
import com.client.ride.ActivityRideHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "UserProfileActivity";
    private Spinner spLanguage;
    Locale myLocale;
    String currentLanguage = "en", currentLang;

    TextView mobiletxt, uploadAadhar, btnEmail;
    EditText etEmail;
    Button submitEmail;
    String strEmail, stringName, stringPhone, stringAuth;
    RelativeLayout rlEmail;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String NAME_KEY = "NameKey";
    public static final String PHN_KEY = "PhnKey";

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

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringName = prefPLoc.getString(NAME_KEY, "");
        stringPhone = prefPLoc.getString(PHN_KEY, "");
        stringAuth = prefPLoc.getString(AUTH_KEY, "");

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
        uploadAadhar = findViewById(R.id.upload_aadhar);
        btnEmail = findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(this);
        etEmail = findViewById(R.id.et_email);
        submitEmail = findViewById(R.id.submit);
        rlEmail = findViewById(R.id.rl_email);
        submitEmail.setOnClickListener(this);

        if (stringPhone.isEmpty())
            mobiletxt.setText("");
        else {
            mobiletxt.setText(stringPhone);
            Log.d(TAG, "phone no:" + stringPhone);

        }

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

    //method to upload aadhar card pictures to the server
    public void uploadAadhar(View view) {
        Intent upload = new Intent(UserProfileActivity.this, AadharCardUpload.class);
        startActivity(upload);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_email) {
            btnEmail.setVisibility(View.GONE);
            rlEmail.setVisibility(View.VISIBLE);

        } else if (id == R.id.submit) {

            strEmail = etEmail.getText().toString();
            if (TextUtils.isEmpty(strEmail)) {
                etEmail.setError("This field cannot be left blank");
            } else {
                sendEmailAdd(strEmail);
            }
        }
    }

    private void sendEmailAdd(String email) {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("email", email);

        JSONObject parameters = new JSONObject(params);
        UserProfileActivity a = UserProfileActivity.this;
        Log.d(TAG, "Values: auth=" + auth + " email=" + email);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-profile-update");
        UtilityApiRequestPost.doPOST(a, "auth-profile-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);
        rlEmail.setVisibility(View.GONE);
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
}
