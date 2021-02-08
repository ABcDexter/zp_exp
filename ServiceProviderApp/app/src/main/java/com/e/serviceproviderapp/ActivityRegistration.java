package com.e.serviceproviderapp;

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
import android.widget.CompoundButton;
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
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityRegistration extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ActivityRegistration";

    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";
    public static final String MOBILE = "Mobile";
    public static final String NAME = "Aadhar";

    EditText etName, etMobile, etOTP;
    Button btnGdr, btnPS, btnJ1, btnJ2, btnJ3, btnCancel, btnSave;
    ImageView ivProfile, ivAdharF, ivAdharB;
    ImageButton login;
    ProgressBar progressBar;
    RelativeLayout rlProfile, rlAdhF, rlAdhB;
    ScrollView scrollView;
    TextView btnVerifyPhone;
    Dialog myDialog, myDialog1, myDialog2;
    String aadhar_f, aadhar_b, profilePic, mobile, name, gdr, ps, job1, job2, job3;
    Bitmap bitmap;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};
    static int count = 0;
    ArrayList<String> arrayList = new ArrayList<String>();
    CheckBox tnc;
    TextView text_tnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        etName = findViewById(R.id.editTextName);
        etMobile = findViewById(R.id.editTextMobile);
        etOTP = findViewById(R.id.editTextOTP);
        btnGdr = findViewById(R.id.btnGender);
        btnPS = findViewById(R.id.btnPS);
        btnJ1 = findViewById(R.id.list_job1);
        btnJ2 = findViewById(R.id.list_job2);
        btnJ3 = findViewById(R.id.list_job3);
        ivProfile = findViewById(R.id.profile_picture);
        ivAdharF = findViewById(R.id.aadharFrontImg);
        ivAdharB = findViewById(R.id.aadharBackImg);
        progressBar = findViewById(R.id.simpleProgressBar);
        rlProfile = findViewById(R.id.containerProfilePic);
        rlAdhF = findViewById(R.id.containerAadhar);
        rlAdhB = findViewById(R.id.containerAadharBack);
        scrollView = findViewById(R.id.scrollLayout);
        login = findViewById(R.id.login);
        btnVerifyPhone = findViewById(R.id.buttonVerifyPhoneNo);

        btnGdr.setOnClickListener(this);
        btnPS.setOnClickListener(this);
        btnJ1.setOnClickListener(this);
        btnJ2.setOnClickListener(this);
        btnJ3.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        ivAdharF.setOnClickListener(this);
        ivAdharB.setOnClickListener(this);
        btnVerifyPhone.setOnClickListener(this);

        myDialog = new Dialog(this);
        myDialog1 = new Dialog(this);
        myDialog2 = new Dialog(this);

        FirebaseAuth.getInstance(); //done to perform a variety of authentication-related operations
        tnc = findViewById(R.id.tnc);
        text_tnc = findViewById(R.id.txt_tnc);
        text_tnc.setOnClickListener(this);
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
        } else if (id == R.id.btnPS) {
            ShowPopup1();
        } else if (id == R.id.list_job1) {
            //showDialog();
            ShowPopup2();
            count = 0;
            arrayList.clear();
        } else if (id == R.id.list_job2) {
            ShowPopup2();
            count = 0;
            arrayList.clear();
        } else if (id == R.id.list_job3) {
            ShowPopup2();
            count = 0;
            arrayList.clear();
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
        } else if (id == R.id.pop1_txt1) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_banbhulpura);
            ps = "banbhulpura";
        } else if (id == R.id.pop1_txt2) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_betalghat);
            ps = "betalghat";
        } else if (id == R.id.pop1_txt3) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_bhimtal);
            ps = "bhimtal";
        } else if (id == R.id.pop1_txt4) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_bhowali);
            ps = "bhowali";
        } else if (id == R.id.pop1_txt5) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_chorgalia);
            ps = "chorgalia";
        } else if (id == R.id.pop1_txt6) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_haldwani);
            ps = "haldwani";
        } else if (id == R.id.pop1_txt7) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_kaladhungi);
            ps = "kaladhungi";
        } else if (id == R.id.pop1_txt8) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_kathgodam);
            ps = "kathgodam";
        } else if (id == R.id.pop1_txt9) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_lalkuwan);
            ps = "lalkuwan";
        } else if (id == R.id.pop1_txt10) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_mallital);
            ps = "mallital";
        } else if (id == R.id.pop1_txt11) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_mukhani);
            ps = "mukhani";
        } else if (id == R.id.pop1_txt12) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_mukteshwar);
            ps = "mukteshwar";
        } else if (id == R.id.pop1_txt13) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_ramnagar);
            ps = "ramnagar";
        } else if (id == R.id.pop1_txt14) {
            myDialog1.dismiss();
            btnPS.setText(R.string.ps_tallital);
            ps = "tallital";
        } else if (id == R.id.buttonVerifyPhoneNo) {
            verifyPhone();
        } else if (id == R.id.cancel_selection) {
            count = 0;
            myDialog2.dismiss();
        } else if (id == R.id.save_selection) {
            if (count != 0) {
                myDialog2.dismiss();
                for (int j = 0; j <= 3; j++) {
                    try {
                        btnJ1.setText(arrayList.get(0));
                        btnJ2.setText(arrayList.get(1));
                        btnJ3.setText(arrayList.get(2));
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

    private void ShowPopup1() {
        myDialog1.setContentView(R.layout.popup1);
        TextView txtTitle = (TextView) myDialog1.findViewById(R.id.pop1_title);
        TextView txt1 = (TextView) myDialog1.findViewById(R.id.pop1_txt1);
        TextView txt2 = (TextView) myDialog1.findViewById(R.id.pop1_txt2);
        TextView txt3 = (TextView) myDialog1.findViewById(R.id.pop1_txt3);
        TextView txt4 = (TextView) myDialog1.findViewById(R.id.pop1_txt4);
        TextView txt5 = (TextView) myDialog1.findViewById(R.id.pop1_txt5);
        TextView txt6 = (TextView) myDialog1.findViewById(R.id.pop1_txt6);
        TextView txt7 = (TextView) myDialog1.findViewById(R.id.pop1_txt7);
        TextView txt8 = (TextView) myDialog1.findViewById(R.id.pop1_txt8);
        TextView txt9 = (TextView) myDialog1.findViewById(R.id.pop1_txt9);
        TextView txt10 = (TextView) myDialog1.findViewById(R.id.pop1_txt10);
        TextView txt11 = (TextView) myDialog1.findViewById(R.id.pop1_txt11);
        TextView txt12 = (TextView) myDialog1.findViewById(R.id.pop1_txt12);
        TextView txt13 = (TextView) myDialog1.findViewById(R.id.pop1_txt13);
        TextView txt14 = (TextView) myDialog1.findViewById(R.id.pop1_txt14);

        txtTitle.setText(R.string.nearest_police_station);
        txt1.setText(R.string.ps_banbhulpura);
        txt2.setText(R.string.ps_betalghat);
        txt3.setText(R.string.ps_bhimtal);
        txt4.setText(R.string.ps_bhowali);
        txt5.setText(R.string.ps_chorgalia);
        txt6.setText(R.string.ps_haldwani);
        txt7.setText(R.string.ps_kaladhungi);
        txt8.setText(R.string.ps_kathgodam);
        txt9.setText(R.string.ps_lalkuwan);
        txt10.setText(R.string.ps_mallital);
        txt11.setText(R.string.ps_mukhani);
        txt12.setText(R.string.ps_mukteshwar);
        txt13.setText(R.string.ps_ramnagar);
        txt14.setText(R.string.ps_tallital);

        txt1.setOnClickListener(this);
        txt2.setOnClickListener(this);
        txt3.setOnClickListener(this);
        txt4.setOnClickListener(this);
        txt5.setOnClickListener(this);
        txt6.setOnClickListener(this);
        txt7.setOnClickListener(this);
        txt8.setOnClickListener(this);
        txt9.setOnClickListener(this);
        txt10.setOnClickListener(this);
        txt11.setOnClickListener(this);
        txt12.setOnClickListener(this);
        txt13.setOnClickListener(this);
        txt14.setOnClickListener(this);

        myDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog1.getWindow().getAttributes();

        wmlp.y = 80;   //y position
        myDialog1.show();
        Window window = myDialog1.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog1.setCanceledOnTouchOutside(false);
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
            }
        });
        builder.show();

    }

    private void nextActivity(String aadharF, String aadharB, String profileP) {
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
                    params.put("pn", mobile);
                    params.put("photo", profileP);
                    params.put("gdr", gdr);
                    params.put("ps", ps);
                    params.put("job1", job1);
                    params.put("job2", job2);
                    params.put("job3", job3);
                    params.put("aadhaarFront", aadharF);
                    params.put("aadhaarBack", aadharB);
                    JSONObject parameters = new JSONObject(params);
                    ActivityRegistration a = ActivityRegistration.this;
                    Log.d(TAG, "Values: aadhaarFront=" + aadharF + " aadhaarBack=" + aadharB +
                            " name=" + name + " pn=" + mobile + " photo=" + profileP + " gdr=" + gdr + " ps=" + ps
                            + " job1=" + job1 + " job2=" + job2 + " job3=" + job3);
                    Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME registor-servitor");
                    UtilityApiRequestPost.doPOST(a, "registor-servitor", parameters, 30000, 0, response -> {
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

            ivAdharF.buildDrawingCache();
            bitmap = ivAdharF.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "aadhar front converted to Base64" + aadhar_f);
            Log.d(TAG, "Control moved to nextActivity()");
        }
        if (identify == 3 || identify == 4) {
            ivAdharB.buildDrawingCache();
            bitmap = ivAdharB.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d(TAG, "aadhar back converted to Base64");
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
            Log.d(TAG, "Control moved to nextActivity()");
        }
        nextActivity(aadhar_f, aadhar_b, profilePic);
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
                        ivAdharF.setImageBitmap(selectedImage);
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
                                ivAdharF.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }
                    break;
                case 3:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ivAdharB.setImageBitmap(selectedImage);
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
                                ivAdharB.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(4);
                                cursor.close();
                            }
                        }
                    }
                    break;
                case 5:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        ivProfile.setImageBitmap(selectedImage);
                        convertAndUpload(5);
                    }
                    break;
                case 6:
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
                                ivProfile.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(6);
                            }
                        }
                    }
                    break;
            }
        }

    }

    public void onSuccess(JSONObject response) throws JSONException {
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        //response on hitting registor-servitor API
        String auth = response.getString("auth");
        String an = response.getString("an");
        String pn = response.getString("pn");
        String name = response.getString("name");

        SharedPreferences sp_jStatus = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        // sp_jStatus.edit().putString(AADHAR, an).apply();
        sp_jStatus.edit().putString(AUTH_KEY, auth).apply();
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