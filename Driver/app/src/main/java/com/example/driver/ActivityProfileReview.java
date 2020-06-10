package com.example.driver;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityProfileReview extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ActivityProfileReview.class.getName();
    ScrollView scrollView;
    private TextView txtName, txtAge;
    EditText nameEdit;
    RelativeLayout rl;
    private ImageButton btnConfirm;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    String name, age, gender;
    Button btn_gender ;
    ImageButton saveName;
    SharedPreferences prefPLoc;
    String stringAuth;
    PopupWindow popupWindow;
    public static final String USER_DATA = "com.client.UserData";
    public static final String USER_NAME = "UserName";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_GENDER = "UserGender";
    public static final String USER_AGE = "UserAge";


    public void onSuccess(JSONObject response) throws JSONException {
        try {
            // Parsing json object response
            // response will be a json object
            String ocrName = response.getString("name");
            String ocrAge = response.getString("age");
            String ocrgnd = response.getString("gdr");
            String ocrph = response.getString("pn");

            txtAge.setText(ocrAge);
            txtName.setText(ocrName);
            btn_gender.setText(ocrgnd);

            SharedPreferences pref = this.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(USER_NAME, ocrName);
            editor.putString(USER_AGE, ocrAge);
            editor.putString(USER_PHONE, ocrph);
            editor.putString(USER_GENDER, ocrgnd);
            editor.apply();

            Log.d(TAG, "name:" + ocrName + " age:" + ocrAge + " gnd:" + ocrgnd + " ph:" + ocrph);
            Log.d(TAG, "name:" + pref.getString(USER_NAME, "") + " age:" +
                    pref.getString(USER_AGE, "") + " gnd:" + pref.getString(USER_GENDER, "") + " ph:" + pref.getString(USER_PHONE, ""));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
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

        getDetails();

        prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefPLoc.getString(AUTH_KEY, "");

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
    }

    private void getDetails() {
        Map<String, String> params = new HashMap();

        params.put("auth", stringAuth);
        Log.d(TAG, "stringAuth: " + stringAuth);
        JSONObject parameters = new JSONObject(params);
        ActivityProfileReview a = ActivityProfileReview.this;
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost");
        UtilityApiRequestPost.doPOST(a, "auth-get-profile", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
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
                popupWindow.setBackgroundDrawable(new ColorDrawable(
                        Color.BLACK));
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

        age = txtAge.getText().toString();
        name = txtName.getText().toString();
        int yrs;
        if (!age.equals("AGE")) {
            yrs = Integer.parseInt(age);
            if (age.isEmpty() || yrs <= 18) {
                Snackbar snackbar = Snackbar
                        .make(scrollView, "YOU ARE TOO YOUNG TO REGISTER WITH US!", Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.d(TAG, "Age less than 18 years");
                return;
            }
        } else {
            yrs = 0;
            Toast.makeText(this, "ENTER A VALID AGE", Toast.LENGTH_SHORT).show();
        }
        if (name.isEmpty() || name.equals("ENTER FULL NAME")) {
            txtName.setError("This Field cannot be left blank");
            txtName.requestFocus();
            Log.d(TAG, "Name Field Left Blank");
            return;
        }

        Intent animIntent = new Intent(ActivityProfileReview.this, ActivityWelcome.class);
        startActivity(animIntent);
        finish();
        Map<String, String> params = new HashMap();

        params.put("auth", stringAuth);
        params.put("name", name);
        params.put("age", age);
        params.put("gdr", gender);
        JSONObject parameters = new JSONObject(params);
        ActivityProfileReview a = ActivityProfileReview.this;
        Log.d(TAG, "Values:  name=" + name + " age=" + age + " gender=" + gender);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST");
        UtilityApiRequestPost.doPOST(a, "auth-update-profile", parameters, 30000, 0, response -> {
            try {
                if (response.getString("status").equals("true")) {
                    Toast.makeText(this, "Profile Updated Successfully! ", Toast.LENGTH_LONG).show();

                }
                if (response.getString("status").equals("false")) {
                    Toast.makeText(this, "Profile Update Unsuccessful! ", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(ActivityProfileReview.this, ActivityWelcome.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }, a::onFailure);
    }

}