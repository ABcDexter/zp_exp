package com.zp_driver;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.function.Consumer;

public class UtilityApiRequestPost {

    //private static String BASE_URL = "https://api.villagetech.in:8090/";
    private static String BASE_URL = "https://api.zippe.in:8090/";

    //static void doPost(String URL, JSONObject parameters)
    public static void doPOST(Activity a, String apiName, JSONObject params, int initialTimeout, int retries, final Consumer<JSONObject> onSuccess, final Consumer<VolleyError> onFailure) {

        // Instantiate the RequestQueue.
        String url = BASE_URL + apiName;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                onSuccess::accept, error -> {
            onFailure.accept(error);
            error.printStackTrace();
        });


        jsonRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return initialTimeout;
            }

            @Override
            public int getCurrentRetryCount() {
                return retries;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
            }
        });


        RequestQueue rQueue = Volley.newRequestQueue(a);
        rQueue.add(jsonRequest);
    }
}

