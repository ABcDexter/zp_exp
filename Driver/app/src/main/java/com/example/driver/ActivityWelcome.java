package com.example.driver;

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

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityWelcome extends AppCompatActivity {

    ImageView zippe_iv, zippe_iv_below;
    SharedPreferences cookie;
    String strAuth /*strToken*/;
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
   // public static final String VERIFICATION_TOKEN = "Token";

//When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);

        cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
       // strToken = sharedPreferences.getString(VERIFICATION_TOKEN, "");
        String auth = strAuth;
        //String token = strToken;
        //checking if the user is registered or not.
        if (auth.isEmpty()) {
            /*if (token.equals("")){
                Intent registerUser = new Intent(ActivityWelcome.this, ActivityLogin.class);
                startActivity(registerUser);
                finish();
            }else {*/
                Intent registerUser = new Intent(ActivityWelcome.this, ActivityLogin.class);
                startActivity(registerUser);
                finish();
           /* }*/
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ActivityWelcome.this, ActivityHome.class);
                    startActivity(intent);
                }
            }, 1000);
        }
        moveit();//animation of zbee
    }

    private void moveit() {
        //to animate zbee we have used ObjectAnimator
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zippe_iv_below, "translationX", 1500, 0f);
        //ObjectAnimator takes the following arguments: target (here zbeeLeft) , String: The name of the property being animated (here  translationX), values: int: A set of values that the animation will animate between over time
        objectAnimator.setDuration(1600);// the duration of the animation
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE); // the number of times it should be repeated

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zippe_iv, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1700);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);
    }

}
