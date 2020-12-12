package com.e.purchasedeptapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UpdateProductDatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "purchaserProductsUpdate";
    private static final String TABLE_PRODUCTS = "updateproducts";
    private static final String KEY_ID = "keyy";
    private static final String KEY_KEY = "id";
    private static final String KEY_STOCK_QNT = "stock_qnt";
    private static final String KEY_COST_PRICE = "cost_price";
    private static final String KEY_REGULAR_PRICE = "regular_price";
    private static final String KEY_NAME = "name";
    private static final String KEY_WEIGHT = "weight";

    public UpdateProductDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_NAME + " TEXT,"+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_KEY + " TEXT,"
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
        values.put(KEY_NAME, productFromApp.get_name()); // ProductFromApp Name
        values.put(KEY_KEY, productFromApp.get_key()); // ProductFromApp Key
        values.put(KEY_STOCK_QNT, productFromApp.get_stock_quantity()); // ProductFromApp StockQuantity
        values.put(KEY_COST_PRICE, productFromApp.get_cost_price()); // ProductFromApp CostPrice
        values.put(KEY_REGULAR_PRICE, productFromApp.get_regular_price()); // ProductFromApp RegularPrice
        values.put(KEY_WEIGHT, productFromApp.get_weight()); // ProductFromApp Weight

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

    public void deleteContact() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_PRODUCTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}