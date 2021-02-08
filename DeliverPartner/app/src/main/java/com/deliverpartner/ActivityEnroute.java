package com.deliverpartner;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityEnroute extends AppCompatActivity implements View.OnClickListener {
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityEnroute";

    public static final String DELIVERY_DETAILS = "com.agent.DeliveryDetails";
    public static final String DID = "DeliveryID";
    public static final String DST_PER = "DSTPer";
    public static final String DST_ADD = "DSTAdd";
    public static final String DST_LND = "DSTLnd";
    public static final String DST_PHN = "DSTPhn";
    public static final String DSTLAT = "DeliveryDstLat";
    public static final String DSTLNG = "DeliveryDstLng";

    ActivityEnroute a = ActivityEnroute.this;
    Map<String, String> params = new HashMap();

    TextView person, address, landmark, phone, senderPhn, amount, yesPayment, noPayment;
    Button yes, no, map, btnCash, btnUPI;
    ImageButton nameInfo, addInfo, landInfo, phoneDial, sendPhnDial, infoCash;
    Dialog myDialog;
    String strName, strAddress, strLandmark, price, strAuth;
    String paid = "00";
    LinearLayout linearLayout, layoutMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroute);
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        person = findViewById(R.id.dst_per);
        address = findViewById(R.id.dst_add);
        landmark = findViewById(R.id.dst_land);
        phone = findViewById(R.id.dst_phone);
        senderPhn = findViewById(R.id.dst_send_phone);
        yes = findViewById(R.id.completed);
        no = findViewById(R.id.failed);
        map = findViewById(R.id.map);

        nameInfo = findViewById(R.id.infoName);
        addInfo = findViewById(R.id.infoAdd);
        landInfo = findViewById(R.id.infoLand);
        phoneDial = findViewById(R.id.dialPhn);
        sendPhnDial = findViewById(R.id.dialSendPhn);
        linearLayout = findViewById(R.id.layout_pay);
        layoutMode = findViewById(R.id.layout_mode);
        amount = findViewById(R.id.amount);
        infoCash = findViewById(R.id.infoAmount);
        btnCash = findViewById(R.id.cash);
        btnUPI = findViewById(R.id.upi);

        yes.setOnClickListener(this);
        senderPhn.setOnClickListener(this);
        btnCash.setOnClickListener(this);
        btnUPI.setOnClickListener(this);
        no.setOnClickListener(this);
        map.setOnClickListener(this);
        nameInfo.setOnClickListener(this);
        addInfo.setOnClickListener(this);
        landInfo.setOnClickListener(this);
        phoneDial.setOnClickListener(this);
        sendPhnDial.setOnClickListener(this);
        phone.setOnClickListener(this);
        sendPhnDial.setOnClickListener(this);
        infoCash.setOnClickListener(this);
        btnCash.setOnClickListener(this);
        btnUPI.setOnClickListener(this);

        getStatus();
        myDialog = new Dialog(this);
    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-get-status");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyFail() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-delivery-fail");
        UtilityApiRequestPost.doPOST(a, "auth-delivery-fail", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyEnd() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-done");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-done", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting agent-delivery-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String did = response.getString("did");
                    SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(DID, did).apply();

                    paid = response.getString("paid");

                    if (paid.equals("3")) {
                        linearLayout.setVisibility(View.VISIBLE);
                        layoutMode.setVisibility(View.VISIBLE);
                        price = response.getString("price");
                        amount.setText("₹ " + price);
                    } else {
                        linearLayout.setVisibility(View.GONE);
                        layoutMode.setVisibility(View.GONE);
                    }

                    if (status.equals("ST")) {
                        String per = response.getString("dstper");
                        String add = response.getString("dstadd");
                        String land = response.getString("dstland");
                        String phn = response.getString("dstphone");
                        String sendPhn = response.getString("srcphone");
                        String lat = response.getString("dstlat");
                        String lng = response.getString("dstlng");

                        person.setText(per);
                        address.setText(add);
                        landmark.setText(land);
                        phone.setText(phn);
                        senderPhn.setText(sendPhn);
                        strName = per;
                        strAddress = add;
                        strLandmark = land;
                        SharedPreferences delvyPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(DST_PER, per).apply();
                        delvyPref.edit().putString(DST_ADD, add).apply();
                        delvyPref.edit().putString(DST_LND, land).apply();
                        delvyPref.edit().putString(DST_PHN, phn).apply();
                        delvyPref.edit().putString(DSTLAT, lat).apply();
                        delvyPref.edit().putString(DSTLNG, lng).apply();
                    }

                } else if (active.equals("false")) {
                    Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting auth-delivery-fail API
        if (id == 2) {
            Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
            startActivity(home);
            finish();
        }

        //response on hitting agent-delivery-done API
        if (id == 3) {
            retireAgent();

        }
        //response on hitting agent-delivery-retire API
        if (id == 4) {
            Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(a, R.string.something_wrong, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    private void retireAgent() {
        String auth = strAuth;
        params.put("auth", auth);
        // params.put("scid", did);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-retire");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-retire", parameters, 20000, 0, response -> {
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
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        LinearLayout ln = (LinearLayout) myDialog.findViewById(R.id.layout_btn);
        yesPayment = (TextView) myDialog.findViewById(R.id.reject_request);
        noPayment = (TextView) myDialog.findViewById(R.id.accept_request);
        if (id == 1) {
            infoText.setText(strName);
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 2) {
            infoText.setText(strAddress);
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 3) {
            infoText.setText(strLandmark);
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 4) {
            infoText.setText("Please collect ₹ " + price + " in cash");
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 5) {
            ln.setVisibility(View.VISIBLE);
            infoText.setText("Have you collected ₹ " + price);
            yesPayment.setOnClickListener(this);
            noPayment.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void callClientPhn(String phnNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phnNumber));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.completed) {
            if (paid.equals("1") || paid.equals("2")) {
                delvyEnd();
            } else
                ShowPopup(5);
        } else if (id == R.id.failed) {
            delvyFail();
        } else if (id == R.id.map) {
            Intent map = new Intent(ActivityEnroute.this, MapsReceiverLocation.class);
            startActivity(map);
            finish();
        } else if (id == R.id.infoName) {
            ShowPopup(1);
        } else if (id == R.id.infoAdd) {
            ShowPopup(2);
        } else if (id == R.id.infoLand) {
            ShowPopup(3);
        } else if (id == R.id.dialPhn || id == R.id.dialSendPhn) {
            String receiverNo = phone.getText().toString();
            callClientPhn(receiverNo);
        } else if (id == R.id.dst_phone || id == R.id.dst_send_phone) {
            String senderNo = senderPhn.getText().toString();
            callClientPhn(senderNo);
        } else if (id == R.id.infoAmount) {
            ShowPopup(4);
        } else if (id == R.id.reject_request) {
            myDialog.dismiss();
        } else if (id == R.id.accept_request) {
            delvyEnd();
            myDialog.dismiss();
        } else if (id == R.id.cash) {
            btnCash.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            btnUPI.setBackgroundResource(R.drawable.rect_box_outline);
        } else if (id == R.id.upi) {
            btnUPI.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            btnCash.setBackgroundResource(R.drawable.rect_box_outline);
            Intent qrCode = new Intent(ActivityEnroute.this, ActivityUPICode.class);
            startActivity(qrCode);
            finish();
        }
    }
}
