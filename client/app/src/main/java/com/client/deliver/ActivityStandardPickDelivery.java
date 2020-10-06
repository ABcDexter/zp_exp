package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.client.R;

public class ActivityStandardPickDelivery extends AppCompatActivity {
    CheckBox[] chkArray = new CheckBox[12];
    TextView confirm, cancel;
    String slot = "";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String EXPRESS = "Express";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_delivery);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel_standard);

        chkArray[0] = (CheckBox) findViewById(R.id.slot1);
        chkArray[1] = (CheckBox) findViewById(R.id.slot2);
        chkArray[2] = (CheckBox) findViewById(R.id.slot3);
        chkArray[3] = (CheckBox) findViewById(R.id.slot4);
        chkArray[4] = (CheckBox) findViewById(R.id.slot5);
        chkArray[5] = (CheckBox) findViewById(R.id.slot6);

        chkArray[0].setOnClickListener(mListener);
        chkArray[1].setOnClickListener(mListener);
        chkArray[2].setOnClickListener(mListener);
        chkArray[3].setOnClickListener(mListener);
        chkArray[4].setOnClickListener(mListener);
        chkArray[5].setOnClickListener(mListener);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!slot.equals("")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(HOUR, slot);
                    editor.putString(MINUTE, "00");
                    editor.putString(EXPRESS, "0");
                    editor.apply();
                    Intent back = new Intent(ActivityStandardPickDelivery.this, ActivityDeliveryTimeSlot.class);
                    back.putExtra("SLOT", slot);
                    startActivity(back);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(ActivityStandardPickDelivery.this, ActivityDeliveryTimeSlot.class);
                startActivity(back);
            }
        });
    }

    private View.OnClickListener mListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final int checkedId = v.getId();
            for (int i = 0; i < chkArray.length; i++) {
                final CheckBox current = chkArray[i];
                if (current.getId() == checkedId) {
                    current.setChecked(true);
                    //slot = Integer.toString(i);
                    if (i == 0) slot = "8";
                    if (i == 1) slot = "10";
                    if (i == 2) slot = "12";
                    if (i == 3) slot = "14";
                    if (i == 4) slot = "16";
                    if (i == 5) slot = "18";
                    Log.d("Checked", "slot = " + slot);
                } else {
                    current.setChecked(false);
                    slot = "";
                    Log.d("Unchecked", "slot = " + slot);
                }
            }
        }
    };
}
