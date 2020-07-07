package com.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.client.ride.ActivityRideOTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UtilityBaseClass extends AppCompatActivity {
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";
    private static final String TAG = "UtilityBaseClass";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String COST_DROP = "CostDrop";
    public static final String TIME_DROP = "TimeDrop";
    public static final String OTP_PICK = "OTPPick";
    public static final String VAN_PICK = "VanPick";

    String stringAuth;
    SharedPreferences prefAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_base_class);

        userTripGetStatus();
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
    }

    private void userTripGetStatus() {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        UtilityBaseClass a = UtilityBaseClass.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "user-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {

        if (id == 3) {
            Log.d(TAG, "RESPONSE:" + response);
            try {

                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TRIP_ID, tid).apply();
                    /*if (status.equals("RQ")) {
                        Snackbar snackbar = Snackbar
                                .make(scrollView, "WAITING FOR DRIVER", Snackbar.LENGTH_INDEFINITE)
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
                        intent.setAction("02");
                        startService(intent);
                    }*/
                    if (status.equals("AS")) {
                        String otp = response.getString("otp");
                        String van = response.getString("van");
                        Intent as = new Intent(UtilityBaseClass.this, ActivityRideOTP.class);
                        as.putExtra("OTP", otp);
                        as.putExtra("VAN", van);
                        startActivity(as);
                        SharedPreferences sp_otp = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
                        sp_otp.edit().putString(OTP_PICK, otp).apply();
                        sp_otp.edit().putString(VAN_PICK, van).apply();
                    }
                } else {
                    Intent homePage = new Intent(UtilityBaseClass.this, ActivityWelcome.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homePage);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

}
