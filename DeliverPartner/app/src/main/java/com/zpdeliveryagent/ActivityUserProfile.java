package com.zpdeliveryagent;

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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivityUserProfile extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ActivityUserProfile.class.getName();
    private Spinner spLanguage;
    Locale myLocale;
    String currentLanguage = "en", currentLang;
    NavigationView nv;
    TextView mobiletxt, nameTxt;
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String NAME = "Name";
    public static final String MOBILE = "Mobile";

    private ImageView imgProfilePic;
    private TextView upload;
    String profilePic;
    Bitmap bitmap;
    ProgressBar simpleProgressBar;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};
    String stringAuth, stringName, stringMobile;

    public void onSuccess(JSONObject response) {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        //response on hitting auth-profile-photo-save API
        simpleProgressBar.setVisibility(View.GONE);
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        //simpleProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UtilityInitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_user_profile);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth
        stringName = cookie.getString(NAME, ""); // retrieve auth value stored locally and assign it to String auth
        stringMobile = cookie.getString(MOBILE, ""); // retrieve auth value stored locally and assign it to String auth

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

        nv = findViewById(R.id.nv);
        spLanguage = findViewById(R.id.spinner);
        upload = findViewById(R.id.upload_file_txt);
        imgProfilePic = findViewById(R.id.profile_picture);
        imgProfilePic.setOnClickListener(this);
        mobiletxt = findViewById(R.id.mobile);
        nameTxt = findViewById(R.id.user_name);

        if (stringMobile.isEmpty())
            mobiletxt.setText("");
        else {
            mobiletxt.setText(stringMobile);
            Log.d(TAG, "phone no:" + stringMobile);
        }
        if (stringName.isEmpty())
            nameTxt.setText("");
        else {
            nameTxt.setText(stringName);
            Log.d(TAG, "name:" + stringName);
        }


        currentLanguage = getIntent().getStringExtra(currentLang);
        List<String> list = new ArrayList<>();

        list.add("Select");
        list.add("English");
        list.add("Hindi");
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



        String imageURL = "https://api.zippe.in:8090/media/dp_" + stringAuth + "_.jpg";

        try {
            Glide.with(this).load(imageURL).into(imgProfilePic);
        } catch (Exception e) {
            Log.d(TAG, "imageURL=" + imageURL);
            Log.d(TAG, "Display Picture Error:" + e.toString());
            e.printStackTrace();
        }
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, ActivityHome.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(ActivityUserProfile.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_picture) {
            selectImage(ActivityUserProfile.this);
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

    private void nextActivity(String profilePic) {

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleProgressBar = findViewById(R.id.simpleProgressBar);

                Log.d(TAG, "Control came to nextActivity()");
                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                String auth = stringAuth;
                params.put("auth", auth);
                params.put("profilePhoto", profilePic);
                JSONObject parameters = new JSONObject(params);
                ActivityUserProfile a = ActivityUserProfile.this;
                Log.d(TAG, "Values: profilePhoto=" + profilePic + " auth=" + auth);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-profile-photo-save");
                UtilityApiRequestPost.doPOST(a, "auth-profile-photo-save", parameters, 30000, 0, a::onSuccess, a::onFailure);
            }
        });
    }

    public void convertAndUpload() {
        imgProfilePic.buildDrawingCache();
        bitmap = imgProfilePic.getDrawingCache();
        //converting image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        profilePic = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d(TAG, "CONVERT profile picture to Base64" + profilePic);
        Log.d(TAG, "Control moved to nextActivity()");

        nextActivity(profilePic);
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
                        imgProfilePic.setImageBitmap(selectedImage);
                        convertAndUpload();
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
                                imgProfilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload();
                            }
                        }
                    }
                    break;
            }
        }

    }
}
