package com.clientzp.Service;

<<<<<<< HEAD
=======
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

>>>>>>> dev
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> dev
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

=======
>>>>>>> dev
import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityServiceSummery extends ActivityDrawer {
    private static final String TAG = "ActivityServiceSummery.class";
    String stringAuthKey, stringSCID;
    TextView dialog_txt;
    SwipeRefreshLayout swipeRefresh;
    ScrollView scrollView;
    ActivityServiceSummery a = ActivityServiceSummery.this;
    Map<String, String> params = new HashMap();
    public static final String AUTH_KEY = "AuthKey";
    TextView rideRate, ridePrice, rideTax, rideTotal, rideVehicle, rideTime, rideDate, txtSummery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_summery, null, false);
        frameLayout.addView(activityView);
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        Intent intent = getIntent();
        stringSCID = intent.getStringExtra("TID");
<<<<<<< HEAD
        //Log.d(TAG, "TID" + stringSCID);
=======
        Log.d(TAG, "TID" + stringSCID);
>>>>>>> dev
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        rideRate = findViewById(R.id.ride_rate);
        ridePrice = findViewById(R.id.ride_price);
        rideTax = findViewById(R.id.ride_tax);
        rideTotal = findViewById(R.id.ride_total);
        rideVehicle = findViewById(R.id.ride_vehicle);
        rideTime = findViewById(R.id.ride_time);
        rideDate = findViewById(R.id.ride_date);
        txtSummery = findViewById(R.id.txtSummery);
        txtSummery.setText(R.string.rent_summary);
        userDeliverySummery();

        swipeRefresh = findViewById(R.id.swipeRefresh);

        //getInfo();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();//this will recreate or reload the activity when swiped down
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    protected void userDeliverySummery() {
        String auth = stringAuthKey;
        String scid = stringSCID;
        params.put("auth", auth);
        params.put("tid", scid);

        JSONObject parameters = new JSONObject(params);
<<<<<<< HEAD
        /*Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-trip-data");
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);*/
=======
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-trip-data");
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);
>>>>>>> dev

        UtilityApiRequestPost.doPOST(a, "auth-trip-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    String rate, price, tax, total, vtype, time, date;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
<<<<<<< HEAD
        //Log.d(TAG, "RESPONSE:" + response);
=======
        Log.d(TAG, "RESPONSE:" + response);
>>>>>>> dev

        //response on hitting auth-trip-data API
        if (id == 2) {

            vtype = response.getString("rvtype");
            rate = response.getString("rate");
            price = response.getString("price");
            tax = response.getString("tax");
            total = response.getString("total");
            time = response.getString("time");
            date = response.getString("sdate");

<<<<<<< HEAD
            rideRate.setText(getString(R.string.message_rs, rate));
            ridePrice.setText(getString(R.string.message_rs, price));
            rideTax.setText(getString(R.string.message_rs, tax));
            rideTotal.setText(getString(R.string.message_rs, total));
=======
            rideRate.setText(getString(R.string.message_rs,rate));
            ridePrice.setText(getString(R.string.message_rs,price));
            rideTax.setText(getString(R.string.message_rs,tax));
            rideTotal.setText(getString(R.string.message_rs,total));
>>>>>>> dev
            rideVehicle.setText(vtype);
            rideTime.setText(getString(R.string.message_min, time));
            //rideTime.setText(time + R.string.mins);
            rideDate.setText(date);

            switch (vtype) {
                case "0":
                    rideVehicle.setText(R.string.e_cycle);
                    break;
                case "1":
                    rideVehicle.setText(R.string.e_scooty);
                    break;
                case "2":
                    rideVehicle.setText(R.string.e_bike);
                    break;
                case "3":
                    rideVehicle.setText(R.string.zbee);
                    break;
                default:
                    rideVehicle.setText(vtype);
                    break;
            }


        }

        //response on hitting user-delivery-get-info API
        if (id == 1) {
            try {

                String st = response.getString("st");

                if (st.equals("RQ") || st.equals("PD") || st.equals("SC")) {

                    ShowPopup(0, "");

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

    }

    public void onFailure(VolleyError error) {
<<<<<<< HEAD
        /*Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
=======
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
>>>>>>> dev
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityServiceSummery.this, ActivityServiceHistoryList.class));
        finish();
    }

    private void ShowPopup(int id, String info) {

        //myDialog.setContentView(R.layout.popup_new_request);
        dialog_txt = findViewById(R.id.txtInfo);
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
            //dialog_txt.setText(R.string.your_delivery_agent_will_arrive_shortly + info);
            dialog_txt.setText(String.format("OTP : %s", info));
<<<<<<< HEAD
            //Log.d(TAG, "AS OTP = " + info);
=======
            Log.d(TAG, "AS OTP = " + info);
>>>>>>> dev
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
            //dialog_txt.setText(R.string.agent_has_arrived + INFO);
            dialog_txt.setText(String.format("OTP : %s", info));
<<<<<<< HEAD
            //Log.d(TAG, "RC OTP = " + info);
=======
            Log.d(TAG, "RC OTP = " + info);
>>>>>>> dev
        }

    }

}
