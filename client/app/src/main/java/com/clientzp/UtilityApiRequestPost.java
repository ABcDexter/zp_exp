package com.clientzp;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.function.Consumer;

public class UtilityApiRequestPost {

    /*private static String BASE_URL = "https://api.villagetech.in:8090/";*/
    //private static String BASE_URL = "https://86d9a981a121.ngrok.io/";
    private static final String BASE_URL = "https://api.zippe.in:8090/";

    //static void doPost(String URL, JSONObject parameters)
    public static void doPOST(Activity a, String apiName, JSONObject params , int initialTimeout, int retries,final Consumer<JSONObject> onSuccess, final Consumer<VolleyError> onFailure) {

        // Instantiate the RequestQueue.
        String url = BASE_URL + apiName;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onSuccess.accept((response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onFailure.accept(error);
                error.printStackTrace();
            }
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


        // Deleting all the cache
      /*  String link = "https://api.villagetech.in:8090/";
        UtilityInitApplication.getInstance()
                .getRequestQueue()
                .getCache()
                .clear();*/

    }
}

