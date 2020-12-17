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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ActivityNewBooking extends AppCompatActivity implements View.OnClickListener {

    TextView date, city, phn, note;
    ImageButton infoDate, infoCity, dialPhn, infoNote;
    Dialog myDialog;
    Button accept, reject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_booking);

        date = findViewById(R.id.tvDate);
        city = findViewById(R.id.tvCity);
        phn = findViewById(R.id.tvPhone);
        note = findViewById(R.id.tvNote);
        accept = findViewById(R.id.btnAccept);
        reject = findViewById(R.id.btnReject);

        infoDate = findViewById(R.id.infoDate);
        infoCity = findViewById(R.id.infoCity);
        infoNote = findViewById(R.id.infoNote);
        dialPhn = findViewById(R.id.dialPhn);

        dialPhn.setOnClickListener(this);
        infoDate.setOnClickListener(this);
        infoCity.setOnClickListener(this);
        infoNote.setOnClickListener(this);
        accept.setOnClickListener(this);
        reject.setOnClickListener(this);

        myDialog = new Dialog(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialPhn) {
            callClientPhn();
        } else if (id == R.id.infoDate) {
            ShowPopup(1);
        } else if (id == R.id.infoCity) {
            ShowPopup(2);
        } else if (id == R.id.infoNote) {
            ShowPopup(3);
        } else if (id == R.id.btnAccept){
            Intent accept = new Intent(ActivityNewBooking.this, ActivityAccepted.class);
            startActivity(accept);
            finish();
        } else if (id == R.id.btnReject){
            Intent reject = new Intent(ActivityNewBooking.this, ActivityHome.class);
            startActivity(reject);
            finish();
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
            infoText.setText("This is the city in which the client is located");
            myDialog.setCanceledOnTouchOutside(true);
        }
        if (id == 3) {
            infoText.setText("These are the requirements of the client.");
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