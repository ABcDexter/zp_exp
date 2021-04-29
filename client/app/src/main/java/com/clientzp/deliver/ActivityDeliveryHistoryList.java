package com.clientzp.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDeliveryHistoryList extends AppCompatActivity {
    private static final String TAG = "ActivityDeliveryHistoryList.class";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    public static final String AUTH_KEY = "AuthKey";

    private RecyclerView rv;
    private List<DeliveryListData> list_data;
    private DeliveryListAdapter adapter;
    String stringAuth;
    TextView txt;

    public void onSuccess(JSONObject response) {
        String responseS = response.toString();
        try {
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("delis");
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                DeliveryListData ld = new DeliveryListData(ob.getString("scid"), ob.getString("st"),
                        ob.getString("price"), ob.getString("itype"));
                list_data.add(ld);
            }
            rv.setAdapter(adapter);
        } catch (JSONException e) {
            txt.setVisibility(View.VISIBLE);
            txt.setText(R.string.try_zippe_delivery);
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityDeliveryHistoryList.this, ActivityPackageDetails.class);
                    startActivity(intent);
                    finish();
                }
            });
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
        //Log.d(TAG, Objects.requireNonNull(error.getMessage()));

        Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_history_list);

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        //Log.d(TAG, "control in ActivityDeliveryHistoryList");
        //loading list view item with this function

        rv = findViewById(R.id.recycler_view);
        txt = findViewById(R.id.tryDelivery);
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
        /*Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost auth-delivery-history");*/
        UtilityApiRequestPost.doPOST(a, "auth-delivery-history", parameters, 30000, 0,
                a::onSuccess, a::onFailure);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliveryHistoryList.this, ActivityWelcome.class));
        finish();
    }
}
