package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityTrackYourProgress extends AppCompatActivity {
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    private static final String TAG = "ActivityTrackYourProgress";
    TextView percentage;
    ProgressBar VerticalProgressBar;
    int intValue = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_your_progress);

        trackProgress();
        percentage = findViewById(R.id.percent);
        if (percentage.getText().toString().equals("100")){
            Intent completed = new Intent(ActivityTrackYourProgress.this, ActivityRideEnded.class);
            startActivity(completed);
            finish();
        }

        VerticalProgressBar = (ProgressBar)findViewById(R.id.progressBar1);

        // Adding colors on progress bar
        VerticalProgressBar.getProgressDrawable().setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);

    }

    private void trackProgress() {
        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        String stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuthCookie);
        JSONObject param = new JSONObject(params);
        ActivityTrackYourProgress a = ActivityTrackYourProgress.this;
        Log.d(TAG, "Values: auth=" + stringAuthCookie);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-progress-percent");
        UtilityApiRequestPost.doPOST(a, "auth-progress-percent", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws Exception {


        if (id == 1) {
            Log.d(TAG, "RESPONSE:" + response);
            String pct = response.getString("pct");
            percentage.setText(pct);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    while(intValue < 100)
                    {
                        intValue = Integer.parseInt(pct);

                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                VerticalProgressBar.setProgress(intValue);

                            }
                        });try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    }
                }
            }).start();
            trackProgress();

        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());

    }

}
