package com.zpclient.ride;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.zpclient.ActivityDrawer;
import com.zpclient.ActivityWelcome;
import com.zpclient.R;
import com.zpclient.UtilityApiRequestPost;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityRideSummery extends ActivityDrawer implements OnMapReadyCallback, TaskLoadedCallback{
    private static final String TAG = "ActivityRideSummery";
    String stringAuthKey, stringTID;
    TextView dialog_txt;
    SwipeRefreshLayout swipeRefresh;
    ScrollView scrollView;
    ActivityRideSummery a = ActivityRideSummery.this;
    Map<String, String> params = new HashMap();
    public static final String AUTH_KEY = "AuthKey";
    private GoogleMap mMap;
    private MarkerOptions src, dst;
    private Polyline currentPolyline;
    double srcLat, srcLng, dstLat, dstLng;
    TextView rideRate, ridePrice, rideTax, rideTotal, rideVehicle, rideTime, rideDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_ride_summery, null, false);
        frameLayout.addView(activityView);
        SharedPreferences prefCookie = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuthKey = prefCookie.getString(AUTH_KEY, "");
        Intent intent = getIntent();
        stringTID = intent.getStringExtra("TID");
        Log.d(TAG, "TID" + stringTID);
        //initializing views
        scrollView = findViewById(R.id.scrollViewReview);
        rideRate = findViewById(R.id.ride_rate);
        ridePrice = findViewById(R.id.ride_price);
        rideTax = findViewById(R.id.ride_tax);
        rideTotal = findViewById(R.id.ride_total);
        rideVehicle = findViewById(R.id.ride_vehicle);
        rideTime = findViewById(R.id.ride_time);
        rideDate = findViewById(R.id.ride_date);

        userDeliverySummery();
        swipeRefresh = findViewById(R.id.swipeRefresh);
        //getInfo();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();//this will recreate or reload the activity when swiped down
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    protected void userDeliverySummery() {
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

    String rate, price, tax, total, vtype, time, date, sLat, sLng, dLat, dLng, srcname, dstname;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE:" + response);

        //response on hitting auth-trip-data API
        if (id == 2) {

            vtype = response.getString("rvtype");
            rate = response.getString("rate");
            price = response.getString("price");
            tax = response.getString("tax");
            total = response.getString("total");
            time = response.getString("time");
            date = response.getString("sdate");
            sLat = response.getString("srclat");
            sLng = response.getString("srclng");
            dLat = response.getString("dstlat");
            dLng = response.getString("dstlng");
            srcname = response.getString("srchub");
            dstname = response.getString("dsthub");

            rideRate.setText(getString(R.string.message_rs, rate));
            ridePrice.setText(getString(R.string.message_rs, price));
            rideTax.setText(getString(R.string.message_rs, tax));
            rideTotal.setText(getString(R.string.message_rs, total));
            rideVehicle.setText(vtype);
            //rideTime.setText(time + R.string.mins);
            rideTime.setText(getString(R.string.message_min, time));
            rideDate.setText(date);

            switch (vtype) {
                case "0":
                    rideVehicle.setText(R.string.e_cycle);
                    break;
                case "1":
                    rideVehicle.setText(R.string.e_scooty);
                    break;
                case "2":
                    rideVehicle.setText(R.string.e_bike);
                    break;
                case "3":
                    rideVehicle.setText(R.string.zbee);
                    break;
                default:
                    rideVehicle.setText(vtype);
                    break;
            }
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapsHistory);
            mapFragment.getMapAsync(this);

            srcLat = Double.parseDouble(sLat);
            srcLng = Double.parseDouble(sLng);
            dstLat = Double.parseDouble(dLat);
            dstLng = Double.parseDouble(dLng);

            src = new MarkerOptions().position(new LatLng(srcLat, srcLng)).title(srcname);
            dst = new MarkerOptions().position(new LatLng(dstLat, dstLng)).title(dstname);

            new FetchURL(ActivityRideSummery.this).execute(getUrl(src.getPosition(), dst.getPosition(), "driving"), "driving");

        }

        //response on hitting user-delivery-get-info API
        if (id == 1) {
            try {

                String st = response.getString("st");

                if (st.equals("RQ") || st.equals("PD") || st.equals("SC")) {

                    ShowPopup(0, "");

                }
                if (st.equals("AS")) {
                    String otp = response.getString("otp");
                    ShowPopup(2, otp);
                }
                if (st.equals("ST")) {
                    ShowPopup(1, "");
                    /*trackDelivery.setVisibility(View.VISIBLE);*/
                }
                if (st.equals("RC")) {
                    String otp = response.getString("otp");
                    ShowPopup(8, otp);
                }
                if (st.equals("FL")) {
                    ShowPopup(3, "");
                }
                if (st.equals("DN")) {
                    ShowPopup(4, "");
                }
                if (st.equals("CN")) {
                    ShowPopup(5, "");
                }
                if (st.equals("TO")) {
                    ShowPopup(6, "");
                }
                if (st.equals("FN")) {
                    ShowPopup(7, "");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void onFailure(VolleyError error) {
        Log.d("TAG", "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityRideSummery.this, ActivityWelcome.class));
        finish();
    }

  /*  public void getInfo() {
        String auth = stringAuthKey;
        String tid = stringTID;
        params.put("auth", auth);
        params.put("tid", tid);
        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "Values: auth=" + auth + " tid=" + tid);
        Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-trip-get-info");
        UtilityApiRequestPost.doPOST(a, "auth-trip-get-info", parameters, 20000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }, a::onFailure);
    }
*/

    private void ShowPopup(int id, String info) {

        //myDialog.setContentView(R.layout.popup_new_request);
        dialog_txt = findViewById(R.id.txtInfo);
        //RQ or PD
        if (id == 0) {
            dialog_txt.setText(R.string.your_agent_will_be_assigned_shortly);
        }
        //ST
        if (id == 1) {
            dialog_txt.setText(R.string.the_package_is_en_route);
        }
        //AS
        if (id == 2) {
            //dialog_txt.setText(R.string.your_delivery_agent_will_arrive_shortly + info);
            dialog_txt.setText(String.format("OTP : %s", info));
            Log.d(TAG, "AS OTP = " + info);
        }
        //FL
        if (id == 3) {
            dialog_txt.setText(R.string.unable_to_complete_your_ride);
        }
        //DN
        if (id == 4) {
            dialog_txt.setText(R.string.ride_cancelled_by_you);
        }
        //CN
        if (id == 5) {
            dialog_txt.setText(R.string.ride_canceled_by_you);
        }
        //TO
        if (id == 6) {
            dialog_txt.setText(R.string.ride_timed_out);
        }
        //FN
        if (id == 7) {
            dialog_txt.setText(R.string.delivery_was_completed_successfully);
        }
        //RC
        if (id == 8) {
            //dialog_txt.setText(R.string.agent_has_arrived + INFO);
            dialog_txt.setText(String.format("OTP : %s", info));
            Log.d(TAG, "RC OTP = " + info);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Added Markers");
        mMap.addMarker(src).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mMap.addMarker(dst).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

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
