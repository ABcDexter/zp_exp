package com.e.serviceproviderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import java.util.Objects;

public class ActivityCompletedJobList extends ActivityDrawer {
    private static final String TAG = "ActivityCompletedJobList";
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";

    private RecyclerView rv;
    private List<JobListData> list_data;
    private JobListAdapter adapter;
    String stringAuth;
TextView textView;
    public void onSuccess(JSONObject response) {
        Log.d(TAG, "RESPONSE:" + response);
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("past");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                JobListData ld = new JobListData(ob.getString("bid"), ob.getString("time"),
                        ob.getString("date"), ob.getString("job"), ob.getString("earn"));
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
        setContentView(R.layout.activity_job_list);
        SharedPreferences prefAuth = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Log.d(TAG, "control in ActivityNewJobList");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.tv_heading);
        textView.setText(R.string.j_completed);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new JobListAdapter(list_data, this, 3);

        getData();
    }

    private void getData() {
        Map<String, String> params = new HashMap();

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        ActivityCompletedJobList a = ActivityCompletedJobList.this;
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost servitor-jobs-completed");
        UtilityApiRequestPost.doPOST(a, "servitor-jobs-completed", parameters, 30000, 0,
                a::onSuccess, a::onFailure);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityCompletedJobList.this, ActivityHome.class));
        finish();
    }
}