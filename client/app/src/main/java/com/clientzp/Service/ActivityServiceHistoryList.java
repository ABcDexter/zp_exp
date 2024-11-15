package com.clientzp.Service;

<<<<<<< HEAD
=======
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

>>>>>>> dev
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

=======
import android.util.Log;
import android.widget.Toast;

>>>>>>> dev
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

public class ActivityServiceHistoryList extends AppCompatActivity {
    private static final String TAG = "ActivityServiceHistoryList.class";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<RideListData> list_data;
    private ServiceListAdapter adapter;
    String stringAuth;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("trips");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                RideListData ld = new RideListData(ob.getString("tid"), ob.getString("st"),
<<<<<<< HEAD
                        ob.getString("sdate"), ob.getString("vtype"), ob.getString("srcname"), ob.getString("dstname"));
=======
                        ob.getString("sdate"),ob.getString("vtype"), ob.getString("srcname"), ob.getString("dstname"));
>>>>>>> dev
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
<<<<<<< HEAD
        // Log.d(TAG, Objects.requireNonNull(error.getMessage()));
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
        //Log.d(TAG, "control in ActivityRideHistory");
=======
        Log.d(TAG, "control in ActivityRideHistory");
>>>>>>> dev
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new ServiceListAdapter(list_data, this);

        getData();
    }

    private void getData() {
        Map<String, String> params = new HashMap();

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        ActivityServiceHistoryList a = ActivityServiceHistoryList.this;
<<<<<<< HEAD
        /*Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost auth-ride-history");*/
=======
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost auth-ride-history");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "auth-ride-history", parameters, 30000, 0,
                a::onSuccess, a::onFailure);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityServiceHistoryList.this, ActivityWelcome.class));
        finish();
    }
}