package com.client.ride;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.client.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private MarkerOptions src, dst;
    Button getDirection;
    private Polyline currentPolyline;
    double srcLat, srcLng, dstLat, dstLng;
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String SRC_LNG = "SrcLng";
    public static final String SRC_LAT = "SrcLat";
    public static final String DST_LAT = "DropLat";
    public static final String DST_LNG = "DropLng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        SharedPreferences pref = getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);
        String stringSrcLat = pref.getString(SRC_LAT, "");
        String stringSrcLng = pref.getString(SRC_LNG, "");
        String stringDstLat = pref.getString(DST_LAT, "");
        String stringDstLng = pref.getString(DST_LNG, "");
        srcLat = Double.parseDouble(stringSrcLat);
        srcLng = Double.parseDouble(stringSrcLng);
        dstLat = Double.parseDouble(stringDstLat);
        dstLng = Double.parseDouble(stringDstLng);

        /*getDirection = findViewById(R.id.btnGetDirection);
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchURL(MapsActivity2.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

            }
        });*/
        //27.658143,85.3199503
        //27.667491,85.3208583
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
}