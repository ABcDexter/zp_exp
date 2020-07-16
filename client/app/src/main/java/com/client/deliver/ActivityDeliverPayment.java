package com.client.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityDeliverPayment extends ActivityDrawer implements View.OnClickListener {

    TextView upiPayment, cost;
    final int UPI_PAYMENT = 0;
    String strAuth, DID;

    private static final String TAG = "ActivityDeliverPayment";
    private static final String DELIVERY_OTP = "DeliveryOtp";
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";

    public static final String DELIVERY_DETAILS = "com.client.delivery.details";
    public static final String DELIVERY_ID = "DeliveryID";

    private static ActivityDeliverPayment instance;
    Dialog myDialog;

    Button done;
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
        done = findViewById(R.id.confirm_btn);

        upiPayment.setOnClickListener(this);
        done.setOnClickListener(this);
        checkStatus();
    }

    public static ActivityDeliverPayment getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting user-delivery-pay API
        if (id == 1) {
            String otp = response.getString("otp");
            SharedPreferences sp_price = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
            sp_price.edit().putString(DELIVERY_OTP, otp).apply();
            checkStatus();
        }
        //response on hitting user-trip-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {

                    Intent home = new Intent(ActivityDeliverPayment.this, ActivityDeliverThankYou.class);
                    startActivity(home);
                    finish();

                } else if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("AS")) {
                        String price = response.getString("price");
                        cost.setText(price);
                    }
                    if (status.equals("TR") || status.equals("FN")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("32");
                        startService(intent);
                    }
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
                String amount = cost.getText().toString();
                String note = "Payment for rental service";
                String name = "Zipp-E";
                String upiId = "9084083967@ybl";
                payUsingUpi(amount, upiId, name, note);
                break;

            case R.id.confirm_btn:
                paymentMade();

        }
    }

    private void paymentMade() {
        String auth = strAuth;
        String did =DID;
        params.put("auth", auth);
        params.put("did", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " price=" + cost.getText().toString());
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
        String did =DID;
        params.put("auth", auth);
        params.put("did", did);
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

}
