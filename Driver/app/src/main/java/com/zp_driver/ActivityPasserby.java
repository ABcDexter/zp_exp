package com.zp_driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityPasserby extends AppCompatActivity {
    Button startRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passerby);
        startRide = findViewById(R.id.startRidePasserby);
        startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(ActivityPasserby.this, SOS.class);
                startActivity(start);
            }
        });
    }
}
