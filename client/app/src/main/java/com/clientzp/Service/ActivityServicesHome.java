package com.clientzp.Service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.clientzp.ActivityDrawer;
import com.clientzp.R;

public class ActivityServicesHome extends ActivityDrawer implements View.OnClickListener {

    Button serviceCategory, serviceHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_services_home, null, false);
        frameLayout.addView(activityView);

        serviceCategory = findViewById(R.id.services_category);
        serviceHistory = findViewById(R.id.services_history);

        serviceCategory.setOnClickListener(this);
        serviceHistory.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.services_category) {
            //take user to https://zippe.in/en/zippe-connect/ url
            //String connectUrl = "https://zippe.in/en/zippe-connect/";
            String connectUrl = "https://zippe.in/service-categories/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);
        }
        if (id == R.id.services_history){
            //take user to https://zippe.in/en/my-account/orders/ url
            String connectUrl = "https://zippe.in/en/my-account/orders/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);
            /*Intent history = new Intent(ActivityServicesHome.this, ActivityServiceHistoryList.class);
            startActivity(history);
            finish();*/
        }
    }
}

