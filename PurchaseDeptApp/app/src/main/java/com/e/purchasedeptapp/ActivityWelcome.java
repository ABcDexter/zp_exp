package com.e.purchasedeptapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityWelcome extends AppCompatActivity {

    SharedPreferences cookie;
    String strAuth ;
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.purchasedeptapp.cookie";


//When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");

        String auth = strAuth;
        Log.d("ActivityWelcome","auth="+auth);
        //checking if the user is registered or not.
        if (auth.isEmpty()) {
                Intent registerUser = new Intent(ActivityWelcome.this, ActivityLogin.class);
                startActivity(registerUser);
                finish();

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ActivityWelcome.this, ActivityHome.class);
                    startActivity(intent);
                }
            }, 1000);
        }

    }


}
