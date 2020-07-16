package com.deliverpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityEnroute extends AppCompatActivity implements View.OnClickListener {
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityEnroute";

    public static final String DELIVERY_DETAILS = "com.agent.DeliveryDetails";
    public static final String DID = "DeliveryID";
    public static final String SRC_PER = "SrcPer";
    public static final String SRC_ADD = "SrcAdd";
    public static final String SRC_LND = "SrcLnd";
    public static final String SRC_PHN = "SrcPhn";
    public static final String SRC_LAT = "SrcLat";
    public static final String SRC_LNG = "SrcLng";
    public static final String DST_PER = "DSTPer";
    public static final String DST_ADD = "DSTAdd";
    public static final String DST_LND = "DSTLnd";
    public static final String DST_PHN = "DSTPhn";
    public static final String DST_LAT = "DSTLat";
    public static final String DST_LNG = "DSTLng";
    String strAuth;
    ActivityEnroute a = ActivityEnroute.this;
    Map<String, String> params = new HashMap();

    TextView person, address, landmark, phone;
    String lat, lng;
    Button yes, no, map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroute);
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        person = findViewById(R.id.dst_per);
        address = findViewById(R.id.dst_add);
        landmark = findViewById(R.id.dst_land);
        phone = findViewById(R.id.dst_phone);
        yes = findViewById(R.id.completed);
        no = findViewById(R.id.failed);
        map = findViewById(R.id.map);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        map.setOnClickListener(this);
        getStatus();
    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyFail() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-fail");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-fail", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyEnd() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-done");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-done", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting agent-delivery-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String did = response.getString("did");
                    SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(DID, did).apply();

                    if (status.equals("ST")) {
                        String per = response.getString("dstper");
                        String add = response.getString("dstadd");
                        String land = response.getString("dstland");
                        String phn = response.getString("dstphone");
                        String lat = response.getString("dstlat");
                        String lng = response.getString("dstlng");

                        person.setText(per);
                        address.setText(add);
                        landmark.setText(land);
                        phone.setText(phn);

                        SharedPreferences delvyPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(DST_PER, per).apply();
                        delvyPref.edit().putString(DST_ADD, add).apply();
                        delvyPref.edit().putString(DST_LND, land).apply();
                        delvyPref.edit().putString(DST_PHN, phn).apply();
                        delvyPref.edit().putString(DST_LAT, lat).apply();
                        delvyPref.edit().putString(DST_LNG, lng).apply();
                    }

                } else if (active.equals("false")) {
                    Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting agent-delivery-fail API
        if (id == 2) {
            Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
            startActivity(home);
            finish();
        }

        //response on hitting agent-delivery-done API
        if (id == 3) {
            Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.completed:
                delvyEnd();
                break;
            case R.id.failed:
                delvyFail();
                break;
            case R.id.map:
                /*Intent map = new Intent(ActivityInProgress.this, ActivityMap.class);
                startActivity(map);*/
                Toast.makeText(this, "Map will open", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
