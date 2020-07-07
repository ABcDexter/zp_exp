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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.client.ActivityDrawer;
import com.client.R;

import java.util.ArrayList;

public class ActivityDeliverPayment extends ActivityDrawer implements View.OnClickListener {

    TextView upiPayment, cost;
    EditText edTip;
    ImageButton infoTip, infoPayment;
    final int UPI_PAYMENT = 0;
    String stringAuthCookie;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    private static final String TAG = "ActivityDeliverPayment";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static ActivityDeliverPayment instance;
    Dialog myDialog;

    Button done;
    ActivityDeliverPayment a = ActivityDeliverPayment.this;

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
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");

        upiPayment = findViewById(R.id.upi);
        cost = findViewById(R.id.payment);
        edTip = findViewById(R.id.tip);
        infoTip = findViewById(R.id.infoTip);
        infoPayment = findViewById(R.id.infoPayment);
        done = findViewById(R.id.done_btn);

        //getPrice();
        upiPayment.setOnClickListener(this);
        //checkStatus();
        done.setOnClickListener(this);
        infoTip.setOnClickListener(this);
        infoPayment.setOnClickListener(this);
        myDialog = new Dialog(this);


    }

    public static ActivityDeliverPayment getInstance() {
        return instance;
    }

    /*private void getPrice() {
        SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String tid = prefTripDetails.getString(TRIP_ID, "");
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tid);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting auth-trip-get-info API
        if (id == 1) {
            String actualPrice = response.getString("price");
            cost.setText(actualPrice);
        }
        //response on hitting user-trip-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {
                    Intent home = new Intent(ActivityRideEnded.this, ActivityWelcome.class);
                    startActivity(home);
                    finish();

                } else if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("TR") || status.equals("FN")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("05");
                        startService(intent);
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideEnded.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (id == 3) {
            Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upi:
                String amount = cost.getText().toString();
                String note = "Payment for delivery service";
                String name = "Zipp-E";
                String upiId = "9084083967@ybl";
                payUsingUpi(amount, upiId, name, note);
                Log.d(TAG, "amount: " + amount);
                break;

            case R.id.done_btn:
                //paymentMade();
                break;
            case R.id.infoTip:
                ShowPopup(1);
                break;
            case R.id.infoPayment:
                ShowPopup(2);
                break;

        }
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        if (id == 1) {
            infoText.setText("THIS AMOUNT IS CREDITED TO PARTNER'S ACCOUNT");
        }
        if (id == 2) {
            infoText.setText("BASE PRICE: " + "Rs. 30" + "\nDISTANCE: " + "15 km");
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    /*private void paymentMade() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("price", cost.getText().toString());
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " price=" + cost.getText().toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-give-otp", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void checkStatus() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }*/

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

