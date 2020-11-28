package com.e.purchasedeptapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "purchaserProducts";
    private static final String TABLE_PRODUCTS = "products";
    private static final String KEY_ID = "id";
    private static final String KEY_KEY = "keyy";
    private static final String KEY_NAME = "name";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_STOCK_QNT = "stock_qnt";
    private static final String KEY_COST_PRICE = "cost_price";
    private static final String KEY_REGULAR_PRICE = "regular_price";
    private static final String KEY_WEIGHT = "weight";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        /*String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_KEY + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_CATEGORY + " TEXT" + ")";*/
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_KEY + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_CATEGORY + " TEXT," + KEY_STOCK_QNT + " TEXT," + KEY_COST_PRICE + " TEXT,"
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

    // code to add the new productFromServer
    void addProduct(ProductFromServer productFromServer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KEY, productFromServer.get_key()); // ProductFromServer Key
        values.put(KEY_NAME, productFromServer.getName()); // ProductFromServer Name
        values.put(KEY_CATEGORY, productFromServer.getCategory()); // ProductFromServer Category
        values.put(KEY_STOCK_QNT, productFromServer.getStockQuantity()); // ProductFromServer StockQuantity
        values.put(KEY_COST_PRICE, productFromServer.getCostPrice()); // ProductFromServer CostPrice
        values.put(KEY_REGULAR_PRICE, productFromServer.getRegularPrice()); // ProductFromServer RegularPrice
        values.put(KEY_WEIGHT, productFromServer.getWeight()); // ProductFromServer Weight

        // Inserting Row
        db.insert(TABLE_PRODUCTS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get the single product
    /*ProductFromServer getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_KEY, KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ProductFromServer product = new ProductFromServer(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),cursor.getString(2), cursor.getString(3));
        // return product
        return product;
    }*/

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_PRODUCTS, null);
        return res;
    }

    // code to get all products in a list view
    public List<ProductFromServer> getAllContacts() {
        List<ProductFromServer> productFromServerList = new ArrayList<ProductFromServer>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ProductFromServer productFromServer = new ProductFromServer();
                productFromServer.setID(Integer.parseInt(cursor.getString(0)));
                productFromServer.set_key(cursor.getString(1));
                productFromServer.setName(cursor.getString(2));
                productFromServer.setCategory(cursor.getString(3));
                productFromServer.setStockQuantity(cursor.getString(4));
                productFromServer.setCostPrice(cursor.getString(5));
                productFromServer.setRegularPrice(cursor.getString(6));
                productFromServer.setWeight(cursor.getString(7));
                // Adding productFromServer to list
                productFromServerList.add(productFromServer);
            } while (cursor.moveToNext());
        }

        // return product list
        return productFromServerList;
    }

    // code to update the single productFromServer
    public int updateContact(ProductFromServer productFromServer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KEY, productFromServer.get_key());
        values.put(KEY_NAME, productFromServer.getName());
        values.put(KEY_CATEGORY, productFromServer.getCategory());
        values.put(KEY_STOCK_QNT, productFromServer.getStockQuantity());
        values.put(KEY_COST_PRICE, productFromServer.getCostPrice());
        values.put(KEY_REGULAR_PRICE, productFromServer.getRegularPrice());
        values.put(KEY_WEIGHT, productFromServer.getWeight());

        // updating row
        return db.update(TABLE_PRODUCTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(productFromServer.getID())});
    }

    // Deleting single product
    /*public void deleteContact(ProductFromServer product) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(product.getID()) });
        db.close();
    }*/
    public void deleteContact() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_PRODUCTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getting products Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public Cursor fetchData(String arg1) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + TABLE_PRODUCTS + " where " + KEY_NAME + " like ?", new String[]{"%" + arg1 + "%"});
        return cursor;
    }

    public List<ProductFromServer> search(String word) {
        Log.d("DatabaseHandler ", "inside search() ");
        List<ProductFromServer> products = null;
        try {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + TABLE_PRODUCTS + " where " + KEY_NAME + " like ?", new String[]{"%" + word + "%"});
            Log.d("DatabaseHandler ", "searching...");
            if (cursor.moveToFirst()) {
                products = new ArrayList<ProductFromServer>();
                do {
                    ProductFromServer productFromServer = new ProductFromServer();
                    productFromServer.setID(Integer.parseInt(cursor.getString(0)));
                    productFromServer.set_key(cursor.getString(1));
                    productFromServer.setName(cursor.getString(2));
                    productFromServer.setCategory(cursor.getString(3));
                    productFromServer.setStockQuantity(cursor.getString(4));
                    productFromServer.setCostPrice(cursor.getString(5));
                    productFromServer.setRegularPrice(cursor.getString(6));
                    productFromServer.setWeight(cursor.getString(7));
                    products.add(productFromServer);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            products = null;
        }
        //return products;
        return products;
    }
}