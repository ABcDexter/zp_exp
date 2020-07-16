package com.client.rent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.client.ride.ActivityRideHome;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityRentOTP extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRideOTP";

    TextView origin, destination, dName, dPhone, vNum, OTP, costEst;
    ImageButton cancel;
    ScrollView scrollView;

    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String COST_DROP = "CostDrop";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";

    public static final String AUTH_KEY = "AuthKey";

    private static ActivityRentOTP instance;
    Dialog myDialog;
    ImageButton costInfo, priceInfo;
    String stringAuthCookie;
    Button giveOTP;
    TextView txt_timing, txt_hour, upiRental, rentalCost;
    ImageButton next;
    ActivityRentOTP a = ActivityRentOTP.this;

    // int number = 0;
    final int UPI_PAYMENT = 0;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-trip-cancel API
        if (id == 1) {
            Intent home = new Intent(ActivityRentOTP.this, ActivityWelcome.class);
            startActivity(home);
            finish();
        }

//response on hitting user-trip-get-status API
        if (id == 2) {
            try {

                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("AS")) {
                        String timeRem = response.getString("time");
                        String price = response.getString("price");
                        costEst.setText(price);
                       /* if (timeRem.equals("")) {
                            //ShowPopup(3);
                        }*/
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("13");
                        startService(intent);
                    }
                    if (status.equals("ST")) {
                        Intent st = new Intent(ActivityRentOTP.this, ActivityRentInProgress.class);
                        startActivity(st);
                    }
                } else {
                    Intent homePage = new Intent(ActivityRentOTP.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        //response on hitting user-rent-get-sup API
        if (id == 3) {
            String pn = response.getString("pn");
            String name = response.getString("name");
            dPhone.setText(pn);
            dName.setText(name);
            SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(DRIVER_NAME, name).apply();
            sp_cookie.edit().putString(DRIVER_PHN, pn).apply();
        }
        // response on hitting user-give-otp API
        if (id == 4) {
            //TODO remove later
            Log.d(TAG, "RESPONSE:" + response);

        }
        // response on hitting user-vehicle-hold API
        if (id == 5) {
            Log.d(TAG, "RESPONSE:" + response);

        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rent_otp, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");

        SharedPreferences prefPLoc = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = prefPLoc.getString(LOCATION_PICK, "");
        String stringDrop = prefPLoc.getString(LOCATION_DROP, "");
        String stringCost = prefPLoc.getString(COST_DROP, "");
        String otpPick = prefPLoc.getString(OTP_PICK, "");
        String vanPick = prefPLoc.getString(VAN_PICK, "");

        costEst = findViewById(R.id.adv_payment_otp);
        dName = findViewById(R.id.supervisor_name);
        dPhone = findViewById(R.id.supervisor_phone);
        OTP = findViewById(R.id.otp_rent);
        vNum = findViewById(R.id.v_no);
        origin = findViewById(R.id.pick_hub);
        destination = findViewById(R.id.drop_hub);
        cancel = findViewById(R.id.cancel_rent_booking);
        costInfo = findViewById(R.id.infoCost);
        scrollView = findViewById(R.id.scrollView_rent_OTP);
        giveOTP = findViewById(R.id.give_otp);

        upiRental = findViewById(R.id.upiRental);
        upiRental.setOnClickListener(this);

        cancel.setOnClickListener(this);
        costEst.setText(stringCost);
        costInfo.setOnClickListener(this);

        giveOTP.setOnClickListener(this);
        dPhone.setOnClickListener(this);

        myDialog = new Dialog(this);

        OTP.setText(otpPick);
        vNum.setText(vanPick);
        supDetails();

        if (stringDrop.isEmpty()) {
            destination.setText("DROP POINT");
        } else {
            int dropSpace = (stringDrop.contains(" ")) ? stringDrop.indexOf(" ") : stringDrop.length() - 1;
            String dropCutName = stringDrop.substring(0, dropSpace);
            destination.setText(dropCutName);
        }

        if (stringPick.isEmpty()) {
            origin.setText("PICK UP");
        } else {
            int pickSpace = (stringPick.contains(" ")) ? stringPick.indexOf(" ") : stringPick.length() - 1;
            String pickCutName = stringPick.substring(0, pickSpace);
            origin.setText(pickCutName);
        }
        checkStatus();
        ShowPopup(2);
    }

    private void supDetails() {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rent-get-sup");
        UtilityApiRequestPost.doPOST(a, "user-rent-get-sup", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        LinearLayout ll = myDialog.findViewById(R.id.layout_btn);
        TextView reject = myDialog.findViewById(R.id.reject_request);
        TextView accept = myDialog.findViewById(R.id.accept_request);

        if (id == 1) {
            infoText.setText("PAYMENT TO BE MADE BEFORE WE ALLOT YOU THE VEHICLE. BALANCE (IF ANY) WILL BE COLLECTED AT THE TIME OF DROPPING THE VEHICLE");
        }
        if (id == 2) {
            infoText.setText("Make payment only after checking the condition of vehicle");
        }
        if (id == 3) {
            infoText.setText("Make payment only after checking the condition of vehicle");
        }
        if (id == 4) {
            infoText.setText("HOLD VEHICLE FOR 1 HOUR?");

            reject.setOnClickListener(this);
            accept.setOnClickListener(this);
        }

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }


    public static ActivityRentOTP getInstance() {
        return instance;
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityRentOTP.this);
        alertDialog.setTitle("CANCEL RIDE");
        String[] items = {"DRIVER DENIED DESTINATION",
                "DRIVER DENIED PICKUP",
                "EXPECTED A SHORTER WAIT TIME",
                "UNABLE TO CONTACT DRIVER",
                "MY REASON IS NOT LISTED"};
        alertDialog.setCancelable(false);

        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        //Toast.makeText(ActivityRideOTP.this, "Thank you for your feedback.", Toast.LENGTH_LONG).show();
                        Snackbar snackbar = Snackbar
                                .make(scrollView, "Thank you for your feedback.", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        break;
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShowPopup(4);

            }
        });

        alertDialog.setPositiveButton("Don't Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
    }

    private void userCancelTrip() {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        JSONObject param = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-cancel");
        UtilityApiRequestPost.doPOST(a, "user-trip-cancel", param, 20000, 0, response -> {
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
            case R.id.cancel_ride_booking:
                showAlertDialog();
                break;
            case R.id.infoCost:
                ShowPopup(1);
                break;
            case R.id.give_otp: //TODO remove this later
                giveOtp();
                break;
            case R.id.upiRental:

                String amount = costEst.getText().toString();
                String note = "Payment for rental service";
                String name = "Zipp-E";
                String upiId = "9084083967@ybl";
                payUsingUpi(amount, upiId, name, note);
                break;

            case R.id.reject_request:
                userCancelTrip();
                Intent intent = new Intent(ActivityRentOTP.this, ActivityRideHome.class);
                startActivity(intent);
                finish();
                break;
            case R.id.accept_request:
                Intent hold = new Intent(ActivityRentOTP.this, ActivityHoldVehicle.class);
                startActivity(hold);
                finish();
                //vehicleHold();
                break;
            case R.id.supervisor_phone:
                callSuper();
                break;
        }
    }
    public void callSuper() {
        String phoneSuper = dPhone.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneSuper));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
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
            Toast.makeText(ActivityRentOTP.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ActivityRentOTP.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityRentOTP.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityRentOTP.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityRentOTP.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
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


    private void giveOtp() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("otp", OTP.getText().toString());
        JSONObject parameters = new JSONObject(params);
        ActivityRentOTP a = ActivityRentOTP.this;
        Log.d(TAG, "Values: auth=" + auth + " otp=" + OTP.getText().toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-give-otp");
        UtilityApiRequestPost.doPOST(a, "user-give-otp", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 4);
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
        ActivityRentOTP a = ActivityRentOTP.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-get-status");
        UtilityApiRequestPost.doPOST(a, "user-trip-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
}
