package com.clientzp.deliver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.clientzp.ActivityDrawer;
import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityFillPickDetails extends ActivityDrawer implements View.OnClickListener {
    TextView txtAddress;
    EditText pinCode, edPickAddress, mobile, name;
    String choose = "";
    ImageButton confirm, nextPin;
    private static final String TAG = "ActivityFillDropAddress";

    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AN_KEY = "AadharKey";
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_ADDRESS = "com.clientzp.ride.Address";
    public static final String ADDRESS_PICK = "com.clientzp.ride.AddressPick";
    public static final String PICK_LAT = "com.clientzp.delivery.PickLatitude";
    public static final String PICK_LNG = "com.clientzp.delivery.PickLongitude";
    public static final String PICK_LANDMARK = "com.clientzp.ride.PickLandmark";
    public static final String PICK_PIN = "com.clientzp.ride.PickPin";
    public static final String PICK_MOBILE = "com.clientzp.ride.PickMobile";
    public static final String PICK_NAME = "com.clientzp.ride.PickName";
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.clientzp.delivery.BussFlag";

    String stringAuth, stringAN;
    SharedPreferences prefAuth, prefBuss;
    String imgBtnConfirm = "";
    Dialog myDialog;
    private static ActivityFillPickDetails instance;
    Vibrator vibrator;
    TextView namePin;
    String placeName, lat, remainder;
    ProgressBar simpleProgressBar;
    LinearLayout addressDetails;
    Map<String, String> params = new HashMap();
    ActivityFillPickDetails a = ActivityFillPickDetails.this;
    ImageButton info_pin, info_name, info_place, info_landmark, info_mobile;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_fill_pick_address, null, false);
        frameLayout.addView(activityView);

        instance = this;

        //initializing views
        txtAddress = findViewById(R.id.txt_address);
        //buildingAddress = findViewById(R.id.address);
        pinCode = findViewById(R.id.pin_code);
        namePin = findViewById(R.id.textPlace);
        nextPin = findViewById(R.id.next_pin);
        nextPin.setOnClickListener(this);
        name = findViewById(R.id.name_person);
        edPickAddress = findViewById(R.id.ed_pick_address);
        mobile = findViewById(R.id.mobile);
        confirm = findViewById(R.id.confirm_address);
        info_pin = findViewById(R.id.infoPickPin);
        info_place = findViewById(R.id.infoPickAddress);
        info_landmark = findViewById(R.id.infoLand);
        info_name = findViewById(R.id.infoName);
        info_mobile = findViewById(R.id.infoMobile);
        info_pin.setOnClickListener(this);
        info_place.setOnClickListener(this);
        info_landmark.setOnClickListener(this);
        info_name.setOnClickListener(this);
        info_mobile.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        addressDetails = findViewById(R.id.address_details);
        confirm.setOnClickListener(this);
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);

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

        EditText etPlace = autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHint(R.string.landmark);
        etPlace.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etPlace.setTextColor(Color.WHITE);
        etPlace.setPadding(0, 0, 150, 0);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setCountry("IN"); // restrict search to country "INDIA"


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
        int id = v.getId();
        if (id == R.id.confirm_address) {
            String str_name = name.getText().toString();
            String str_mobile = mobile.getText().toString();
            String str_landmark = edPickAddress.getText().toString();
            if (TextUtils.isEmpty(placeName)) {
                Toast.makeText(instance, R.string.mandatory_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(str_landmark)) {
                edPickAddress.setError(getString(R.string.this_mandatory_field));
                return;
            }
            if (TextUtils.isEmpty(str_name)) {
                name.setError(getString(R.string.this_mandatory_field));
                return;
            }

            if (TextUtils.isEmpty(str_mobile)) {
                mobile.setError(getString(R.string.this_mandatory_field));
                return;
            }
            if (lat.isEmpty()) {
                Toast.makeText(this, R.string.pick_point_land, Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(PICK_LAT, lat);
                editor.putString(PICK_LNG, remainder);
                editor.putString(ADDRESS_PICK, edPickAddress.getText().toString());
                editor.putString(PICK_PIN, pinCode.getText().toString());
                editor.putString(PICK_LANDMARK, placeName);
                editor.putString(PICK_MOBILE, mobile.getText().toString());
                editor.putString(PICK_NAME, name.getText().toString());
                Log.d(TAG, "&&&&&&&" + "PICK_LANDMARK: " + placeName + " ADDRESS_PICK:" + edPickAddress.getText().toString());
                editor.apply();
                Intent intent = new Intent(ActivityFillPickDetails.this, ActivityFillDropAddress.class);
                startActivity(intent);
                finish();
            }
        } else if (id == R.id.next_pin) {
            String pin_code = pinCode.getText().toString();
            if (TextUtils.isEmpty(pin_code)) {
                pinCode.setError(getString(R.string.this_mandatory_field));
                return;
            } else {
                addressAlert();
            }
        } else if (id == R.id.infoPickPin) {
            ShowPopup(1);
        } else if (id == R.id.infoPickAddress) {
            ShowPopup(2);
        } else if (id == R.id.infoLand) {
            ShowPopup(3);
        } else if (id == R.id.infoName) {
            ShowPopup(4);
        } else if (id == R.id.infoMobile) {
            ShowPopup(5);
        }

    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        TextView infoText = myDialog.findViewById(R.id.info_text);

        if (id == 1) {
            infoText.setText(R.string.pick_point_pin);
        }
        if (id == 2) {
            infoText.setText(R.string.pick_point_place);
        }
        if (id == 3) {
            infoText.setText(R.string.pick_point_land);
        }
        if (id == 4) {
            infoText.setText(R.string.pick_point_name);
        }
        if (id == 5) {
            infoText.setText(R.string.pick_point_mobile);
        }
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(true);
    }

    private void addressAlert() {
        // Create the object of
        // AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityFillPickDetails.this);
        // Set the message show for the Alert time
        builder.setMessage(R.string.address_alert);
        // Set Alert Title
        builder.setTitle(R.string.please_note);
        // Set Cancelable false
        // for when the user clicks on the outside
        // the Dialog Box then it will remain show
        builder.setCancelable(false);
        // Set the positive button with ok name
        // OnClickListener method is use of
        // DialogInterface interface.
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationName();
                dialog.cancel();
            }
        });

        // Set the Negative button with No name
        // OnClickListener method is use
        // of DialogInterface interface.
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user click cancel
                // then user goes to the previous window
                Intent back = new Intent(ActivityFillPickDetails.this, ActivityWelcome.class);
                startActivity(back);
                finish();
            }
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC7721")));
        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonNegative.setTextColor(ContextCompat.getColor(this, R.color.Black));
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

    public static ActivityFillPickDetails getInstance() {
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
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityFillPickDetails.this, ActivityPackageDetails.class));
        finish();
    }
}
