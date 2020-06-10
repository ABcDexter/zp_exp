package com.example.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleList extends AppCompatActivity {
    private static final String TAG = "VehicleList.class";

    public static final String AUTH_COOKIE = "com.driver.cookie";
    public static final String COOKIE = "Cookie";
    public static final String BUSS = "Buss";
    public static final String BUSS_FLAG = "com.client.ride.BussFlag";
    Vibrator vibrator;
    Dialog myDialog;
    private static VehicleList instance;

    private RecyclerView rv;
    private List<VehicleListData> list_data;
    private VehicleListAdapter adapter;
    String TID;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String TRIP_ID = "TripId";
    public static final String TRIP_NAME = "TripName";
    public static final String TRIP_SRC = "TripSrc";
    public static final String TRIP_DST = "TripDst";
    public static final String TRIP_PHN = "TripPhn";


    public void onSuccess(JSONObject response, int id) throws JSONException {
        Log.d(TAG + "jsArrayRequest", "RESPONSE:" + response.toString());
        if (id == 1) {
            String responseS = response.toString();
            try {
                //converting JSONObject to JSONArray
                JSONObject jsonObject = new JSONObject(responseS);
                JSONArray array = jsonObject.getJSONArray("vehicles");
                if (array.length() > 0) {
                    //display list of only 10 results
                    for (int i = 0; i < 10; i++) {
                        JSONObject ob = array.getJSONObject(i);
                        VehicleListData ld = new VehicleListData(ob.getString("an"), ob.getString("regn"),
                                ob.getString("vtype"));
                        list_data.add(ld); //adding data to list in adapter
                        SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        sp_cookie.edit().putString(TRIP_ID, TID).apply();//saving tid locally
                    }
                    rv.setAdapter(adapter);// setting adapter to recycler view
                    ShowPopup();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(BUSS_FLAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(BUSS);
                    editor.apply();// clearing the buss flag
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (id == 2) {
            // getting data from server
            String name = response.getString("name");
            String phone = response.getString("phone");
            String src = response.getString("srcname");
            String dst = response.getString("dstname");
            //saving data locally
            SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
            sp_cookie.edit().putString(TRIP_NAME, name).apply();
            sp_cookie.edit().putString(TRIP_PHN, phone).apply();
            sp_cookie.edit().putString(TRIP_SRC, src).apply();
            sp_cookie.edit().putString(TRIP_DST, dst).apply();
            // calling ActivityRideAccepted Activity
            Intent accepted = new Intent(VehicleList.this, ActivityRideAccepted.class);
            startActivity(accepted);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Toast.makeText(this, "CHECK YOUR INTERNET CONNECTION!", Toast.LENGTH_LONG).show();
    }

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);
        instance = this;
        //initializing variables
        rv = findViewById(R.id.recycler_view);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        myDialog = new Dialog(this);

        //getting value of tid from previous activity ActivityHome
        Intent intent = getIntent();
        TID = intent.getStringExtra("TID");

        //loading list view item with this function
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new VehicleListAdapter(list_data, VehicleList.this);

        //enter this only if this activity is called from VehicleListAdapter
        try {
            Intent values = getIntent();
            String van = values.getStringExtra("VAN");
            assert van != null;
            if (!van.isEmpty()) {
                Log.d(TAG, "VAN=" + van);
                driverRideAccept(van);// method to hit driver-ride-accept API
            }
        } catch (Exception e) {
            Log.d(TAG, "Error:" + e.getMessage());
            getData();//method to hit auth-vehicle-get-avail API
        }
    }

    public static VehicleList getInstance() {
        return instance;
    }

    //method to check if vehicles are available near the location of driver
    protected void getData() {
        Map<String, String> params = new HashMap();
        SharedPreferences prefPLoc = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        String stringAuth = prefPLoc.getString(COOKIE, "");
        params.put("auth", stringAuth);
        JSONObject parameters = new JSONObject(params);
        VehicleList a = VehicleList.this;
        Log.d(TAG, "Values: auth=" + stringAuth);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost auth-vehicle-get-avail");
        UtilityApiRequestPost.doPOST(a, "auth-vehicle-get-avail", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully

    }

    //method to accept ride request of user by driver
    private void driverRideAccept(String van) {
        Map<String, String> params = new HashMap();
        //retrieve stored values stored locally
        SharedPreferences prefPLoc = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        String stringAuth = prefPLoc.getString(COOKIE, "");
        SharedPreferences pref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        String tid = pref.getString(TRIP_ID, "");
        params.put("auth", stringAuth);
        params.put("tid", tid);
        params.put("van", van);

        JSONObject parameters = new JSONObject(params);
        VehicleList a = VehicleList.this;
        Log.d(TAG, "Values: auth=" + stringAuth + " tid=" + tid + " van=" + van);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost driver-ride-accept");
        UtilityApiRequestPost.doPOST(a, "driver-ride-accept", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully
    }

    //method to initiate and populate dialog box
    private void ShowPopup() {
        myDialog.setContentView(R.layout.popup_text);
        //vibrate device for 1000 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(1000);
        }
    }
}
