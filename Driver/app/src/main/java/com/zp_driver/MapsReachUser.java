package com.zp_driver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsReachUser extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, View.OnClickListener {
    private static final String TAG = "MapsReachUser";

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
    public static final String MY_LAT = "MYSrcLAT";
    public static final String MY_LNG = "MYSrcLng";

    public static final String DAY_VAN = "com.driver.Van";
    public static final String VAN = "Van";
    String strAuth, strTid, strSrcLat, strSrcLng, strVan;
    MapsReachUser a = MapsReachUser.this;
    Map<String, String> params = new HashMap();

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};
    FusedLocationProviderClient mFusedLocationClient;
    String lat, lng;
    TextView yes, no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_reach_user);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsReachUser.this);// needed for gsp tracking
        getLastLocation();
        SharedPreferences pref = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
        strTid = pref.getString(TID, "");
        String stringSrcLat = pref.getString(SRCLAT, "");
        String stringSrcLng = pref.getString(SRCLNG, "");
        String stringMySrcLat = pref.getString(MY_LAT, "");
        String stringMySrcLng = pref.getString(MY_LNG, "");

        Log.d(TAG, "SRCLAT=" + stringSrcLat + " SRCLNG=" + stringSrcLng + " MYLAT=" + stringMySrcLat + " MYLNG=" + stringMySrcLng);

        SharedPreferences prefVan = getSharedPreferences(DAY_VAN, Context.MODE_PRIVATE);
        strVan = prefVan.getString(VAN, "");

        srcLat = Double.parseDouble(stringSrcLat);
        srcLng = Double.parseDouble(stringSrcLng);
        dstLat = Double.parseDouble(stringMySrcLat);
        dstLng = Double.parseDouble(stringMySrcLng);
        Log.d(TAG, "srcLat" + srcLat + " srcLng" + srcLng + " dstLat" + dstLat + " dstLng" + dstLng);
//TODO rename it properly as current location and user's location

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, "");

        getStatus();
        src = new MarkerOptions().position(new LatLng(srcLat, srcLng)).title(String.valueOf("PICK UP"));
        dst = new MarkerOptions().position(new LatLng(dstLat, dstLng)).title("You are here");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);

        new FetchURL(MapsReachUser.this).execute(getUrl(src.getPosition(), dst.getPosition(), "driving"), "driving");

        yes = findViewById(R.id.accept_request);
        no = findViewById(R.id.reject_request);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Added Markers");
        mMap.addMarker(src).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mMap.addMarker(dst).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(srcLat, srcLng))
                .zoom(18)
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


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public void getLastLocation() {
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                lat = location.getLatitude() + "";
                                lng = location.getLongitude() + "";
                                Log.d(TAG, "lat = " + lat + " lng = " + lng);
                                SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                                sp_cookie.edit().putString(MY_LAT, lat).apply();
                                sp_cookie.edit().putString(MY_LNG, lng).apply();
                            }
                        }
                    });
        }
    }
    //gps tracking with high accuracy. This means that the location is checked every 1 millisecond

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }
    // with every change in location this method is called

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude() + "";
            lng = mLastLocation.getLongitude() + "";
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else
            getLastLocation();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.accept_request) {
            rideAccept();
        } else if (id == R.id.reject_request) {
            Intent home = new Intent(MapsReachUser.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
    }

    public void rideAccept() {
        String auth = strAuth;
        params.put("auth", auth);
        params.put("tid", strTid);
        params.put("van", strVan);
        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "auth= " + auth + " did= " + strTid + " van=" + strVan);
        Log.d(TAG, "UtilityApiRequestPost.doPOST a driver-ride-accept");
        UtilityApiRequestPost.doPOST(a, "driver-ride-accept", parameters, 30000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, a::onFailure);

    }

    public void getStatus() {
        String auth = strAuth;
        params.put("auth", auth);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME driver-ride-get-status");
        UtilityApiRequestPost.doPOST(a, "driver-ride-get-status", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    public void onSuccess(JSONObject response, int id) throws NegativeArraySizeException {
        //response on hitting driver-ride-accept API
        Log.d(TAG, "RESPONSE:" + response);
        if (id == 1) {
            Intent home = new Intent(MapsReachUser.this, ActivityHome.class);
            startActivity(home);
            finish();
        }
        //response on hitting driver-ride-get-status API
        if (id == 2) {
            try {
                String active = response.getString("active");
                if (active.equals("true")) {
                    String status = response.getString("st");
                    String tid = response.getString("tid");
                    SharedPreferences sp_cookie = getSharedPreferences(TRIP_DETAILS, Context.MODE_PRIVATE);
                    sp_cookie.edit().putString(TID, tid).apply();
                    if (status.equals("AS")) {
                        /*String srcLat = response.getString("srclat");
                        String srcLng = response.getString("srclng");
                        String dstLat = response.getString("dstlat");
                        String dstLng = response.getString("dstlng");
                        String phone = response.getString("uphone");
                        String name = response.getString("uname");*/

                        //getStatus();
                        Log.d(TAG, "stay here status = AS");

                    }

                    Intent home = new Intent(MapsReachUser.this, ActivityHome.class);
                    startActivity(home);

                } else if (active.equals("false")) {
                    Log.d(TAG, "stay here, active = false");
                    /*Intent home = new Intent(MapsReachUser.this, ActivityHome.class);
                    startActivity(home);*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(a, R.string.something_wrong, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }
}