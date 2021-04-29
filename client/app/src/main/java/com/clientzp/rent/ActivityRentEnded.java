package com.clientzp.rent;

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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityRentEnded extends ActivityDrawer implements View.OnClickListener {

    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    private static final String TAG = "ActivityRentEnded";
    private static ActivityRentEnded instance;
    final int UPI_PAYMENT = 0;
    TextView upiPayment, cost;
    String stringAuthCookie;
    ImageButton payNow, info;
    ActivityRentEnded a = ActivityRentEnded.this;
    ScrollView scrollView;
    String CostOnly;
    Dialog myDialog;
    Button dummy;

    public static ActivityRentEnded getInstance() {
        return instance;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rent_ended, null, false);
        frameLayout.addView(activityView);
        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");

        upiPayment = findViewById(R.id.upi);
        cost = findViewById(R.id.payment);
        dummy = findViewById(R.id.dummy);
        dummy.setOnClickListener(this);
        //cost.setText("₹ 100.00");
        //getPrice();
        upiPayment.setOnClickListener(this);
        info = findViewById(R.id.infoCost);
        info.setOnClickListener(this);
        payNow = findViewById(R.id.pay_now);
        payNow.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollView_ride_OTP);
        myDialog = new Dialog(this);

        checkStatus();
    }

    private void getInfo() {
        SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String tid = prefTripDetails.getString(TRIP_ID, "");
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("tid", tid);
        JSONObject parameters = new JSONObject(params);

        /*Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");*/
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        //Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting auth-trip-get-info API
        if (id == 1) {
            Intent rate = new Intent(ActivityRentEnded.this, ActivityRateRent.class);
            startActivity(rate);
            finish();
        }
        //response on hitting user-trip-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {
                    String tid = response.getString("tid");

                    //Log.d(TAG, "active=" + active + " tid="+tid);

                    if (!tid.equals("-1")) {
                        getInfo();
                    }
                } else if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("TR") || status.equals("FN")) {
                        String price = response.getString("price");
                        cost.setText(getString(R.string.message_rs, price));
                        CostOnly = price;

                    }
                    new Handler().postDelayed(this::checkStatus, 30000);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        //response on hitting user-give-otp API
        if (id == 6) {
            Intent rate = new Intent(ActivityRentEnded.this, ActivityRateRent.class);
            startActivity(rate);
            finish();
            //TODO remove later
            //Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        }
    }

    public void onFailure(VolleyError error) {
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dummy) {
            rentPay();
        } else if (id == R.id.upi) {
            String amount = CostOnly;
            String note = "Payment for rental service";
            String name = "Zipp-E";
            String upiId = "9084083967@ybl";
            payUsingUpi(amount, upiId, name, note);

        } else if (id == R.id.pay_now) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, R.string.make_payment_to_continue, Snackbar.LENGTH_INDEFINITE);
            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        } else if (id == R.id.infoCost) {
            ShowPopup();
        }
    }

    private void ShowPopup() {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        infoText.setText(R.string.balance_amount_as_per_selection);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        wmlp.y = 55;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        myDialog.setCanceledOnTouchOutside(true);
    }

    private void paymentMade() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("price", cost.getText().toString());
        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Values: auth=" + auth + " price=" + cost.getText().toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-give-otp");*/
        UtilityApiRequestPost.doPOST(a, "user-give-otp", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void rentPay() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        //params.put("otp", OTP.getText().toString());
        JSONObject parameters = new JSONObject(params);
        ActivityRentEnded a = ActivityRentEnded.this;
        /*Log.d(TAG, "Values: auth=" + auth *//*+ " otp=" + OTP.getText().toString()*//*);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rent-pay");*/
        UtilityApiRequestPost.doPOST(a, "user-rent-pay", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 6);
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

        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");*/
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
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
            Toast.makeText(ActivityRentEnded.this, R.string.no_upi_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    //Log.d("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    //Log.d("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                //Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(this)) {
            String str = data.get(0);
            //Log.d("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
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
                Toast.makeText(ActivityRentEnded.this, R.string.transaction_successful, Toast.LENGTH_SHORT).show();
                rentPay();
                //Log.d("UPI", "responseStr: " + approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityRentEnded.this, R.string.payment_cancelled_by_user, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityRentEnded.this, R.string.transaction_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityRentEnded.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

}
