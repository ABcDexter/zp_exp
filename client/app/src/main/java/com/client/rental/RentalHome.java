package com.client.rental;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.client.ActivityDrawer;
import com.client.R;
import com.client.ride.HubList;

import java.util.Calendar;

public class RentalHome extends ActivityDrawer implements View.OnClickListener {

    TextView pickPT, dropPt;
    ImageButton confirm;
    Spinner hoursSP;
    TextView timingSP;
    String hours_str, timing_str;
    private static final String TAG = "RentalHome";
    String Rent, vType, NoRiders, pMode, pickID, dropID, pickPoint, dropPoint, stringAuth;
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String RENT_RIDE = "RentRide";
    public static final String V_TYPE = "VType";
    public static final String RIDERS = "Riders";
    public static final String PAYMENT_MODE = "PaymentMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_rental_home, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);

        hoursSP = findViewById(R.id.no_hours);
        timingSP = findViewById(R.id.timing);
        timingSP.setOnClickListener(this);

        pickPT = findViewById(R.id.pickup_pt);
        pickPT.setOnClickListener(this);
        dropPt = findViewById(R.id.drop_pt);
        dropPt.setOnClickListener(this);
        confirm = findViewById(R.id.confirm_rental);
        confirm.setOnClickListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.hours_array));
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        hoursSP.setAdapter(adapter);
        hoursSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!(hoursSP.getSelectedItem() == "NO OF HOURS")) {
                    hours_str = hoursSP.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringPick = pref.getString(LOCATION_PICK, "");
        String stringDrop = pref.getString(LOCATION_DROP, "");
        String stringDropID = pref.getString(LOCATION_DROP_ID, "");
        String stringPickID = pref.getString(LOCATION_PICK_ID, "");

        Log.d(TAG, "RENT_RIDE:" + pref.getString(RENT_RIDE, "") + " V_TYPE:" +
                pref.getString(V_TYPE, "") + " RIDERS:" + pref.getString(RIDERS, "")
                + " PAYMENT_MODE:" + pref.getString(PAYMENT_MODE, ""));

        Rent = pref.getString(RENT_RIDE, "");
        vType = pref.getString(V_TYPE, "");
        NoRiders = pref.getString(RIDERS, "");
        pMode = pref.getString(PAYMENT_MODE, "");

        if (stringPick.isEmpty()) {
            pickPT.setText("PICK UP POINT");
            Log.d(TAG, "Pick Location  is " + stringPick);
        } else {
            pickPT.setText(stringPick);
            pickPoint = pickPT.getText().toString();
            pickID = stringPickID;
            Log.d(TAG, "Pick Location  is " + stringPick + " ID is " + stringPickID);
        }

        if (stringDrop.isEmpty()) {
            dropPt.setText("DROP POINT");
            Log.d(TAG, "Drop Location  is " + stringDrop);
        } else {
            dropPt.setText(stringDrop);
            dropPoint = dropPt.getText().toString();
            dropID = stringDropID;
            Log.d(TAG, "Drop Location  is " + stringDrop + " ID is " + stringDropID);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pickup_pt:
                Intent pick = new Intent(RentalHome.this, HubList.class);
                pick.putExtra("Request", "origin_rental");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(pick);
                break;
            case R.id.drop_pt:
                Intent drop = new Intent(RentalHome.this, HubList.class);
                drop.putExtra("Request", "destination_rental");
                Log.d(TAG, "control moved to HUBLIST activity with key destination_rental");
                startActivity(drop);
                break;

            case R.id.confirm_rental:
                if (!(Rent == null || vType == null || NoRiders == null || pMode == null || timing_str == null || hours_str ==null)) {
                    Log.d(TAG, "Values: npas=" + NoRiders + " srcid = " + pickID + " dstid = "
                            + dropID + " rtype = " + Rent + " vtype = " + vType + " pmode = " + pMode
                            +"Timings = "+ timing_str +"No of Hours = "+ hours_str);
                    Intent intent = new Intent(RentalHome.this, ConfirmRental.class);
                    intent.putExtra("timing", timing_str);
                    intent.putExtra("hours", hours_str);
                    startActivity(intent);
                } else
                    Log.d(TAG, "Values: npas=" + NoRiders + " srcid = " + pickID + " dstid = "
                            + dropID + " rtype = " + Rent + " vtype = " + vType + " pmode = " + pMode);


                break;
            case R.id.timing:
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(RentalHome.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time;
                        if (selectedHour >= 0 && selectedHour < 12) {
                            time = selectedHour + " : " + selectedMinute + " AM";
                        } else {
                            if (selectedHour == 12) {
                                time = selectedHour + " : " + selectedMinute + " PM";
                            } else {
                                selectedHour = selectedHour - 12;
                                time = selectedHour + " : " + selectedMinute + " PM";
                            }
                        }
                        timingSP.setText(time);
                        timing_str = timingSP.getText().toString();
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("SELECT TIME ");
                mTimePicker.setCanceledOnTouchOutside(false);
                mTimePicker.show();

                if (!(timingSP.getText() == "TIMINGS")) {
                    timing_str = timingSP.getText().toString();
                }
                break;
        }
    }
}
