package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.client.ActivityDrawer;
import com.client.R;

public class ActivityDeliverHome extends ActivityDrawer implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    TextView pickAddress, dropAddress;
    ImageButton confirm;
    Spinner content, size;
String ContentType, ContentSize;
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_deliver_home, null, false);
        frameLayout.addView(activityView);

        //initializing vies
        pickAddress = findViewById(R.id.txt_pick_address);
        dropAddress = findViewById(R.id.txt_drop_address);
        confirm = findViewById(R.id.confirm_deliver);
        content = findViewById(R.id.content_type);
        size = findViewById(R.id.content_size);
        confirm.setOnClickListener(this);
        pickAddress.setOnClickListener(this);
        dropAddress.setOnClickListener(this);

        //retrieving locally stored data
//retrieving locally stored data
        SharedPreferences pref = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        String addPick = pref.getString(ADDRESS_PICK, "");
        String addDrop = pref.getString(ADDRESS_DROP, "");
        String pickLand = pref.getString(PICK_LANDMARK, "");
        String dropLand = pref.getString(DROP_LANDMARK, "");
        String pickPin = pref.getString(PICK_PIN, "");
        String dropPin = pref.getString(DROP_PIN, "");
        String pickMobile = pref.getString(PICK_MOBILE, "");
        String dropMobile = pref.getString(DROP_MOBILE, "");

        if (!addPick.equals("")) {
            pickAddress.setText(addPick + ", " + pickLand);
        }
        if (!addDrop.equals("")) {
            dropAddress.setText(addDrop + ", " + dropLand);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.content_array)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.WHITE);
                    tv.setBackgroundColor(Color.DKGRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item_orange);
        content.setAdapter(adapter);
        content.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapterNoRiders = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, getResources().getStringArray(R.array.size_array)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.WHITE);
                    tv.setBackgroundColor(Color.DKGRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        adapterNoRiders.setDropDownViewResource(R.layout.spinner_item_blue);
        size.setAdapter(adapterNoRiders);
        size.setOnItemSelectedListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_pick_address:
                Intent pickIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverFillAddress.class);
                pickIntent.putExtra("FillPick", "pick");
                startActivity(pickIntent);
                break;
            case R.id.txt_drop_address:
                Intent dropIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverFillAddress.class);
                dropIntent.putExtra("FillPick", "drop");
                startActivity(dropIntent);
                break;
            case R.id.confirm_deliver:
                Intent confirmIntent = new Intent(ActivityDeliverHome.this, ActivityDeliverPayment.class);
                startActivity(confirmIntent);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.content_type:
                ContentType = content.getItemAtPosition(position).toString();
                switch (ContentType) {
                    case "DOCUMENTS / BOOKS":
                        ContentType = "0";
                        break;
                    case "CLOTHES / ACCESSORIES":
                        ContentType = "1";
                        break;
                    case "FOOD":
                        ContentType = "2";
                        break;
                    case "HOUSEHOLD":
                        ContentType = "2";
                        break;
                    case "ELECTRONICS / ELECTRICAL ITEMS":
                        ContentType = "2";
                        break;

                }
                break;
            case R.id.content_size:
                ContentSize = size.getItemAtPosition(position).toString();
                switch (ContentSize) {
                    case "S (35 x 25 x 13 cm)":
                        ContentSize = "1";
                        break;
                    case "M (70 x 50 x 26 cm)":
                        ContentSize = "2";
                        break;
                    case "L (105 x 75 x 39 cm)":
                        ContentSize = "3";
                        break;
                    case "XL (104 x 100 x 52 cm)":
                        ContentSize = "4";
                        break;
                    case "XXL (175 x 125 x 65 cm)":
                        ContentSize = "5";
                        break;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
