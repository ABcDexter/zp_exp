package com.zpdeliveryagent;

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

    ImageView zippe_iv, zippe_iv_below;
    SharedPreferences cookie;
    String strAuth, strAN;
    public static final String TAG = "ActivityWelcome";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String AADHAR = "Aadhar";

    public static boolean isAppRunning;

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");
        strAN = cookie.getString(AADHAR, "");
        String auth = strAuth;
        String an = strAN;
        Log.d(TAG, "auth=" + auth);
        Log.d(TAG, "an=" + an);
        //checking if the user is registered or not.
        if (an.equals("")) {
            Log.d(TAG, "an=" + an);
            Intent registerUser = new Intent(ActivityWelcome.this, ActivityRegistration.class);
            startActivity(registerUser);
            finish();
        } else if (auth.equals("")) {
            Log.d(TAG, "auth=" + auth);
            Intent loginUser = new Intent(ActivityWelcome.this, ActivityLogin.class);
            startActivity(loginUser);
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ActivityWelcome.this, ActivityHome.class);
                    startActivity(intent);
                }
            }, 500);
        }

        zippe_iv = findViewById(R.id.iv_zippee);
        zippe_iv_below = findViewById(R.id.iv_zippee_bottom);

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

    /**
     * when app is killed then this is called
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
    }
}
