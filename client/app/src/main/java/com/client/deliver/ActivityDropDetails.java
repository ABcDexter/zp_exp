package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityDropDetails extends ActivityDrawer implements View.OnClickListener {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static final Integer[] IMAGES = {R.drawable.delivery_man, R.drawable.packed_parcel, R.drawable.delivery_man, R.drawable.packed_parcel};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();
    TextView dropAddress;
    Button timeSlot;
    String lat, lng, stringAuth, stringAN, addDrop, dropLat, dropLng, dropLand, dropPin, dropMobile,
            pickLat, pickLng, conType, conSize, express;
    SharedPreferences prefAuth;
    EditText detailsDrop;
    ImageButton next;
    private static final String TAG = "ActivityDropDetails";

    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String EXPRESS = "Express";
    public static final String ADD_INFO_DROP_POINT = "AddInfoDropPoint";

    ActivityDropDetails a = ActivityDropDetails.this;
    Map<String, String> params = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_drop_details, null, false);
        frameLayout.addView(activityView);
        init();

        //retrieving locally stored data
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
        express = pref.getString(EXPRESS, "");

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        next = findViewById(R.id.next_deliver);
        next.setOnClickListener(this);
        dropAddress = findViewById(R.id.drop_details);
        detailsDrop = findViewById(R.id.drop_add_details);
        dropAddress.setOnClickListener(this);
        if (!addDrop.equals("")) {

            String upToNCharacters = addDrop.substring(0, Math.min(addDrop.length(), 25));
            dropAddress.setText(upToNCharacters);
        }
        timeSlot = findViewById(R.id.standard_drop_delv);
        timeSlot.setOnClickListener(this);
    }

    private void init() {
        for (int i = 0; i < IMAGES.length; i++)
            ImagesArray.add(IMAGES[i]);

        mPager = (ViewPager) findViewById(R.id.pager);


        mPager.setAdapter(new SlidingImage_Adapter(ActivityDropDetails.this, ImagesArray));


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

    private void deliveryEstimate() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", pickLat);
        params.put("srclng", pickLng);
        params.put("dstlat", dropLat);
        params.put("dstlng", dropLng);
        params.put("itype", conType);
        params.put("idim", conSize);
        params.put("express", express);//0,1
        params.put("pmode", "1");


        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " srclat= " + pickLat
                + " srclng= " + pickLng + " dstlat=" + dropLat + " dstlng=" + dropLng
                + " itype= " + conType + " idim= " + conSize + " express= " + express + " pmode= " + "1");
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: user-delivery-estimate");

        UtilityApiRequestPost.doPOST(a, "user-delivery-estimate", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drop_details:
                Intent dropIntent = new Intent(ActivityDropDetails.this, ActivityFillDropAddress.class);
                startActivity(dropIntent);
                break;
            case R.id.standard_drop_delv:
                Intent dropDelv = new Intent(ActivityDropDetails.this, ActivityStandardDropDelivery.class);
                startActivity(dropDelv);
                break;
            case R.id.next_deliver:
                saveData();
                deliveryEstimate();
                break;
        }
    }
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ADD_INFO_DROP_POINT, detailsDrop.getText().toString());
        editor.apply();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDropDetails.this, ActivityPickDetails.class));
        finish();
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        //response on hitting user-delivery-estimate API
        Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        if (id == 1) {
            String price = response.getString("price");
            String time = response.getString("time");
            // String price = response.getString("price");
            Intent confirm = new Intent(ActivityDropDetails.this, ActivityDeliverConfirm.class);
            confirm.putExtra("PRICE", price);
            startActivity(confirm);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
}
