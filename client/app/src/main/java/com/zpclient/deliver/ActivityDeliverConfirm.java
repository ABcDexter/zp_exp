package com.zpclient.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zpclient.ActivityDrawer;
import com.zpclient.R;
import com.zpclient.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;

public class ActivityDeliverConfirm extends ActivityDrawer implements View.OnClickListener {
    TextView upiPayment, cost, payOnPickup, payOnDelivery;
    final int UPI_PAYMENT = 0;
    ScrollView scrollView;
    ImageButton infoPayment; //infoTip;
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
    //public static final String EXPRESS = "Express";
    public static final String DEL_TYPE = "DeliveyType";// 1 means express delivery, 0 means standard delivery

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
    public static final String R_STND_DELVY = "R_STND_DELVY";//TODO find better way
    private static ActivityDeliverConfirm instance;
    Dialog myDialog;

    ImageButton done;
    ActivityDeliverConfirm a = ActivityDeliverConfirm.this;
    Map<String, String> params = new HashMap();
    String stringAuth, distance, did,
            pAddress, pLat, pLng, pName, pLand, pPin, pMobile, pHour, pMinute, pYear, pMonth, pDay, detailsPick,
            fr, li, none, kw, kc, pe, br, no, detailsPackage, conType, conSize, express,
            dLat, dLng, dName, dAddress, dLand, dPin, dMobile, dHour, dMinute, dYear, dMonth, dDay, detailsDrop;

    //ImageView zbeeR, zbeeL;
    // Button cancel;
    Animation animMoveL2R, animMoveR2L;
    String standtime;
    String pMode = "00";
    String  costOnly;
    Button dummy;

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
        express = pref.getString(DEL_TYPE, "");

        if (express.equals("0")) {
            String[] splitArray = pHour.split(":");
            standtime = splitArray[0];
            Log.d(TAG, "time slot = " + standtime);
            //pHour=standtime;
        }
        cost = findViewById(R.id.payment);
        //edTip = findViewById(R.id.tip);
        infoPayment = findViewById(R.id.infoPayment);
        //infoTip = findViewById(R.id.infoTip);
        done = findViewById(R.id.confirm_btn);
        scrollView = findViewById(R.id.scrollViewDC);
        //cancel = findViewById(R.id.cancelRequest);

        //checkStatus();
        done.setOnClickListener(this);
        infoPayment.setOnClickListener(this);
        //infoTip.setOnClickListener(this);

        myDialog = new Dialog(this);
        //deliveryEstimate();

        deliveryEstimate();

