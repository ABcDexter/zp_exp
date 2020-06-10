package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityProfileReview extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityProfileReview";
    ScrollView scrollView;
    private TextView txtName, txtAge;
    EditText nameEdit;
    RelativeLayout rl;
    private ImageButton btnConfirm;
    public static final String AUTH_KEY = "AuthKey";
    public static final String AGE_KEY = "AgeKey";
    public static final String GDR_KEY = "GdrKey";
    public static final String HS_KEY = "HsKey";
    public static final String NAME_KEY = "NameKey";
    public static final String PHONE_KEY = "PhoneKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    String name, gender;
    Button btn_gender;
    ImageButton saveName;
    PopupWindow popupWindow;
    public static final String USER_DATA = "com.client.UserData";
    public static final String USER_NAME = "UserName";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_GENDER = "UserGender";
    public static final String USER_AGE = "UserAge";

    SharedPreferences prefUserDetails;
    String strAuth, strName, strAge, strPn, strGdr;

    public void onSuccess(JSONObject response, int id) throws JSONException {
        if (id == 1) {
            Intent intent = new Intent(ActivityProfileReview.this, ActivityWelcome.class);
            startActivity(intent);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_review);
        scrollView = findViewById(R.id.mainLayout);
        txtName = findViewById(R.id.txt_name);
        txtName.setOnClickListener(this);
        txtAge = findViewById(R.id.txt_age);
        btnConfirm = findViewById(R.id.confirmDetails);
        btnConfirm.setOnClickListener(this);
        btn_gender = findViewById(R.id.btnGender);
        btn_gender.setOnClickListener(this);

        rl = findViewById(R.id.rl_saveName);
        saveName = findViewById(R.id.saveName);
        saveName.setOnClickListener(this);
        nameEdit = findViewById(R.id.edt_name);
        prefUserDetails = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        strAuth = prefUserDetails.getString(AUTH_KEY, "");
        strAge = prefUserDetails.getString(AGE_KEY, "");
        strPn = prefUserDetails.getString(PHONE_KEY, "");
        strName = prefUserDetails.getString(NAME_KEY, "");
        strGdr = prefUserDetails.getString(GDR_KEY, "");
        txtName.setText(strName);
        txtAge.setText(strAge);
        btn_gender.setText(strGdr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_name:
                rl.setVisibility(View.VISIBLE);
                txtName.setVisibility(View.INVISIBLE);
                break;
            case R.id.saveName:
                rl.setVisibility(View.INVISIBLE);
                txtName.setVisibility(View.VISIBLE);
                txtName.setText(nameEdit.getText());
                break;
            case R.id.confirmDetails:
                updateDetails();
                break;
            case R.id.btnGender:
                LayoutInflater layoutInflater = (LayoutInflater) ActivityProfileReview.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert layoutInflater != null;
                View customView = layoutInflater.inflate(R.layout.popup, null);

                TextView female = customView.findViewById(R.id.female);
                TextView male = customView.findViewById(R.id.male);
                TextView non_binary = customView.findViewById(R.id.non_binary);

                //instantiate popup window
                popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                //display the popup window
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                popupWindow.showAtLocation(scrollView, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(false);
                //close the popup window on button click
                female.setOnClickListener(this);
                male.setOnClickListener(this);
                non_binary.setOnClickListener(this);
                break;

            case R.id.female:
                popupWindow.dismiss();
                btn_gender.setText("FEMALE");
                break;
            case R.id.male:
                popupWindow.dismiss();
                btn_gender.setText("MALE");
                break;
            case R.id.non_binary:
                popupWindow.dismiss();
                btn_gender.setText("NON BINARY");
                break;
        }
    }

    private void updateDetails() {

        name = txtName.getText().toString();

        if (name.isEmpty() || name.equals("ENTER FULL NAME")) {
            txtName.setError("This Field cannot be left blank");
            txtName.requestFocus();
            Log.d(TAG, "Name Field Left Blank");
            return;
        }

        Intent animIntent = new Intent(ActivityProfileReview.this, ActivityWelcome.class);
        startActivity(animIntent);
        finish();

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        String stringAuth = prefPLoc.getString(AUTH_KEY, "");
        Log.d(TAG, " stringAuth auth = " + stringAuth);

        Map<String, String> params = new HashMap();

        params.put("auth", stringAuth);
        params.put("name", name);
        params.put("gdr", btn_gender.getText().toString());
        JSONObject parameters = new JSONObject(params);
        ActivityProfileReview a = ActivityProfileReview.this;
        Log.d(TAG, "Values:  name=" + name + " gender=" + btn_gender.getText().toString() + " auth = " + stringAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME: auth-profile-update");
        UtilityApiRequestPost.doPOST(a, "auth-profile-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }
}