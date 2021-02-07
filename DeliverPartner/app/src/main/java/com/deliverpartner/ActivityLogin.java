package com.deliverpartner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityLogin";
    ScrollView scrollView;
    private EditText etMobile, etOTP, etLoginKey;
    TextView btnVerifyPhone;
    private ImageButton btnSignIn;

    public static final String PICTURE_UPLOAD_STATUS = "com.agent.pictureUploadStatus";
    public static final String AADHAR = "Aadhar";
    public static final String MOBILE = "Mobile";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    String mobile, loginKey;

    public void onSuccess(JSONObject response) {
        Log.d(TAG, "RESPONSE:" + response);

        try {
            String status = response.getString("status");
            if (status.equals("true")){
                String auth = response.getString("auth");
                String an = response.getString("an");

                SharedPreferences pref_uploadStatus = this.getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
                pref_uploadStatus.edit().putString(AUTH_KEY, auth).apply();

                SharedPreferences aadhar = this.getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
                aadhar.edit().putString(AADHAR, an).apply();

                Intent next = new Intent(ActivityLogin.this, ActivityWelcome.class);
                startActivity(next);
                finish();
            }
            else {
                Toast.makeText(this, "Please check your Login Key", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void onFailure(VolleyError error) {
        Snackbar snackbar = Snackbar.make(scrollView, R.string.login_unsuccessful, Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //application will always run in portrait mode

        //initializing variables
        scrollView = findViewById(R.id.mainLayout);
        etMobile = findViewById(R.id.editTextMobile);
        etOTP = findViewById(R.id.editTextOTP);
        etLoginKey = findViewById(R.id.editTextKey);
        btnSignIn = findViewById(R.id.login);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);

        btnSignIn.setOnClickListener(this);
        btnVerifyPhone.setOnClickListener(this);

        FirebaseAuth.getInstance(); //done to perform a variety of authentication-related operations

    }

    // called when any button is clicked
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonVerifyPhoneNo) {
            verifyPhone();
        } else if (id == R.id.login) {
            final ProgressBar simpleProgressBar = findViewById(R.id.simpleProgressBar);
            String code = etOTP.getText().toString().trim();
            loginKey = etLoginKey.getText().toString().trim();
            if (code.length() != 6) {
                Log.d(TAG, "Error in OTP");
                etOTP.setError("Enter valid OTP");
                etOTP.requestFocus();
                return;
            }
            if (loginKey.isEmpty() || loginKey.length() > 10) {
                Log.d(TAG, "Error in Login Key");
                etLoginKey.setError("Enter valid Login Key");
                etLoginKey.requestFocus();
                return;
            }
            //verifying the code entered manually
            verifyVerificationCode(code);

            simpleProgressBar.setVisibility(View.VISIBLE);
            Map<String, String> params = new HashMap();
            params.put("pn", mobile);
            params.put("key", loginKey);

            JSONObject parameters = new JSONObject(params);
            ActivityLogin a = ActivityLogin.this;
            Log.d(TAG, "Values: phone=" + mobile + "\n" + "key=" + loginKey);
            Log.d(TAG, "UtilityApiRequestPost.doPOST login-agent");
            UtilityApiRequestPost.doPOST(a, "login-agent", parameters, 30000, 0, a::onSuccess, a::onFailure);
        }
    }


    //check id the mobile number is valid or not
    private void verifyPhone() {
        mobile = etMobile.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() < 10) {
            etMobile.setError("ENTER A VALID NUMBER");
            etMobile.requestFocus();
            Log.d(TAG, "Error in Mobile Number");
            return;
        }
        sendVerificationCode(mobile);// firebase method to send 6 digit OTP to this "mobile" number
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
        Log.d(TAG, "OTP test received from Firebase to mobile number" + mobile + "in method sendVerificationCode");
    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();
            Log.d(TAG, "OTP not detected automatically");
            if (code != null) {
                Log.d(TAG, "OTP detected automatically");
                etOTP.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        //if verification fails for whatever reason
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, R.string.verification_failed + e.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    //verifying the entered OTP code
    private void verifyVerificationCode(String code) {
        try {
            Log.d(TAG, "signing in the user in method verifyVerificationCode");
        } catch (Exception e) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, "Verification Code is wrong", Snackbar.LENGTH_LONG);
            snackbar.show();
            Log.d(TAG, "Error" + e);
        }
    }

}