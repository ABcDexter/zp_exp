package com.e.serviceproviderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

public class ActivityAllJobsAccepted extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityAllJobsAccepted";
    public static final String AUTH_COOKIE = "serviceproviderapp.cookie";
    public static final String AUTH_KEY = "Auth";

    private RecyclerView rv;
    private List<JobListData> list_data;
    private JobListAdapter adapter;
    String stringAuth, strJobId;
    CardView cardView;
    TextView txtEmtUpcoming, txtEmtCurrent, jobType, jobDate, jobTime, jobEarn;

    Map<String, String> params = new HashMap();
    ActivityAllJobsAccepted a = ActivityAllJobsAccepted.this;

    public void onSuccess(JSONObject response, int id) throws JSONException, NegativeArraySizeException {
        Log.d(TAG, "RESPONSE of API servitor-jobs-accepted " + response);
        if (id == 1) {

            String responseS = response.toString();
            //String current = response.getString("current");
            String status = response.getString("status");
            if (status.equals("false")) {
                txtEmtCurrent = findViewById(R.id.txt_no_current);
                txtEmtCurrent.setVisibility(View.VISIBLE);
                txtEmtCurrent.setText(R.string.no_current_job);
                cardView.setVisibility(View.GONE);
            } else {
                jobType = findViewById(R.id.txt_jobType);
                jobDate = findViewById(R.id.txt_jobDate);
                jobTime = findViewById(R.id.txt_jobTime);
                jobEarn = findViewById(R.id.txt_jobEarn);

                String bid = response.getString("bid");
                String job = response.getString("job");
                String date = response.getString("date");
                String time = response.getString("time");
                String earn = response.getString("earn");

                jobType.setText(job);
                jobDate.setText(date);
                jobTime.setText(time);
                jobEarn.setText(earn);
                strJobId=bid;

                cardView.setOnClickListener(this);
            }

            try {
                JSONObject jsonObject = new JSONObject(responseS);
                JSONArray array = jsonObject.getJSONArray("upcoming");
                if (array.length() == 0) {
                    txtEmtUpcoming = findViewById(R.id.txt_empty_array);
                    txtEmtUpcoming.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                    txtEmtUpcoming.setText(R.string.no_up_jobs);
                } else {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject ob = array.getJSONObject(i);
                        JobListData ld = new JobListData(ob.getString("bid"), ob.getString("time"),
                                ob.getString("date"), ob.getString("job"), ob.getString("earn"));
                        list_data.add(ld);
                    }
                }
                rv.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onFailure(VolleyError error) {
        Log.d(TAG, Objects.requireNonNull(error.getMessage()));

        Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_jobs_accepted);

        SharedPreferences prefAuth = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Log.d(TAG, "control in ActivityNewJobList");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new JobListAdapter(list_data, this, 2);

        cardView = findViewById(R.id.cv_ongoing_job);

        getData();

    }

    private void getData() {
        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost servitor-jobs-accepted");

        UtilityApiRequestPost.doPOST(a, "servitor-jobs-accepted", parameters, 2000, 0, response -> {
            try {
                a.onSuccess(response, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, a::onFailure);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cv_ongoing_job) {
            Intent ongoing = new Intent(this, ActivityOngoingJob.class);
            ongoing.putExtra("BID", strJobId);
            startActivity(ongoing);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityAllJobsAccepted.this, ActivityHome.class));
        finish();
    }
}