package com.clientzp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityLoginKey extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityRegistration";
    TextView text_tnc, text_policies;
    PopupWindow popupWindow, popupWindowState;
    EditText mobileEdit, keyEdit;
    ScrollView scrollView;
    String mobile, strUserMobile, strUserKey;
    private ImageButton btnSignIn;
    ProgressBar simpleProgressBar;
    CheckBox tnc;
    //firebase auth object
    private FirebaseAuth mAuth;
    public static final String AUTH_KEY = "AuthKey";
    public static final String NAME_KEY = "NameKey";
    public static final String PHN_KEY = "PhnKey";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AN_KEY = "AadharKey";
    String newToken;

    public void onSuccess(JSONObject response) throws JSONException {
        //Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
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
        Intent next = new Intent(ActivityLoginKey.this, ActivityWelcome.class);
        startActivity(next);
        finish();
    }

    public void onFailure(VolleyError error) {
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        simpleProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_key);
        //initialising views

        scrollView = findViewById(R.id.mainLayout);

        mobileEdit = findViewById(R.id.editTextMobile);
        keyEdit = findViewById(R.id.editTextKey);
        btnSignIn = findViewById(R.id.login);
        btnSignIn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        tnc = findViewById(R.id.tnc);
        text_tnc = findViewById(R.id.txt_tnc);
        text_tnc.setOnClickListener(this);
        text_policies = findViewById(R.id.txt_policies);
        text_policies.setOnClickListener(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(ActivityLoginKey.this, new OnSuccessListener<InstanceIdResult>() {
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
        } else if (id == R.id.txt_policies) {
            Uri uri = Uri.parse("https://zippe.in/en/privacy-policy/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.login) {
            simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
            strUserMobile = mobileEdit.getText().toString();
            strUserKey = keyEdit.getText().toString();

            if (TextUtils.isEmpty(strUserMobile)) {
                mobileEdit.setError("This field cannot be left blank");
                return;
            }
            if (TextUtils.isEmpty(strUserKey)) {
                keyEdit.setError("This field cannot be left blank");
                return;
            }

            if (!tnc.isChecked()) {
                Toast.makeText(this, R.string.agree_to_terms, Toast.LENGTH_SHORT).show();
            } else {
                //verifying the code entered manually
                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                params.put("pn", strUserMobile);
                params.put("key", strUserKey);
                params.put("fcm", newToken);
                JSONObject parameters = new JSONObject(params);
                ActivityLoginKey a = ActivityLoginKey.this;
                /*Log.d(TAG, "Values: key=" + strUserKey + " mobile=" + strUserMobile + " fcm token=" + newToken);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME login-user");*/
                UtilityApiRequestPost.doPOST(a, "login-user", parameters, 30000, 0, response -> {
                    try {
                        a.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, a::onFailure);
            }
        }
    }
}
