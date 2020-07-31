package com.client.deliver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.client.R;
import com.client.UtilityApiRequestPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityDeliveryHistoryList extends AppCompatActivity {
    private static final String TAG = "ActivityDeliveryHistoryList.class";
    public static final String SESSION_COOKIE = "com.client.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<DeliveryListData> list_data;
    private DeliveryListAdapter adapter;
    String stringAuth;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("delis");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                DeliveryListData ld = new DeliveryListData(ob.getString("id"), ob.getString("st"),
                        ob.getString("price"));
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
        Log.d("ActivityDeliveryHistoryList class", Objects.requireNonNull(error.getMessage()));

        Toast.makeText(this, "CHECK YOUR INTERNET CONNECTION!", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_history_list);

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        Log.d(TAG, "control in ActivityDeliveryHistoryList");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list_data = new ArrayList<>();
        adapter = new DeliveryListAdapter(list_data, this);

        getData();
    }

    private void getData() {
        Map<String, String> params = new HashMap();

        String auth = stringAuth;
        params.put("auth", auth);

        JSONObject parameters = new JSONObject(params);
        ActivityDeliveryHistoryList a = ActivityDeliveryHistoryList.this;
        Log.d(TAG, "auth = " + auth);
        Log.d("CONTROL", "Control moved to to UtilityApiRequestPost auth-delivery-history");
        UtilityApiRequestPost.doPOST(a, "auth-delivery-history", parameters, 30000, 0, a::onSuccess, a::onFailure);

    }
}
