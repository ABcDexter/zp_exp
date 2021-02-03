package com.client.rent;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.client.ride.ActivityRideHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityRentOTP extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityRentOTP";
    Button dummy;

    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String COST_DROP = "CostDrop";
    //public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";
    public static final String DRIVER_PHN = "DriverPhn";
    public static final String DRIVER_NAME = "DriverName";
    public static final String AUTH_KEY = "AuthKey";
    public static final String VEHICLE_TYPE = "VehicleType";
    public static final String LOCATION_PICK_LAT = "PickLocationLat";
    public static final String LOCATION_PICK_LNG = "PickLocationLng";
    public static final String LOCATION_DROP_LAT = "DropLocationLat";
    public static final String LOCATION_DROP_LNG = "DropLocationLng";

    private static ActivityRentOTP instance;
    TextView origin, destination, dName, dPhone, OTP, costEst, upiRental, vtype;
    ScrollView scrollView;
    Dialog myDialog;
    ImageButton costInfo, PickInfo, DropInfo, cancel;
    String stringAuthCookie, stringPick, stringDrop, PriceOnly, pickLat, pickLng, dropLat, dropLng;
    ActivityRentOTP a = ActivityRentOTP.this;
    ImageView lock;
    final int UPI_PAYMENT = 0;
    RelativeLayout rlPhone;
    ImageView supPhoto;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-trip-cancel API
        if (id == 1) {
            Intent home = new Intent(ActivityRentOTP.this, ActivityRentHome.class);
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
                    String photo = response.getString("photourl");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    if (status.equals("AS")) {
                        String timeRem = response.getString("time");
                        String price = response.getString("price");
                        Glide.with(this).load(photo).into(supPhoto);
                        costEst.setText("₹ " + price);
                        PriceOnly = price;
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
                    Intent homePage = new Intent(ActivityRentOTP.this, ActivityRentHome.class);
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
        //response on hitting user-rent-pay API
        if (id == 6) {
            String otp = response.getString("otp");
            //String van = response.getString("vno");
            /*SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            sp_otp.edit().putString(OTP_PICK, otp).apply();*/
            OTP.setText(otp);
            lock.setVisibility(View.GONE);
            OTP.setVisibility(View.VISIBLE);
            checkStatus();
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
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
        stringPick = prefPLoc.getString(LOCATION_PICK, "");
        stringDrop = prefPLoc.getString(LOCATION_DROP, "");
        pickLat = prefPLoc.getString(LOCATION_PICK_LAT, "");
        pickLng = prefPLoc.getString(LOCATION_PICK_LNG, "");
        dropLat = prefPLoc.getString(LOCATION_DROP_LAT, "");
        dropLng = prefPLoc.getString(LOCATION_DROP_LNG, "");
        String stringCost = prefPLoc.getString(COST_DROP, "");
        /*String otpPick = prefPLoc.getString(OTP_PICK, "");*/
        String vType = prefPLoc.getString(VEHICLE_TYPE, "");

        supPhoto = findViewById(R.id.photo_sup);
        costEst = findViewById(R.id.adv_payment_otp);
        vtype = findViewById(R.id.vtype);
        dName = findViewById(R.id.supervisor_name);
        dPhone = findViewById(R.id.supervisor_phone);
        rlPhone = findViewById(R.id.rl_p);
        OTP = findViewById(R.id.otp_rent);
        lock = findViewById(R.id.otp_lock);
        origin = findViewById(R.id.pick_hub);
        destination = findViewById(R.id.drop_hub);
        cancel = findViewById(R.id.cancel_rent_booking);
        costInfo = findViewById(R.id.infoCost);
        scrollView = findViewById(R.id.scrollView_rent_OTP);
        PickInfo = findViewById(R.id.infoPick);
        DropInfo = findViewById(R.id.infoDrop);
        dummy = findViewById(R.id.dummy);
        upiRental = findViewById(R.id.upiRental);

        dummy.setOnClickListener(this);
        upiRental.setOnClickListener(this);
        DropInfo.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        lock.setOnClickListener(this);
        PickInfo.setOnClickListener(this);
        cancel.setOnClickListener(this);
        costEst.setText(stringCost);
        //costEst.setText("₹ 260.00");
        costInfo.setOnClickListener(this);
        destination.setOnClickListener(this);
        origin.setOnClickListener(this);
        if (!vType.isEmpty()) {
            if (vType.equals("0")) {
                vtype.setText(R.string.e_cycle);
            }
            if (vType.equals("1")) {
                vtype.setText(R.string.e_scooty);
            }
            if (vType.equals("2")) {
                vtype.setText(R.string.e_bike);
            }
        }

        dPhone.setOnClickListener(this);

        myDialog = new Dialog(this);

        /*if (otpPick.isEmpty()) {
            OTP.setVisibility(View.GONE);
            lock.setVisibility(View.VISIBLE);
        } else {
            OTP.setVisibility(View.VISIBLE);
            OTP.setText(otpPick);
            lock.setVisibility(View.GONE);
        }*/

        supDetails();

        if (stringDrop.isEmpty()) {
            destination.setText(R.string.drop_point);
        } else {
            String upToNCharacters = stringDrop.substring(0, Math.min(stringDrop.length(), 10));
            destination.setText(upToNCharacters);
        }

        if (stringPick.isEmpty()) {
            origin.setText(R.string.pick_point);
        } else {
            String upToNCharacters = stringPick.substring(0, Math.min(stringPick.length(), 10));
            origin.setText(upToNCharacters);

        }
        checkStatus();
        //ShowPopup(2);
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
            infoText.setText(R.string.cost_calc_as_per_time);
        }
        if (id == 2) {
            infoText.setText(R.string.check_condi_before_paying);
        }
        if (id == 3) {
            infoText.setText(R.string.check_condi_before_paying);
        }
        /*if (id == 4) {
            infoText.setText("HOLD VEHICLE FOR 1 HOUR?");

            reject.setOnClickListener(this);
            accept.setOnClickListener(this);
        }*/

        if (id == 5) {
            infoText.setText(stringPick);
        }
        if (id == 6) {
            infoText.setText(stringDrop);
        }
        if (id == 7) {
            infoText.setText(getString(R.string.pay_to_unlock_otp) + costEst.getText().toString() + getString(R.string.unlock_otp));
        }
        if (id == 8) {
            infoText.setText("Open Map. Work in Progress");
        }
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


    public static ActivityRentOTP getInstance() {
        return instance;
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
        int id = v.getId();
        if (id == R.id.dummy) {
            rentPay();
        } else if (id == R.id.cancel_rent_booking) {//showAlertDialog();
            //ShowPopup(4);
            userCancelTrip();
            Intent intent = new Intent(ActivityRentOTP.this, ActivityRideHome.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.drop_hub) {
            ShowPopup(6);
        } else if (id == R.id.pick_hub) {
            ShowPopup(5);
        } else if (id == R.id.infoCost) {
            ShowPopup(1);
        } else if (id == R.id.infoPick) {
            String lat1 = pickLat;
            String lng1 = pickLng;
            Intent map = new Intent(ActivityRentOTP.this, MapsHubLocation.class);
            map.putExtra("lat", lat1);
            map.putExtra("lng", lng1);
            startActivity(map);
            //ShowPopup(8);
        } else if (id == R.id.infoDrop) {
            String lat2 = dropLat;
            String lng2 = dropLng;
            Intent map1 = new Intent(ActivityRentOTP.this, MapsHubLocation.class);
            map1.putExtra("lat", lat2);
            map1.putExtra("lng", lng2);
            startActivity(map1);
            //ShowPopup(8);
        } else if (id == R.id.upiRental) {
            String amount = PriceOnly;
            String note = "Payment for rental service";
            String name = "Zipp-E";
            String upiId = "rajnilakshmi@ybl";
            payUsingUpi(amount, upiId, name, note);

            /*case R.id.reject_request:
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
                break;*/
            /*case R.id.supervisor_phone:
                callSuper();
                break;*/
        } else if (id == R.id.otp_lock) {
            ShowPopup(7);
        } else if (id == R.id.rl_p) {
            callSuper();
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
            Toast.makeText(ActivityRentOTP.this, R.string.no_upi_found, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ActivityRentOTP.this, R.string.transaction_successful, Toast.LENGTH_SHORT).show();
                rentPay();
                Log.d("UPI", "responseStr: " + approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityRentOTP.this, R.string.payment_cancelled_by_user, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityRentOTP.this, R.string.transaction_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityRentOTP.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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


    /*private void giveOtp() {
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
    }*/
    private void rentPay() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        //params.put("otp", OTP.getText().toString());
        JSONObject parameters = new JSONObject(params);
        ActivityRentOTP a = ActivityRentOTP.this;
        Log.d(TAG, "Values: auth=" + auth /*+ " otp=" + OTP.getText().toString()*/);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rent-pay");
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
