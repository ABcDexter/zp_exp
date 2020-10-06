package com.client.deliver;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityDeliverConfirm extends ActivityDrawer implements View.OnClickListener {

    TextView cost;
    EditText edTip;
    ScrollView scrollView;
    ImageButton infoPayment, infoTip;
    private static final String TAG = "ActivityDeliverConfirm";

    public static final String AUTH_KEY = "AuthKey";
    public static final String DELIVERY_DETAILS = "com.client.delivery.details";
    public static final String DELIVERY_ID = "DeliveryID";
    public static final String DELIVERY_PRICE = "Price";

    public static final String FRAGILE = "fragile";
    public static final String LIQUID = "liquid";
    public static final String KEEP_WARM = "keep_warm";
    public static final String KEEP_COLD = "keep_cold";
    public static final String NONE = "None";
    public static final String PERISHABLE = "Perishable";
    public static final String BREAKABLE = "Breakable";
    public static final String ADD_INFO_PACKAGE = "";

    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String PICK_NAME = "com.client.ride.PickName";
    public static final String PICK_YEAR = "PickYear";
    public static final String PICK_MONTH = "PickMonth";
    public static final String PICK_DAY = "PickDay";
    public static final String PICK_HOUR = "PickHour";
    public static final String PICK_MINUTE = "PickMinute";
    public static final String ADD_INFO_PICK_POINT = "AddInfoPickPoint";


    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String DROP_NAME = "com.client.ride.DropName";
    public static final String DROP_HOUR = "DropHour";
    public static final String DROP_MINUTE = "DropMinute";
    public static final String DROP_YEAR = "DropYear";
    public static final String DROP_MONTH = "DropMonth";
    public static final String DROP_DAY = "DropDay";
    public static final String ADD_INFO_DROP_POINT = "AddInfoDropPoint";

    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";

    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String EXPRESS = "Express";

    private static ActivityDeliverConfirm instance;
    Dialog myDialog;

    ImageButton done;
    ActivityDeliverConfirm a = ActivityDeliverConfirm.this;
    Map<String, String> params = new HashMap();
    String stringAuth, distance, did,
            pAddress, pLat, pLng, pName, pLand, pPin, pMobile, pHour, pMinute, pYear, pMonth, pDay, detailsPick,
            fr, li, none, kw, kc, pe, br, no, detailsPackage, conType, conSize, express,
            dLat, dLng, dName, dAddress, dLand, dPin, dMobile, dHour, dMinute, dYear, dMonth, dDay, detailsDrop;

    ImageView zbeeR, zbeeL;
    // Button cancel;
    Animation animMoveL2R, animMoveR2L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_deliver_confirm, null, false);
        frameLayout.addView(activityView);
        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefCookie.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        pAddress = pref.getString(ADDRESS_PICK, "");
        pLat = pref.getString(PICK_LAT, "");
        pLng = pref.getString(PICK_LNG, "");
        dAddress = pref.getString(ADDRESS_DROP, "");
        dLat = pref.getString(DROP_LAT, "");
        dLng = pref.getString(DROP_LNG, "");
        pLand = pref.getString(PICK_LANDMARK, "");
        dLand = pref.getString(DROP_LANDMARK, "");
        pPin = pref.getString(PICK_PIN, "");
        dPin = pref.getString(DROP_PIN, "");
        pMobile = pref.getString(PICK_MOBILE, "");
        dMobile = pref.getString(DROP_MOBILE, "");
        conType = pref.getString(CONTENT_TYPE, "");
        conSize = pref.getString(CONTENT_DIM, "");
        fr = pref.getString(FRAGILE, "");
        br = pref.getString(BREAKABLE, "");
        li = pref.getString(LIQUID, "");
        pe = pref.getString(PERISHABLE, "");
        kw = pref.getString(KEEP_WARM, "");//TODO look at it
        kc = pref.getString(KEEP_COLD, "");
        none = pref.getString(NONE, "");
        detailsPackage = pref.getString(ADD_INFO_PACKAGE, "");
        pName = pref.getString(PICK_NAME, "");
        dName = pref.getString(DROP_NAME, "");
        dHour = pref.getString(DROP_HOUR, "");
        dMinute = pref.getString(DROP_MINUTE, "");
        dYear = pref.getString(DROP_YEAR, "");
        dMonth = pref.getString(DROP_MONTH, "");
        dDay = pref.getString(DROP_DAY, "");
        detailsDrop = pref.getString(ADD_INFO_DROP_POINT, "");
        pHour = pref.getString(PICK_HOUR, "");
        pMinute = pref.getString(PICK_MINUTE, "");
        pYear = pref.getString(PICK_YEAR, "");
        pMonth = pref.getString(PICK_MONTH, "");
        pDay = pref.getString(PICK_DAY, "");
        detailsPick = pref.getString(ADD_INFO_PICK_POINT, "");
        express = pref.getString(EXPRESS, "");


        cost = findViewById(R.id.payment);
        edTip = findViewById(R.id.tip);
        infoPayment = findViewById(R.id.infoPayment);
        infoTip = findViewById(R.id.infoTip);
        done = findViewById(R.id.confirm_btn);
        scrollView = findViewById(R.id.scrollViewDC);
        //cancel = findViewById(R.id.cancelRequest);

        //checkStatus();
        done.setOnClickListener(this);
        infoPayment.setOnClickListener(this);
        infoTip.setOnClickListener(this);

        myDialog = new Dialog(this);
        //deliveryEstimate();
        zbeeR = findViewById(R.id.image_zbee);
        zbeeL = findViewById(R.id.image_zbee_below);

        Intent intent = getIntent();
        String price = intent.getStringExtra("PRICE");
        cost.setText("₹ " + price);
        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);
        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);


    }

    public static ActivityDeliverConfirm getInstance() {
        return instance;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                moveit();
                userDeliverySchedule();
                break;
            case R.id.infoTip:
                ShowPopup(1);
                break;
            case R.id.infoPayment:
                ShowPopup(2);
                break;
           /* case R.id.cancelRequest:
                cancelRequest();
                break;*/
        }
    }

    private void moveit() {
        zbeeL.setVisibility(View.VISIBLE);
        zbeeL.startAnimation(animMoveL2R);
        zbeeR.startAnimation(animMoveR2L);
        /*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zbeeL, "translationX", 1500, 0f);
        objectAnimator.setDuration(1500);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zbeeR, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*/
    }

    protected void userDeliverySchedule() {
        String auth = stringAuth;

        params.put("auth", auth);
        params.put("srclat", pLat);
        params.put("srclng", pLng);
        params.put("dstlat", dLat);
        params.put("dstlng", dLng);
        params.put("srcphone", pMobile);
        params.put("dstphone", dMobile);
        params.put("srcper", pName);
        params.put("dstper", dName);
        params.put("srcadd", pAddress);
        params.put("srcpin", pPin);
        params.put("srcland", pLand);
        params.put("dstadd", dAddress);
        params.put("dstpin", dPin);
        params.put("dstland", dLand);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("det", "none");//package details
        //params.put("det", detailsPackage);//package details
        /*params.put("srcdet", detailsPick);// src details
        params.put("dstdet", detailsDrop);//dst details*/
        params.put("fr", fr);
        params.put("li", li);
        params.put("kc", kc);
        params.put("kw", "0");
        params.put("pe", pe);
        //params.put("no", no);
        params.put("express", express);//0,1
        params.put("pYear", "2020");//TODO remove hard coded values
        params.put("pMonth", "9");//TODO remove hard coded values
        params.put("pDate", "2");//TODO remove hard coded values
        params.put("pHour", "17");
        params.put("pMinute", "30");
        params.put("pmode", "1");
        params.put("tip", edTip.getText().toString());

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pLat
                + " srclng= " + pLng + " dstlat=" + dLat + " dstlng=" + dLng + " srcphone=" + pMobile
                + " dstphone=" + dMobile + " srcper=" + pName + " dstper=" + dName + " srcadd=" + pAddress
                + " dstadd=" + dAddress + " srcpin=" + pPin + " dstpin=" + dPin + " srcland=" + pLand
                + " dstland=" + dLand + " det= none" + detailsPackage
                + " express=" + express + " pYear=" + pYear + " pMonth=" + pMonth + " pDate=" + pDay
                + " pHour=" + pHour + " pMinute=" + pMinute + " tip=" + edTip.getText().toString()
                + " itype= " + conType + " idim= " + conSize + " fr= " + fr + " br= " + br
                + " li= " + li + " pe= " + pe + " kc= " + kc + " kw= " + kw + " no= " + no);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-schedule");

        UtilityApiRequestPost.doPOST(a, "user-delivery-schedule", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void checkStatus() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("scid", did);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "user-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void cancelRequest() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("scid", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-cancel");
        UtilityApiRequestPost.doPOST(a, "user-delivery-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        if (id == 1) {
            infoText.setText(R.string.the_amount_shall_be_directly_credited_to_our_agent_account);
        }
        if (id == 2) {
            infoText.setText(getString(R.string.base_price) + "₹ 15" + "\nDistance : " + distance + " km");
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        wmlp.y = 50;   //y position

        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting user-delivery-schedule API
        if (id == 2) {
            did = response.getString("scid");
            SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(DELIVERY_ID, did).apply();
            Intent as = new Intent(ActivityDeliverConfirm.this, ActivityDeliverPayment.class);
            startActivity(as);
            //checkStatus();
        }
        //response on hitting user-delivery-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("SC")) {
                        String price = response.getString("price");
                        Intent as = new Intent(ActivityDeliverConfirm.this, ActivityDeliverPayment.class);
                        startActivity(as);
                        SharedPreferences sp_price = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                        sp_price.edit().putString(DELIVERY_PRICE, price).apply();
                    }
                } else {
                    Intent homePage = new Intent(ActivityDeliverConfirm.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //response on hitting user-delivery-cancel API
        if (id == 4) {
            Intent home = new Intent(ActivityDeliverConfirm.this, ActivityPackageDetails.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverConfirm.this, ActivityDeliveryReview.class));
        finish();
    }
}

