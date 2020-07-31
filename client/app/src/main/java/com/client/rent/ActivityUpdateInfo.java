package com.client.rent;

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
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.HubList;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityUpdateInfo extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityUpdateInfo";
    TextView destination, hours;
    ImageButton infoCost;
    Button update;
    ScrollView scrollView;
    PopupWindow popupWindow;
    String dropID;
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String NO_HOURS = "NoHours";
    public static final String LOCATION_DROP_ID = "DropLocationID";

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    private static ActivityUpdateInfo instance;
    FusedLocationProviderClient mFusedLocationClient;
    Dialog imageDialog2;
    String stringAuthCookie, NoHours, stringHrs, PriceOnly;
    TextView updateCost;
    TextView upi;
    final int UPI_PAYMENT = 0;
    Button dummy;
    Dialog myDialog;
    String[] costAsTime = {"", "1", "0.90", "0.80", "0.75", "0.70", "0.65", "0.60", "0.55", "0.50",
                            "0.50", "0.50","0.50"};

    public static ActivityUpdateInfo getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting user-rental-update API
        if (id == 2) {
            Toast.makeText(this, "Details Updated Successfully!", Toast.LENGTH_LONG).show();
            SharedPreferences pref = this.getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(NO_HOURS, hours.getText().toString());
            editor.apply();

            Intent goBack = new Intent(ActivityUpdateInfo.this, ActivityRentInProgress.class);
            startActivity(goBack);
            finish();

        }
        if (id == 3) {
            String cost = response.getString("price");
            updateCost.setText("₹ " + cost);
            PriceOnly = cost;
            update.setClickable(false);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, "Updated Unsuccessful!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // don’t set any content view here, since its already set in ActivityDrawer
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_update_info, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        instance = this;
        //retrieve locally stored data
        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefPLoc.getString(AUTH_KEY, "");
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        stringHrs = pref.getString(NO_HOURS, "");
        Log.d(TAG, "######stringHrs = "+stringHrs);
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");
        SharedPreferences tripPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        hours = findViewById(R.id.hours);
        destination = findViewById(R.id.drop_hub);
        update = findViewById(R.id.update_data);
        scrollView = findViewById(R.id.scrollView_rent_progress);
        //hours.setText(stringHrs);
        updateCost = findViewById(R.id.update_cost);
        update.setOnClickListener(this);
        destination.setOnClickListener(this);
        hours.setOnClickListener(this);
        infoCost = findViewById(R.id.infoCost);
        infoCost.setOnClickListener(this);
        if (stringDrop.isEmpty()) {
            destination.setText("DROP POINT");
        } else {
            String upToNCharacters = stringDrop.substring(0, Math.min(stringDrop.length(), 20));
            destination.setText(upToNCharacters);

            dropID = stringDropID;
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ActivityUpdateInfo.this);
        imageDialog2 = new Dialog(this);
        upi = findViewById(R.id.upiRental);
        upi.setOnClickListener(this);
        dummy = findViewById(R.id.dummy);
        dummy.setOnClickListener(this);
        myDialog = new Dialog(this);
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText("Cost as per rental time. Please pay full amount to begin your trip.");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.infoCost:
                ShowPopup(1);
                break;
            case R.id.dummy:
                userUpdateTrip();
                break;

            case R.id.upiRental:

                String amount = PriceOnly;
                String note = "Payment for rental service";
                String name = "Zipp-E";
                String upiId = "9084083967@ybl";
                payUsingUpi(amount, upiId, name, note);
                break;

            case R.id.update_data:
                /*alertDialog();*/
                userUpdateTrip();
                break;
            case R.id.drop_hub:
                Intent drop = new Intent(ActivityUpdateInfo.this, HubList.class);
                drop.putExtra("Request", "destination_rental_in_progress");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(drop);
                break;
            case R.id.hours:
                ImagePopup2();
                break;

            case R.id.txt1:
                //stringHrs = "1";
                imageDialog2.dismiss();
                userUpdateTime("1");
                break;
            case R.id.txt2:
                //stringHrs = "2";
                imageDialog2.dismiss();
                userUpdateTime("2");
                break;
            case R.id.txt3:
                //stringHrs = "3";
                imageDialog2.dismiss();
                userUpdateTime("3");
                break;
            case R.id.txt4:
                //stringHrs = "4";
                imageDialog2.dismiss();
                userUpdateTime("4");
                break;
            case R.id.txt5:
                //stringHrs = "5";
                imageDialog2.dismiss();
                userUpdateTime("5");
                break;
            case R.id.txt6:
                //stringHrs = "6";
                imageDialog2.dismiss();
                userUpdateTime("6");
                break;
            case R.id.txt9:
                //stringHrs = "9";
                imageDialog2.dismiss();
                userUpdateTime("9");
                break;
            case R.id.txt11:
                //stringHrs = "12";
                imageDialog2.dismiss();
                userUpdateTime("12");
                break;

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
            Toast.makeText(ActivityUpdateInfo.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ActivityUpdateInfo.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                userUpdateTrip();
                Log.d("UPI", "responseStr: " + approvalRefNo);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ActivityUpdateInfo.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityUpdateInfo.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActivityUpdateInfo.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
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


    private void ImagePopup2() {

        imageDialog2.setContentView(R.layout.popup_hours_edit);
        String hrs = stringHrs;
Log.d(TAG, "@@@@@@@hrs "+ hrs+ "stringHrs "+stringHrs);
        TextView head = (TextView) imageDialog2.findViewById(R.id.txt_head);

        TextView txt1 = (TextView) imageDialog2.findViewById(R.id.txt1);
        TextView txt2 = (TextView) imageDialog2.findViewById(R.id.txt2);
        TextView txt3 = (TextView) imageDialog2.findViewById(R.id.txt3);
        TextView txt4 = (TextView) imageDialog2.findViewById(R.id.txt4);
        TextView txt5 = (TextView) imageDialog2.findViewById(R.id.txt5);
        TextView txt6 = (TextView) imageDialog2.findViewById(R.id.txt6);
        TextView txt7 = (TextView) imageDialog2.findViewById(R.id.txt7);
        TextView txt8 = (TextView) imageDialog2.findViewById(R.id.txt8);
        TextView txt9 = (TextView) imageDialog2.findViewById(R.id.txt9);
        TextView txt10 = (TextView) imageDialog2.findViewById(R.id.txt10);
        TextView txt11 = (TextView) imageDialog2.findViewById(R.id.txt11);
        TextView txt12 = (TextView) imageDialog2.findViewById(R.id.txt12);

        head.setText("Extend by");
        if (hrs.equals("1")) {
            //txt11.setVisibility(View.GONE);
            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[2]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[3]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[4]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[5]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[6]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[7]+" / min");
            txt7.setText("7 hr (420 mins) @ ₹ "+costAsTime[8]+" / min");
            txt8.setText("8 hr (480 mins) @ ₹ "+costAsTime[9]+" / min");
            txt9.setText("9 hr (560 mins) @ ₹ "+costAsTime[10]+" / min");
            txt10.setText("10 hr (600 mins) @ ₹ "+costAsTime[11]+" / min");
            txt11.setText("11 hr (660 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("2")) {
            txt11.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[3]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[4]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[5]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[6]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[7]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[8]+" / min");
            txt7.setText("7 hr (420 mins) @ ₹ "+costAsTime[9]+" / min");
            txt8.setText("8 hr (480 mins) @ ₹ "+costAsTime[10]+" / min");
            txt9.setText("9 hr (560 mins) @ ₹ "+costAsTime[11]+" / min");
            txt10.setText("10 hr (600 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("3")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[4]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[5]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[6]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[7]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[8]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[9]+" / min");
            txt7.setText("7 hr (420 mins) @ ₹ "+costAsTime[10]+" / min");
            txt8.setText("8 hr (480 mins) @ ₹ "+costAsTime[11]+" / min");
            txt9.setText("9 hr (560 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("4")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[5]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[6]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[7]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[8]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[9]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[10]+" / min");
            txt7.setText("7 hr (420 mins) @ ₹ "+costAsTime[11]+" / min");
            txt8.setText("8 hr (480 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("5")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[6]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[7]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[8]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[9]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[10]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[11]+" / min");
            txt7.setText("7 hr (420 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("6")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[7]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[8]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[9]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[10]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[11]+" / min");
            txt6.setText("6 hr (360 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("7")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);
            txt6.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[8]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[9]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[10]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[11]+" / min");
            txt5.setText("5 hr (300 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("8")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);
            txt6.setVisibility(View.GONE);
            txt5.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[9]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[10]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[11]+" / min");
            txt4.setText("4 hr (240 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("9")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);
            txt6.setVisibility(View.GONE);
            txt5.setVisibility(View.GONE);
            txt4.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[10]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[11]+" / min");
            txt3.setText("3 hr (180 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("10")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);
            txt6.setVisibility(View.GONE);
            txt5.setVisibility(View.GONE);
            txt4.setVisibility(View.GONE);
            txt3.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[11]+" / min");
            txt2.setText("2 hr (120 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        if (hrs.equals("11")) {
            txt11.setVisibility(View.GONE);
            txt10.setVisibility(View.GONE);
            txt9.setVisibility(View.GONE);
            txt8.setVisibility(View.GONE);
            txt7.setVisibility(View.GONE);
            txt6.setVisibility(View.GONE);
            txt5.setVisibility(View.GONE);
            txt4.setVisibility(View.GONE);
            txt3.setVisibility(View.GONE);
            txt2.setVisibility(View.GONE);

            txt1.setText("1 hr (60 mins) @ ₹ "+costAsTime[12]+" / min");
        }
        txt12.setVisibility(View.GONE);
        txt1.setOnClickListener(this);
        txt2.setOnClickListener(this);
        txt3.setOnClickListener(this);
        txt4.setOnClickListener(this);
        txt5.setOnClickListener(this);
        txt6.setOnClickListener(this);
        txt7.setOnClickListener(this);
        txt8.setOnClickListener(this);
        txt9.setOnClickListener(this);
        txt10.setOnClickListener(this);
        txt11.setOnClickListener(this);

        imageDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = imageDialog2.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position

        imageDialog2.show();
        Window window = imageDialog2.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        imageDialog2.setCanceledOnTouchOutside(true);
    }

    private void userUpdateTrip() {
        String hour = stringHrs;
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        params.put("dstid", dropID);
        params.put("hrs", hour);
        JSONObject param = new JSONObject(params);
        ActivityUpdateInfo a = ActivityUpdateInfo.this;
        Log.d(TAG, "Values: auth=" + stringAuth + " dstid=" + dropID + " hrs=" + hour);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rental-update");
        UtilityApiRequestPost.doPOST(a, "user-rental-update", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void userUpdateTime(String time) {
        String stringAuth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", stringAuth);
        params.put("updatedtime", time);
        JSONObject param = new JSONObject(params);
        ActivityUpdateInfo a = ActivityUpdateInfo.this;
        Log.d(TAG, "Values: auth=" + stringAuth + " updatedtime=" + time);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-rental-update");
        UtilityApiRequestPost.doPOST(a, "user-time-update", param, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityUpdateInfo.this, ActivityRentInProgress.class));
        finish();
    }
}
