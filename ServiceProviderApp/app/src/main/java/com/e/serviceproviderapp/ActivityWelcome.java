package com.e.serviceproviderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityWelcome extends AppCompatActivity {

    ImageView zippe_iv, zippe_iv_below;
    SharedPreferences cookie;
    String strAuth, strToken;
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String PICTURE_UPLOAD_STATUS = "serviceproviderapp.pictureUploadStatus";
    public static final String VERIFICATION_TOKEN = "Token";

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");
        String auth = strAuth;
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        strToken = sharedPreferences.getString(VERIFICATION_TOKEN, "");
        String token = strToken;

        //checking if the user is registered or not.
        if (auth.equals("")) {

            Intent registerUser = new Intent(ActivityWelcome.this, ActivityLogin.class);
            startActivity(registerUser);
            finish();

        } else {
            Intent intent = new Intent(ActivityWelcome.this, ActivityHome.class);
            startActivity(intent);
        }

        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);

        //moveit();//animation of zbee
    }

   /* private void moveit() {
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
    }*/

}
