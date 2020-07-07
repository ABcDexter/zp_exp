package com.deliverpartner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityVehicleNeed extends AppCompatActivity implements View.OnClickListener {

    Button rentVehicle, haveVehicle, contactSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_need);

        //initialise variables
        rentVehicle = findViewById(R.id.rent_vehicle);
        haveVehicle = findViewById(R.id.have_vehicle);
        contactSupport = findViewById(R.id.contact_support);

        rentVehicle.setOnClickListener(this);
        haveVehicle.setOnClickListener(this);
        contactSupport.setOnClickListener(this);

        FirebaseMessaging.getInstance().subscribeToTopic("NEWYORK_WEATHER");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rent_vehicle:
                Toast.makeText(this, "Lead to rental section of user app", Toast.LENGTH_LONG).show();
                break;
            case R.id.have_vehicle:
                Intent haveIntent = new Intent(ActivityVehicleNeed.this, ActivityHome.class);
                startActivity(haveIntent);
                break;
            case R.id.contact_support:
                Toast.makeText(this, "OPEN CHART-BOT", Toast.LENGTH_LONG).show();

                break;
        }
    }
}
