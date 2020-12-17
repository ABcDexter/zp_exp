package com.e.purchasedeptapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityUpdateToServer extends AppCompatActivity {

    private static final String DATABASE_NAME = "purchaserProductsUpdate";
    private static final String TAG = "ActivityUpdateToServer";
    public static final String AUTH_COOKIE = "com.purchasedeptapp.cookie";
    public static final String AUTH_KEY = "Auth";
    String strAuth;
    JSONArray resultSet = new JSONArray();
    int totalColumn;
    List<ProductFromApp> productList;
    Button update;
    SQLiteDatabase mDatabase;
    ListView displayProductsToUpdate;
    UtilityUpdateProductAdapter adapter;
    private static final String TABLE_PRODUCTS = "updateproducts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_to_server);
        Log.d(TAG, " inside the activity");
        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth
        productList = new ArrayList<>();
        update = findViewById(R.id.updateBtn);
        displayProductsToUpdate = findViewById(R.id.listViewProductsUpdateList);
        //opening the database
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        adapter = new UtilityUpdateProductAdapter(this, R.layout.update_product, productList, mDatabase);
        showEmployeesFromDatabase();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                divideArray();
            }
        });


    }

    public void convertToJson(int posStart, int posEnd) {
        String query = "SELECT * FROM updateproducts ";
        Cursor cursor = mDatabase.rawQuery(query, null);
        cursor.moveToPosition(posStart);

        while (cursor.getPosition() < posEnd) {
            cursor.getPosition();
            Log.d(TAG, "cursor.getPosition() " + cursor.getPosition());

            JSONObject rowObject = new JSONObject();

            for (int i = 2; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("ActivityUpdateToServer", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        Log.d("ActivityUpdateToServer resultSet ", resultSet.toString());
        if (resultSet.length() != 0) {
            findProductUpdate();
        } else {
            Log.d(TAG, " Json Array empty");
            Toast.makeText(this, "All Items have bee uploaded to server.", Toast.LENGTH_SHORT).show();
            adapter.clear(); //clear the entire list
        }
        cursor.close();
    }

    private void divideArray() {
        Log.d(TAG, "in divideArray()");
        String query = "SELECT * FROM updateproducts ";
        Cursor cursor = mDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        totalColumn = cursor.getColumnCount();
        int totalItems = cursor.getCount();
        Log.d(TAG, "totalColumn = " + totalColumn);
        Log.d(TAG, "totalItems = " + totalItems);
        Log.d(TAG, "position = " + cursor.getPosition());

        for (int f = cursor.getPosition(); f < 100; f++) {
            Log.d(TAG, "for loop value of f " + f);
            cursor.moveToPosition(f);
            Log.d(TAG, "cursor position inside for loop = " + cursor.getPosition());
        }
        Log.d(TAG, "final cursor position after for loop = " + cursor.getPosition());
        convertToJson(0, cursor.getPosition());
        Log.d(TAG, "~~~~~~");

    }

    private static final String KEY_ID = "keyy";

    private void findProductUpdate() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("auth", strAuth);
            parameters.put("update", resultSet);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ActivityUpdateToServer a = ActivityUpdateToServer.this;
        Log.d(TAG, "Values: auth=" + strAuth);
        Log.d(TAG, "parameters = " + parameters.toString());
        Log.d(TAG, "UtilityApiRequestPost.doPOST auth-product-batch-update");
        UtilityApiRequestPost.doPOST(a, "auth-product-batch-update", parameters, 30000, 0, a::onSuccess, a::onFailure);
    }

    public void onSuccess(JSONObject response) {
        //response on hitting auth-product-batch-update API
        Log.d(TAG, "RESPONSE:" + response);

        String ALTER_TBL = "delete from updateproducts " +
                " where " + KEY_ID + " in (select " + KEY_ID + " from updateproducts " + " order by keyy LIMIT 99);";
        mDatabase.execSQL(ALTER_TBL);
        String query = "SELECT * FROM updateproducts ";
        Cursor cursor = mDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        totalColumn = cursor.getColumnCount();
        int totalItems = cursor.getCount();
        Log.d(TAG, "totalColumn = " + totalColumn);
        Log.d(TAG, "totalItems = " + totalItems);
        Log.d(TAG, "position = " + cursor.getPosition());
        resultSet = new JSONArray();
        Log.d(TAG, "resultSet array: " + resultSet.toString());
        divideArray();
    }

    public void onFailure(VolleyError error) {
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());
    }

    private void showEmployeesFromDatabase() {
        //we used rawQuery(sql, selectionargs) for fetching all the employees
        Cursor res = mDatabase.rawQuery("select * from " + TABLE_PRODUCTS, null);
        //if the cursor has some data
        if (res.moveToFirst()) {
            //looping through all the records
            do {
                //pushing each record in the employee list
                productList.add(new ProductFromApp(
                        res.getString(0),
                        res.getString(2),
                        res.getString(3),
                        res.getString(4),
                        res.getString(5)/*,
                        res.getString(4),
                        res.getString(5)*/
                ));

            } while (res.moveToNext());
        }
        //closing the cursor
        res.close();

        //adding the adapter to listview
        displayProductsToUpdate.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent home = new Intent(ActivityUpdateToServer.this, ActivityHome.class);
        startActivity(home);
        finish();
    }

}