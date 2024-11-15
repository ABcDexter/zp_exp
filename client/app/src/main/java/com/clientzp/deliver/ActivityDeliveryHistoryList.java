package com.clientzp.deliver;

<<<<<<< HEAD
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

=======
>>>>>>> dev
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
=======
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

>>>>>>> dev
import com.android.volley.VolleyError;
import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.clientzp.UtilityApiRequestPost;
<<<<<<< HEAD
=======
import com.clientzp.ride.ActivityRideHistory;
import com.clientzp.ride.ActivityRideHome;
>>>>>>> dev

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
        setContentView(R.layout.activity_delivery_history_list);

        SharedPreferences prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

<<<<<<< HEAD
        //Log.d(TAG, "control in ActivityDeliveryHistoryList");
=======
        Log.d(TAG, "control in ActivityDeliveryHistoryList");
>>>>>>> dev
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
<<<<<<< HEAD
        /*Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost auth-delivery-history");*/
=======
        Log.d(TAG, "auth = " + auth);
        Log.d(TAG, "Control moved to to UtilityApiRequestPost auth-delivery-history");
>>>>>>> dev
        UtilityApiRequestPost.doPOST(a, "auth-delivery-history", parameters, 30000, 0,
                a::onSuccess, a::onFailure);

    }
<<<<<<< HEAD

=======
>>>>>>> dev
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliveryHistoryList.this, ActivityWelcome.class));
        finish();
    }
}
