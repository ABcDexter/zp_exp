package com.e.serviceproviderapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityLogin";
    ScrollView scrollView;
    private EditText etLoginKey;

   // public static final String AADHAR = "Aadhar";
    public static final String AN = "An";
    public static final String MOBILE = "Mobile";
    public static final String NAME = "Name";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    String loginKey;
    SharedPreferences cookie;
    String strMobile, mob;

    public void onSuccess(JSONObject response) {
        Log.d(TAG, "RESPONSE:" + response);

        try {
            String status = response.getString("status");
            if (status.equals("true")) {
                String auth = response.getString("auth");
                String an = response.getString("an");
                String pn = response.getString("pn");
                String name = response.getString("name");

                SharedPreferences pref_uploadStatus = this.getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
                pref_uploadStatus.edit().putString(AUTH_KEY, auth).apply();
                pref_uploadStatus.edit().putString(AN, an).apply();
                pref_uploadStatus.edit().putString(MOBILE, pn).apply();
                pref_uploadStatus.edit().putString(NAME, name).apply();

                Intent next = new Intent(ActivityLogin.this, ActivityWelcome.class);
                startActivity(next);
                finish();
            } else {
                Toast.makeText(this, "Please check your Login Key", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void onFailure(VolleyError error) {
        Snackbar snackbar = Snackbar.make(scrollView, R.string.login_unsuccessful, Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //application will always run in portrait mode

        //initializing variables
        scrollView = findViewById(R.id.mainLayout);
        etLoginKey = findViewById(R.id.editTextKey);
        ImageButton btnSignIn = findViewById(R.id.login);

        cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strMobile = cookie.getString(MOBILE, "");
        mob = strMobile;

        btnSignIn.setOnClickListener(this);

    }

    // called when any button is clicked
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.login) {
            final ProgressBar simpleProgressBar = findViewById(R.id.simpleProgressBar);
            loginKey = etLoginKey.getText().toString().trim();

            if (loginKey.isEmpty() || loginKey.length() > 9) {
                Log.d(TAG, "Error in Login Key");
                etLoginKey.setError("Enter valid Login Key");
                etLoginKey.requestFocus();
                return;
            }

            simpleProgressBar.setVisibility(View.VISIBLE);
            Map<String, String> params = new HashMap();
            params.put("key", loginKey);
            params.put("pn", "1234567890" );

            JSONObject parameters = new JSONObject(params);
            ActivityLogin a = ActivityLogin.this;
            Log.d(TAG, "Values: phone=" + mob + "\n" + "key=" + loginKey);
            Log.d(TAG, "UtilityApiRequestPost.doPOST login-servitor");
            UtilityApiRequestPost.doPOST(a, "login-servitor", parameters, 30000, 0, a::onSuccess, a::onFailure);
        }
    }

}