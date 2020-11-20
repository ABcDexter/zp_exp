package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ActivityDeliverThankYou extends AppCompatActivity {


    private static final String TAG = "ActivityDeliverThankYou";

    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";

    TextView details;
    String stringAuth;

    ActivityDeliverThankYou a = ActivityDeliverThankYou.this;
    Map<String, String> params = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_thank_you);
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefCookie.getString(AUTH_KEY, "");
        details = findViewById(R.id.view_delivery);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deliveryRetire();
                Intent viewDetails = new Intent(ActivityDeliverThankYou.this, ActivityDeliveryHistoryList.class);
                startActivity(viewDetails);
                finish();
            }
        });

    }

    private void deliveryRetire() {
        String auth = stringAuth;
        params.put("auth", auth);
        // params.put("scid", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-retire");
        UtilityApiRequestPost.doPOST(a, "user-delivery-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverThankYou.this, ActivityWelcome.class));
        finish();
    }
}
