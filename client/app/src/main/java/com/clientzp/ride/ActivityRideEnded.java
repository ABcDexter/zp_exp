package com.clientzp.ride;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.ActivityRateZippe;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.clientzp.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityRideEnded extends ActivityDrawer implements View.OnClickListener {
    public static final String AUTH_KEY = "AuthKey";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    TextView upiPayment, cost, cash;
    final int UPI_PAYMENT = 0;
    String stringAuthCookie;
    private static final String TAG = "ActivityRideEnded";
    private static ActivityRideEnded instance;
    Dialog myDialog;
    String onlyPrice;
    ImageButton paynow;
    //Button done;
    ActivityRideEnded a = ActivityRideEnded.this;
    Map<String, String> params = new HashMap();
    ScrollView scrollView;
    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_ended, null, false);
        frameLayout.addView(activityView);
        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");
        txt = findViewById(R.id.txt);

        upiPayment = findViewById(R.id.upi);
        cost = findViewById(R.id.payment);
        cash = findViewById(R.id.cash);
        cash.setOnClickListener(this);
        getPrice();
        upiPayment.setOnClickListener(this);
        checkStatus();
        /*done = findViewById(R.id.confirm_btn);
        done.setOnClickListener(this);*/
        myDialog = new Dialog(this);

        paynow = findViewById(R.id.pay_now);
        paynow.setOnClickListener(this);
        scrollView = findViewById(R.id.scrollView_ride_OTP);
    }

    public static ActivityRideEnded getInstance() {
        return instance;
    }

    private void getPrice() {
        SharedPreferences prefTripDetails = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String tid = prefTripDetails.getString(TRIP_ID, "");
        String auth = stringAuthCookie;
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
            String actualPrice = response.getString("price");
            cost.setText(getString(R.string.message_rs, actualPrice));
            onlyPrice = actualPrice;
        }
        //response on hitting user-trip-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("false")) {
                    Intent home = new Intent(ActivityRideEnded.this, ActivityRateZippe.class);
                    startActivity(home);
                    finish();

                } else if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("TR")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("05");
                        startService(intent);
                        txt.setText(R.string.you_have_chosen_to_end);
                    }
                    if (status.equals("FN")) {
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("05");
                        startService(intent);
                    }
                } else {
                    Intent homePage = new Intent(ActivityRideEnded.this, ActivityRateZippe.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //response on hitting API for payment made
        if (id == 3) {
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
        if (id == R.id.upi) {
            String amount = onlyPrice;
            String note = "Payment for ride service";
            String name = "Zipp-E";
            String upiId = "rajnilakshmi@ybl";
            payUsingUpi(amount, upiId, name, note);

        } else if (id == R.id.cash) {
            ShowPopup();
        } else if (id == R.id.pay_now) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, R.string.make_payment_ride, Snackbar.LENGTH_INDEFINITE);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private void ShowPopup() {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(getString(R.string.pay_cash) + cost.getText().toString());
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        myDialog.setCanceledOnTouchOutside(true);
    }

    private void paymentMade() {
        String auth = stringAuthCookie;
        params.put("auth", auth);
        params.put("price", cost.getText().toString());
        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Values: auth=" + auth + " price=" + cost.getText().toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");*/
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
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        /*Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");*/
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
            Toast.makeText(ActivityRideEnded.this, R.string.no_upi_found, Toast.LENGTH_SHORT).show();
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
                paymentMade();
                Toast.makeText(ActivityRideEnded.this, R.string.transaction_successful, Toast.LENGTH_LONG).show();
                //Log.d("UPI", "responseStr: " + approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityRideEnded.this, R.string.payment_cancelled_by_user, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ActivityRideEnded.this, R.string.transaction_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ActivityRideEnded.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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

}
