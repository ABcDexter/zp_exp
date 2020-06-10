package com.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ActivityMain";
    ScrollView scrollView;
    private EditText etMobile, etOTP;
    TextView btnVerifyPhone;
    private ImageButton btnSignIn;
    private ImageView imgAadharFront, imgAadharBack;
    public static final String AUTH_KEY = "AuthKey";
    public static final String AGE_KEY = "AgeKey";
    public static final String GDR_KEY = "GdrKey";
    public static final String HS_KEY = "HsKey";
    public static final String NAME_KEY = "NameKey";
    public static final String PHONE_KEY = "PhoneKey";
    public static final String AN_KEY = "AadharKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    String mobile, aadhar_f, aadhar_b, mVerificationId;
    //firebase auth object
    private FirebaseAuth mAuth;
    Bitmap bitmap;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);

        String age = response.getString("age");
        String an = response.getString("an");
        String auth = response.getString("auth");
        String dl = response.getString("dl");
        String gdr = response.getString("gdr");
        String hs = response.getString("hs");
        String name = response.getString("name");
        String pn = response.getString("pn");
        String tid = response.getString("tid");


        //TODO use the value of userExists

        SharedPreferences sp_cookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        sp_cookie.edit().putString(AUTH_KEY, auth).apply();
        sp_cookie.edit().putString(AGE_KEY, age).apply();
        sp_cookie.edit().putString(GDR_KEY, gdr).apply();
        sp_cookie.edit().putString(HS_KEY, hs).apply();
        sp_cookie.edit().putString(NAME_KEY, name).apply();
        sp_cookie.edit().putString(PHONE_KEY, pn).apply();
        sp_cookie.edit().putString(AN_KEY, an).apply();
        Intent next = new Intent(ActivityMain.this, ActivityProfileReview.class);
        startActivity(next);
        finish();
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.mainLayout);
        etMobile = findViewById(R.id.editTextMobile);

        etOTP = findViewById(R.id.editTextOTP);
        btnSignIn = findViewById(R.id.confirmDetails);
        imgAadharBack = findViewById(R.id.aadharBackImg);
        imgAadharBack.setOnClickListener(this);
        imgAadharFront = findViewById(R.id.aadharFrontImg);
        imgAadharFront.setOnClickListener(this);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);
        btnVerifyPhone.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonVerifyPhoneNo:
                verifyPhone();
                break;
            case R.id.aadharFrontImg:
                selectImage(ActivityMain.this, 1);
                break;
            case R.id.aadharBackImg:
                selectImage(ActivityMain.this, 2);
                break;
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void selectImage(Context context, int fromRID) {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (fromRID == 1) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 1);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 2);

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                    if (fromRID == 2) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 3);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 4);//one can be replaced with any action code

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                }
            });
            builder.show();
        }
    }

    private void nextActivity(String aadharF, String aadharB) {

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
                String code = etOTP.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    Log.d(TAG, "Error in OTP");
                    etOTP.setError("Enter valid code");
                    etOTP.requestFocus();
                    return;
                }
                //verifying the code entered manually
                verifyVerificationCode(code);
                Log.d(TAG, "Control came to nextActivity()");
                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                params.put("phone", mobile);
                params.put("aadhaarFront", aadharF);
                params.put("aadhaarBack", aadharB);
                JSONObject parameters = new JSONObject(params);
                ActivityMain a = ActivityMain.this;
                Log.d(TAG, "Values: phone=" + mobile + " aadhaarFront=" + aadharF + " aadhaarBack=" + aadharB);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME register-user");
                UtilityApiRequestPost.doPOST(a, "register-user", parameters, 30000, 0, response -> {
                    try {
                        a.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, a::onFailure);
            }
        });
    }

    public void convertAndUpload(int identify) {

        if (identify == 1 || identify == 2) {

            imgAadharFront.buildDrawingCache();
            bitmap = imgAadharFront.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "CONVERT aadhar front converted to Base64" + aadhar_f);
            Log.d(TAG, "Control moved to nextActivity()");
        }
        if (identify == 3 || identify == 4) {
            imgAadharBack.buildDrawingCache();
            bitmap = imgAadharBack.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "CONVERT aadhar back converted to Base64");
            Log.d(TAG, "Control moved to nextActivity()");
        }
        nextActivity(aadhar_f, aadhar_b);
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharFront.setImageBitmap(selectedImage);
                        convertAndUpload(1);
                    }
                    break;
                case 2:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imgAadharFront.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }
                    break;
                case 3:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharBack.setImageBitmap(selectedImage);
                        convertAndUpload(3);
                    }

                    break;
                case 4:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Log.d(TAG, "image set");
                                imgAadharBack.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(4);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }

    }

    private void verifyPhone() {
        mobile = etMobile.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() < 10) {
            etMobile.setError("ENTER A VALID NUMBER");
            etMobile.requestFocus();
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