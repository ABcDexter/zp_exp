package com.client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityRateZippe extends ActivityDrawer implements View.OnClickListener {

    ImageButton happy,sad;
    ScrollView scrollView;
    Dialog myDialog,checkDialog;

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
        myDialog = new Dialog(this);
        checkDialog = new Dialog(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.satisfied:
                ShowPopup();
                break;
            case R.id.notSatisfied:
                CheckPopup();
                break;

        }
    }
    private void ShowPopup() {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText("Thank you for riding with ZIPP-E");

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateZippe.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
            }
        }, 5000);
    }

    private void CheckPopup() {

        checkDialog.setContentView(R.layout.popup_checkbox);
        TextView cancel = (TextView) checkDialog.findViewById(R.id.cancel);
        TextView submit = (TextView) checkDialog.findViewById(R.id.submit);

        CheckBox chk1 = checkDialog.findViewById(R.id.attitude);
        CheckBox chk2 = checkDialog.findViewById(R.id.condition);
        CheckBox chk3 = checkDialog.findViewById(R.id.clean);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopup1();
                checkDialog.dismiss();
            }
        });
        checkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkDialog.show();
        checkDialog.setCanceledOnTouchOutside(false);
    }

    private void ShowPopup1() {

        myDialog.setContentView(R.layout.popup_color);

        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText("Thank you for your feedback. We promise to take appropriate action.");

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateZippe.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
            }
        }, 5000);
    }

}
