 package com.e.serviceproviderapp;

import android.app.Dialog;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.halcyon.squareprogressbar.SquareProgressBar;


 public class ActivityUserProfile extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = ActivityUserProfile.class.getName();
    private Spinner spLanguage;
    Locale myLocale;
    String currentLanguage = "en", currentLang;
    NavigationView nv;
    TextView mobileTxt, nameTxt;
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String MOBILE = "Mobile";
    public static final String NAME = "Name";

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
    String strAuth, strName, strMobile;
    Dialog imageDialog, myDialog2;
    static int count = 0;
    ArrayList<String> arrayList = new ArrayList<String>();
    TextView jobTxt1, jobTxt2, jobTxt3;
    Button btnCancel, btnSave;
    String job1, job2, job3;
    Map<String, String> params = new HashMap();
    ActivityUserProfile a = ActivityUserProfile.this;
    public void onSuccess(JSONObject response, int id) {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        //response on hitting auth-profile-photo-save API
        if (id == 1) {
            Log.d(TAG, "RESPONSE of auth-location-update :" + response);
            simpleProgressBar.setVisibility(View.GONE);

        }
        //response on hitting servitor-jobs-list API
        if (id == 2) {
            Log.d(TAG, "RESPONSE of servitor-jobs-list :" + response);
            try {
                String job1 = response.getString("job1");
                String job2 = response.getString("job2");
                String job3 = response.getString("job3");

                jobTxt1.setText(job1);
                jobTxt2.setText(job2);
                jobTxt3.setText(job3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //response on hitting auth-profile-update API
        if (id == 3) {
            Log.d(TAG, "RESPONSE of auth-profile-update :" + response);
        }
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
        mobileTxt = findViewById(R.id.mobile);
        nameTxt = findViewById(R.id.user_name);
        jobTxt1 = findViewById(R.id.txt_job_1);
        jobTxt2 = findViewById(R.id.txt_job_2);
        jobTxt3 = findViewById(R.id.txt_job_3);
        jobTxt1.setOnClickListener(this);
        jobTxt2.setOnClickListener(this);
        jobTxt3.setOnClickListener(this);
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to strAuth
        strName = cookie.getString(NAME, ""); // retrieve name stored locally and assign it to strName
        strMobile = cookie.getString(MOBILE, ""); // retrieve mobile stored locally and assign it to strMobile

        Log.d("AUTH", "Auth Key from server: " + strAuth);
        Log.d("NAME", "Name Key from server: " + strName);
        Log.d("MOBILE", "Mobile Key from server: " + strMobile);
        if (strMobile.isEmpty())
            mobileTxt.setText("XXXXXXXXXX");
        else {
            mobileTxt.setText(strMobile);
        }
        if (strName.isEmpty())
            nameTxt.setText("NAME");
        else {
            nameTxt.setText(strName);
        }

        currentLanguage = getIntent().getStringExtra(currentLang);
        List<String> list = new ArrayList<>();

        list.add("Select");
        list.add("English");
        list.add("Hindi");

        nv = findViewById(R.id.nv);

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

        upload = findViewById(R.id.upload_file_txt);
        upload.setOnClickListener(this);
        imgProfilePic = findViewById(R.id.profile_picture);
        imgProfilePic.setOnClickListener(this);

        imageDialog = new Dialog(this);
        myDialog2 = new Dialog(this);
        getData();
        String imageURL = "https://api.villageapps.in:8090/media/dp_" + strAuth + "_.jpg";
        try {
            Glide.with(this).load(imageURL).into(imgProfilePic);
        } catch (Exception e) {
            Log.d(TAG, "imageURL=" + imageURL);
            Log.d(TAG, "Display Picture Error:" + e.toString());
            e.printStackTrace();
        }
        SquareProgressBar squareProgressBar = findViewById(R.id.sprogressbar);
        squareProgressBar.setImage(R.drawable.btn_bkg);
        squareProgressBar.setProgress(50.0);
        squareProgressBar.setWidth(10);
        //squareProgressBar.setHoloColor(android.R.color.holo_purple);
        squareProgressBar.setIndeterminate(true);
        squareProgressBar.setColor("#D7FB05");

    }

    private void getData() {

        params.put("auth", strAuth);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + strAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST servitor-jobs-list");
        UtilityApiRequestPost.doPOST(a, "servitor-jobs-list", parameters, 30000, 0, response -> {
            a.onSuccess(response, 2);
        }, a::onFailure);
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
        int id = v.getId();
        if (id == R.id.profile_picture) {
            selectImage(ActivityUserProfile.this);
        } else if (id == R.id.upload_file_txt) {
            updateJobs();
        } else if (id == R.id.txt_job_1) {
            ShowPopup2();
            count = 0;
            arrayList.clear();
        } else if (id == R.id.txt_job_2) {
            ShowPopup2();
            count = 0;
            arrayList.clear();
        } else if (id == R.id.txt_job_3) {
            ShowPopup2();
            count = 0;
            arrayList.clear();
        } else if (id == R.id.cancel_selection) {
            count = 0;
            myDialog2.dismiss();
        } else if (id == R.id.save_selection) {
            if (count != 0) {
                myDialog2.dismiss();
                for (int j = 0; j <= 3; j++) {
                    try {
                        jobTxt1.setText(arrayList.get(0));
                        jobTxt2.setText(arrayList.get(1));
                        jobTxt3.setText(arrayList.get(2));
                        job1 = arrayList.get(0);
                        job2 = arrayList.get(1);
                        job3 = arrayList.get(2);
                    } catch (IndexOutOfBoundsException e) {
                        Log.d(TAG, "error" + e.getMessage());
                    }
                }
                Log.d(TAG, "list" + arrayList.toString());

            } else {
                Toast.makeText(this, "Please select a job", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateJobs() {
        String j1 = jobTxt1.getText().toString();
        String j2 = jobTxt2.getText().toString();
        String j3 = jobTxt3.getText().toString();
        params.put("auth", strAuth);
        params.put("job1", j1);
        params.put("job2", j2);
        params.put("job3", j3);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth = " + strAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-profile-update");
        UtilityApiRequestPost.doPOST(a, "auth-profile-update", parameters, 30000, 0, response -> {
            a.onSuccess(response, 3);
        }, a::onFailure);
    }

    private void ShowPopup2() {
        myDialog2.setContentView(R.layout.popup2);
        TextView txtTitle = (TextView) myDialog2.findViewById(R.id.pop2_title);
        CheckBox txt1 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt1);
        CheckBox txt2 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt2);
        CheckBox txt3 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt3);
        CheckBox txt4 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt4);
        CheckBox txt5 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt5);
        CheckBox txt6 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt6);
        CheckBox txt7 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt7);
        CheckBox txt8 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt8);
        CheckBox txt9 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt9);
        CheckBox txt10 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt10);
        CheckBox txt11 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt11);
        CheckBox txt12 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt12);
        CheckBox txt13 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt13);
        CheckBox txt14 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt14);
        CheckBox txt15 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt15);
        CheckBox txt16 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt16);
        CheckBox txt17 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt17);
        CheckBox txt18 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt18);
        CheckBox txt19 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt19);
        CheckBox txt20 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt20);
        CheckBox txt21 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt21);
        CheckBox txt22 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt22);
        CheckBox txt23 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt23);
        CheckBox txt24 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt24);
        CheckBox txt25 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt25);
        CheckBox txt26 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt26);
        CheckBox txt27 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt27);
        CheckBox txt28 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt28);
        CheckBox txt29 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt29);
        CheckBox txt30 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt30);
        CheckBox txt31 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt31);
        CheckBox txt32 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt32);
        CheckBox txt33 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt33);
        CheckBox txt34 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt34);
        CheckBox txt35 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt35);
        CheckBox txt36 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt36);
        CheckBox txt37 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt37);
        CheckBox txt38 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt38);
        CheckBox txt39 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt39);
        CheckBox txt40 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt40);
        CheckBox txt41 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt41);
        CheckBox txt42 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt42);
        CheckBox txt43 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt43);
        CheckBox txt44 = (CheckBox) myDialog2.findViewById(R.id.pop2_txt44);

        btnCancel = (Button) myDialog2.findViewById(R.id.cancel_selection);
        btnSave = (Button) myDialog2.findViewById(R.id.save_selection);

        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        txtTitle.setText(R.string.jobs_skills);

        txt1.setText(R.string.j_accountant);
        txt2.setText(R.string.j_aluminiumFabricator);
        txt3.setText(R.string.j_applianceRepair);
        txt4.setText(R.string.j_architect);
        txt5.setText(R.string.j_carpenter);
        txt6.setText(R.string.j_caterer);
        txt7.setText(R.string.j_charteredAccountant);
        txt8.setText(R.string.j_chef);
        txt9.setText(R.string.j_civilEngineer);
        txt10.setText(R.string.j_coach);
        txt11.setText(R.string.j_computerRepairTechnician);
        txt12.setText(R.string.j_cook);
        txt13.setText(R.string.j_deepCleaning);
        txt14.setText(R.string.j_delivery);
        txt15.setText(R.string.j_designer);
        txt16.setText(R.string.j_dietician);
        txt17.setText(R.string.j_doctor);
        txt18.setText(R.string.j_driver);
        txt19.setText(R.string.j_dryCleaning);
        txt20.setText(R.string.j_electrician);
        txt21.setText(R.string.j_errandPerson);
        txt22.setText(R.string.j_fitnessTrainer);
        txt23.setText(R.string.j_gardener);
        txt24.setText(R.string.j_homeTutor);
        txt25.setText(R.string.j_houseKeeping);
        txt26.setText(R.string.j_inspectionVisit);
        txt27.setText(R.string.j_ironfabricator);
        txt28.setText(R.string.j_laundry);
        txt29.setText(R.string.j_lawyer);
        txt30.setText(R.string.j_maid);
        txt31.setText(R.string.j_mason);
        txt32.setText(R.string.j_masonHelper);
        txt33.setText(R.string.j_mechanic);
        txt34.setText(R.string.j_motorTraining);
        txt35.setText(R.string.j_nurse);
        txt36.setText(R.string.j_painter);
        txt37.setText(R.string.j_plumber);
        txt38.setText(R.string.j_securityGuard);
        txt39.setText(R.string.j_surveyor);
        txt40.setText(R.string.j_sweeper);
        txt41.setText(R.string.j_tailor);
        txt42.setText(R.string.j_towingService);
        txt43.setText(R.string.j_yogaTrainer);
        txt44.setText(R.string.j_yogaTrainer);

        txt1.setOnCheckedChangeListener(this);
        txt2.setOnCheckedChangeListener(this);
        txt3.setOnCheckedChangeListener(this);
        txt4.setOnCheckedChangeListener(this);
        txt5.setOnCheckedChangeListener(this);
        txt6.setOnCheckedChangeListener(this);
        txt7.setOnCheckedChangeListener(this);
        txt8.setOnCheckedChangeListener(this);
        txt9.setOnCheckedChangeListener(this);
        txt10.setOnCheckedChangeListener(this);
        txt11.setOnCheckedChangeListener(this);
        txt12.setOnCheckedChangeListener(this);
        txt13.setOnCheckedChangeListener(this);
        txt14.setOnCheckedChangeListener(this);
        txt15.setOnCheckedChangeListener(this);
        txt16.setOnCheckedChangeListener(this);
        txt17.setOnCheckedChangeListener(this);
        txt18.setOnCheckedChangeListener(this);
        txt19.setOnCheckedChangeListener(this);
        txt20.setOnCheckedChangeListener(this);
        txt21.setOnCheckedChangeListener(this);
        txt22.setOnCheckedChangeListener(this);
        txt23.setOnCheckedChangeListener(this);
        txt24.setOnCheckedChangeListener(this);
        txt25.setOnCheckedChangeListener(this);
        txt26.setOnCheckedChangeListener(this);
        txt27.setOnCheckedChangeListener(this);
        txt28.setOnCheckedChangeListener(this);
        txt29.setOnCheckedChangeListener(this);
        txt30.setOnCheckedChangeListener(this);
        txt31.setOnCheckedChangeListener(this);
        txt32.setOnCheckedChangeListener(this);
        txt33.setOnCheckedChangeListener(this);
        txt34.setOnCheckedChangeListener(this);
        txt35.setOnCheckedChangeListener(this);
        txt36.setOnCheckedChangeListener(this);
        txt37.setOnCheckedChangeListener(this);
        txt38.setOnCheckedChangeListener(this);
        txt39.setOnCheckedChangeListener(this);
        txt40.setOnCheckedChangeListener(this);
        txt41.setOnCheckedChangeListener(this);
        txt42.setOnCheckedChangeListener(this);
        txt43.setOnCheckedChangeListener(this);
        txt44.setOnCheckedChangeListener(this);

        /*btnJ2.setText(R.string.j_carpenter);
        btnJ1.setText(R.string.j_electrician);
        btnJ3.setText(R.string.j_plumber);*/
        myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog2.getWindow().getAttributes();

        wmlp.y = 80;   //y position
        myDialog2.show();
        Window window = myDialog2.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog2.setCanceledOnTouchOutside(false);
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
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 1);

                    } else if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                String auth = strAuth;
                params.put("auth", auth);
                params.put("profilePhoto", profilePic);
                JSONObject parameters = new JSONObject(params);
                ActivityUserProfile a = ActivityUserProfile.this;
                Log.d(TAG, "Values: profilePhoto=" + profilePic + " auth=" + auth);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-profile-photo-save");
                UtilityApiRequestPost.doPOST(a, "auth-profile-photo-save", parameters, 20000, 0, response -> {
                    try {
                        a.onSuccess(response, 1);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                }, a::onFailure);
            }
        });
    }

    public void convertAndUpload(int identify) {

        if (identify == 1 || identify == 2) {

            imgProfilePic.buildDrawingCache();
            bitmap = imgProfilePic.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            profilePic = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "CONVERT aadhar front converted to Base64" + profilePic);
            Log.d(TAG, "Control moved to nextActivity()");
        }
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
                                imgProfilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityUserProfile.this, ActivityHome.class));
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Log.d(TAG, "list" + arrayList.toString());
        if (count < 3) {
            if (id == R.id.pop2_txt1) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Accountant");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Accountant");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt2) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Aluminium Fabricator");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Aluminium Fabricator");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt3) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Appliance Repair");

                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Appliance Repair");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt4) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Architect");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Architect");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt5) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Carpenter");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Carpenter");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt6) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Caterer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Caterer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt7) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Chartered Accountant");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Chartered Accountant");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt8) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Chef");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Chef");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt9) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Civil Engineer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Civil Engineer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt10) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Coach");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Coach");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt11) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Computer Repair Technician");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Computer Repair Technician");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt12) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Cook");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Cook");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt13) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Deep Cleaning");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Deep Cleaning");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt14) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Delivery");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Delivery");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt15) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Designer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Designer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt16) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Dietician");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Dietician");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt17) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Doctor");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Doctor");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt18) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Driver");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Driver");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt19) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Dry Cleaning");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Dry Cleaning");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt20) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Electrician");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Electrician");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt21) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Errand Person");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Errand Person");
                }
                Log.d(TAG, "list" + arrayList.toString());
            }
            if (id == R.id.pop2_txt22) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Fitness Trainer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Fitness Trainer");

                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt23) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Gardener");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Gardener");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt24) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("House Keeping");

                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("House Keeping");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt25) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Inspection Visit");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Inspection Visit");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt26) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Iron Fabricator");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Iron Fabricator");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt27) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Laundry");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Laundry");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt28) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Lawyer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Lawyer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt29) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Maid");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Maid");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt30) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Mason");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Mason");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt31) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Masons Helper");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Masons Helper");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt32) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Mechanic");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Mechanic");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt33) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Motor Training");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Motor Training");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt34) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Nurse");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Nurse");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt35) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Painter");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Painter");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt36) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Photographer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Photographer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt37) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Plumber");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Plumber");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt38) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Security Guard");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Security Guard");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt39) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Surveyor");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Surveyor");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt40) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Sweeper");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Sweeper");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt41) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Tailor");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Tailor");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt42) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Teacher / Home Tutor");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Teacher / Home Tutor");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt43) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Towing Service");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Towing Service");
                }
                Log.d(TAG, "list" + arrayList.toString());
            } else if (id == R.id.pop2_txt44) {
                if (isChecked) {
                    Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
                    count++;
                    arrayList.add("Yoga Trainer");
                } else {
                    Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
                    count--;
                    arrayList.remove("Yoga Trainer");
                }
                Log.d(TAG, "list" + arrayList.toString());
            }
        } else {
            myDialog2.dismiss();
        }
    }
}

