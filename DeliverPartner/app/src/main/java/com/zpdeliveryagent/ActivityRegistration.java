package com.zpdeliveryagent;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityRegistration extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityRegistration";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String MOBILE = "Mobile";
    public static final String NAME = "Name";
    public static final String AADHAR = "Aadhar";

    EditText etName, etMobile, etOTP;
    Button btnGdr;
    ImageView ivProfile, ivDLF, ivDLB;
    ImageButton login;
    ProgressBar progressBar;
    RelativeLayout rlProfile, rlDLF, rlDLB;
    ScrollView scrollView;
    TextView btnVerifyPhone;
    Dialog myDialog;
    String dl_f, dl_b, profilePic, mobile, name, gdr;
    Bitmap bitmap;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};
    CheckBox tnc;
    TextView text_tnc;

    //firebase auth object
    private FirebaseAuth mAuth;
    String newToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        etName = findViewById(R.id.editTextName);
        etMobile = findViewById(R.id.editTextMobile);
        etOTP = findViewById(R.id.editTextOTP);
        btnGdr = findViewById(R.id.btnGender);
        ivProfile = findViewById(R.id.profile_picture);
        ivDLF = findViewById(R.id.aadharFrontImg);
        ivDLB = findViewById(R.id.aadharBackImg);
        progressBar = findViewById(R.id.simpleProgressBar);
        rlProfile = findViewById(R.id.containerProfilePic);
        rlDLF = findViewById(R.id.containerAadhar);
        rlDLB = findViewById(R.id.containerAadharBack);
        scrollView = findViewById(R.id.scrollLayout);
        login = findViewById(R.id.login);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);

        btnGdr.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        ivDLF.setOnClickListener(this);
        ivDLB.setOnClickListener(this);
        btnVerifyPhone.setOnClickListener(this);

        myDialog = new Dialog(this);

        FirebaseAuth.getInstance(); //done to perform a variety of authentication-related operations
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
        } else if (id == R.id.btnGender) {
            ShowPopup();
        } else if (id == R.id.profile_picture) {

            selectImage(ActivityRegistration.this, 3);
            Log.d(TAG, "profile_picture clicked");
        } else if (id == R.id.aadharFrontImg) {
            selectImage(ActivityRegistration.this, 1);
        } else if (id == R.id.aadharBackImg) {
            selectImage(ActivityRegistration.this, 2);
        } else if (id == R.id.pop_txt1) {
            myDialog.dismiss();
            btnGdr.setText(R.string.female);
            gdr = "f";
        } else if (id == R.id.pop_txt2) {
            myDialog.dismiss();
            btnGdr.setText(R.string.male);
            gdr = "m";
        } else if (id == R.id.pop_txt3) {
            myDialog.dismiss();
            btnGdr.setText(R.string.non_binary);
            gdr = "o";
        } else if (id == R.id.buttonVerifyPhoneNo) {
            verifyPhone();
        }
    }

    private void ShowPopup() {
        myDialog.setContentView(R.layout.popup);
        TextView txtTitle = (TextView) myDialog.findViewById(R.id.pop_title);
        TextView txt1 = (TextView) myDialog.findViewById(R.id.pop_txt1);
        TextView txt2 = (TextView) myDialog.findViewById(R.id.pop_txt2);
        TextView txt3 = (TextView) myDialog.findViewById(R.id.pop_txt3);

        txtTitle.setText(R.string.gender);
        txt1.setText(R.string.female);
        txt2.setText(R.string.male);
        txt3.setText(R.string.non_binary);
        txt1.setOnClickListener(this);
        txt2.setOnClickListener(this);
        txt3.setOnClickListener(this);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(false);
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
            Log.d(TAG, "" + e.getMessage());
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

        final CharSequence[] options = {/*"Take Photo", */"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (fromRID == 1) {
                   /* if (options[item].equals("Take Photo")) {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 1);

                    } else */if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 2);

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
                if (fromRID == 2) {
                    /*if (options[item].equals("Take Photo")) {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 3);

                    } else*/ if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 4);//one can be replaced with any action code

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
                if (fromRID == 3) {
                    Log.d(TAG,"fromRID  3");
                    /*if (options[item].equals("Take Photo")) {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 5);

                    } else*/ if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 6);//one can be replaced with any action code

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            }
        });
        builder.show();

    }

    private void nextActivity(String dlF, String dlB, String profileP) {
        name = etName.getText().toString();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tnc.isChecked()) {
                    Toast.makeText(ActivityRegistration.this, R.string.agree_to_terms, Toast.LENGTH_SHORT).show();
                } else {
                    progressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

                    Log.d(TAG, "Control came to nextActivity()");
                    progressBar.setVisibility(View.VISIBLE);
                    Map<String, String> params = new HashMap();

                    params.put("name", name);
                    params.put("phone", mobile);
                    params.put("photo", profileP);
                    params.put("gdr", gdr);
                    params.put("licenseFront", dlF);
                    params.put("licenseBack", dlB);
                    params.put("fcm", newToken);
                    JSONObject parameters = new JSONObject(params);
                    ActivityRegistration a = ActivityRegistration.this;
                    Log.d(TAG, "Values: licenseFront=" + dlF + " licenseBack=" + dlB +
                            " name=" + name + " pn=" + mobile + " photo=" + profileP + " gdr=" + gdr+ " fcm=" + newToken);
                    Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME register-agent");
                    UtilityApiRequestPost.doPOST(a, "register-agent", parameters, 30000, 0, response -> {
                        try {
                            a.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, a::onFailure);
                }
            }
        });
    }

    public void convertAndUpload(int identify) {

        if (identify == 1 || identify == 2) {

            ivDLF.buildDrawingCache();
            bitmap = ivDLF.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            dl_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "dl front converted to Base64" + dl_f);
            Log.d(TAG, "Control moved to nextActivity()");
        }
        if (identify == 3 || identify == 4) {
            ivDLB.buildDrawingCache();
            bitmap = ivDLB.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            dl_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "dl back converted to Base64");
            Log.d(TAG, "Control moved to nextActivity()");
        }
        if (identify == 5 || identify == 6) {
            ivProfile.buildDrawingCache();
            bitmap = ivProfile.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            profilePic = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "profile picture converted to Base64");
            Log.d(TAG, "request code 5");

            Log.d(TAG, "Control moved to nextActivity()");
        }
        nextActivity(dl_f, dl_b, profilePic);
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                /*case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ivDLF.setImageBitmap(selectedImage);
                        convertAndUpload(1);
                    }
                    break;*/
                case 2:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            ivDLF.setImageBitmap(selectedImage);
                            convertAndUpload(2);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityRegistration.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(ActivityRegistration.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
                    }
                    /*if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                ivDLF.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }*/
                    break;
               /* case 3:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ivDLB.setImageBitmap(selectedImage);
                        convertAndUpload(3);
                    }

                    break;*/
                case 4:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            ivDLB.setImageBitmap(selectedImage);
                            convertAndUpload(4);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityRegistration.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(ActivityRegistration.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
                    }
                    /*if (resultCode == RESULT_OK && data != null) {
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
                                ivDLB.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(4);
                                cursor.close();
                            }
                        }
                    }*/
                    break;
               /* case 5:
                    if (resultCode == RESULT_OK && data != null) {
                        final Uri uri = data.getData();
                        useImage(uri);

                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ivProfile.setImageBitmap(selectedImage);
                        convertAndUpload(5);
                    }
                    break;*/
                case 6:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            ivProfile.setImageBitmap(selectedImage);
                            convertAndUpload(6);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityRegistration.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(ActivityRegistration.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
                    }
                    /*if (resultCode == RESULT_OK && data != null) {

                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                ivProfile.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(6);
                            }
                        }
                    }*/
                    break;
            }
        }

    }
    void useImage(Uri uri)
    {
        Log.d(TAG, "inside useImage(Uri uri)");
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //use the bitmap as you like
        ivProfile.setImageBitmap(bitmap);
    }
    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        //response on hitting register-agent API
        String an = response.getString("an");
        String pn = response.getString("pn");
        String name = response.getString("name");

        SharedPreferences sp_jStatus = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        sp_jStatus.edit().putString(AADHAR, an).apply();
        sp_jStatus.edit().putString(NAME, name).apply();
        sp_jStatus.edit().putString(MOBILE, pn).apply();

        Intent home = new Intent(ActivityRegistration.this, ActivityLogin.class);
        startActivity(home);
        finish();
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }
}