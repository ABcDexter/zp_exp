package com.client.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.viewpager.widget.ViewPager;

import com.client.ActivityDrawer;
import com.client.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class ActivityPickDetails extends ActivityDrawer implements View.OnClickListener {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static final Integer[] IMAGES = {R.drawable.delivery_man, R.drawable.packed_parcel, R.drawable.delivery_man, R.drawable.packed_parcel};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    Button expressDelv, standardDelv;
    Dialog timeDialog, deliveryDialog, standardDialog;
    TextView pickAddress;

    String lat, lng, stringAuth, stringAN, addPick, pickLat, pickLng, pickLand, pickPin, pickMobile;

    private static final String TAG = "ActivityPickDetails";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
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

    SharedPreferences prefAuth;
    ImageButton next;
    String express = "0";
    EditText detailsPick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_pick_details, null, false);
        frameLayout.addView(activityView);
        init();
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        addPick = pref.getString(ADDRESS_PICK, "");
        pickLat = pref.getString(PICK_LAT, "");
        pickLng = pref.getString(PICK_LNG, "");
        pickLand = pref.getString(PICK_LANDMARK, "");
        pickPin = pref.getString(PICK_PIN, "");
        pickMobile = pref.getString(PICK_MOBILE, "");

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");


        pickAddress = findViewById(R.id.pick_details);
        pickAddress.setOnClickListener(this);

        //timeSlot = findViewById(R.id.time_slot);
        expressDelv = findViewById(R.id.express_delv);
        detailsPick = findViewById(R.id.add_details_pick);
        standardDelv = findViewById(R.id.standard_delv);
        next = findViewById(R.id.next_deliver);
        //timeSlot.setOnClickListener(this);
        next.setOnClickListener(this);
        expressDelv.setOnClickListener(this);
        standardDelv.setOnClickListener(this);
        timeDialog = new Dialog(this);
        deliveryDialog = new Dialog(this);
        standardDialog = new Dialog(this);
        Intent intent = getIntent();
        String slot = intent.getStringExtra("SLOT");

        /*if (!slot.isEmpty()) {
            standardDelv.setText(slot);
        }*/
        if (!addPick.equals("")) {
            String upToNCharacters = addPick.substring(0, Math.min(addPick.length(), 25));
            pickAddress.setText(upToNCharacters);
        }
    }

    private void init() {
        for (int i = 0; i < IMAGES.length; i++)
            ImagesArray.add(IMAGES[i]);

        mPager = (ViewPager) findViewById(R.id.pager);


        mPager.setAdapter(new SlidingImage_Adapter(ActivityPickDetails.this, ImagesArray));


        CirclePageIndicator indicator = (CirclePageIndicator)
                findViewById(R.id.indicator);

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
        switch (v.getId()) {

            case R.id.confirm:
                express = "1";
                expressDelv.getText();
                timeDialog.dismiss();
                break;
            case R.id.cancel:
                express = "0";
                timeDialog.dismiss();
                break;
            case R.id.pick_details:
                Intent pickIntent = new Intent(ActivityPickDetails.this, ActivityFillPickDetails.class);
                startActivity(pickIntent);
                break;
            case R.id.express_delv:
                PopupTime();
                break;
            case R.id.standard_delv:
                Intent stndDelv = new Intent(ActivityPickDetails.this, ActivityStandardPickDelivery.class);
                startActivity(stndDelv);
                break;
            case R.id.next_deliver:
                TodayDate();
                saveData();
                Intent next = new Intent(ActivityPickDetails.this, ActivityDropDetails.class);
                startActivity(next);
                break;
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ADD_INFO_PICK_POINT, detailsPick.getText().toString());
        editor.apply();

    }

    int hour = 0;
    int min = 0;
    String format = "AM";

    private void PopupTime() {
        timeDialog.setContentView(R.layout.popup_time);
        TimePicker simpleTimePicker = timeDialog.findViewById(R.id.simpleTimePicker);
        TextView confirm = (TextView) timeDialog.findViewById(R.id.confirm);
        TextView cancel = (TextView) timeDialog.findViewById(R.id.cancel);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);

        simpleTimePicker.setIs24HourView(false); // used to display AM/PM mode
        simpleTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay < 7.59) {
                    simpleTimePicker.setHour(8);
                    simpleTimePicker.setMinute(0);
                    hour = simpleTimePicker.getHour();
                    min = simpleTimePicker.getMinute();
                } else if (hourOfDay > 19.59) {
                    simpleTimePicker.setHour(20);
                    simpleTimePicker.setMinute(0);
                    hour = simpleTimePicker.getHour();
                    min = simpleTimePicker.getMinute();
                }

                /*if (hour == 0) {

                    hour += 12;

                    format = "AM";
                } else if (hour == 12) {

                    format = "PM";

                } else if (hour > 12) {

                    hour -= 12;

                    format = "PM";

                } else {

                    format = "AM";
                }*/

                //expressDelv.setText(hour + ":" + min + format);

                String str_minute = "";

                /*if (hour>12){
                    hour = hour - 12;
                    format = "PM";
                }else {
                    format="AM";
                }*/

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

                expressDelv.setText(hourOfDay + ":" + str_minute);
               /* if (express.equals("1")) {*/
                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PICK_HOUR, Integer.toString(hourOfDay));
                    editor.putString(PICK_MINUTE, str_minute);
                    editor.putString(EXPRESS, "1");
                    editor.apply();

                    Log.d(TAG,"hour="+hourOfDay+" min = "+str_minute);
                /*}*/
            }
        });

        timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = timeDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        timeDialog.show();
        Window window = timeDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        timeDialog.setCanceledOnTouchOutside(true);

    }


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityPickDetails.this, ActivityPackageDetails.class));
        finish();
    }
}
