package com.e.purchasedeptapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateProductDatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "purchaserProductsUpdate";
    private static final String TABLE_PRODUCTS = "updateproducts";
    private static final String KEY_ID = "keyy";
    private static final String KEY_KEY = "id";
    private static final String KEY_STOCK_QNT = "stock_qnt";
    private static final String KEY_COST_PRICE = "cost_price";
    private static final String KEY_REGULAR_PRICE = "regular_price";
    private static final String KEY_WEIGHT = "weight";

    public UpdateProductDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_KEY + " TEXT,"
                + KEY_STOCK_QNT + " TEXT," + KEY_COST_PRICE + " TEXT,"
                + KEY_REGULAR_PRICE + " TEXT," + KEY_WEIGHT + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        // Create tables again
        onCreate(db);
    }

    // code to add the new productFromApp
    void addProduct(ProductFromApp productFromApp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KEY, productFromApp.get_key()); // ProductFromApp Key
        values.put(KEY_STOCK_QNT, productFromApp.getStockQuantity()); // ProductFromApp StockQuantity
        values.put(KEY_COST_PRICE, productFromApp.getCostPrice()); // ProductFromApp CostPrice
        values.put(KEY_REGULAR_PRICE, productFromApp.getRegularPrice()); // ProductFromApp RegularPrice
        values.put(KEY_WEIGHT, productFromApp.getWeight()); // ProductFromApp Weight

        // Inserting Row
        db.insert(TABLE_PRODUCTS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }


    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_PRODUCTS, null);
        return res;
    }

    // code to get all products in a list view
    /*public List<ProductFromApp> getAllContacts() {
        List<ProductFromApp> productFromAppList = new ArrayList<ProductFromApp>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ProductFromApp productFromApp = new ProductFromApp();
                productFromApp.setID(Integer.parseInt(cursor.getString(0)));
                productFromApp.set_key(cursor.getString(1));
                productFromApp.setName(cursor.getString(2));
                productFromApp.setStockQuantity(cursor.getString(3));
                productFromApp.setCostPrice(cursor.getString(4));
                productFromApp.setRegularPrice(cursor.getString(5));
                productFromApp.setWeight(cursor.getString(6));
                // Adding productFromApp to list
                productFromAppList.add(productFromApp);
            } while (cursor.moveToNext());
        }

        // return product list
        return productFromAppList;
    }*/

    public void deleteContact() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_PRODUCTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}