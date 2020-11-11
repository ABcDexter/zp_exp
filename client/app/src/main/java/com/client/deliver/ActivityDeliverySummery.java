package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityDeliverySummery extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityDeliverySummery";
    String stringAuthKey, stringSCID;
    TextView pName, pNum, dName, dNum;
    TextView pAddress, dAddress, content, size, deliveryType, time, date;
    //Dialog myDialog;
    TextView dialog_txt, trackDelivery;
    SwipeRefreshLayout swipeRefresh;
    ScrollView scrollView;
    ActivityDeliverySummery a = ActivityDeliverySummery.this;
    Map<String, String> params = new HashMap();

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

    public static final String REVIEW = "com.delivery.Review";//TODO find better way
    public static final String R_C_TYPE = "CTYPE";
    public static final String R_C_SIZE = "CSIZE";
    public static final String R_C_FRAGILE = "CFRAGILE";
    public static final String R_C_LIQUID = "CLIQUID";
    public static final String R_C_COLD = "CCOLD";
    public static final String R_C_WARM = "CWARM";
    public static final String R_C_PERISHABLE = "CPERISHABLE";
    public static final String R_C_NONE = "CNONE";
    public static final String R_EXP_DELVY = "R_EXP_DELVY";//TODO find better way
    public static final String R_STND_DELVY = "R_STND_DELVY";//TODO find better waypublic static final String REVIEW = "com.delivery.Review";//TODO find better way


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_delivery_summery, null, false);
        frameLayout.addView(activityView);
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        Intent intent = getIntent();
        stringSCID = intent.getStringExtra("SCID");
        Log.d(TAG, "SCID" + stringSCID);
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        pName = findViewById(R.id.pick_name);
        pNum = findViewById(R.id.pick_mobile);
        dName = findViewById(R.id.drop_name);
        dNum = findViewById(R.id.drop_mobile);
        pAddress = findViewById(R.id.pick_address);
        dAddress = findViewById(R.id.drop_address);
        content = findViewById(R.id.package_content);
        size = findViewById(R.id.package_size);
        //care = findViewById(R.id.package_care);
        deliveryType = findViewById(R.id.delv_type);
        time = findViewById(R.id.delv_time);
        date = findViewById(R.id.delv_date);
        userDeliverySummery();

        trackDelivery = findViewById(R.id.track_delivery);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        //trackDelivery.setOnClickListener(this);

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

    protected void userDeliverySummery() {
        String auth = stringAuthKey;
        String scid = stringSCID;
        params.put("auth", auth);
        params.put("scid", scid);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-delivery-data");
        Log.d(TAG, "Values: auth=" + auth + " scid=" + scid);

        UtilityApiRequestPost.doPOST(a, "auth-delivery-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    String srcName, srcPhn, dstName, dstPhn, fr, li, pe, kw, kc, express, st, itype, idim, srcAdd, dstAdd, det, ptime, pDate;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-delivery-data API
        if (id == 2) {

            fr = response.getString("fr");//False,True
            li = response.getString("li");
            pe = response.getString("pe");
            kw = response.getString("kw");
            kc = response.getString("kc");
            express = response.getString("express");//True-->expree; False-->standard
            itype = response.getString("itype");
            idim = response.getString("idim");
            srcName = response.getString("srcper");
            dstName = response.getString("dstper");
            srcAdd = response.getString("srcadd");
            dstAdd = response.getString("dstadd");
            srcPhn = response.getString("srcphone");
            dstPhn = response.getString("dstphone");
            det = response.getString("det");
            ptime = response.getString("picktime");
            pDate = response.getString("pickdate");

            pName.setText(srcName);
            pNum.setText(srcPhn);
            pAddress.setText(srcAdd);
            dName.setText(dstName);
            dNum.setText(dstPhn);
            dAddress.setText(dstAdd);
            time.setText(ptime);
            date.setText(pDate);

            /*if (fr.equals("True") || li.equals("True") || kc.equals("True") || pe.equals("True") || kw.equals("True")) {
                care.setText(fr + " " + li + " " + kc + " " + pe + " " + " " + kw + " ");
            }*/
            if (express.equals("True")) {
                deliveryType.setText(R.string.express);
            } else deliveryType.setText(R.string.standard);

            switch (itype) {
                case "DOC":
                    content.setText(R.string.documents_books);
                    break;
                case "FOO":
                    content.setText(R.string.restaurant_orders);
                    break;
                case "HOU":
                    content.setText(R.string.household_items);
                    break;
                case "ELE":
                    content.setText(R.string.electronics_electrical);
                    break;
                case "CLO":
                    content.setText(R.string.clothes_accessories);
                    break;
                case "MED":
                    content.setText(R.string.medicines);
                    break;
                default:
                    content.setText(itype);
                    break;
            }

            switch (idim) {
                case "S":
                    size.setText(R.string.small);
                    break;
                case "M":
                    size.setText(R.string.medium);
                    break;
                case "L":
                    size.setText(R.string.large);
                    break;
                case "XL":
                    size.setText(R.string.x_large);
                    break;
                case "XXL":
                    size.setText(R.string.x_x_large);
                    break;
                default:
                    size.setText(idim);
                    break;
            }



        }

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

                    Intent payment = new Intent(ActivityDeliverySummery.this, ActivityDeliverPayment.class);
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

                    SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
                    SharedPreferences.Editor reditor = review.edit();
                    reditor.remove(R_C_COLD);
                    reditor.remove(R_C_FRAGILE);
                    reditor.remove(R_C_LIQUID);
                    reditor.remove(R_C_NONE);
                    reditor.remove(R_C_WARM);
                    reditor.remove(R_C_PERISHABLE);
                    reditor.remove(R_C_TYPE);
                    reditor.remove(R_C_SIZE);
                    reditor.remove(R_EXP_DELVY);
                    reditor.remove(R_STND_DELVY);
                    reditor.apply();
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
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverySummery.this, ActivityDeliveryTimeSlot.class));
        finish();
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
            Log.d(TAG, "AS OTP = "+ info);
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
            Log.d(TAG, "RC OTP = "+ info);
        }

    }

}
