package com.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.client.ride.ActivityRideHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "UserProfileActivity";
    private Spinner spLanguage;
    Locale myLocale;
    String currentLanguage = "en", currentLang;

    TextView mobiletxt, uploadAadhar, btnEmail;
    EditText etEmail;
    ImageButton submitEmail;
    String strEmail, stringName, stringPhone, stringAuth;
    RelativeLayout rlEmail;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String NAME_KEY = "NameKey";
    public static final String PHN_KEY = "PhnKey";
    ImageView profileImage;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};
    ImageButton btnSave;
    Bitmap bitmap;
    String pImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UtilityInitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_user_profile, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        SharedPreferences prefPLoc = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringName = prefPLoc.getString(NAME_KEY, "");
        stringPhone = prefPLoc.getString(PHN_KEY, "");
        stringAuth = prefPLoc.getString(AUTH_KEY, "");

        SwitchCompat switchCompat = findViewById(R.id.switchCompat);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            switchCompat.setChecked(true);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UtilityInitApplication.getInstance().setIsNightModeEnabled(true);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);

                } else {
                    UtilityInitApplication.getInstance().setIsNightModeEnabled(false);
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            }
        });
        mobiletxt = findViewById(R.id.mobile);
        nameText = findViewById(R.id.user_name);
        uploadAadhar = findViewById(R.id.upload_aadhar);
        btnEmail = findViewById(R.id.btn_email);
        btnSave = findViewById(R.id.save_update);
        btnEmail.setOnClickListener(this);
        etEmail = findViewById(R.id.et_email);
        submitEmail = findViewById(R.id.submit);
        rlEmail = findViewById(R.id.rl_email);
        submitEmail.setOnClickListener(this);
        profileImage = findViewById(R.id.profilePic);
        profileImage.setOnClickListener(this);

        if (stringPhone.isEmpty())
            mobiletxt.setText("");
        else {
            mobiletxt.setText(stringPhone);
            Log.d(TAG, "phone no:" + stringPhone);

        }

        if (stringName.isEmpty())
            nameText.setText("");
        else {
            nameText.setText(stringName);
            Log.d(TAG, "name:" + stringName);

        }

        currentLanguage = getIntent().getStringExtra(currentLang);
        List<String> list = new ArrayList<>();

        list.add("Select");
        list.add("English");
        list.add("Hindi");

        spLanguage = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("en");
                        break;
                    case 2:
                        setLocale("hi");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, ActivityRideHome.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(UserProfileActivity.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //method to upload aadhar card pictures to the server
    public void uploadAadhar(View view) {
        Intent upload = new Intent(UserProfileActivity.this, AadharCardUpload.class);
        startActivity(upload);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_email) {
            btnEmail.setVisibility(View.GONE);
            rlEmail.setVisibility(View.VISIBLE);

        }
        if (id == R.id.submit) {

            strEmail = etEmail.getText().toString();
            if (TextUtils.isEmpty(strEmail)) {
                etEmail.setError("This field cannot be left blank");
            } else {
                sendEmailAdd(strEmail);
            }
        }
        if (id == R.id.profilePic) {
            selectImage(UserProfileActivity.this);

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

    private void selectImage(Context context) {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {

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
            });
            builder.show();
        }
    }

    private void nextActivity(String picture) {

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Control came to nextActivity()");
                Map<String, String> params = new HashMap();
                String auth = stringAuth;
                params.put("auth", auth);
                params.put("profilePhoto", picture);
                JSONObject parameters = new JSONObject(params);
                UserProfileActivity a = UserProfileActivity.this;
                Log.d(TAG, "Values: profilePhoto=" + picture + " auth=" + auth);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-profile-photo-save");
                UtilityApiRequestPost.doPOST(a, "auth-profile-photo-save", parameters, 30000, 0, response -> {
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

            profileImage.buildDrawingCache();
            bitmap = profileImage.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            pImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "Profile Image converted to Base64" + pImage);
            Log.d(TAG, "Control moved to nextActivity()");
        }

        nextActivity(pImage);
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
                        profileImage.setImageBitmap(selectedImage);
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
                                profileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }
                    break;
            }
        }

    }

    private void sendEmailAdd(String email) {
        String auth = stringAuth;
        Map<String, String> params = new HashMap();
        params.put("auth", auth);
        params.put("email", email);

        JSONObject parameters = new JSONObject(params);
        UserProfileActivity a = UserProfileActivity.this;
        Log.d(TAG, "Values: auth=" + auth + " email=" + email);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-profile-update");
        UtilityApiRequestPost.doPOST(a, "auth-profile-update", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG, "RESPONSE:" + response);
        rlEmail.setVisibility(View.GONE);
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
}
