package com.client.deliver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.client.ActivityDrawer;
import com.client.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityDeliveryTimeSlot extends ActivityDrawer implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static final Integer[] IMAGES = {R.drawable.delivery_man};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    CheckBox expressDelv, standardDelv, beware, knock, ring, allDay;
    CheckBox slot1, slot2, slot3, slot4, slot5, slot6;
    Dialog timeDialog, deliveryDialog, standardDialog, additionalDetails;

    String lat, lng, stringAuth, stringAN, pickLat, pickLng, pickLand, pickPin, pickMobile;

    private static final String TAG = "ActivityDeliveryTimeSlot";

    public static final String REVIEW = "com.delivery.Review";//TODO find better way
    public static final String R_STND_DELVY = "R_STND_DELVY";//TODO find better way
    public static final String R_EXP_DELVY = "R_EXP_DELVY";//TODO find better way
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String PICK_YEAR = "PickYear";
    public static final String PICK_MONTH = "PickMonth";
    public static final String PICK_DAY = "PickDay";
    public static final String PICK_HOUR = "PickHour";
    public static final String PICK_MINUTE = "PickMinute";
    public static final String EXPRESS = "Express";
    public static final String ADD_INFO_PICK_POINT = "AddInfoPickPoint";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    SharedPreferences prefAuth;
    ImageButton next;
    String EXPress;
    String express = "00";

    TextView /*expTime*/ stndTime, addDetails, expTomorrow, stndtomorrow;
    String timeSlot = "0";
    Vibrator vibrator;
    Switch stndDay, expDay;
    boolean isBefore = false;
    Calendar datetime = Calendar.getInstance();
    String addDrop, dropLat, dropLng, dropLand, dropPin, dropMobile,
            conType, conSize;
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String ADD_INFO_DROP_POINT = "AddInfoDropPoint";
    ActivityDeliveryTimeSlot a = ActivityDeliveryTimeSlot.this;
    Map<String, String> params = new HashMap();
    String details;


    int hour = 0;
    int min = 0;
    String format = "AM";
    String StringMinute, StringHours;
    int exp_date = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_delivery_time_slot, null, false);
        frameLayout.addView(activityView);
        init();
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        addDrop = pref.getString(ADDRESS_DROP, "");
        dropLat = pref.getString(DROP_LAT, "");
        dropLng = pref.getString(DROP_LNG, "");
        pickLat = pref.getString(PICK_LAT, "");
        pickLng = pref.getString(PICK_LNG, "");
        dropLand = pref.getString(DROP_LANDMARK, "");
        dropPin = pref.getString(DROP_PIN, "");
        dropMobile = pref.getString(DROP_MOBILE, "");
        conType = pref.getString(CONTENT_TYPE, "");
        conSize = pref.getString(CONTENT_DIM, "");
        EXPress = pref.getString(EXPRESS, "");
        pickLand = pref.getString(PICK_LANDMARK, "");
        pickPin = pref.getString(PICK_PIN, "");
        pickMobile = pref.getString(PICK_MOBILE, "");

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //expTime = findViewById(R.id.express_time);
        stndTime = findViewById(R.id.standard_time);

        expressDelv = findViewById(R.id.chk_express);
        expressDelv.setOnCheckedChangeListener(this);
        standardDelv = findViewById(R.id.chk_standard);
        standardDelv.setOnCheckedChangeListener(this);
        next = findViewById(R.id.next_deliver);
        addDetails = findViewById(R.id.add_details);
        addDetails.setOnClickListener(this);
        //timeSlot.setOnClickListener(this);
        next.setOnClickListener(this);
        expressDelv.setOnClickListener(this);
        standardDelv.setOnClickListener(this);
        timeDialog = new Dialog(this);
        deliveryDialog = new Dialog(this);
        standardDialog = new Dialog(this);
        additionalDetails = new Dialog(this);
        Intent intent = getIntent();
        String slot = intent.getStringExtra("SLOT");

        /*if (!slot.isEmpty()) {
            standardDelv.setText(slot);
        }*/

    }

    private void init() {
        for (int i = 0; i < IMAGES.length; i++)
            ImagesArray.add(IMAGES[i]);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new SlidingImage_Adapter(ActivityDeliveryTimeSlot.this, ImagesArray));
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);

        indicator.setViewPager(mPager);

        final float density = getResources().getDisplayMetrics().density;
        //Set circle indicator radius
        indicator.setRadius(5 * density);

        NUM_PAGES = IMAGES.length;

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 5000, 10000);

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*if (id == R.id.confirm_exp) {
            express = "1";
            //expTime.setText(hour + ":" + StringMinute);
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //editor.putString(PICK_HOUR, Integer.toString(hour));
            editor.putString(PICK_HOUR, StringHours);
            editor.putString(PICK_MINUTE, StringMinute);
            editor.putString(EXPRESS, "1");
            editor.apply();
            Log.d(TAG, "hour" + StringHours + " min=" + StringMinute);
            SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
            SharedPreferences.Editor reditor = review.edit();
            reditor.putString(R_EXP_DELVY, "express");
            reditor.apply();
            expressDelv.getText();
            if (exp_date == 1) {
                TodayDate();
            }
            if (exp_date == 2) {
                TomorrowDate();
            }
            timeDialog.dismiss();
        }
        else*/
        if (id == R.id.confirm_standard) {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
            Log.d(TAG, "time=" + currentHour);
            Log.d(TAG, "exp_date=" + exp_date);
            if (exp_date == 1) {
                TodayDate();

                switch (timeSlot) {
                    case "0":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                        Toast.makeText(this, R.string.make_selection, Toast.LENGTH_LONG).show();

                        break;
                    case "8":
                        if (currentHour >= 10) //False if after 10am
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 8)*/ {
                            standardDialog.dismiss();
                            saveStandardTime("8:00-10:00 am");
                        }
                        break;
                    case "10":
                        if (currentHour >= 12) //False if after 12pm
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 10)*/ {
                            standardDialog.dismiss();
                            saveStandardTime("10:00-12:00 pm");
                        }
                        break;
                    case "12":
                            /*if (currentHour ==12 || currentHour == 13){
                                standardDialog.dismiss();
                                saveStandardTime("12:00-14:00 ");
                            }
                            else */
                        if (currentHour >= 14) //False if after 2pm
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 12) */ {
                            standardDialog.dismiss();
                            saveStandardTime("12:00-14:00 ");
                        }
                        break;
                    case "14":
                        if (currentHour >= 16) //False if after 4pm
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 14)*/ {
                            standardDialog.dismiss();
                            saveStandardTime("14:00-16:00");
                        }
                        break;
                    case "16":
                        if (currentHour >= 18) //False if after 6pm
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 16)*/ {
                            standardDialog.dismiss();
                            saveStandardTime("16:00-18:00");
                        }
                        break;
                    case "18":
                        if (currentHour >= 20) //False if after 8pm
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(1000);
                            }
                            Toast.makeText(this, R.string.invalid_selection, Toast.LENGTH_LONG).show();
                        } else /*if (currentHour >= 18)*/ {
                            standardDialog.dismiss();
                            saveStandardTime("18:00-20:00");
                        }
                        break;
                }
            } /*else {*/
                    /*if (exp_date == 1) {
                        TodayDate();
                    }*/
            if (exp_date == 2) {
                Toast.makeText(ActivityDeliveryTimeSlot.this, R.string.delivery_sch_tommorrow, Toast.LENGTH_LONG).show();
                TomorrowDate();
                standardDialog.dismiss();
                saveStandardTime(timeSlot);
            }
                    /*standardDialog.dismiss();
                    saveStandardTime(timeSlot);*/
            /*}*/
        } else if (id == R.id.cancel_exp) {
            express = "0";
            timeDialog.dismiss();
        } else if (id == R.id.next_deliver) {//TodayDate();
            //saveData();
            //deliveryEstimate();
            if (/*expTime.getText().toString().equals("") && stndTime.getText().toString().equals("")*/ express.equals("00")) {
                // Log.d(TAG, "expTime=" + expTime.getText().toString() + "stndTime=" + stndTime.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
                Log.d(TAG, "express=" + express);
                Toast.makeText(a, "Please choose time", Toast.LENGTH_SHORT).show();
            } else {
                if (express.equals("1")) {
                    //expTime.setText(hour + ":" + StringMinute);
                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //editor.putString(PICK_HOUR, Integer.toString(hour));
                    editor.putString(PICK_HOUR, StringHours);
                    editor.putString(PICK_MINUTE, StringMinute);
                    editor.putString(EXPRESS, "1");
                    editor.apply();
                    Log.d(TAG, "hour" + StringHours + " min=" + StringMinute);
                    SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
                    SharedPreferences.Editor reditor = review.edit();
                    reditor.putString(R_EXP_DELVY, "express");
                    reditor.apply();
                    expressDelv.getText();
                    if (exp_date == 1) {
                        TodayDate();
                    }
                    if (exp_date == 2) {
                        TomorrowDate();
                    }
                }
                Intent next = new Intent(ActivityDeliveryTimeSlot.this, ActivityDeliveryReview.class);
                startActivity(next);
            }
        } else if (id == R.id.add_details) {
            DetailsCheckbox();
        } else if (id == R.id.confirm_details) {
            additionalDetails.dismiss();
        } else if (id == R.id.cancel_standard) {
            standardDialog.dismiss();
        }
    }

    private void saveStandardTime(String tmSlot) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PICK_HOUR, tmSlot);
        editor.putString(PICK_MINUTE, "00");
        /*editor.putString(HOUR, tmSlot);
        editor.putString(MINUTE, "00");*/
        editor.putString(EXPRESS, "0");
        editor.apply();
        Log.d(TAG, "hour" + tmSlot + " min=" + "00");

        SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
        SharedPreferences.Editor reditor = review.edit();
        reditor.putString(R_STND_DELVY, "standard");
        reditor.apply();
    }


    private void StandardTimeCheckbox() {
        standardDialog.setContentView(R.layout.popup_time_slot);
        slot1 = standardDialog.findViewById(R.id.slot1);
        slot2 = standardDialog.findViewById(R.id.slot2);
        slot3 = standardDialog.findViewById(R.id.slot3);
        slot4 = standardDialog.findViewById(R.id.slot4);
        slot5 = standardDialog.findViewById(R.id.slot5);
        slot6 = standardDialog.findViewById(R.id.slot6);
        stndDay = standardDialog.findViewById(R.id.stnd_day_switch);
        stndDay.setChecked(false);
        exp_date = 1;
        stndDay.setOnCheckedChangeListener(this);
        stndtomorrow = standardDialog.findViewById(R.id.tomorrow);

        TextView confirm = standardDialog.findViewById(R.id.confirm_standard);
        TextView cancel = standardDialog.findViewById(R.id.cancel_standard);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);

        slot1.setOnCheckedChangeListener(this);
        slot2.setOnCheckedChangeListener(this);
        slot3.setOnCheckedChangeListener(this);
        slot4.setOnCheckedChangeListener(this);
        slot5.setOnCheckedChangeListener(this);
        slot6.setOnCheckedChangeListener(this);

        standardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = standardDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        standardDialog.show();
        Window window = standardDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        standardDialog.setCanceledOnTouchOutside(false);

    }

    private void DetailsCheckbox() {
        additionalDetails.setContentView(R.layout.popup_checkbox_delivery_details);
        beware = additionalDetails.findViewById(R.id.beware);
        knock = additionalDetails.findViewById(R.id.knock);
        ring = additionalDetails.findViewById(R.id.ring);
        allDay = additionalDetails.findViewById(R.id.all_day);
        TextView confirm = additionalDetails.findViewById(R.id.confirm_details);
        confirm.setOnClickListener(this);

        beware.setOnCheckedChangeListener(this);
        knock.setOnCheckedChangeListener(this);
        ring.setOnCheckedChangeListener(this);
        allDay.setOnCheckedChangeListener(this);

        additionalDetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = additionalDetails.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        additionalDetails.show();
        Window window = additionalDetails.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        additionalDetails.setCanceledOnTouchOutside(true);

    }

    /*private void PopupTime() {
        timeDialog.setContentView(R.layout.popup_time);
        TimePicker simpleTimePicker = timeDialog.findViewById(R.id.simpleTimePicker);
        TextView confirm = (TextView) timeDialog.findViewById(R.id.confirm_exp);
        TextView cancel = (TextView) timeDialog.findViewById(R.id.cancel_exp);
        expTomorrow = (TextView) timeDialog.findViewById(R.id.exp_tomorrow);
        expDay = timeDialog.findViewById(R.id.exp_day_switch);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        expDay.setOnCheckedChangeListener(this);

        simpleTimePicker.setIs24HourView(false); // used to display AM/PM mode
        simpleTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay < 7.59) {
                    simpleTimePicker.setHour(8);
                    simpleTimePicker.setMinute(0);
                    *//*hour = simpleTimePicker.getHour();
                    min = simpleTimePicker.getMinute();*//*
                } else if (hourOfDay > 19.59) {
                    simpleTimePicker.setHour(20);
                    simpleTimePicker.setMinute(0);
                   *//* hour = simpleTimePicker.getHour();
                    min = simpleTimePicker.getMinute();*//*
                }

                *//*if (hour == 0) {

                    hour += 12;

                    format = "AM";
                } else if (hour == 12) {

                    format = "PM";

                } else if (hour > 12) {

                    hour -= 12;

                    format = "PM";

                } else {

                    format = "AM";
                }*//*

                String str_minute = "";

                if (hourOfDay > 20 && minute > 0) {
                    hourOfDay = 20;
                    minute = 0;
                }
                if (hourOfDay < 8 && minute > 0) {
                    hourOfDay = 8;
                    minute = 0;
                }
                if (minute < 10) {
                    str_minute = "0" + minute;
                } else {
                    str_minute = Integer.toString(minute);
                }
                //check if the time chosen is before the current time
                if (hourOfDay < datetime.get(Calendar.HOUR_OF_DAY)) {
                    isBefore = true;
                    expDay.setChecked(true);
                    expTomorrow.setTextColor(ContextCompat.getColor(ActivityDeliveryTimeSlot.this, R.color.colorPrimaryDark));
                    Toast.makeText(ActivityDeliveryTimeSlot.this, R.string.delivery_sch_tommorrow, Toast.LENGTH_LONG).show();

                } else if (hourOfDay == datetime.get(Calendar.HOUR_OF_DAY)) {
                    if (minute < datetime.get(Calendar.MINUTE)) {
                        isBefore = true;
                        expDay.setChecked(true);
                        expTomorrow.setTextColor(ContextCompat.getColor(ActivityDeliveryTimeSlot.this, R.color.colorPrimaryDark));
                        Toast.makeText(ActivityDeliveryTimeSlot.this, R.string.delivery_sch_tommorrow, Toast.LENGTH_LONG).show();
                    }
                } else {
                    isBefore = false;
                    expDay.setChecked(false);
                    expTomorrow.setTextColor(Color.WHITE);

                }
                hour = hourOfDay;
                StringMinute = str_minute;
                *//*expTime.setText(hourOfDay + ":" + str_minute);
     *//**//* if (express.equals("1")) {*//*
     *//*
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PICK_HOUR, Integer.toString(hourOfDay));
                editor.putString(PICK_MINUTE, str_minute);
                editor.putString(EXPRESS, "1");
                editor.apply();
                SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
                SharedPreferences.Editor reditor = review.edit();
                reditor.putString(R_EXP_DELVY, "express");
                reditor.apply();*//*
                Log.d(TAG, "hour=" + hourOfDay + " min = " + str_minute);
                *//*}*//*
            }
        });

        timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = timeDialog.getWindow().getAttributes();

        wmlp.y = 80;   //y position
        timeDialog.show();
        Window window = timeDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        timeDialog.setCanceledOnTouchOutside(true);

    }
*/

    private void TodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDateandTime = sdf.format(new Date());
        Log.d(TAG, "current date" + currentDateandTime);


        String first = currentDateandTime.split("\\.")[0];
        String second = currentDateandTime.split("\\.")[1];
        String third = currentDateandTime.split("\\.")[2];

        Log.d(TAG, "first :" + first + "second :" + second + "third :" + third);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PICK_YEAR, first);
        editor.putString(PICK_MONTH, second);
        editor.putString(PICK_DAY, third);
        editor.apply();

    }

    private void TomorrowDate() {
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

        Log.d(TAG, first + second + third);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PICK_YEAR, first);
        editor.putString(PICK_MONTH, second);
        editor.putString(PICK_DAY, third);
        editor.apply();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliveryTimeSlot.this, ActivityFillDropAddress.class));
        finish();
    }

    private void expDelAlert() {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDeliveryTimeSlot.this);
        // Set the message show for the Alert time
        builder.setMessage(R.string.first_order_at_8_00am_nlast_order_at_8_00pm_nwithin_2_hours_upto_10km);
        // Set Alert Title
        builder.setTitle(R.string.please_note);
        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);
        // Set the positive button with ok name OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                expressDelv.setChecked(false);
                dialog.cancel();
            }
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC7721")));
        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.chk_express) {
            if (expressDelv.isChecked()) {
                standardDelv.setChecked(false);
                //PopupTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String str = sdf.format(new Date());
                Log.d(TAG, "sdf=" + str);
                String[] splitArray = str.split(":");
                String HH = splitArray[0];
                String MM = splitArray[1];
                Log.d(TAG, "HH=" + HH + " MM=" + MM);
                int intHH = Integer.parseInt(HH);
                Log.d(TAG, "intHH=" + intHH);

                if (/*HH.equals("7")|| HH.equals("20")*/intHH < 8 || intHH > 19) {
                    expDelAlert();
                    express = "00";
                } else {
                    express = "1";
                    StringHours = HH;
                    StringMinute = MM;
                }
                stndTime.setText("");
            }

        }
        if (id == R.id.chk_standard) {
            if (standardDelv.isChecked()) {
                expressDelv.setChecked(false);
                StandardTimeCheckbox();
                express = "0";
                //expTime.setText("");
            }
        }
        if (id == R.id.slot1) {
            if (slot1.isChecked()) {
                slot2.setChecked(false);
                slot3.setChecked(false);
                slot4.setChecked(false);
                slot5.setChecked(false);
                slot6.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "8";
                stndTime.setText("8:00 - 10:00 am");
            }
        }
        if (id == R.id.slot2) {
            if (slot2.isChecked()) {
                slot1.setChecked(false);
                slot3.setChecked(false);
                slot4.setChecked(false);
                slot5.setChecked(false);
                slot6.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "10";
                stndTime.setText("10:00 - 12:00 pm");
            }
        }
        if (id == R.id.slot3) {
            if (slot3.isChecked()) {
                slot1.setChecked(false);
                slot2.setChecked(false);
                slot4.setChecked(false);
                slot5.setChecked(false);
                slot6.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "12";
                stndTime.setText("12:00 - 2:00 pm");
            }
        }
        if (id == R.id.slot4) {
            if (slot4.isChecked()) {
                slot1.setChecked(false);
                slot2.setChecked(false);
                slot3.setChecked(false);
                slot5.setChecked(false);
                slot6.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "14";
                stndTime.setText("2:00 - 4:00 pm");
            }
        }
        if (id == R.id.slot5) {
            if (slot5.isChecked()) {
                slot1.setChecked(false);
                slot2.setChecked(false);
                slot3.setChecked(false);
                slot4.setChecked(false);
                slot6.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "16";
                stndTime.setText("4:00 - 6:00 pm");
            }
        }
        if (id == R.id.slot6) {
            if (slot6.isChecked()) {
                slot1.setChecked(false);
                slot2.setChecked(false);
                slot3.setChecked(false);
                slot4.setChecked(false);
                slot5.setChecked(false);
                //StandardTimeCheckbox();
                express = "0";
                timeSlot = "18";
                stndTime.setText("6:00 - 8:00 pm");
            }
        }
        if (id == R.id.beware) {
            if (beware.isChecked()) {
                details = "beware of dogs";
            }

        }
        if (id == R.id.stnd_day_switch) {
            if (stndDay.isChecked()) {
                stndtomorrow.setTextColor(ContextCompat.getColor(ActivityDeliveryTimeSlot.this, R.color.colorPrimaryDark));
                //TomorrowDate();
                exp_date = 2;
            } else {
                stndtomorrow.setTextColor(Color.WHITE);
                exp_date = 1;
            }
        }
        if (id == R.id.exp_day_switch) {
            if (expDay.isChecked()) {
                expTomorrow.setTextColor(ContextCompat.getColor(ActivityDeliveryTimeSlot.this, R.color.colorPrimaryDark));
                //TomorrowDate();
                exp_date = 2;
            } else {
                expTomorrow.setTextColor(Color.WHITE);
                //TodayDate();
                exp_date = 1;
            }
        }
    }


}