        animMoveL2R = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_l2r);
        animMoveR2L = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_r2l);
        upiPayment = findViewById(R.id.upi);
        payOnPickup = findViewById(R.id.pay_pickup);
        payOnDelivery = findViewById(R.id.pod);
        dummy = findViewById(R.id.dummy);

        upiPayment.setOnClickListener(this);
        payOnDelivery.setOnClickListener(this);
        payOnPickup.setOnClickListener(this);
        dummy.setOnClickListener(this);


    }

    public static ActivityDeliverConfirm getInstance() {
        return instance;
    }

    private void deliveryEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", pLat);
        params.put("srclng", pLng);
        params.put("dstlat", dLat);
        params.put("dstlng", dLng);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("express", express);//0,1
        params.put("pmode", "1");

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pLat
                + " srclng= " + pLng + " dstlat=" + dLat + " dstlng=" + dLng
                + " itype= " + conType + " idim= " + conSize + " express= " + express + " pmode= " + "1");
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-estimate");

        UtilityApiRequestPost.doPOST(a, "user-delivery-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    private void showProgressIndication() {
        SquareProgressBar squareProgressBar = findViewById(R.id.sprogressbar);
        squareProgressBar.setImage(R.drawable.btn_bkg);
        squareProgressBar.setVisibility(View.VISIBLE);
        squareProgressBar.setProgress(50.0);
        squareProgressBar.setWidth(10);
        squareProgressBar.setIndeterminate(true);
        squareProgressBar.setColor("#D7FB05");
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.confirm_btn) {
            showProgressIndication();
            userDeliverySchedule();

        } else if (id == R.id.infoPayment) {
            ShowPopup();

        } else if (id == R.id.upi) {
            upiPayment.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            payOnDelivery.setBackgroundResource(R.drawable.rect_box_outline);
            payOnPickup.setBackgroundResource(R.drawable.rect_box_outline);
            String amount = costOnly;
            String note = "Payment for rental service";
            String name = "Zipp-E";
            String upiId = "rajnilakshmi@ybl";
            payUsingUpi(amount, upiId, name, note);
        } else if (id == R.id.dummy) {
            pMode = "1";
            userDeliverySchedule();

        } else if (id == R.id.pay_pickup) {
            upiPayment.setBackgroundResource(R.drawable.rect_box_outline);
            payOnDelivery.setBackgroundResource(R.drawable.rect_box_outline);
            payOnPickup.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            pMode = "2";
            //userDeliverySchedule();
        } else if (id == R.id.pod) {
            payOnDelivery.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            payOnPickup.setBackgroundResource(R.drawable.rect_box_outline);
            upiPayment.setBackgroundResource(R.drawable.rect_box_outline);
            pMode = "3";
            //userDeliverySchedule();
        }
    }

    void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(ActivityDeliverConfirm.this, R.string.no_upi_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.d("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String[] equalStr = response[i].split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(ActivityDeliverConfirm.this, R.string.transaction_successful, Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
                pMode = "1";
                userDeliverySchedule();
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityDeliverConfirm.this, R.string.payment_cancelled_by_user, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityDeliverConfirm.this, R.string.transaction_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityDeliverConfirm.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    /*private void moveit() {
        zbeeL.setVisibility(View.VISIBLE);
        zbeeR.startAnimation(animMoveL2R);
        zbeeL.startAnimation(animMoveR2L);
        *//*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zbeeL, "translationX", 1500, 0f);
        objectAnimator.setDuration(1500);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zbeeR, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);*//*
    }*/
// api to 
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
        params.put("fr", fr);
        params.put("li", li);
        params.put("kc", kc);
        params.put("kw", "0");
        params.put("pe", pe);
        params.put("express", express);//0,1
        params.put("pYear", pYear);
        params.put("pMonth", pMonth);
        params.put("pDate", pDay);
        if (express.equals("0")) {
            params.put("pHour", standtime);
        } else params.put("pHour", pHour);

        params.put("pMinute", pMinute);
        params.put("pmode", pMode);

        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pLat
                + " srclng= " + pLng + " dstlat=" + dLat + " dstlng=" + dLng + " srcphone=" + pMobile
                + " dstphone=" + dMobile + " srcper=" + pName + " dstper=" + dName + " srcadd=" + pAddress
                + " dstadd=" + dAddress + " srcpin=" + pPin + " dstpin=" + dPin + " srcland=" + pLand
                + " dstland=" + dLand + " det= none" + detailsPackage
                + " express=" + express + " pYear=" + pYear + " pMonth=" + pMonth + " pDate=" + pDay
                + " pHour=" + pHour + " pMinute=" + pMinute + " itype= " + conType + " idim= "
                + conSize + " fr= " + fr + " br= " + br + " li= " + li + " pe= " + pe + " kc= "
                + kc + " kw= " + kw + " no= " + no + " pmode=" + pMode);
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


    private void ShowPopup() {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        //infoText.setText(getString(R.string.base_price) + " ₹ 15" + "\n" + getString(R.string.distance) + distance + " km");
        infoText.setText(getString(R.string.base_price, distance));

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
        if (id == 1) {
            String price = response.getString("price");
            distance = response.getString("dist");
            //cost.setText("₹ " + price);
            cost.setText(getString(R.string.message_rs,price));
            costOnly = price;
        }
        //response on hitting user-delivery-schedule API
        if (id == 2) {
            did = response.getString("scid");
            SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(DELIVERY_ID, did).apply();
            Intent home = new Intent(ActivityDeliverConfirm.this, ActivityDeliverThankYou.class);
            startActivity(home);
            finish();

        }
        //response on hitting user-delivery-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("SC")) {
                        String price = response.getString("price");
                        cost.setText(getString(R.string.message_rs,price));
                        costOnly = price;
                        /*Intent as = new Intent(ActivityDeliverConfirm.this, ActivityDeliverPayment.class);
                        startActivity(as);*/
                        SharedPreferences sp_price = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                        sp_price.edit().putString(DELIVERY_PRICE, price).apply();
                    }
                }
                if (active.equals("false")) {

                    //deliveryRetire();
                    Intent home = new Intent(ActivityDeliverConfirm.this, ActivityDeliverThankYou.class);
                    startActivity(home);
                    finish();

                    SharedPreferences preferencesD = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferencesD.edit();
                    editor1.remove(DELIVERY_ID);
                    editor1.apply();
                    SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
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

                }/*else {
                    Intent homePage = new Intent(ActivityDeliverConfirm.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }*/

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
        //response on hitting user-delivery-retire API
        if (id == 5) {
            Intent home = new Intent(ActivityDeliverConfirm.this, ActivityDeliverThankYou.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverConfirm.this, ActivityDeliveryReview.class));
        finish();
    }
}

