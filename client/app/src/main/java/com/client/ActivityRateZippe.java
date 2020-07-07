package com.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.client.ride.ActivityRideHome;
import com.google.android.material.snackbar.Snackbar;

public class ActivityRateZippe extends ActivityDrawer implements View.OnClickListener {

    ImageButton happy,sad;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rate, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);


        /*Objects.requireNonNull(getSupportActionBar()).setTitle("fgh");*/


        happy = findViewById(R.id.satisfied);
        happy.setOnClickListener(this);
        sad = findViewById(R.id.notSatisfied);
        sad.setOnClickListener(this);


        scrollView = findViewById(R.id.scrollViewRateActivity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.satisfied:
            case R.id.notSatisfied:
                Intent finishIntent = new Intent(ActivityRateZippe.this, ActivityRideHome.class);
                startActivity(finishIntent);
                finish();

                Snackbar snackbar = Snackbar
                        .make(scrollView, "THANK YOU FOR RIDING WITH US", Snackbar.LENGTH_LONG);
                snackbar.show();


        }
    }
}
