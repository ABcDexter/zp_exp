package com.clientzp.ride;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
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

public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private static final String TAG = "MapsActivity2";

    private GoogleMap mMap;
    private MarkerOptions src, dst;
    private Polyline currentPolyline;
    double srcLat, srcLng, dstLat, dstLng;
    public static final String PREFS_LOCATIONS = "com.clientzp.ride.Locations";
    public static final String SRC_LNG = "SrcLng";
    public static final String SRC_LAT = "SrcLat";
    public static final String DST_LAT = "DropLat";
    public static final String DST_LNG = "DropLng";
    String srcName, dstName;
    String stringAuthKey, stringTID;
    public static final String TRIP_ID = "TripID";
    public static final String TRIP_DETAILS = "com.clientzp.ride.TripDetails";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";
    MapsActivity2 a = MapsActivity2.this;
    Map<String, String> params = new HashMap();
    String sLat, sLng, dLat, dLng, srcname, dstname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        SharedPreferences prefTrip = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        stringTID = prefTrip.getString(TRIP_ID, "");

        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        authTripData();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");
        mMap.addMarker(src);
        mMap.addMarker(dst);

        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(srcLat, srcLng))
                .zoom(12)//zoom level
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

    protected void authTripData() {
        String auth = stringAuthKey;
        String tid = stringTID;
        params.put("auth", auth);
        params.put("tid", tid);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost.doPOST API NAME: auth-delivery-data");
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);

        UtilityApiRequestPost.doPOST(a, "auth-trip-data", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-trip-data API
        if (id == 2) {

            sLat = response.getString("srclat");
            sLng = response.getString("srclng");
            dLat = response.getString("dstlat");
            dLng = response.getString("dstlng");
            srcname = response.getString("srchub");
            dstname = response.getString("dsthub");

            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapNearBy);
            mapFragment.getMapAsync(this);

            srcLat = Double.parseDouble(sLat);
            srcLng = Double.parseDouble(sLng);
            dstLat = Double.parseDouble(dLat);
            dstLng = Double.parseDouble(dLng);

            src = new MarkerOptions().position(new LatLng(srcLat, srcLng)).title(srcname);
            dst = new MarkerOptions().position(new LatLng(dstLat, dstLng)).title(dstname);

            new FetchURL(MapsActivity2.this).execute(getUrl(src.getPosition(), dst.getPosition(), "driving"), "driving");

        }
    }


    public void onFailure(VolleyError error) {
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }
}