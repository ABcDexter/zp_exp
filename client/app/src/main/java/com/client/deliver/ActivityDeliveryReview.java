package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ActivityDeliveryReview extends ActivityDrawer implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    int agree = 0;
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

    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String PICK_NAME = "com.client.ride.PickName";

    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String DROP_NAME = "com.client.ride.DropName";

    public static final String PICK_YEAR = "PickYear";
    public static final String PICK_MONTH = "PickMonth";
    public static final String PICK_DAY = "PickDay";
    public static final String PICK_HOUR = "PickHour";
    public static final String PICK_MINUTE = "PickMinute";
    public static final String EXPRESS = "Express";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";

    EditText pName, pNum, dName, dNum;
    TextView pAddress, dAddress, content, size, care, deliveryType, time, date;
    CheckBox disclaimer;
    ImageView next;
    Vibrator vibrator;
    ScrollView scrollView;
    String exp;
    String lat, lng, stringAuth, stringAN, pickLat, pickLng, pickLand, pickPin, pickMobile;

    private static final String TAG = "ActivityDeliveryReview";


    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";

    SharedPreferences prefAuth;
    String EXPress;
    String express = "0";
    EditText detailsPick;
    TextView expTime, stndTime, addDetails, expTomorrow, stndtomorrow;
    String timeSlot = "0";
    Switch stndDay, expDay;
    boolean isBefore = false;
    Calendar datetime = Calendar.getInstance();
    String addDrop, dropLat, dropLng, dropLand, dropPin, dropMobile,
            conType, conSize;
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String ADD_INFO_DROP_POINT = "AddInfoDropPoint";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_delivery_review, null, false);
        frameLayout.addView(activityView);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //retrieving locally stored data
        SharedPreferences prefDetails = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        String srcAddress = prefDetails.getString(ADDRESS_PICK, "");
        String srcName = prefDetails.getString(PICK_NAME, "");
        String srcNum = prefDetails.getString(PICK_MOBILE, "");
        String dstName = prefDetails.getString(DROP_NAME, "");
        String dstNum = prefDetails.getString(DROP_MOBILE, "");
        String dstAddress = prefDetails.getString(ADDRESS_DROP, "");
        String stndHour = prefDetails.getString(HOUR, "");
        String stndMin = prefDetails.getString(MINUTE, "");
        String expHour = prefDetails.getString(PICK_HOUR, "");
        String expMin = prefDetails.getString(PICK_MINUTE, "");
        String expYear = prefDetails.getString(PICK_YEAR, "");
        String expMonth = prefDetails.getString(PICK_MONTH, "");
        String expDay = prefDetails.getString(PICK_DAY, "");
        exp = prefDetails.getString(EXPRESS, "");
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        addDrop = pref.getString(ADDRESS_DROP, "");
        dropLat = pref.getString(DROP_LAT, "");
        dropLng = pref.getString(DROP_LNG, "");
        pickLat = pref.getString(PICK_LAT, "");
        pickLng = pref.getString(PICK_LNG, "");
        dropLand = pref.getString(DROP_LANDMARK, "");
        dropPin = pref.getString(DROP_PIN, "");
        dropMobile = pref.getString(DROP_MOBILE, "");
        conType = pref.getString(CONTENT_TYPE, "");
        conSize = pref.getString(CONTENT_DIM, "");
        EXPress = pref.getString(EXPRESS, "");
        pickLand = pref.getString(PICK_LANDMARK, "");
        pickPin = pref.getString(PICK_PIN, "");
        pickMobile = pref.getString(PICK_MOBILE, "");


        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
        String type = review.getString(R_C_TYPE, "");
        String dim = review.getString(R_C_SIZE, "");
        String fr = review.getString(R_C_FRAGILE, "");
        String li = review.getString(R_C_LIQUID, "");
        String cold = review.getString(R_C_COLD, "");
        String pe = review.getString(R_C_PERISHABLE, "");
        String none = review.getString(R_C_NONE, "");
        String warm = review.getString(R_C_WARM, "");
        String expressDelv = review.getString(R_EXP_DELVY, "");
        String stndDelv = review.getString(R_STND_DELVY, "");
        Log.d("DeliverReview", "expHour=" + expHour);
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        pName = findViewById(R.id.pick_name);
        pName.setText(srcName);
        pNum = findViewById(R.id.pick_mobile);
        pNum.setText(srcNum);
        dName = findViewById(R.id.drop_name);
        dName.setText(dstName);
        dNum = findViewById(R.id.drop_mobile);
        dNum.setText(dstNum);
        pAddress = findViewById(R.id.pick_address);
        pAddress.setText(srcAddress);
        dAddress = findViewById(R.id.drop_address);
        dAddress.setText(dstAddress);
        content = findViewById(R.id.package_content);
        content.setText(type);
        size = findViewById(R.id.package_size);
        size.setText(dim);
        care = findViewById(R.id.package_care);
        care.setText(fr + " " + li + " " + cold + " " + pe + " " + none + " " + warm + " ");
        deliveryType = findViewById(R.id.delv_type);
        if (!expressDelv.equals("")) {
            deliveryType.setText(expressDelv);
        } else if (!stndDelv.equals("")) {
            deliveryType.setText(stndDelv);
        }
        time = findViewById(R.id.delv_time);
        if (!stndHour.equals("")) {
            time.setText(expHour);
        }
        if (!expHour.equals("")) {
            time.setText(expHour + ":" + expMin);
        }
        date = findViewById(R.id.delv_date);
        date.setText(expDay + "/" + expMonth + "/" + expYear);

        disclaimer = findViewById(R.id.chk_disclaimer);
        disclaimer.setOnCheckedChangeListener(this);
        next = findViewById(R.id.confirm_deliver);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_deliver:
                if (agree == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                    Snackbar snackbar1 = Snackbar.make(scrollView, R.string.agree_to_terms, Snackbar.LENGTH_LONG);
                    View sbView1 = snackbar1.getView();
                    TextView textView1 = (TextView) sbView1.findViewById(R.id.snackbar_text);
                    textView1.setTextColor(Color.YELLOW);
                    snackbar1.show();
                } else {
                    storeData();
                    /*Intent confirm = new Intent(ActivityDeliveryReview.this, ActivityDeliverConfirm.class);
                    startActivity(confirm);*/
                    deliveryEstimate();
                }
                break;

        }
    }

    private void storeData() {

    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        //response on hitting user-delivery-estimate API
        Log.d("DeliveryReview" + "jsObjRequest", "RESPONSE:" + response);
        if (id == 1) {
            String price = response.getString("price");
            String time = response.getString("time");
            String dist = response.getString("dist");
            // String price = response.getString("price");
            Intent confirm = new Intent(ActivityDeliveryReview.this, ActivityDeliverConfirm.class);
            confirm.putExtra("PRICE", price);
            confirm.putExtra("DISTANCE", dist);
            startActivity(confirm);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d("DeliveryReview", "onErrorResponse: " + error.toString());
        Log.d("DeliveryReview", "Error:" + error.toString());
    }

    ActivityDeliveryReview a = ActivityDeliveryReview.this;
    Map<String, String> params = new HashMap();

    private void deliveryEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", pickLat);
        params.put("srclng", pickLng);
        params.put("dstlat", dropLat);
        params.put("dstlng", dropLng);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("express", EXPress);//0,1
        params.put("pmode", "1");


        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pickLat
                + " srclng= " + pickLng + " dstlat=" + dropLat + " dstlng=" + dropLng
                + " itype= " + conType + " idim= " + conSize + " express= " + EXPress + " pmode= " + "1");
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-estimate");

        UtilityApiRequestPost.doPOST(a, "user-delivery-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.chk_disclaimer) {
            if (disclaimer.isChecked()) {
                agree = 1;
            } else agree = 0;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliveryReview.this, ActivityDeliveryTimeSlot.class));
        finish();
    }
}
