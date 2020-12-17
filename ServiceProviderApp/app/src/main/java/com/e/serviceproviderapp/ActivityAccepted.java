package com.e.serviceproviderapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ActivityAccepted extends AppCompatActivity implements View.OnClickListener {
    TextView date, name, address, phn, note;
    EditText otp;
    ImageButton infoDate, dialPhn, infoNote, infoAdd, infoName;
    Dialog myDialog;
    Button accept, reject;
    String checkOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted);

        date = findViewById(R.id.tv_date);
        name = findViewById(R.id.tv_name);
        address = findViewById(R.id.tv_address);
        phn = findViewById(R.id.tv_phone);
        note = findViewById(R.id.tv_note);
        otp = findViewById(R.id.enter_otp);
        infoDate = findViewById(R.id.infoDate);
        infoNote = findViewById(R.id.infoNote);
        infoName = findViewById(R.id.infoName);
        infoAdd = findViewById(R.id.infoAddress);
        dialPhn = findViewById(R.id.dialPhn);
        accept = findViewById(R.id.yes);
        reject = findViewById(R.id.no);

        myDialog = new Dialog(this);

        infoDate.setOnClickListener(this);
        infoNote.setOnClickListener(this);
        infoName.setOnClickListener(this);
        infoAdd.setOnClickListener(this);
        dialPhn.setOnClickListener(this);
        accept.setOnClickListener(this);
        reject.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialPhn) {
            callClientPhn();
        } else if (id == R.id.infoDate) {
            ShowPopup(1);
        } else if (id == R.id.infoNote) {
            ShowPopup(3);
        } else if (id == R.id.infoAddress) {
            ShowPopup(2);
        } else if (id == R.id.infoName) {
            ShowPopup(4);
        } else if (id == R.id.btnAccept) {
            Intent accept = new Intent(ActivityAccepted.this, ActivityAccepted.class);
            startActivity(accept);
            finish();
        } else if (id == R.id.btnReject) {
            Intent reject = new Intent(ActivityAccepted.this, ActivityHome.class);
            startActivity(reject);
            finish();
        } else if (id == R.id.yes) {
            checkOtp = otp.getText().toString();
            if (!checkOtp.isEmpty()) {
                //ShowPopup(5);
                //method to hit api
            } else
                otp.requestFocus();// if OTP field is empty, then driverStartTrip method will not be called
        }
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);
        LinearLayout ln = (LinearLayout) myDialog.findViewById(R.id.layout_btn);

        if (id == 1) {
            infoText.setText("This the the date for work");
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 2) {
            infoText.setText("This is the address of the client.");
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 3) {
            infoText.setText("These are the requirements of the client.");
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 4) {
            infoText.setText("This is the name of the client.");
            myDialog.setCanceledOnTouchOutside(true);
        }

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    public void callClientPhn() {
        String phoneDriver = phn.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneDriver));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }
}