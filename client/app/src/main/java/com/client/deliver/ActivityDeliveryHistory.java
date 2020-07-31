package com.client.deliver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityDeliveryHistory extends AppCompatActivity {

    private static final String TAG = "ActivityDeliveryHistory.class";

    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    String stringAuth;
    Map<String, String> params = new HashMap();
    ActivityDeliveryHistory a = ActivityDeliveryHistory.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_history);


        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        getData();

    }
    private void getData() {

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "auth = " + auth);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost auth-delivery-history");
        UtilityApiRequestPost.doPOST(a, "auth-delivery-history", parameters, 30000, 0, a::onSuccess, a::onFailure);

    }

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        /*try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("delis");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                String state = ob.getString("st");
                String price = ob.getString("price");
                String tip = ob.getString("tip");

                if (state.equals("AS")) {
                    Intent delConfirm = new Intent(ActivityDeliveryHistory.this, ActivityDeliverPayment.class);
                    startActivity(delConfirm);
                }
                if (state.equals("ST")) {
                    ShowPopup(1, "");
                    trackDelivery.setVisibility(View.VISIBLE);
                    //trackDelivery();
                }
                if (state.equals("RQ")) {
                    *//*Intent delConfirm = new Intent(ActivityDeliveryOrders.this, ActivityDeliverConfirm.class);
                    startActivity(delConfirm);*//*
                    ShowPopup(8, "");
                }
                if (state.equals("PD")) {
                    String otp = response.getString("otp");

                    ShowPopup(2, otp);

                }
                if (state.equals("FL")) {
                    ShowPopup(3, "");

                }
                if (state.equals("DN")) {
                    ShowPopup(4, "");
                }
                if (state.equals("CN")) {
                    ShowPopup(5, "");
                }
                if (state.equals("TO")) {
                    ShowPopup(6, "");
                }
                if (state.equals("FN")) {
                    ShowPopup(7, "");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public void onFailure(VolleyError error) {
        Log.d("ActivityDeliveryHistoryList class", Objects.requireNonNull(error.getMessage()));

        Toast.makeText(this, "CHECK YOUR INTERNET CONNECTION!", Toast.LENGTH_LONG).show();

    }

}
