package com.client.deliver;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityDeliverPayment extends ActivityDrawer implements View.OnClickListener {

    TextView upiPayment, cost;
    final int UPI_PAYMENT = 0;
    String strAuth, DID, costOnly;

    private static final String TAG = "ActivityDeliverPayment";
    private static final String DELIVERY_OTP = "DeliveryOtp";
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";

    public static final String DELIVERY_DETAILS = "com.client.delivery.details";
    public static final String DELIVERY_ID = "DeliveryID";
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
    public static final String R_STND_DELVY = "R_STND_DELVY";//TODO find better way
    private static ActivityDeliverPayment instance;
    Dialog myDialog;
    ImageView infoCost;
    Button dummy;
    ActivityDeliverPayment a = ActivityDeliverPayment.this;
    Map<String, String> params = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_deliver_payment, null, false);
        frameLayout.addView(activityView);
        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        strAuth = prefCookie.getString(AUTH_KEY, "");

        SharedPreferences deliveryPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
        DID = deliveryPref.getString(DELIVERY_ID, "");

        upiPayment = findViewById(R.id.upi);
        cost = findViewById(R.id.payment);
        dummy = findViewById(R.id.dummy);
        infoCost = findViewById(R.id.infoCost);
        infoCost.setOnClickListener(this);
        upiPayment.setOnClickListener(this);
        dummy.setOnClickListener(this);
        checkStatus();
        myDialog = new Dialog(this);

    }

    private void ShowPopup() {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(getString(R.string.please_pay) + cost.getText().toString());
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 40;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(true);
    }

    public static ActivityDeliverPayment getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting user-delivery-pay API
        if (id == 1) {
            /*SharedPreferences sp_price = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
            sp_price.edit().putString(DELIVERY_OTP, otp).apply();*/
            checkStatus();
        }
        //response on hitting user-delivery-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {

                    Intent home = new Intent(ActivityDeliverPayment.this, ActivityDeliverThankYou.class);
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

                } else if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("SC")) {
                        String price = response.getString("price");
                        cost.setText(getString(R.string.rs) + price);
                        costOnly = price;
                    }
                    /*if (status.equals("FN")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("32");
                        startService(intent);

                        SharedPreferences preferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        SharedPreferences preferencesD = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = preferencesD.edit();
                        editor1.clear();
                        editor1.apply();

                    }*/
                } else {
                    Intent homePage = new Intent(ActivityDeliverPayment.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upi:
                String amount = costOnly;
                String note = "Payment for rental service";
                String name = "Zipp-E";
                String upiId = "rajnilakshmi@ybl";
                payUsingUpi(amount, upiId, name, note);
                break;

            case R.id.dummy:
                paymentMade();
            case R.id.infoCost:
                ShowPopup();
                break;
        }
    }

    private void paymentMade() {
        String auth = strAuth;
        String did = DID;
        params.put("auth", auth);
        params.put("scid", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " scid=" + did);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-pay");
        UtilityApiRequestPost.doPOST(a, "user-delivery-pay", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }


    public void checkStatus() {
        String auth = strAuth;
        String did = DID;
        params.put("auth", auth);
        params.put("scid", did);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "user-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
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
            Toast.makeText(ActivityDeliverPayment.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
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
                break;
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
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
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
                Toast.makeText(ActivityDeliverPayment.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
                paymentMade();
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityDeliverPayment.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityDeliverPayment.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityDeliverPayment.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverPayment.this, ActivityDeliverConfirm.class));
        finish();
    }
}
