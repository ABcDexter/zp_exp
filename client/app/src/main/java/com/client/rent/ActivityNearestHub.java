package com.client.rent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityNearestHub extends ActivityDrawer implements View.OnClickListener {
    String stringAuthCookie;
    ActivityNearestHub a = ActivityNearestHub.this;
    private static final String TAG = "ActivityNearestHub";
    public static final String AUTH_KEY = "AuthKey";

    TextView hub1, hub2, hub3, dst1, dst2, dst3;
    ImageButton loc1, loc2, loc3;
    LinearLayout ll1, ll2, ll3;
    String Lat1, Lng1, Lat2, Lng2, Lat3, Lng3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_hub);

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");

        hub1 = findViewById(R.id.hub1);
        hub2 = findViewById(R.id.hub2);
        dst1 = findViewById(R.id.dst1);
        dst2 = findViewById(R.id.dst2);
        dst3 = findViewById(R.id.dst3);
        hub3 = findViewById(R.id.hub3);
        loc1 = findViewById(R.id.loc1);
        loc2 = findViewById(R.id.loc2);
        loc3 = findViewById(R.id.loc3);
        ll1 = findViewById(R.id.ll_hub1);
        ll2 = findViewById(R.id.ll_hub2);
        ll3 = findViewById(R.id.ll_hub3);

        loc1.setOnClickListener(this);
        loc2.setOnClickListener(this);
        loc3.setOnClickListener(this);
        hubsNearMe();
    }

    private void hubsNearMe() {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rent-end");
        UtilityApiRequestPost.doPOST(a, "user-rent-end", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-rent-end API
        if (id == 1) {
            String name1 = response.getString("close1pn");
            String lat1 = response.getString("close1lat");
            String lng1 = response.getString("close1lng");
            String distance1 = response.getString("close1dst");

            String name2 = response.getString("close2pn");
            String lat2 = response.getString("close2lat");
            String lng2 = response.getString("close2lng");
            String distance2 = response.getString("close2dst");

            String name3 = response.getString("close3pn");
            String lat3 = response.getString("close3lat");
            String lng3 = response.getString("close3lng");
            String distance3 = response.getString("close3dst");


            hub1.setText(name1);
            hub2.setText(name2);
            hub3.setText(name3);
            dst1.setText(distance1 +" km");
            dst2.setText(distance2 +" km");
            dst3.setText(distance3+" km");

            Lat1 =lat1;
            Lng1 = lng1;
            Lat2 =lat2;
            Lng2 = lng2;
            Lat3 =lat3;
            Lng3 = lng3;
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loc1:
                Intent map = new Intent(ActivityNearestHub.this, MapsHubLocation.class);
                map.putExtra("lat",Lat1);
                map.putExtra("lng",Lng1);
                startActivity(map);
                break;
            case R.id.loc2:
                Intent map2 = new Intent(ActivityNearestHub.this, MapsHubLocation.class);
                map2.putExtra("lat",Lat2);
                map2.putExtra("lng",Lng2);
                startActivity(map2);
                break;
            case R.id.loc3:
                Intent map3 = new Intent(ActivityNearestHub.this, MapsHubLocation.class);
                map3.putExtra("lat",Lat3);
                map3.putExtra("lng",Lng3);
                startActivity(map3);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityNearestHub.this, ActivityRentInProgress.class));
        finish();
    }
}
