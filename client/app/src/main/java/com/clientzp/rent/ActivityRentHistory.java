package com.clientzp.rent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> dev
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
import com.clientzp.ride.RideListData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD
=======
import java.util.Objects;
>>>>>>> dev

public class ActivityRentHistory extends AppCompatActivity {
    private static final String TAG = "ActivityRentHistory.class";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<RideListData> list_data;
    private RentListAdapter adapter;
    String stringAuth;
    TextView txt;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
<<<<<<< HEAD
        //Log.d(TAG, "RESPONSE:" + responseS);
=======
        Log.d(TAG, "RESPONSE:" + responseS);
>>>>>>> dev
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("trips");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                RideListData ld = new RideListData(ob.getString("tid"), ob.getString("st"),
                        ob.getString("sdate"), ob.getString("vtype"), ob.getString("srcname"), ob.getString("dstname"));
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            txt.setVisibility(View.VISIBLE);
            txt.setText(R.string.try_zippe);
<<<<<<< HEAD
            txt.setOnClickListener(view -> {
                Intent intent = new Intent(ActivityRentHistory.this, ActivityRentHome.class);
                startActivity(intent);
                finish();
=======
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityRentHistory.this, ActivityRentHome.class);
                    startActivity(intent);
                    finish();
                }
>>>>>>> dev
            });
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
<<<<<<< HEAD
        //Log.d(TAG, Objects.requireNonNull(error.getMessage()));
=======
        Log.d(TAG, Objects.requireNonNull(error.getMessage()));
>>>>>>> dev
        Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

<<<<<<< HEAD
        //Log.d(TAG, "control in ActivityRentHistory");
=======
        Log.d(TAG, "control in ActivityRentHistory");
>>>>>>> dev
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        txt = findViewById(R.id.tryRent);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new RentListAdapter(list_data, this);

        getData();
    }

    private void getData() {
        Map<String, String> params = new HashMap();

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        ActivityRentHistory a = ActivityRentHistory.this;
<<<<<<< HEAD
        //Log.d(TAG, "auth = " + auth);
        //Log.d(TAG, "Control moved to to UtilityApiRequestPost user-rent-history");
=======
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost user-rent-history");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "user-rent-history", parameters, 30000, 0,
                a::onSuccess, a::onFailure);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityRentHistory.this, ActivityWelcome.class));
        finish();
    }
}
