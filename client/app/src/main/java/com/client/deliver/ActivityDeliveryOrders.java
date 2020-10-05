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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
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

    public static final String PREFS_ADDRESS = "com.client.ride.Address";

    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String DROP_NAME = "com.client.ride.DropName";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String PICK_NAME = "com.client.ride.PickName";

    ActivityDeliveryOrders a = ActivityDeliveryOrders.this;
    Map<String, String> params = new HashMap();

    private static ActivityDeliveryOrders instance;
    String stringAuthKey, stringSCID;
    //Dialog myDialog;
    TextView dialog_txt, trackDelivery;
    SwipeRefreshLayout swipeRefresh;

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

        trackDelivery = findViewById(R.id.track_delivery);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        //trackDelivery.setOnClickListener(this);
        Intent intent = getIntent();
        stringSCID = intent.getStringExtra("SCID");
        Log.d(TAG, "SCID" + stringSCID);
        //myDialog = new Dialog(this);
        getInfo();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();//this will recreate or reload the activity when swiped down
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    public static ActivityDeliveryOrders getInstance() {
        return instance;
    }

    public void getInfo() {
        String auth = stringAuthKey;
        String scid = stringSCID;
        params.put("auth", auth);
        params.put("scid", scid);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-delivery-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-delivery-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    /*public void trackDelivery() {
        String auth = stringAuthKey;
        String did = stringSCID;
        params.put("auth", auth);
        params.put("scid", did);
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
    }*/
    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException, JSONException {

        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting user-delivery-get-info API
        if (id == 1) {
            try {
               /* Intent intent = new Intent(this, UtilityPollingService.class);
                intent.setAction("33");
                startService(intent);*/
                String st = response.getString("st");
                //String active = response.getString("active");

                if (st.equals("SC")) {
                    //String scid = response.getString("scid");
                    //String price = response.getString("price");
                        /*SharedPreferences pref = this.getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(DELIVERY_ID, scid);
                        editor.apply();*/

                    Intent payment = new Intent(ActivityDeliveryOrders.this, ActivityDeliverPayment.class);
                    startActivity(payment);
                }

                if (st.equals("RQ") || st.equals("PD")) {
                    /*Intent delConfirm = new Intent(ActivityDeliveryOrders.this, ActivityDeliverConfirm.class);
                    startActivity(delConfirm);*/
                    ShowPopup(0, "");

                    SharedPreferences preferencesD = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferencesD.edit();
                    editor1.remove(DELIVERY_ID);
                    editor1.apply();
                    SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(PICK_LAT);
                    editor.remove(PICK_LNG);
                    editor.remove(ADDRESS_PICK);
                    editor.remove(PICK_PIN);
                    editor.remove(PICK_LANDMARK);
                    editor.remove(PICK_MOBILE);
                    editor.remove(PICK_NAME);
                    editor.remove(DROP_LAT);
                    editor.remove(DROP_LNG);
                    editor.remove(ADDRESS_DROP);
                    editor.remove(DROP_PIN);
                    editor.remove(DROP_LANDMARK);
                    editor.remove(DROP_MOBILE);
                    editor.remove(DROP_NAME);
                    editor.apply();
                }
                if (st.equals("AS")) {
                    String otp = response.getString("otp");
                    ShowPopup(2, otp);
                }
                if (st.equals("ST")) {
                    ShowPopup(1, "");
                    /*trackDelivery.setVisibility(View.VISIBLE);*/
                }
                if (st.equals("RC")) {
                    String otp = response.getString("otp");
                    ShowPopup(8, otp);
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
                //trackDelivery();
                break;

        }
    }

    private void ShowPopup(int id, String info) {

        //myDialog.setContentView(R.layout.popup_new_request);
        dialog_txt = findViewById(R.id.txtInfo);
        String INFO = info;
        //RQ or PD
        if (id == 0) {
            dialog_txt.setText(R.string.your_agent_will_be_assigned_shortly);
        }
        //ST
        if (id == 1) {
            dialog_txt.setText(R.string.the_package_is_en_route);
        }
        //AS
        if (id == 2) {
            dialog_txt.setText(getString(R.string.your_delivery_agent_will_arrive_shortly) + INFO);
        }
        //FL
        if (id == 3) {
            dialog_txt.setText(R.string.we_are_sorry);
        }
        //DN
        if (id == 4) {
            dialog_txt.setText(R.string.delivery_denied_by_your_agent);
        }
        //CN
        if (id == 5) {
            dialog_txt.setText(R.string.delivery_was_cancelled_by_you);
        }
        //TO
        if (id == 6) {
            dialog_txt.setText(R.string.delivery_timed_out);
        }
        //FN
        if (id == 7) {
            dialog_txt.setText(R.string.delivery_was_completed_successfully);
        }
        //RC
        if (id == 8) {
            dialog_txt.setText(getString(R.string.agent_has_arrived) + INFO);
        }

    }

}
