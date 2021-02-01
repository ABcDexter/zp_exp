package com.e.serviceproviderapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class ActivityNewJobs extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityNewJobs";

    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";

    String stringAuthKey, stringBID, auth;
    String jdate, jtime, jhrs, jearn, jarea, jnote;
    TextView date, time, hrs, area, earn, note;
    ImageButton infoDate, infoTime, infoHrs, infoArea, infoEarn, infoNote;
    Dialog myDialog;
    Button accept, reject;

    ActivityNewJobs a = ActivityNewJobs.this;
    Map<String, String> params = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_jobs);

        SharedPreferences prefCookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        auth = stringAuthKey;
        Intent intent = getIntent();
        stringBID = intent.getStringExtra("BID");
        Log.d(TAG, "BID" + stringBID);

        date = findViewById(R.id.tvDate);
        time = findViewById(R.id.tvTime);
        hrs = findViewById(R.id.tvHrs);
        area = findViewById(R.id.tvArea);
        earn = findViewById(R.id.tvEarn);
        note = findViewById(R.id.tvNote);
        accept = findViewById(R.id.btnAccept);
        reject = findViewById(R.id.btnReject);

        infoDate = findViewById(R.id.infoDate);
        infoTime = findViewById(R.id.infoTime);
        infoHrs = findViewById(R.id.infoHrs);
        infoArea = findViewById(R.id.infoArea);
        infoEarn = findViewById(R.id.infoEarn);
        infoNote = findViewById(R.id.infoNote);

        infoDate.setOnClickListener(this);
        infoTime.setOnClickListener(this);
        infoHrs.setOnClickListener(this);
        infoArea.setOnClickListener(this);
        infoEarn.setOnClickListener(this);
        infoNote.setOnClickListener(this);
        accept.setOnClickListener(this);
        reject.setOnClickListener(this);

        myDialog = new Dialog(this);
        getJobData();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.infoDate) {
            ShowPopup(1);
        } else if (id == R.id.infoTime) {
            ShowPopup(4);
        } else if (id == R.id.infoHrs) {
            ShowPopup(5);
        } else if (id == R.id.infoEarn) {
            ShowPopup(6);
        } else if (id == R.id.infoArea) {
            ShowPopup(2);
        } else if (id == R.id.infoNote) {
            ShowPopup(3);
        } else if (id == R.id.btnAccept) {
            Intent accept = new Intent(ActivityNewJobs.this, ActivityOTP.class);
            accept.putExtra("BID", stringBID);
            accept.putExtra("CALL", "1");
            startActivity(accept);
            finish();
        } else if (id == R.id.btnReject) {
            Intent reject = new Intent(ActivityNewJobs.this, ActivityHome.class);
            startActivity(reject);
            finish();
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
            infoText.setText(getString(R.string.j_area, jarea));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 3) {
            infoText.setText(getString(R.string.j_note, jnote));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 4) {
            infoText.setText(getString(R.string.j_time, jtime));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 5) {
            infoText.setText(getString(R.string.j_hrs, jhrs));
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 6) {
            infoText.setText(getString(R.string.j_earn, jearn));
            myDialog.setCanceledOnTouchOutside(true);
        }

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-trip-data API
        if (id == 2) {

            String jbid = response.getString("bid");
            jdate = response.getString("date");
            jtime = response.getString("time");
            jhrs = response.getString("hours");
            jearn = response.getString("earn");
            jarea = response.getString("area");
            jnote = response.getString("customer_note");

            date.setText(jdate);
            time.setText(jtime);
            hrs.setText(jhrs);
            earn.setText(getString(R.string.message_rs, jearn));
            area.setText(jarea);
            note.setText(jnote);

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
        startActivity(new Intent(ActivityNewJobs.this, ActivityHome.class));
        finish();
    }
}