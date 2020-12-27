package com.client.rent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.client.ActivityWelcome;
import com.client.R;
import com.client.UtilityApiRequestPost;
import com.client.ride.RideListAdapter;
import com.client.ride.RideListData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityRentHistory extends AppCompatActivity {
    private static final String TAG = "ActivityRentHistory.class";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<RideListData> list_data;
    private RentListAdapter adapter;
    String stringAuth;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("trips");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                RideListData ld = new RideListData(ob.getString("tid"), ob.getString("st"),
                        ob.getString("sdate"),ob.getString("vtype"));
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, Objects.requireNonNull(error.getMessage()));
        Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Log.d(TAG, "control in ActivityRentHistory");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
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
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost user-rent-history");
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
