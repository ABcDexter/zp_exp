package com.example.driver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
    private ImageView imgAadharFront, imgAadharBack, imgDLFront, imgDLBack;

    public static final String VERIFICATION_TOKEN = "Token";
    public static final String AADHAR = "Aadhar";
    public static final String MOBILE = "Mobile";
    public static final String PICTURE_UPLOAD_STATUS = "com.driver.pictureUploadStatus";
    String mobile, aadhar_f, aadhar_b, dl_b, dl_f, mVerificationId;
    Bitmap bitmap;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};

    public void onSuccess(JSONObject response) {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        try {
            String tokenPerson = response.getString("token");
            String aadharPerson = response.getString("an");
            String mobilePerson = response.getString("pn");

            SharedPreferences pref_uploadStatus = this.getSharedPreferences(PICTURE_UPLOAD_STATUS, Context.MODE_PRIVATE);
            pref_uploadStatus.edit().putString(VERIFICATION_TOKEN, tokenPerson).apply();
            pref_uploadStatus.edit().putString(AADHAR, aadharPerson).apply();
            pref_uploadStatus.edit().putString(MOBILE, mobilePerson).apply();
            Intent next = new Intent(ActivityMain.this, ActivityVerifyDLDetails.class);
            startActivity(next);
            finish();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Snackbar snackbar = Snackbar.make(scrollView, "UPLOAD SUCCESSFUL!", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void onFailure(VolleyError error) {
        Snackbar snackbar = Snackbar.make(scrollView, "UPLOAD UNSUCCESSFUL! CHECK YOUR INTERNET CONNECTION", Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //application will always run in portrait mode

        //initializing variables
        scrollView = findViewById(R.id.mainLayout);
        etMobile = findViewById(R.id.editTextMobile);
        etOTP = findViewById(R.id.editTextOTP);
        btnSignIn = findViewById(R.id.confirmDetails);
        imgAadharBack = findViewById(R.id.aadharBackImg);
        imgAadharFront = findViewById(R.id.aadharFrontImg);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);
        imgDLBack = findViewById(R.id.dlBackImg);
        imgDLFront = findViewById(R.id.dlFrontImg);

        imgDLBack.setOnClickListener(this);
        imgDLFront.setOnClickListener(this);
        btnVerifyPhone.setOnClickListener(this);
        imgAadharBack.setOnClickListener(this);
        imgAadharFront.setOnClickListener(this);

        FirebaseAuth.getInstance(); //done to perform a variety of authentication-related operations

    }

    // called when any button is clicked
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonVerifyPhoneNo:
                verifyPhone();
                break;
            case R.id.aadharFrontImg:
                selectImage(1);
                break;
            case R.id.aadharBackImg:
                selectImage(2);
                break;
            case R.id.dlFrontImg:
                selectImage(3);
                break;
            case R.id.dlBackImg:
                selectImage(4);
                break;
        }
    }

    //check if user has given STORAGE, CAMERA and INTERNET permissions
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

    //method to select image from gallery
    private void selectImage(int fromRID) {

//permission check
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"}; //dialog box will appear with these options
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (fromRID == 1) {//this "fromRID" tells us which button has called this method
                        if (options[item].equals("Take Photo")) {
                            //camera is activated
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 1);

                        } else if (options[item].equals("Choose from Gallery")) {
                            //option to select picture from all photo viewing apps on the phone appear
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 2);

                        } else if (options[item].equals("Cancel")) {
                            //dialog box is dismissed
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
                    if (fromRID == 3) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 5);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 6);//one can be replaced with any action code

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                    if (fromRID == 4) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 7);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 8);//one can be replaced with any action code

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                }
            });
            builder.show();
        }
    }

    private void nextActivity(String aadharF, String aadharB, String dl_f, String dl_b) {

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

                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                params.put("phone", mobile);
                params.put("aadhaarFront", aadharF);
                params.put("aadhaarBack", aadharB);
                params.put("licenseFront", dl_f);
                params.put("licenseBack", dl_b);
                JSONObject parameters = new JSONObject(params);
                ActivityMain a = ActivityMain.this;
                Log.d(TAG, "Values: phone=" + mobile +"\n" +" aadhaarFront=" + aadharF +"\n" + " aadhaarBack="
                        + aadharB +"\n" + "licenseFront= " + dl_f +"\n" +"licenseBack=" + dl_b);
                Log.d(TAG, "UtilityApiRequestPost.doPOST register-driver");
                UtilityApiRequestPost.doPOST(a, "register-driver", parameters, 30000, 0, a::onSuccess, a::onFailure);
            }
        });

    }

    public void convertAndUpload(int identify) {

        if (identify == 1 || identify == 2) {
            //this is true for image selected for Aadhaar Card Front
            imgAadharFront.buildDrawingCache();
            bitmap = imgAadharFront.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }
        if (identify == 3 || identify == 4) {
            //this is true for image selected for Aadhaar Card Back
            imgAadharBack.buildDrawingCache();
            bitmap = imgAadharBack.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }
        if (identify == 5 || identify == 6) {
            //this is true for image selected for Driving License Front
            imgDLFront.buildDrawingCache();
            bitmap = imgDLFront.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            dl_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }
        if (identify == 7 || identify == 8) {
            //this is true for image selected for  Driving License Back
            imgDLBack.buildDrawingCache();
            bitmap = imgDLBack.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            dl_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

        nextActivity(aadhar_f, aadhar_b, dl_f, dl_b);
    }


    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Match the request pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_OK && data != null) {
                switch (requestCode) {
                    case 1: {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharFront.setImageBitmap(selectedImage);// picture taken from camera will be displayed in the UI
                        convertAndUpload(1); // method to convert images to base64 before uploading to server
                    }
                    break;
                    case 2: {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imgAadharFront.setImageBitmap(BitmapFactory.decodeFile(picturePath));// selected picture will be displayed in the UI
                                cursor.close();
                                convertAndUpload(2);// method to convert images to base64 before uploading to server
                            }
                        }
                    }
                    break;
                    case 3: {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharBack.setImageBitmap(selectedImage);
                        convertAndUpload(3);
                    }

                    break;
                    case 4: {
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
                    case 5: {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgDLFront.setImageBitmap(selectedImage);
                        convertAndUpload(5);
                    }
                    break;
                    case 6: {
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
                                imgDLFront.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(6);
                                cursor.close();
                            }
                        }
                    }
                    break;
                    case 7: {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgDLBack.setImageBitmap(selectedImage);
                        convertAndUpload(7);
                    }
                    break;
                    case 8:
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
                                imgDLBack.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(8);
                                cursor.close();
                            }
                        }
                        break;
                }
            }
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
                    .make(scrollView, "Verification Failed: " + e.getMessage(), Snackbar.LENGTH_LONG);
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