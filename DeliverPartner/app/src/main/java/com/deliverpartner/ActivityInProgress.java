package com.deliverpartner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityInProgress extends AppCompatActivity implements View.OnClickListener {

    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityInProgress";

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
    ActivityInProgress a = ActivityInProgress.this;
    Map<String, String> params = new HashMap();

    TextView person, address, landmark, phone;
    String lat, lng;
    Button yes, no, map;
    EditText otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_progress);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        person = findViewById(R.id.src_per);
        address = findViewById(R.id.src_add);
        landmark = findViewById(R.id.src_land);
        phone = findViewById(R.id.src_phone);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        otp = findViewById(R.id.enter_otp);
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

    public void delvyCancel() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-cancel");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyStart() {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("otp", otp.getText().toString().trim());
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " otp=" + otp.getText().toString().trim());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-start");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-start", parameters, 20000, 0, response -> {
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

                    if (status.equals("PD")) {
                        String per = response.getString("srcper");
                        String add = response.getString("srcadd");
                        String land = response.getString("srcland");
                        String phn = response.getString("srcphone");
                        String lat = response.getString("srclat");
                        String lng = response.getString("srclng");

                        person.setText(per);
                        address.setText(add);
                        landmark.setText(land);
                        phone.setText(phn);

                        SharedPreferences delvyPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(SRC_PER, per).apply();
                        delvyPref.edit().putString(SRC_ADD, add).apply();
                        delvyPref.edit().putString(SRC_LND, land).apply();
                        delvyPref.edit().putString(SRC_PHN, phn).apply();
                        delvyPref.edit().putString(SRC_LAT, lat).apply();
                        delvyPref.edit().putString(SRC_LNG, lng).apply();
                        /*Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                        startActivity(home);
                        finish();*/
                    }

                    if (status.equals("ST")){
                        Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                        startActivity(home);
                        finish();
                    }

                } else if (active.equals("false")) {
                    Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting agent-delivery-cancel API
        if (id == 2) {
            Intent home = new Intent(ActivityInProgress.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
        if (id == 3) {
            getStatus();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                delvyStart();

                break;
            case R.id.no:
                delvyCancel();
                break;
            case R.id.map:
                /*Intent map = new Intent(ActivityInProgress.this, ActivityMap.class);
                startActivity(map);*/
                Toast.makeText(this, "Map will open", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
