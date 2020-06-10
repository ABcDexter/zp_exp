package com.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class ActivityTripSummary extends AppCompatActivity {
TextView prc, tm, dst, sp;

    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.client.ride.TripDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);
        prc = findViewById(R.id.price);
        tm = findViewById(R.id.time);
        dst = findViewById(R.id.dist);
        sp = findViewById(R.id.speed);

        getData();


        SharedPreferences pref = getApplicationContext().getSharedPreferences(TRIP_DETAILS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(TRIP_ID);
        editor.apply();

    }

    private void getData() {
        Intent intent = getIntent();

        String strPrice = intent.getStringExtra("PRICE");
        String strTime = intent.getStringExtra("TIME");
        String strDist = intent.getStringExtra("DIST");
        String strSpeed = intent.getStringExtra("SPEED");

        prc.setText(strPrice);
        tm.setText(strTime);
        dst.setText(strDist);
        sp.setText(strSpeed);
    }
}
