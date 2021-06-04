package com.zp_driver;

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

    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    Vibrator vibrator;
    Dialog myDialog;
    private static VehicleList instance;

    private RecyclerView rv;
    private List<VehicleListData> list_data;
    private VehicleListAdapter adapter;
    String TID;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String DAY_VAN = "com.driver.Van";
    public static final String VAN = "Van";
    public static final String TRIP_ID = "TripId";
    String auth;
    Map<String, String> params = new HashMap();
    VehicleList a = VehicleList.this;


    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);
        instance = this;
        SharedPreferences prefPLoc = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        String stringAuth = prefPLoc.getString(AUTH_KEY, "");
        auth = stringAuth;
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
            SharedPreferences prefVan = getSharedPreferences(DAY_VAN, Context.MODE_PRIVATE);
            prefVan.edit().putString(VAN, van).apply();

            //assert van != null;
            if (!van.isEmpty()) {
                Log.d(TAG, "VAN=" + van);
                vehicleSet(van);
            }
        } catch (Exception e) {
            Log.d(TAG, "Error:" + e.getMessage());
            getData();//method to hit auth-vehicle-get-avail API
        }
    }

    public static VehicleList getInstance() {
        return instance;
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

    //method to check if vehicles are available near the location of driver
    protected void getData() {
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost auth-vehicle-get-avail");
        UtilityApiRequestPost.doPOST(a, "auth-vehicle-get-avail", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully

    }

    protected void vehicleSet(String van) {

        params.put("auth", auth);
        params.put("van", van);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " van=" + van);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost driver-vehicle-set");
        UtilityApiRequestPost.doPOST(a, "driver-vehicle-set", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 2);// call this method if api was hit successfully
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);// call this method if api was hit unsuccessfully

    }

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

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //response on hitting driver-vehicle-set API
        if (id == 2) {

            Intent home = new Intent(VehicleList.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(a, R.string.something_wrong, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onErrorResponse: " + error.toString());
        Toast.makeText(this, "CHECK YOUR INTERNET CONNECTION!", Toast.LENGTH_LONG).show();
    }

}
