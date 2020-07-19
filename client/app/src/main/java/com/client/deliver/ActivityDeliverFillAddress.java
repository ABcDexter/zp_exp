package com.client.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.UtilityPollingService;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityDeliverFillAddress extends AppCompatActivity implements View.OnClickListener {
    TextView txtAddress;
    EditText pinCode, landmark, mobile, name;
    String choose = "";
    ImageButton confirm;
    private static final String TAG = "ActivityDeliverFillAddress";

    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AN_KEY = "AadharKey";
    public static final String AUTH_KEY = "AuthKey";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String PICK_LAT = "com.client.delivery.PickLatitude";
    public static final String PICK_LNG = "com.client.delivery.PickLongitude";
    public static final String DROP_LAT = "com.client.delivery.PickLatitude";
    public static final String DROP_LNG = "com.client.delivery.DropLongitude";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";
    public static final String PICK_NAME = "com.client.ride.PickName";
    public static final String DROP_NAME = "com.client.ride.DropName";

    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.delivery.BussFlag";

    String stringAuth, stringBuss, bussFlag, stringAN;
    SharedPreferences prefAuth, prefBuss;
    String imgBtnConfirm = "";
    Dialog myDialog;
    private static ActivityDeliverFillAddress instance;
    Vibrator vibrator;
    TextView reject_rq, accept_rq, dialog_txt;
    String placeName, lat, remainder;
    ProgressBar simpleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_fill_address);
        instance = this;

        //initializing views
        txtAddress = findViewById(R.id.txt_address);
        //buildingAddress = findViewById(R.id.address);
        pinCode = findViewById(R.id.pin_code);
        name = findViewById(R.id.name_person);
        landmark = findViewById(R.id.landmark);
        mobile = findViewById(R.id.mobile);
        confirm = findViewById(R.id.confirm_address);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        confirm.setOnClickListener(this);
        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");
        stringAN = prefAuth.getString(AN_KEY, "");
        prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        String fillRequest = intent.getStringExtra("FillPick");

        assert fillRequest != null;
        if (fillRequest.equals("pick")) {
            choose = "pick";
            txtAddress.setText("PICKUP ADDRESS");

        } else if (fillRequest.equals("drop")) {
            choose = "drop";
            txtAddress.setText("Delivery Address");
        }
        if (imgBtnConfirm.equals("false")) {
            Log.d(TAG, "confirm.setEnabled(false)");
        } else
            Log.d(TAG, "confirm.setEnabled(true)");

        myDialog = new Dialog(this);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyD61UBJv3DR1fcTzHg3U7FgSYFz9vBX3fk");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        //autocompleteFragment.setHint("FLAT, FLOOR, BUILDING NAME");
        /*View fView = autocompleteFragment.getView();
        EditText editText = fView.findViewById(R.id.place_autocomplete_search_input);
        editText.setTextColor(Color.WHITE);*/
        EditText etPlace = (EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHint("FLAT, BUILDING NAME");
        etPlace.setTextColor(Color.WHITE);
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
            case R.id.reject_request:
                bussFlag = "BussMeNot";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                Log.d(TAG, "User not interested in a buss");
                myDialog.dismiss();
                break;
            case R.id.accept_request:
                bussFlag = "BussMe";
                prefBuss.edit().putString(BUSS, bussFlag).apply();
                getAgent();
                myDialog.dismiss();
                break;
            case R.id.confirm_address:
                if (choose.equals("pick")) {
                    SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(PICK_LAT, lat);
                    editor.putString(PICK_LNG, remainder);
                    editor.putString(ADDRESS_PICK, placeName);
                    editor.putString(PICK_PIN, pinCode.getText().toString());
                    editor.putString(PICK_LANDMARK, landmark.getText().toString());
                    editor.putString(PICK_MOBILE, mobile.getText().toString());
                    editor.putString(PICK_NAME, name.getText().toString());
                    editor.apply();
                    Log.d(TAG, "choose = pick");
                    getAgent();
                }
                if (choose.equals("drop")) {
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
                    Log.d(TAG, "choose = drop");

                    Intent addIntent = new Intent(ActivityDeliverFillAddress.this, ActivityDeliverHome.class);
                    startActivity(addIntent);
                }
                break;

        }

    }

    public void getAgent() {
        Map<String, String> params = new HashMap();
        String auth = stringAuth;
        params.put("auth", auth);
        params.put("srclat", lat);
        params.put("srclng", remainder);
        JSONObject parameters = new JSONObject(params);
        ActivityDeliverFillAddress a = ActivityDeliverFillAddress.this;

        Log.d(TAG, "Values: auth=" + params);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME user-is-agent-av");
        UtilityApiRequestPost.doPOST(a, "user-is-agent-av", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public static ActivityDeliverFillAddress getInstance() {
        return instance;
    }

    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsArrayRequest", "RESPONSE:" + response.toString());
        //response on hitting user-is-agent-av API
        if (id == 1) {
            prefBuss = getSharedPreferences(BUSS_FLAG, Context.MODE_PRIVATE);
            stringBuss = prefBuss.getString(BUSS, "");
            String responseS = response.toString();
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("agents");
            if (array.length() == 0) {
                confirm.setEnabled(false);//the user cannot go to the next activity if vehicle not available at the hub
                imgBtnConfirm = "false";
                Log.d(TAG, "confirmRentButton.setEnabled(false)");
                if (stringBuss.equals("BussMeNot")) {
                    Log.d(TAG, "user not interested in notifications");
                    SharedPreferences prefBuzz = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = prefBuzz.edit();
                    editor1.remove(BUSS_FLAG);
                    editor1.apply();
                    Toast.makeText(this, "No Delivery Agents Available Currently!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ActivityDeliverFillAddress.this, ActivityDeliverHome.class);
                    startActivity(intent);
                    finish();

                } else if (stringBuss.equals("BussMe")) {
                    simpleProgressBar.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(this, UtilityPollingService.class);
                    intent.setAction("31");
                    startService(intent);
                } else
                    ShowPopup(1);
            } else if (array.length() > 0) {
                simpleProgressBar.setVisibility(View.GONE);

                ShowPopup(2);
                SharedPreferences pref = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(BUSS);
                editor.apply();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject vehicle = array.getJSONObject(i);
                    //String an = vehicle.getString("an");
                }
                Intent addIntent = new Intent(ActivityDeliverFillAddress.this, ActivityDeliverHome.class);
                startActivity(addIntent);
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    private void ShowPopup(int id) {

        myDialog.setContentView(R.layout.popup_new_request);
        reject_rq = myDialog.findViewById(R.id.reject_request);
        accept_rq = myDialog.findViewById(R.id.accept_request);
        dialog_txt = myDialog.findViewById(R.id.info_text);
        LinearLayout ln = myDialog.findViewById(R.id.layout_btn);
        if (id == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
            ln.setVisibility(View.VISIBLE);
            dialog_txt.setText("no delivery agents available currently.\nNotify me when available.");
            reject_rq.setOnClickListener(this);
            accept_rq.setOnClickListener(this);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
            myDialog.setCanceledOnTouchOutside(false);
        }
        if (id == 2) {
            //TODO send push notification
            dialog_txt.setText("Delivery Agents are available.");
            myDialog.setCanceledOnTouchOutside(true);

        }
    }

}
