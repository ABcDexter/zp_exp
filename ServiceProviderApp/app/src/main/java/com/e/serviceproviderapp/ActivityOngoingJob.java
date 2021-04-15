package com.e.serviceproviderapp;

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

public class ActivityOngoingJob extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityOngoingJob";

    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";
    String stringAuthKey, stringBID, auth;
    String jaddress, jdate, jtime, jhrs, jearn, jname, jphone, jnote;
    Dialog myDialog;
    Button btnEndService;
    ActivityOngoingJob a = ActivityOngoingJob.this;
    Map<String, String> params = new HashMap();

    TextView date, time, hrs, earn, name, address, phn, note;
    ImageButton infoDate, infoTime, infoHrs, infoEarn, dialPhn, infoNote, infoAdd, infoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_job);

        SharedPreferences prefCookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        auth = stringAuthKey;
        Intent intent = getIntent();
        stringBID = intent.getStringExtra("BID");
        Log.d(TAG, "BID" + stringBID);

        btnEndService = findViewById(R.id.end_service);
        btnEndService.setOnClickListener(this);

        date = findViewById(R.id.tv_date);
        time = findViewById(R.id.tv_time);
        hrs = findViewById(R.id.tv_hrs);
        earn = findViewById(R.id.tv_earn);
        name = findViewById(R.id.tv_name);
        address = findViewById(R.id.tv_address);
        phn = findViewById(R.id.tv_phone);
        note = findViewById(R.id.tv_note);
        infoDate = findViewById(R.id.infoDate);
        infoTime = findViewById(R.id.infoTime);
        infoHrs = findViewById(R.id.infoHrs);
        infoEarn = findViewById(R.id.infoEarn);
        infoNote = findViewById(R.id.infoNote);
        infoName = findViewById(R.id.infoName);
        infoAdd = findViewById(R.id.infoAddress);
        dialPhn = findViewById(R.id.dialPhn);

        infoDate.setOnClickListener(this);
        infoTime.setOnClickListener(this);
        infoHrs.setOnClickListener(this);
        infoEarn.setOnClickListener(this);
        infoNote.setOnClickListener(this);
        infoName.setOnClickListener(this);
        infoAdd.setOnClickListener(this);
        dialPhn.setOnClickListener(this);
        myDialog = new Dialog(this);
        getJobData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.end_service) {
            endJob();
        } else if (id == R.id.dialPhn) {
            callClientPhn();
        } else if (id == R.id.infoDate) {
            ShowPopup(1);
        } else if (id == R.id.infoTime) {
            ShowPopup(5);
        } else if (id == R.id.infoHrs) {
            ShowPopup(6);
        } else if (id == R.id.infoEarn) {
            ShowPopup(7);
        } else if (id == R.id.infoNote) {
            ShowPopup(3);
        } else if (id == R.id.infoAddress) {
            ShowPopup(2);
        } else if (id == R.id.infoName) {
            ShowPopup(4);
        }
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        LinearLayout ln = (LinearLayout) myDialog.findViewById(R.id.layout_btn);

        if (id == 1) {
            infoText.setText(getString(R.string.j_date, jdate));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 2) {
            infoText.setText(getString(R.string.j_address, jaddress));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 3) {
            infoText.setText(getString(R.string.j_note, jnote));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 4) {
            infoText.setText(getString(R.string.j_name, jname));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 5) {
            infoText.setText(getString(R.string.j_time, jtime));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 6) {
            infoText.setText(getString(R.string.j_hrs, jhrs));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 7) {
            infoText.setText(getString(R.string.j_earn, jearn));
            myDialog.setCanceledOnTouchOutside(true);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void callClientPhn() {
        String phoneDriver = phn.getText().toString().trim();
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

    private void getJobData() {
        String auth = stringAuthKey;
        String bid = stringBID;
        params.put("auth", auth);
        params.put("bid", bid);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: servitor-booking-data");
        Log.d(TAG, "Values: auth=" + auth + " bid=" + bid);

        UtilityApiRequestPost.doPOST(a, "servitor-booking-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    private void endJob() {
        String auth = stringAuthKey;
        String bid = stringBID;
        params.put("auth", auth);
        params.put("bid", bid);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: servitor-booking-cancel");
        Log.d(TAG, "Values: auth=" + auth + " bid=" + bid);

        UtilityApiRequestPost.doPOST(a, "servitor-booking-cancel", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting servitor-booking-start API
        if (id == 1) {
            Intent accepted = new Intent(this, ActivityAllJobsAccepted.class);
            startActivity(accepted);
            finish();
        }
        //response on hitting servitor-booking-cancel API
        if (id == 1) {
            Intent rejected = new Intent(this, ActivityHome.class);
            startActivity(rejected);
            finish();
        }
        //response on hitting servitor-booking-data API
        if (id == 2) {

            String jbid = response.getString("bid");
            jdate = response.getString("date");
            jtime = response.getString("time");
            jhrs = response.getString("hours");
            jearn = response.getString("earn");
            jaddress = response.getString("customer_address");
            jname = response.getString("customer_name");
            jphone = response.getString("customer_phone");
            jnote = response.getString("customer_note");

            date.setText(jdate);
            time.setText(jtime);
            hrs.setText(jhrs);
            earn.setText(getString(R.string.message_rs, jearn));
            address.setText(jaddress);
            note.setText(jnote);
            name.setText(jname);
            phn.setText(jphone);

        }

    }

    public void onFailure(VolleyError error) {
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityOngoingJob.this, ActivityHome.class));
        finish();
    }
}