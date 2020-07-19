package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, View.OnClickListener {
    private static final String TAG = "MapsActivity2";

    private GoogleMap mMap;
    private MarkerOptions src, dst;
    Button getDirection;
    private Polyline currentPolyline;
    double srcLat, srcLng, dstLat, dstLng;
    public static final String TRIP_DETAILS = "com.driver.tripDetails";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.agent.cookie";
    public static final String TID = "RideID";
    public static final String SRCLAT = "TripSrcLat";
    public static final String SRCLNG = "TripSrcLng";
    public static final String DSTLAT = "TripDstLat";
    public static final String DSTLNG = "TripDstLng";

    String strAuth, strTid, strSrcLat, strSrcLng;
    MapsActivity2 a = MapsActivity2.this;
    Map<String, String> params = new HashMap();
    Button end,fail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");

        SharedPreferences pref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        strTid = pref.getString(TID, "");
        String stringSrcLat = pref.getString(SRCLAT, "");
        String stringSrcLng = pref.getString(SRCLNG, "");
        String stringDstLat = pref.getString(DSTLAT, "");
        String stringDstLng = pref.getString(DSTLNG, "");
        getStatus();

        srcLat = Double.parseDouble(stringSrcLat);
        srcLng = Double.parseDouble(stringSrcLng);
        dstLat = Double.parseDouble(stringDstLat);
        dstLng = Double.parseDouble(stringDstLng);


        end = findViewById(R.id.completedRide);
        fail = findViewById(R.id.failedRide);

        end.setOnClickListener(this);
        fail.setOnClickListener(this);
        src = new MarkerOptions().position(new LatLng(srcLat, srcLng)).title("Pick Up");
        dst = new MarkerOptions().position(new LatLng(dstLat, dstLng)).title("Destination");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);

        new FetchURL(MapsActivity2.this).execute(getUrl(src.getPosition(), dst.getPosition(), "driving"), "driving");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");
        mMap.addMarker(src);
        mMap.addMarker(dst);

        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(srcLat, srcLng))
                .zoom(7)
                .bearing(0)
                .tilt(45)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 5000, null);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    public void rideFail() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-fail");
        UtilityApiRequestPost.doPOST(a, "auth-trip-fail", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void rideEnd() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-end");
        UtilityApiRequestPost.doPOST(a, "driver-ride-end", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 3);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
        //response on hitting driver-ride-get-status API
        if (id == 1) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String did = response.getString("did");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TID, did).apply();

                    if (status.equals("ST")) {
                        String dstLat1 = response.getString("dstlat");
                        String dstLng1 = response.getString("dstlng");
                        dstLat = Double.parseDouble(dstLat1);
                        dstLng = Double.parseDouble(dstLng1);
                        SharedPreferences delvyPref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                        delvyPref.edit().putString(DSTLAT, dstLat1).apply();
                        delvyPref.edit().putString(DSTLNG, dstLng1).apply();

                    }
                    /*Intent home = new Intent(MapsActivity2.this, ActivityHome.class);
                    startActivity(home);
                    finish();*/
                } else if (active.equals("false")) {
                    Intent home = new Intent(MapsActivity2.this, ActivityHome.class);
                    startActivity(home);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (id==2){
            Intent home = new Intent(MapsActivity2.this, ActivityHome.class);
            startActivity(home);
        }
        if (id==3){
            Intent home = new Intent(MapsActivity2.this, ActivityHome.class);
            startActivity(home);
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.completedRide:
                rideEnd();
                break;
            case R.id.failedRide:
                rideFail();
                break;
            case R.id.map:
                /*Intent map = new Intent(ActivityInProgress.this, ActivityMap.class);
                startActivity(map);*/
                Toast.makeText(this, "Map will open", Toast.LENGTH_LONG).show();
                break;
        }
    }
}