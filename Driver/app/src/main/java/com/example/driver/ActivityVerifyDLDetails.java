package com.example.driver;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityVerifyDLDetails extends AppCompatActivity {
    ImageView zbeeRight, zbeeLeft;
    private static final String TAG = "ActivityVerifyDLDetails";
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    public static final String VERIFICATION_TOKEN = "Token";
    public static final String MOBILE = "Mobile";
    public static final String AADHAR = "Aadhar";
    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    TextView message;
    private static ActivityVerifyDLDetails instance;

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        String registerStatus = response.getString("status");
        if (registerStatus.equals("true")) {
            //enter this if "status" equals "true"
            String auth = response.getString("auth");
            SharedPreferences sp_cookie = this.getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(COOKIE, auth).apply();

            message.setText("Details Verified.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent next = new Intent(ActivityVerifyDLDetails.this, ActivityWelcome.class);
                    startActivity(next);
                    finish();
                }
            }, 30000);
        } else if (registerStatus.equals("false")) {
            //enter this if "status" equals "false"
            //Polling is-driver-verified APIs
            Intent i = new Intent(this, UtilityPollingService.class);
            i.setAction("0");
            startService(i);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
    }

    public static ActivityVerifyDLDetails getInstance() {
        return instance;
    }

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_dl_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //application will always run in portrait mode

        message = findViewById(R.id.txt_verify);
        zbeeRight = findViewById(R.id.imageZbee);
        zbeeLeft = findViewById(R.id.imageZbeeBelow);
        instance = this;
        animateZbee(); //animation of zbee
        isDriverVerified(); //this method hits is-driver-verified API

    }

    protected void isDriverVerified() {
        SharedPreferences sharedPreferences = getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
        //retrieve data stored in SharedPreferences
        String token = sharedPreferences.getString(VERIFICATION_TOKEN, "");
        String aadhar = sharedPreferences.getString(AADHAR, "");
        String mobile = sharedPreferences.getString(MOBILE, "");

        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("an", aadhar);
        params.put("pn", mobile);
        JSONObject parameters = new JSONObject(params);
        ActivityVerifyDLDetails a = ActivityVerifyDLDetails.this;

        Log.d(TAG, "Values: token=" + token + " an=" + aadhar + " pn=" + mobile);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME is-driver-verified");
        UtilityApiRequestPost.doPOST(a, "is-driver-verified", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unSuccessfully

    }

    private void animateZbee() {
        //to animate zbee we have used ObjectAnimator
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zbeeLeft, "translationX", 1500, 0f);
        //ObjectAnimator takes the following arguments: target (here zbeeLeft) , String: The name of the property being animated (here  translationX), values: int: A set of values that the animation will animate between over time
        objectAnimator.setDuration(1500); // the duration of the animation
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE); // the number of times it should be repeated

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zbeeRight, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);

    }
}
