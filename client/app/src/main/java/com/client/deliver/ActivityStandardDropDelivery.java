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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivityStandardDropDelivery extends AppCompatActivity {
    CheckBox[] chkArray = new CheckBox[3];
    TextView confirm, cancel;
    String slot = "";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String DROP_HOUR = "DropHour";
    public static final String DROP_MINUTE = "DropMinute";
    public static final String EXPRESS = "Express";
    public static final String DROP_YEAR = "DropYear";
    public static final String DROP_MONTH = "DropMonth";
    public static final String DROP_DAY = "DropDay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_drop_delivery);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel_standard);

        chkArray[0] = (CheckBox) findViewById(R.id.slot1);
        chkArray[1] = (CheckBox) findViewById(R.id.slot2);
        chkArray[2] = (CheckBox) findViewById(R.id.slot3);


        chkArray[0].setOnClickListener(mListener);
        chkArray[1].setOnClickListener(mListener);
        chkArray[2].setOnClickListener(mListener);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!slot.equals("")) {

                    Calendar calendar = Calendar.getInstance();
                    Date today = calendar.getTime();

                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    Date tomorrow = calendar.getTime();

                    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

                    //String todayAsString = dateFormat.format(today);
                    String tomorrowAsString = dateFormat.format(tomorrow);

                    Log.d("####", "tomorrow " + tomorrowAsString);

                    String first = tomorrowAsString.split("\\.")[0];
                    String second = tomorrowAsString.split("\\.")[1];
                    String third = tomorrowAsString.split("\\.")[2];

                    Log.d("TAG", "first :" + first + "second :" + second + "third :" + third);


                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(DROP_HOUR, slot);
                    editor.putString(DROP_MINUTE, "00");
                    editor.putString(EXPRESS, "0");
                    editor.putString(DROP_YEAR, first);
                    editor.putString(DROP_MONTH, second);
                    editor.putString(DROP_DAY, third);
                    editor.apply();
                    Intent back = new Intent(ActivityStandardDropDelivery.this, ActivityDropDetails.class);
                    back.putExtra("SLOT", slot);
                    startActivity(back);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(ActivityStandardDropDelivery.this, ActivityDropDetails.class);
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
                    if (i == 1) slot = "12";
                    if (i == 2) slot = "16";

                    Log.d("Checked", "slot = " + slot);
                } else {
                    current.setChecked(false);
                    slot = "";
                    Log.d("Unchecked", "slot = " + slot);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityStandardDropDelivery.this, ActivityDropDetails.class));
        finish();
    }
}
