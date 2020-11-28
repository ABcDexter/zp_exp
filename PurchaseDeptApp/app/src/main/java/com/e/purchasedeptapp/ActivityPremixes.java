package com.e.purchasedeptapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActivityPremixes extends AppCompatActivity {

    private static final String DATABASE_NAME = "purchaserProducts";

    List<Product> productList;
    SQLiteDatabase mDatabase;
    ListView listViewProducts;
    UtilityProductAdapter adapter;
TextView textView;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        textView = findViewById(R.id.txtCatName);
        textView.setText(R.string.premixes);
        listViewProducts = findViewById(R.id.listViewProducts);
        productList = new ArrayList<>();

        //opening the database
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        //creating the adapter object
        adapter = new UtilityProductAdapter(this, R.layout.list_layout_product, productList,mDatabase);
        searchView = findViewById(R.id.etSearch);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchContact(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContact(newText);
                return false;
            }
        });
        //this method will display the employees in the list
        showEmployeesFromDatabase();
    }
    private void searchContact(String word) {
        // Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM products", null);
        Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM products WHERE category = 'Premixes & Health/Energy drinks'" + " AND name" + " like ?", new String[]{"%" + word + "%"});
        if (cursorEmployees.moveToFirst()) {
            productList.clear();
            do {
                productList.add(new Product(
                        cursorEmployees.getString(0),
                        cursorEmployees.getString(1),
                        cursorEmployees.getString(2),
                        cursorEmployees.getString(3),
                        cursorEmployees.getString(4),
                        cursorEmployees.getString(5),
                        cursorEmployees.getString(6),
                        cursorEmployees.getString(7)
                ));
            } while (cursorEmployees.moveToNext());
        }
        cursorEmployees.close();
        listViewProducts.setAdapter(adapter);
        //notifyDataSetChanged();
    }

    private void showEmployeesFromDatabase() {

        //we used rawQuery(sql, selectionargs) for fetching all the employees
        //Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM contacts", null);
        String query = "SELECT * FROM products WHERE category = 'Premixes & Health/Energy drinks'" ;
        Cursor cursorEmployees = mDatabase.rawQuery(query, null);

        //if the cursor has some data
        if (cursorEmployees.moveToFirst()) {
            //looping through all the records
            do {
                //pushing each record in the employee list
                productList.add(new Product(
                        cursorEmployees.getString(0),
                        cursorEmployees.getString(1),
                        cursorEmployees.getString(2),
                        cursorEmployees.getString(3),
                        cursorEmployees.getString(4),
                        cursorEmployees.getString(5),
                        cursorEmployees.getString(6),
                        cursorEmployees.getString(7)
                ));

            } while (cursorEmployees.moveToNext());
        }
        //closing the cursor
        cursorEmployees.close();

        //adding the adapter to listview
        listViewProducts.setAdapter(adapter);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent home = new Intent(ActivityPremixes.this, ActivityHome.class);
        startActivity(home);
        finish();
    }
}