package com.client.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.client.ActivityDrawer;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityFillDropAddress extends ActivityDrawer implements View.OnClickListener {
    TextView txtAddress;
    EditText pinCode, landmark, mobile, name;
    ImageButton confirm, nextPin;
    private static final String TAG = "ActivityFillDropAddress";

    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AN_KEY = "AadharKey";
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String DROP_NAME = "com.client.ride.DropName";

    String stringAuth,  stringAN;
    SharedPreferences prefAuth;
    String imgBtnConfirm = "";
    Dialog myDialog;
    private static ActivityFillDropAddress instance;
    Vibrator vibrator;
    TextView namePin;
    String placeName, lat, remainder;
    ProgressBar simpleProgressBar;
    LinearLayout addressDetails;
    Map<String, String> params = new HashMap();
    ActivityFillDropAddress a = ActivityFillDropAddress.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_fill_drop_address, null, false);
        frameLayout.addView(activityView);

        instance = this;

        //initializing views
        txtAddress = findViewById(R.id.txt_address);
        //buildingAddress = findViewById(R.id.address);
        namePin = findViewById(R.id.textPlace);
        nextPin = findViewById(R.id.next_pin);
        nextPin.setOnClickListener(this);
        pinCode = findViewById(R.id.pin_code);
        name = findViewById(R.id.name_person);
        landmark = findViewById(R.id.landmark);
        mobile = findViewById(R.id.mobile);
        confirm = findViewById(R.id.confirm_address);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        addressDetails = findViewById(R.id.address_details);
        confirm.setOnClickListener(this);
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");


        if (imgBtnConfirm.equals("false")) {
            Log.d(TAG, "confirm.setEnabled(false)");
        } else
            Log.d(TAG, "confirm.setEnabled(true)");

        myDialog = new Dialog(this);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyD61UBJv3DR1fcTzHg3U7FgSYFz9vBX3fk");

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        //autocompleteFragment.setHint("FLAT, FLOOR, BUILDING NAME");

        EditText etPlace = (EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHint("DROP TO");
        etPlace.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etPlace.setTextColor(Color.WHITE);
        etPlace.setPadding(0, 0, 150, 0);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //txtView.setText(place.getName()+","+place.getId());
                placeName = place.getName();
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());

                String latlng = Objects.requireNonNull(place.getLatLng()).toString();
                lat = latlng.substring(0, latlng.indexOf(",")).replaceAll("[^0-9.]", "");
                remainder = latlng.substring(latlng.indexOf(",") + 1).replaceAll("[^0-9.]", "");

                Log.d(TAG, "lat: " + lat);
                Log.d(TAG, "lng: " + remainder);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.confirm_address:

                SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(DROP_LAT, lat);
                editor.putString(DROP_LNG, remainder);
                editor.putString(ADDRESS_DROP, placeName);
                editor.putString(DROP_PIN, pinCode.getText().toString());
                editor.putString(DROP_LANDMARK, landmark.getText().toString());
                editor.putString(DROP_MOBILE, mobile.getText().toString());
                editor.putString(DROP_NAME, name.getText().toString());
                editor.apply();

                Intent addIntent = new Intent(ActivityFillDropAddress.this, ActivityDeliveryTimeSlot.class);
                startActivity(addIntent);
                break;
            case R.id.next_pin:
                locationName();
                break;
        }
    }

    public void locationName() {
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("pin", pinCode.getText().toString());

        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "Values: auth=" + params);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-location-name-from-pin");
        UtilityApiRequestPost.doPOST(a, "auth-location-name-from-pin", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public static ActivityFillDropAddress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsArrayRequest", "RESPONSE:" + response.toString());

        if (id == 2) {
            String name = response.getString("name");
            if (name.equals("This PIN code is not serviceable yet.")) {
                namePin.setText(R.string.pin_not_serviceable);
                addressDetails.setVisibility(View.GONE);
            } else {
                addressDetails.setVisibility(View.VISIBLE);
                pinCode.setEnabled(false);
                namePin.setText(name);
                nextPin.setVisibility(View.GONE);
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityFillDropAddress.this, ActivityFillPickDetails.class));
        finish();
    }
}
