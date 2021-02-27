package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActivityRegistration extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityRegistration";
    TextView text_tnc;
    Button btn_gender, btn_state;
    PopupWindow popupWindow, popupWindowState;
    EditText nameEdit, mobileEdit, otpEdit;
    TextView btnVerifyPhone;
    ScrollView scrollView;
    String mobile, mVerificationId, strUserName, strUserMobile, strUserState, strUserGender;
    private ImageButton btnSignIn;
    ProgressBar simpleProgressBar;
    CheckBox tnc;
    //firebase auth object
    private FirebaseAuth mAuth;
    public static final String AUTH_KEY = "AuthKey";
    public static final String NAME_KEY = "NameKey";
    public static final String PHN_KEY = "PhnKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AN_KEY = "AadharKey";
    String newToken;

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
//response on hitting register-user-no-aadhaar API
        String auth = response.getString("auth");
        String name = response.getString("name");
        String an = response.getString("an");
        String pn = response.getString("pn");

        //TODO use the value of userExists

        SharedPreferences sp_cookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        sp_cookie.edit().putString(AUTH_KEY, auth).apply();
        sp_cookie.edit().putString(NAME_KEY, name).apply();
        sp_cookie.edit().putString(PHN_KEY, pn).apply();
        sp_cookie.edit().putString(AN_KEY, an).apply();
        Intent next = new Intent(ActivityRegistration.this, ActivityWelcome.class);
        startActivity(next);
        finish();
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        simpleProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //initialising views

        btn_gender = findViewById(R.id.btnGender);
        btn_state = findViewById(R.id.btnHomeState);
        scrollView = findViewById(R.id.mainLayout);
        nameEdit = findViewById(R.id.editTextName);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);
        btnVerifyPhone.setOnClickListener(this);
        btn_gender.setOnClickListener(this);
        btn_state.setOnClickListener(this);
        mobileEdit = findViewById(R.id.editTextMobile);
        otpEdit = findViewById(R.id.editTextOTP);
        btnSignIn = findViewById(R.id.login);
        btnSignIn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        tnc = findViewById(R.id.tnc);
        text_tnc = findViewById(R.id.txt_tnc);
        text_tnc.setOnClickListener(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(ActivityRegistration.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                newToken = instanceIdResult.getToken();
                Log.e("newToken", newToken);

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_tnc) {
            Uri uri = Uri.parse("https://zippe.in/en/terms-and-conditions/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.buttonVerifyPhoneNo) {
            verifyPhone();
        } else if (id == R.id.btnGender) {
            LayoutInflater layoutInflater = (LayoutInflater) ActivityRegistration.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            View customView = layoutInflater.inflate(R.layout.popup, null);

            TextView female = customView.findViewById(R.id.female);
            TextView male = customView.findViewById(R.id.male);
            TextView non_binary = customView.findViewById(R.id.non_binary);

            //instantiate popup window
            popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //display the popup window
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            popupWindow.showAtLocation(scrollView, Gravity.CENTER, 0, 0);
            popupWindow.setOutsideTouchable(false);
            //close the popup window on button click
            female.setOnClickListener(this);
            male.setOnClickListener(this);
            non_binary.setOnClickListener(this);
        } else if (id == R.id.btnHomeState) {
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            } catch (Exception e) {
                // TODO: handle exception
            }

            LayoutInflater layoutInflaterState = (LayoutInflater) ActivityRegistration.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflaterState != null;
            View customViewState = layoutInflaterState.inflate(R.layout.popup_state, null);

            TextView uk = customViewState.findViewById(R.id.uk);
            TextView raj = customViewState.findViewById(R.id.raj);
            //instantiate popup window
            popupWindowState = new PopupWindow(customViewState, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //display the popup window
            popupWindowState.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            popupWindowState.showAtLocation(scrollView, Gravity.CENTER, 0, 0);
            popupWindowState.setOutsideTouchable(false);
            //close the popup window on button click
            uk.setOnClickListener(this);
            raj.setOnClickListener(this);
        } else if (id == R.id.female) {
            popupWindow.dismiss();
            btn_gender.setText(R.string.female);
        } else if (id == R.id.male) {
            popupWindow.dismiss();
            btn_gender.setText(R.string.male);
        } else if (id == R.id.non_binary) {
            popupWindow.dismiss();
            btn_gender.setText(R.string.non_binary);
        } else if (id == R.id.uk) {
            popupWindowState.dismiss();
            btn_state.setText(R.string.uttarakhand);
        } else if (id == R.id.raj) {
            popupWindowState.dismiss();
            btn_state.setText(R.string.rajasthan);
        } else if (id == R.id.login) {
            simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
            strUserName = nameEdit.getText().toString();
            strUserMobile = mobileEdit.getText().toString();
            strUserState = btn_state.getText().toString();
            strUserGender = btn_gender.getText().toString();

            if (TextUtils.isEmpty(strUserName)) {
                nameEdit.setError("This field cannot be left blank");
                return;
            }
            if (TextUtils.isEmpty(strUserMobile)) {
                mobileEdit.setError("This field cannot be left blank");
                return;
            }

            String code = otpEdit.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                Log.d(TAG, "Error in OTP");
                otpEdit.setError("Enter valid code");
                otpEdit.requestFocus();
                return;
            }
            if (!tnc.isChecked()) {
                Toast.makeText(this, R.string.agree_to_terms, Toast.LENGTH_SHORT).show();
            } else {
                //verifying the code entered manually
                verifyVerificationCode(code);
                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                params.put("phone", strUserMobile);
                params.put("name", strUserName);
                params.put("home", strUserState);
                params.put("gender", strUserGender);
                params.put("fcm", newToken);
                JSONObject parameters = new JSONObject(params);
                ActivityRegistration a = ActivityRegistration.this;
                Log.d(TAG, "Values: name=" + strUserName + " mobile=" + strUserMobile +
                        " home state=" + strUserState + " gender=" + strUserGender + " fcm token=" + newToken);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME register-user-no-aadhaar");
                UtilityApiRequestPost.doPOST(a, "register-user-no-aadhaar", parameters, 30000, 0, response -> {
                    try {
                        a.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, a::onFailure);
            }
        }
    }

    private void verifyPhone() {
        mobile = mobileEdit.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() < 10) {
            mobileEdit.setError("ENTER A VALID NUMBER");
            mobileEdit.requestFocus();
            Log.d(TAG, "Error in Mobile Number");
            return;
        }
        sendVerificationCode(mobile);
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
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();
            Log.d(TAG, "OTP not detected automatically");
            if (code != null) {
                Log.d(TAG, "OTP detected automatically");
                otpEdit.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, "Verification Failed: " + e.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.show();

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };

    private void verifyVerificationCode(String code) {
        try {
            //creating the credential
            //PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            //signing the user
            Log.d(TAG, "signing in the user in method verifyVerificationCode");
        } catch (Exception e) {
            Snackbar snackbar = Snackbar
                    .make(scrollView, "Verification Code is wrong", Snackbar.LENGTH_LONG);
            snackbar.show();
            Log.d(TAG, "Error" + e);
        }
    }
}
