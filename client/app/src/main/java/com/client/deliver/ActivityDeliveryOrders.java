package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.client.rent.MapsHubLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ActivityDeliveryOrders extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityDeliveryOrders";
    public static final String DELIVERY_DETAILS = "com.client.delivery.details";
    public static final String DELIVERY_ID = "DeliveryID";
    public static final String AUTH_KEY = "AuthKey";

    ActivityDeliveryOrders a = ActivityDeliveryOrders.this;
    Map<String, String> params = new HashMap();

    private static ActivityDeliveryOrders instance;
    String stringAuthKey, stringDID;
    //Dialog myDialog;
    TextView dialog_txt, trackDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_delivery_orders, null, false);
        frameLayout.addView(activityView);
        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        SharedPreferences delivery_pref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
        stringDID = delivery_pref.getString(DELIVERY_ID, "");
        checkStatus();
        trackDelivery = findViewById(R.id.track_delivery);
        trackDelivery.setOnClickListener(this);
        //myDialog = new Dialog(this);
    }

    public static ActivityDeliveryOrders getInstance() {
        return instance;
    }

    public void checkStatus() {
        String auth = stringAuthKey;
        String did = stringDID;
        params.put("auth", auth);
        params.put("did", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " did=" + did);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "user-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void trackDelivery() {
        String auth = stringAuthKey;
        String did = stringDID;
        params.put("auth", auth);
        params.put("did", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " did=" + did);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-track");
        UtilityApiRequestPost.doPOST(a, "user-delivery-track", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException, JSONException {

        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting user-delivery-get-status API
        if (id == 1) {
            try {
                Intent intent = new Intent(this, UtilityPollingService.class);
                intent.setAction("33");
                startService(intent);
                String st = response.getString("st");
                if (st.equals("RQ")) {
                    /*Intent delConfirm = new Intent(ActivityDeliveryOrders.this, ActivityDeliverConfirm.class);
                    startActivity(delConfirm);*/
                    ShowPopup(8, "");
                }
                if (st.equals("AS")) {
                    Intent delConfirm = new Intent(ActivityDeliveryOrders.this, ActivityDeliverPayment.class);
                    startActivity(delConfirm);
                }
                if (st.equals("ST")) {
                    ShowPopup(1, "");
                    trackDelivery.setVisibility(View.VISIBLE);
                    //trackDelivery();
                }
                if (st.equals("PD")) {
                    String otp = response.getString("otp");

                    ShowPopup(2, otp);

                }
                if (st.equals("FL")) {
                    ShowPopup(3, "");

                }
                if (st.equals("DN")) {
                    ShowPopup(4, "");
                }
                if (st.equals("CN")) {
                    ShowPopup(5, "");
                }
                if (st.equals("TO")) {
                    ShowPopup(6, "");
                }
                if (st.equals("FN")) {
                    ShowPopup(7, "");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (id == 2) {
            String lat = response.getString("lat");
            String lng = response.getString("lng");

            Intent map = new Intent(ActivityDeliveryOrders.this, MapsHubLocation.class);
            map.putExtra("lat", lat);
            map.putExtra("lng", lng);
            startActivity(map);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.track_delivery:
                trackDelivery();
                break;

        }
    }

    private void ShowPopup(int id, String info) {

        //myDialog.setContentView(R.layout.popup_new_request);
        dialog_txt = findViewById(R.id.txtInfo);
        String INFO = info;
        if (id == 1) {
            dialog_txt.setText("Your package is en route. Agent will call on reaching destination.");
        }
        if (id == 2) {
            dialog_txt.setText("payment received for this delivery.\nyour OTP is: " + INFO);
        }
        if (id == 3) {
            dialog_txt.setText("We are sorry ! Your Delivery was cancelled due to unavoidable circumstances. Our executives will get in touch with you.");
        }
        if (id == 4) {
            dialog_txt.setText("Delivery denied by agent.");
        }
        if (id == 5) {
            dialog_txt.setText("delivery cancelled by you.");
        }
        if (id == 6) {
            dialog_txt.setText("delivery timed out.");
        }
        if (id == 7) {
            dialog_txt.setText("Delivery completed successfully.");
        }
        if (id == 8) {
            dialog_txt.setText("Waiting for agent to accept your delivery.");
        }
        /*myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);*/
    }

}
