package com.zpdeliveryagent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ActivityTotalEarnings extends AppCompatActivity implements View.OnClickListener {
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    private static final String TAG = "ActivityTotalEarnings";
    ActivityTotalEarnings a = ActivityTotalEarnings.this;
    Map<String, String> params = new HashMap();
    Button btnDatePicker;
    TextView txtDate, earnings;
    String strAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_earnings);
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");
        btnDatePicker = findViewById(R.id.btn_date);

        txtDate = findViewById(R.id.date);
        earnings = findViewById(R.id.earnings);
        btnDatePicker.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {

            Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            //int mDay = c.get(Calendar.DAY_OF_MONTH);
            int mDay = 1;
            //TextView txtMonth;
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_DARK,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            txtDate.setText((monthOfYear + 1) + "-" + year);
                            int months= monthOfYear+1;
                            String month = String.valueOf(months);
                            getEarnings(month);
                        }
                    }, mYear, mMonth, mDay);

            ((ViewGroup) datePickerDialog.getDatePicker()).findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
            datePickerDialog.show();
        }

    }

    private void getEarnings(String month) {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("month", month);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " month=" + month);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME agent-delivery-earning");
        UtilityApiRequestPost.doPOST(a, "agent-delivery-earning", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting agent-delivery-get-status API
        if (id == 1) {
            String total = response.getString("total");
            earnings.setText(total);
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(a, R.string.something_wrong, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

}