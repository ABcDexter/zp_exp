package com.client.rent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.client.ActivityDrawer;
import com.client.ActivityWelcome;
import com.client.R;

public class ActivityRateRent extends ActivityDrawer implements View.OnClickListener {

    ImageButton happy, sad;
    ScrollView scrollView;
    Dialog myDialog, checkDialog;
    TextView textView;

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
        sad = findViewById(R.id.notSatisfied);
        scrollView = findViewById(R.id.scrollViewRateActivity);
        textView = findViewById(R.id.txt_rate);

        textView.setText("RATE YOUR EXPERIENCE");
        happy.setOnClickListener(this);
        sad.setOnClickListener(this);

        myDialog = new Dialog(this);
        checkDialog = new Dialog(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

        infoText.setText("Thanks for renting ZIPP-E");

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateRent.this, ActivityWelcome.class);
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
        CheckBox chk4 = checkDialog.findViewById(R.id.other);

        chk1.setText("Attitude of contact person");
        chk2.setText("Vehicle condition");
        chk3.setText("Vehicle cleanliness");
        chk4.setText("Any other");
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
        WindowManager.LayoutParams wmlp = checkDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        checkDialog.show();
        Window window = checkDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        checkDialog.setCanceledOnTouchOutside(false);
    }

    private void ShowPopup1() {

        myDialog.setContentView(R.layout.popup_color);

        TextView infoText = (TextView) myDialog.findViewById(R.id.info_text);

        infoText.setText("Thank you for your feedback. We promise to take appropriate action.");

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = myDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 77;   //y position
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        myDialog.setCanceledOnTouchOutside(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent finishIntent = new Intent(ActivityRateRent.this, ActivityWelcome.class);
                startActivity(finishIntent);
                finish();
            }
        }, 5000);
    }

}
