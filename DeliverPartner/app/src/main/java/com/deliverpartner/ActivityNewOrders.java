package com.deliverpartner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityNewOrders extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityNewOrders";

    public static final String DELIVERY_DETAILS = "com.agent.DeliveryDetails";
    public static final String DID = "DeliveryID";
    public static final String SRCLND = "DeliverySrcLand";
    public static final String DSTLND = "DeliveryDstLand";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";

    String strAuth, strDid, strSrcLnd, strDstLnd;
    ActivityNewOrders a = ActivityNewOrders.this;
    Map<String, String> params = new HashMap();

    TextView src, dst, yes, no, info;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        SharedPreferences delPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
        strDid = delPref.getString(DID, ""); // retrieve auth value stored locally and assign it to String auth
        strSrcLnd = delPref.getString(SRCLND, ""); // retrieve auth value stored locally and assign it to String auth
        strDstLnd = delPref.getString(DSTLND, ""); // retrieve auth value stored locally and assign it to String auth

        info = findViewById(R.id.info_text);
        src = findViewById(R.id.srcLnd);
        dst = findViewById(R.id.dstLnd);
        yes = findViewById(R.id.accept_request);
        no = findViewById(R.id.reject_request);
        relativeLayout = findViewById(R.id.rl_request);
        src.setText(strSrcLnd);
        dst.setText(strDstLnd);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        if (strDid.isEmpty()) {
            info.setText("NO new delivery");
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
        //getStatus();
    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyAccept() {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("did", strDid);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth= " + auth + " did= " + strDid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST agent-delivery-accept");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-accept", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accept_request:
                delvyAccept();
                break;
            case R.id.reject_request:
                Intent home = new Intent(ActivityNewOrders.this, ActivityHome.class);
                startActivity(home);
                finish();
                break;
        }
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
//response on hitting agent-set-mode API
        if (id == 1) {
            Log.d(TAG, "RESPONSE:" + response);
            Intent home = new Intent(ActivityNewOrders.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {
                    try {
                        String status = response.getString("st");
                        String did = response.getString("did");
                        Intent home = new Intent(ActivityNewOrders.this, ActivityHome.class);
                        startActivity(home);
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                        info.setText("NO new delivery");
                        relativeLayout.setVisibility(View.GONE);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
}
