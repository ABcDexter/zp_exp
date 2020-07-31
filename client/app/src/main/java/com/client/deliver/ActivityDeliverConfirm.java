package com.client.deliver;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityDeliverConfirm extends ActivityDrawer implements View.OnClickListener {

    TextView cost;
    EditText edTip;
    ScrollView scrollView;
    ImageButton infoPayment, infoTip;
    private static final String TAG = "ActivityDeliverConfirm";

    public static final String AUTH_KEY = "AuthKey";
    public static final String DELIVERY_DETAILS = "com.client.delivery.details";
    public static final String DELIVERY_ID = "DeliveryID";
    public static final String DELIVERY_PRICE = "Price";
    public static final String FLAMMABLE = "flammable";
    public static final String FRAGILE = "fragile";
    public static final String LIQUID = "liquid";
    public static final String KEEP_WARM = "keep_warm";
    public static final String KEEP_COLD = "keep_cold";
    public static final String KEEP_DRY = "keep_dry";
    public static final String ADD_INFO = "";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String PICK_NAME = "com.client.ride.PickName";
    public static final String DROP_NAME = "com.client.ride.DropName";

    private static ActivityDeliverConfirm instance;
    Dialog myDialog;

    ImageButton done;
    ActivityDeliverConfirm a = ActivityDeliverConfirm.this;
    Map<String, String> params = new HashMap();
    String stringAuth, addPick, addDrop, pickLat, pickLng, fr, fl, li, kd, kw, kc, details, distance, did, pickName, dropName,
            dropLat, dropLng, pickLand, dropLand, pickPin, dropPin, pickMobile, dropMobile, conType, conSize;
    ImageView zbeeR, zbeeL;

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
        addPick = pref.getString(ADDRESS_PICK, "");
        pickLat = pref.getString(PICK_LAT, "");
        pickLng = pref.getString(PICK_LNG, "");
        addDrop = pref.getString(ADDRESS_DROP, "");
        dropLat = pref.getString(DROP_LAT, "");
        dropLng = pref.getString(DROP_LNG, "");
        pickLand = pref.getString(PICK_LANDMARK, "");
        dropLand = pref.getString(DROP_LANDMARK, "");
        pickPin = pref.getString(PICK_PIN, "");
        dropPin = pref.getString(DROP_PIN, "");
        pickMobile = pref.getString(PICK_MOBILE, "");
        dropMobile = pref.getString(DROP_MOBILE, "");
        conType = pref.getString(CONTENT_TYPE, "");
        conSize = pref.getString(CONTENT_DIM, "");
        fr = pref.getString(FRAGILE, "");
        fl = pref.getString(FLAMMABLE, "");
        li = pref.getString(LIQUID, "");
        kd = pref.getString(KEEP_DRY, "");
        kw = pref.getString(KEEP_WARM, "");
        kc = pref.getString(KEEP_COLD, "");
        details = pref.getString(ADD_INFO, "");
        pickName = pref.getString(PICK_NAME, "");
        dropName = pref.getString(DROP_NAME, "");

        cost = findViewById(R.id.payment);
        edTip = findViewById(R.id.tip);
        infoPayment = findViewById(R.id.infoPayment);
        infoTip = findViewById(R.id.infoTip);
        done = findViewById(R.id.confirm_btn);
        scrollView = findViewById(R.id.scrollViewDC);

        //checkStatus();
        done.setOnClickListener(this);
        infoPayment.setOnClickListener(this);
        infoTip.setOnClickListener(this);

        myDialog = new Dialog(this);
        deliveryEstimate();
        zbeeR = findViewById(R.id.image_zbee);
        zbeeL = findViewById(R.id.image_zbee_below);
    }

    private void deliveryEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", pickLat);
        params.put("srclng", pickLng);
        params.put("dstlat", dropLat);
        params.put("dstlng", dropLng);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("fr", fr);
        params.put("fl", fl);
        params.put("li", li);
        params.put("kd", kd);
        params.put("kw", kw);
        params.put("kc", kc);


        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pickLat
                + " srclng= " + pickLng + " dstlat=" + dropLat + " dstlng=" + dropLng
                + " itype= " + conType + " idim= " + conSize + " fr= " + fr + " fl= " + fl
                + " li= " + li + " kd= " + kd + " kc= " + kc + " kw= " + kw);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-estimate");

        UtilityApiRequestPost.doPOST(a, "user-delivery-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }


    public static ActivityDeliverConfirm getInstance() {
        return instance;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                moveit();
                userRequestDelivery();
                /*Intent next = new Intent(ActivityDeliverConfirm.this, ActivityDeliverPayment.class);
                startActivity(next);
                finish();*/
                break;
            case R.id.infoTip:
                ShowPopup(1);
                break;
            case R.id.infoPayment:
                ShowPopup(2);
                break;
        }
    }

    private void moveit() {
        zbeeL.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(zbeeL, "translationX", 1500, 0f);
        objectAnimator.setDuration(1500);
        objectAnimator.start();
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(zbeeR, "translationX", 0f, 1500);
        objectAnimator1.setDuration(1500);
        objectAnimator1.start();
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);
    }

    protected void userRequestDelivery() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", pickLat);
        params.put("srclng", pickLng);
        params.put("dstlat", dropLat);
        params.put("dstlng", dropLng);
        params.put("srcphone", pickMobile);
        params.put("dstphone", dropMobile);
        params.put("srcper", pickName);
        params.put("dstper", dropName);
        params.put("srcadd", addPick);
        params.put("dstadd", addDrop);
        params.put("srcpin", pickPin);
        params.put("dstpin", dropPin);
        params.put("dstland", dropLand);
        params.put("srcland", pickLand);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("fr", fr);
        params.put("fl", fl);
        params.put("li", li);
        params.put("kd", kd);
        params.put("kw", kw);
        params.put("kc", kc);
        params.put("tip", edTip.getText().toString());
        if (!details.equals(""))
            params.put("details", details);
        String tip = edTip.getText().toString();
        if (!tip.equals(""))
            params.put("tip", tip);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pickLat
                + " srclng= " + pickLng + " dstlat=" + dropLat + " dstlng=" + dropLng
                + " itype= " + conType + " idim= " + conSize + " fr= " + fr + " fl= " + fl
                + " li= " + li + " kd= " + kd + " kc= " + kc + " kw= " + kw + " details= " + details);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-request");

        UtilityApiRequestPost.doPOST(a, "user-delivery-request", parameters, 2000, 0, response -> {
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
        params.put("did", did);

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
        params.put("did", did);
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

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);
        if (id == 1) {
            infoText.setText("The amount shall be directly credited to our  agent's account.");
        }
        if (id == 2) {
            infoText.setText("Base price : " + "₹ 15" + "\nDistance : " + distance + " km");
        }
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

        //response on hitting user-delivery-estimate API
        if (id == 1) {
            String actualPrice = response.getString("price");
            distance = response.getString("dist");
            cost.setText("₹ "+actualPrice);
        }
        //response on hitting user-delivery-request API
        if (id == 2) {
            did = response.getString("did");
            SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(DELIVERY_ID, did).apply();
            checkStatus();
        }
        //response on hitting user-delivery-get-status API
        if (id == 3) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    if (status.equals("RQ")) {
                        Snackbar snackbar = Snackbar
                                .make(scrollView, "WAITING FOR AGENT TO ACCEPT", Snackbar.LENGTH_INDEFINITE)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelRequest();
                                    }
                                });
                        snackbar.setActionTextColor(Color.RED);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                        Intent intent = new Intent(this, UtilityPollingService.class);
                        intent.setAction("32");
                        startService(intent);
                    }
                    if (status.equals("AS")) {
                        String price = response.getString("price");
                        Intent as = new Intent(ActivityDeliverConfirm.this, ActivityDeliverPayment.class);
                        startActivity(as);
                        SharedPreferences sp_price = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                        sp_price.edit().putString(DELIVERY_PRICE, price).apply();
                    }
                } else {
                    Intent homePage = new Intent(ActivityDeliverConfirm.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //response on hitting user-delivery-cancel API
        if (id == 4) {
            Intent home = new Intent(ActivityDeliverConfirm.this, ActivityDeliverHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverConfirm.this, ActivityDeliverItemDetails.class));
        finish();
    }
}

