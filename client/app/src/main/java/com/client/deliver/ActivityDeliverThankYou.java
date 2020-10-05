package com.client.deliver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.client.ActivityWelcome;
import com.client.R;


public class ActivityDeliverThankYou extends AppCompatActivity {

    TextView details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_thank_you);

        details = findViewById(R.id.view_delivery);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewDetails = new Intent(ActivityDeliverThankYou.this, ActivityDeliveryHistoryList.class);
                startActivity(viewDetails);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverThankYou.this, ActivityWelcome.class));
        finish();
    }
}
