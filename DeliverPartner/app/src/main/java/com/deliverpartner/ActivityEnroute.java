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
    String strAuth;
    ActivityEnroute a = ActivityEnroute.this;
    Map<String, String> params = new HashMap();

    TextView person, address, landmark, phone, senderPhn;
    String lat, lng;
    Button yes, no, map;
    ImageButton nameInfo, addInfo, landInfo, phoneDial, sendPhnDial;
    Dialog myDialog;
    String strName, strAddress, strLandmark;

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

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        map.setOnClickListener(this);
        nameInfo.setOnClickListener(this);
        addInfo.setOnClickListener(this);
        landInfo.setOnClickListener(this);
        phoneDial.setOnClickListener(this);
        sendPhnDial.setOnClickListener(this);
        phone.setOnClickListener(this);
        sendPhnDial.setOnClickListener(this);

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
            Intent home = new Intent(ActivityEnroute.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText(strName);
        }
        if (id == 2) {
            infoText.setText(strAddress);
        }
        if (id == 3) {
            infoText.setText(strLandmark);
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
        switch (v.getId()) {
            case R.id.completed:
                delvyEnd();
                break;
            case R.id.failed:
                delvyFail();
                break;
            case R.id.map:
                Intent map = new Intent(ActivityEnroute.this, MapsReceiverLocation.class);
                startActivity(map);
                finish();
                break;

            case R.id.infoName:
                ShowPopup(1);
                break;
            case R.id.infoAdd:
                ShowPopup(2);
                break;
            case R.id.infoLand:
                ShowPopup(3);
                break;
            case R.id.dialPhn:
            case R.id.dialSendPhn:
                String receiverNo = phone.getText().toString();
                callClientPhn(receiverNo);
                break;
            case R.id.dst_phone:
            case R.id.dst_send_phone:
                String senderNo = senderPhn.getText().toString();
                callClientPhn(senderNo);
                break;
        }
    }
}
