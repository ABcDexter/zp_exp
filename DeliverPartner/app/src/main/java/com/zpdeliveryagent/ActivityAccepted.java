package com.zpdeliveryagent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityAccepted extends AppCompatActivity implements View.OnClickListener {

    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityAccepted";

    public static final String DELIVERY_DETAILS = "com.agent.DeliveryDetails";
    public static final String DID = "DeliveryID";
    public static final String SRC_PER = "SrcPer";
    public static final String SRC_ADD = "SrcAdd";
    public static final String SRC_LND = "SrcLnd";
    public static final String SRC_PHN = "SrcPhn";
    public static final String SRCLAT = "DeliverySrcLat";
    public static final String SRCLNG = "DeliverySrcLng";

    ActivityAccepted a = ActivityAccepted.this;
    Map<String, String> params = new HashMap();

    TextView person, address, landmark, phone, amount, yesPayment, noPayment;
    String strName, strAddress, strLandmark, price, strAuth, checkOtp;
    String paid="00";
    Button yes, no, map;
    EditText otp;
    ImageButton nameInfo, addInfo, landInfo, phoneDial, infoCash;
    Dialog myDialog;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth

        person = findViewById(R.id.src_per);
        address = findViewById(R.id.src_add);
        landmark = findViewById(R.id.src_land);
        phone = findViewById(R.id.src_phone);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        otp = findViewById(R.id.enter_otp);
        map = findViewById(R.id.map);

        nameInfo = findViewById(R.id.infoName);
        addInfo = findViewById(R.id.infoAdd);
        landInfo = findViewById(R.id.infoLand);
        phoneDial = findViewById(R.id.dialPhn);
        linearLayout = findViewById(R.id.layout_pay);
        amount = findViewById(R.id.amount);
        infoCash = findViewById(R.id.infoAmount);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        map.setOnClickListener(this);
        nameInfo.setOnClickListener(this);
        addInfo.setOnClickListener(this);
        landInfo.setOnClickListener(this);
        infoCash.setOnClickListener(this);
        phoneDial.setOnClickListener(this);
        phone.setOnClickListener(this);
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

    public void delvyCancel() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-cancel");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-cancel", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void delvyStart(String otp) {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("otp", otp);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " otp=" + otp);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-start");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-start", parameters, 20000, 0, response -> {
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
                    paid = response.getString("paid");

                    if (paid.equals("2")) {
                        linearLayout.setVisibility(View.VISIBLE);
                        price = response.getString("price");
                        amount.setText("₹ " + price);
                    } else linearLayout.setVisibility(View.GONE);
                    SharedPreferences sp_cookie = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(DID, did).apply();

                    if (status.equals("AS") || status.equals("RC")) {
                        String per = response.getString("srcper");
                        String add = response.getString("srcadd");
                        String land = response.getString("srcland");
                        String phn = response.getString("srcphone");
                        String lat = response.getString("srclat");
                        String lng = response.getString("srclng");

                        person.setText(per);
                        address.setText(add);
                        landmark.setText(land);
                        phone.setText(phn);
                        strName = per;
                        strAddress = add;
                        strLandmark = land;

                        SharedPreferences delvyPref = getSharedPreferences(DELIVERY_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(SRC_PER, per).apply();
                        delvyPref.edit().putString(SRC_ADD, add).apply();
                        delvyPref.edit().putString(SRC_LND, land).apply();
                        delvyPref.edit().putString(SRC_PHN, phn).apply();
                        delvyPref.edit().putString(SRCLAT, lat).apply();
                        delvyPref.edit().putString(SRCLNG, lng).apply();

                    }

                    if (status.equals("ST")) {
                        Intent home = new Intent(ActivityAccepted.this, ActivityHome.class);
                        startActivity(home);
                        finish();
                    }

                } else if (active.equals("false")) {
                    Intent home = new Intent(ActivityAccepted.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //response on hitting agent-delivery-cancel API
        if (id == 2) {
            Intent home = new Intent(ActivityAccepted.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
        //response on hitting agent-delivery-start API
        if (id == 3) {
            try {
                String status = response.getString("status");
                if (status.equals("403")) {
                    Toast.makeText(this, R.string.incorrect_otp, Toast.LENGTH_LONG).show();
                    otp.requestFocus();
                }
                if (status.equals("402")) {
                    Toast.makeText(this, R.string.first_reach_loc, Toast.LENGTH_LONG).show();
                    otp.requestFocus();
                }
                /*Intent home = new Intent(ActivityRideAccepted.this, MapsActivity2.class);
                startActivity(home);
                finish();*/
            } catch (Exception e) {
                Intent home = new Intent(ActivityAccepted.this, ActivityEnroute.class);
                startActivity(home);
                finish();
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(a, R.string.something_wrong, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
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
            infoText.setText("Have you collected ₹ " + price + " in cash ?");
            yesPayment.setOnClickListener(this);
            noPayment.setOnClickListener(this);
            myDialog.setCanceledOnTouchOutside(false);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void callClientPhn() {
        String phoneDriver = phone.getText().toString().trim();
        /*Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneDriver));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);*/

        Uri call = Uri.parse("tel:" + phoneDriver);
        Intent surf = new Intent(Intent.ACTION_DIAL, call);
        startActivity(surf);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.yes) {
            checkOtp = otp.getText().toString();
            if (!checkOtp.isEmpty()) {
                if (paid.equals("1")|| paid.equals("3")) {
                    delvyStart(checkOtp);
                } else
                    ShowPopup(5);
            } else
                otp.requestFocus();// if OTP field is empty, then driverStartTrip method will not be called
        } else if (id == R.id.no) {
            delvyCancel();
        } else if (id == R.id.map) {
            Intent map = new Intent(ActivityAccepted.this, MapsClientLocation.class);
            startActivity(map);
            finish();
        } else if (id == R.id.infoName) {
            ShowPopup(1);
        } else if (id == R.id.infoAdd) {
            ShowPopup(2);
        } else if (id == R.id.infoLand) {
            ShowPopup(3);
        } else if (id == R.id.infoAmount) {
            ShowPopup(4);
        } else if (id == R.id.reject_request) {
            myDialog.dismiss();
        } else if (id == R.id.accept_request) {
            delvyStart(checkOtp);//method to check if the OTP entered is correct or not
            myDialog.dismiss();
        } else if (id == R.id.dialPhn || id == R.id.src_phone) {
            callClientPhn();
        }
    }
}
