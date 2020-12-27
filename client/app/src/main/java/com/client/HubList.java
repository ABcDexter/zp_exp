package com.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.client.rent.ActivityRentHome;
import com.client.rent.ActivityUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class HubList extends AppCompatActivity {
    private static final String TAG = "HubList.class";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<HubListData> list_data;
    private MyHubListAdapter adapter;
    String stringAuth;
    String request;
    ImageView close;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("hublist");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                HubListData ld = new HubListData(ob.getString("pn"), ob.getString("id"),
                        ob.getString("lat"), ob.getString("lng"));
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d("HubList class", Objects.requireNonNull(error.getMessage()));

        Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_list);

        Intent intent = getIntent();
        request = intent.getStringExtra("Request");

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Log.d(TAG, "control in HubList");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        close = findViewById(R.id.close_hub);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        assert request != null;
        if (request.equals("origin")) {
            Log.d(TAG, "key value is origin");
            adapter = new MyHubListAdapter(list_data, this, 1);
            Log.d(TAG, "intentValue = 1");
        }
        if (request.equals("destination")) {
            Log.d(TAG, "key value is destination");
            adapter = new MyHubListAdapter(list_data, this, 2);
            Log.d(TAG, "intentValue = 2");
        }
        if (request.equals("pick_rent")) {
            Log.d(TAG, "key value is pick_rent");
            adapter = new MyHubListAdapter(list_data, this, 3);
            Log.d(TAG, "intentValue = 3");
        }
        if (request.equals("destination_rental")) {
            Log.d(TAG, "key value is destination_rental");
            adapter = new MyHubListAdapter(list_data, this, 4);
            Log.d(TAG, "intentValue = 4");
        }
        if (request.equals("destination_rental_in_progress")) {
            Log.d(TAG, "key value is destination_rental_in_progress");
            adapter = new MyHubListAdapter(list_data, this, 5);
            Log.d(TAG, "intentValue = 5");
        }
        getData();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (request.equals("pick_rent")) {
                    startActivity(new Intent(HubList.this, ActivityRentHome.class));
                    finish();
                }
                if (request.equals("destination_rental")) {
                    startActivity(new Intent(HubList.this, ActivityRentHome.class));
                    finish();
                }
                if (request.equals("destination_rental_in_progress")) {
                    startActivity(new Intent(HubList.this, ActivityUpdateInfo.class));
                    finish();
                }
            }
        });
    }

    private void getData() {
        Map<String, String> params = new HashMap();

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        HubList a = HubList.this;
        Log.d(TAG, "auth = " + auth);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost auth-place-get");
        UtilityApiRequestPost.doPOST(a, "auth-place-get", parameters, 30000, 0, a::onSuccess, a::onFailure);

    }
}
