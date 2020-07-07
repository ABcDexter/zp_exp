package com.client.rent;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.client.ActivityDrawer;
import com.client.R;

public class EnjoyTrip extends ActivityDrawer implements View.OnClickListener {

    TextView shareDetails, emergencyCall, timingTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_enjoy_trip, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);


        /*Objects.requireNonNull(getSupportActionBar()).setTitle("fgh");*/

        shareDetails = findViewById(R.id.share_ride_details);
        shareDetails.setOnClickListener(this);
        emergencyCall = findViewById(R.id.emergency);
        emergencyCall.setOnClickListener(this);
        timingTxt = findViewById(R.id.timingTxt);

        Intent intent = getIntent();

        String timing_str = intent.getStringExtra("timing");

        timingTxt.setText(timing_str);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_ride_details:
                selectAction(EnjoyTrip.this);
                break;

            case R.id.emergency:
                btnSetOnEmergency();
                break;


        }
    }
    public void btnSetOnEmergency() {
        String number = "7060743705";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }
    private void selectAction(Context context) {
        final CharSequence[] options = {"SEND SMS", "VIDEO CALL", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("SEND SMS")) {
                    String messageBody = "TRACK MY RIDE HERE";
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setData(Uri.parse("sms:"));
                    sendIntent.putExtra("sms_body", messageBody);
                    startActivity(sendIntent);

                } else if (options[item].equals("VIDEO CALL")) {
                    Intent whatsappLaunch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    startActivity(whatsappLaunch);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

}
