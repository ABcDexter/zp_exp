package com.e.purchasedeptapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityFindProductUpdate extends AppCompatActivity {
    private static final String TAG = "ActivityFindProductUpdate";
    public static final String AUTH_COOKIE = "com.purchasedeptapp.cookie";
    public static final String AUTH_KEY = "Auth";
    String strAuth;

    DatabaseHandler db;

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView textView, looking;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_product_update);

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        looking = findViewById(R.id.looking);
        db = new DatabaseHandler(this);
        db.deleteContact();
        findProductUpdate();
    }

    private void findProductUpdate() {
        Map<String, String> params = new HashMap();
        params.put("auth", strAuth);

        JSONObject parameters = new JSONObject(params);
        ActivityFindProductUpdate a = ActivityFindProductUpdate.this;
        Log.d(TAG, "Values: auth=" + strAuth);
        Log.d(TAG, "UtilityApiRequestPost.doPOST purchaser-product-get");
        UtilityApiRequestPost.doPOST(a, "purchaser-product-get", parameters, 30000, 0, a::onSuccess, a::onFailure);
    }

    public void onSuccess(JSONObject response) {
        Log.d(TAG, "RESPONSE:" + response);
        //response on hitting purchaser-product-get API
        String responseS = response.toString();
        try {
            //converting JSONObject to JSONArray
            JSONObject jsonObject = new JSONObject(responseS);
            JSONArray array = jsonObject.getJSONArray("product");
            if (array.length() > 0) {
                Log.d(TAG, "array.length = " + array.length());
// Start long running operation in a background thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < array.length(); i++) {
                            if (i == array.length()) {
                                /*looking.setText(R.string.products_updated);
                                progressBar.setVisibility(View.GONE);
                                textView.setVisibility(View.GONE);*/
                            } else {
                                try {
                                    JSONObject ob = array.getJSONObject(i);
                                    // Inserting Contacts
                                    Log.d("Insert: ", "Inserting .." + i);
                                    db.addProduct(new ProductFromServer(ob.getString("id"),
                                            ob.getString("name"), ob.getString("categories"),
                                            ob.getString("stock_quantity"), ob.getString("cost_price"),
                                            ob.getString("regular_price"), ob.getString("weight")));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                progressStatus += 1;
                                // Update the progress bar and display the
                                //current value in the text view
                                handler.post(new Runnable() {
                                    public void run() {
                                        progressBar.setProgress(progressStatus);
                                        textView.setText(progressStatus + "/" + array.length());
                                    }
                                });
                                /*if (progressStatus == array.length()){
                                    looking.setText(R.string.products_updated);
                                    progressBar.setVisibility(View.GONE);
                                    textView.setVisibility(View.GONE);
                                }  */
                                try {
                                    // Sleep for 200 milliseconds.
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();

                /*List<ProductFromServer> contacts = db.getAllContacts();
                Log.d("Reading: ", "Reading all contacts..");
                for (ProductFromServer cn : contacts) {
                    String log = "Id: " + cn.getID() + " ,ID: " + cn.get_key() + " ,Name: " + cn.getName() + " ,Category: " +
                            cn.getCategory();
                    // Writing Contacts to log
                    Log.d(TAG, log);
                }*/

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent home = new Intent(ActivityFindProductUpdate.this, ActivityHome.class);
        startActivity(home);
        finish();
    }
}
