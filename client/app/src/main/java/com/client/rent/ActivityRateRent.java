package com.client.rent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRateRent extends ActivityDrawer implements View.OnClickListener {

    ImageButton happy, sad;
    ScrollView scrollView;
    Dialog myDialog, checkDialog;
    TextView textView;
    String stringAuthCookie;

    public static final String AUTH_KEY = "AuthKey";
    private static final String TAG = "ActivityRateRent";
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String VAN_PICK = "VanPick";
    public static final String OTP_PICK = "OTPPick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rate, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        /*Objects.requireNonNull(getSupportActionBar()).setTitle("fgh");*/
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthCookie = prefCookie.getString(AUTH_KEY, "");
        happy = findViewById(R.id.satisfied);
        sad = findViewById(R.id.notSatisfied);
        scrollView = findViewById(R.id.scrollViewRateActivity);
        textView = findViewById(R.id.txt_rate);

        textView.setText(R.string.rate_exp);
        happy.setOnClickListener(this);
        sad.setOnClickListener(this);

        myDialog = new Dialog(this);
        checkDialog = new Dialog(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.satisfied) {
            ShowPopup();
        } else if (id == R.id.notSatisfied) {
            CheckPopup();
        }
    }

    private void ShowPopup() {
        rateTrip("", "1");
        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(R.string.thanks_for_renting);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(false);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateRent.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
                myDialog.dismiss();
            }
        }, 5000);*/
    }

    public void rateTrip(String rev, String i) {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("rate", i);
        params.put("rev", rev);
        JSONObject parameters = new JSONObject(params);
        ActivityRateRent a = ActivityRateRent.this;
        Log.d(TAG, "Values: auth=" + auth+ " rate="+i+" rev="+rev);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-rate");
        UtilityApiRequestPost.doPOST(a, "auth-trip-rate", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    public void retireTrip() {
        String auth = stringAuthCookie;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        ActivityRateRent a = ActivityRateRent.this;
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-trip-retire");
        UtilityApiRequestPost.doPOST(a, "user-trip-retire", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        //response on hitting auth-trip-get-info API
        if (id == 1) {
            SharedPreferences preferences = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = preferences.edit();
            editor2.clear();
            editor2.apply();

            SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_LOCATIONS, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.remove(LOCATION_PICK);
            editor.remove(LOCATION_DROP);
            editor.remove(LOCATION_DROP_ID);
            editor.remove(LOCATION_PICK_ID);
            editor.remove(OTP_PICK);
            editor.apply();

            SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
            SharedPreferences.Editor editor1 = prefBuzz.edit();
            editor1.remove(BUSS_FLAG);
            editor1.apply();

            retireTrip();

        }
        if (id==2){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myDialog.dismiss();
                    Intent finishIntent = new Intent(ActivityRateRent.this, ActivityWelcome.class);
                    startActivity(finishIntent);
                    finish();
                }
            }, 5000);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    private void CheckPopup() {
        checkDialog.setContentView(R.layout.popup_checkbox);
        TextView cancel = (TextView) checkDialog.findViewById(R.id.cancel);
        TextView submit = (TextView) checkDialog.findViewById(R.id.submit);
        EditText specify = checkDialog.findViewById(R.id.specify);
        CheckBox chk1 = checkDialog.findViewById(R.id.attitude);
        CheckBox chk2 = checkDialog.findViewById(R.id.condition);
        CheckBox chk3 = checkDialog.findViewById(R.id.clean);
        CheckBox chk4 = checkDialog.findViewById(R.id.other);

        chk1.setText(R.string.att_contact_person);
        chk2.setText(R.string.veh_condi);
        chk3.setText(R.string.veh_clean);
        chk4.setText(R.string.any_other);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopup1();
                String str1="",str2="",str3="",str4="";
                checkDialog.dismiss();
                if (chk1.isChecked()) {
                    str1 = "Attitude of contact person";
                }else if (!chk1.isChecked()){
                    str1="";
                }
                if (chk2.isChecked()){
                    str2 = "Vehicle condition";
                }else if (!chk2.isChecked()){
                    str2="";
                }
                if (chk3.isChecked()){
                    str3 = "Vehicle cleanliness";
                }else if (!chk3.isChecked()){
                    str3="";
                }
                if (chk4.isChecked()){
                    str4 = specify.getText().toString();
                }else if (!chk4.isChecked()){
                    str4="";
                }
                rateTrip(str1+str2+str3+str4, "0");
            }
        });

        checkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = checkDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        checkDialog.show();
        Window window = checkDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        checkDialog.setCanceledOnTouchOutside(false);
    }

    private void ShowPopup1() {

        myDialog.setContentView(R.layout.popup_color);

        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText(R.string.thanks_for_feedback);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(false);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateRent.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
            }
        }, 5000);*/
    }

}
