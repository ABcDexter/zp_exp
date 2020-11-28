package com.e.purchasedeptapp;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class UtilityProductAdapter extends ArrayAdapter<Product> {

    Context mCtx;
    int listLayoutRes;
    List<Product> productList;
    SQLiteDatabase mDatabase;

    public UtilityProductAdapter(Context mCtx, int listLayoutRes, List<Product> productList, SQLiteDatabase mDatabase) {
        super(mCtx, listLayoutRes, productList);

        this.mCtx = mCtx;
        this.listLayoutRes = listLayoutRes;
        this.productList = productList;
        this.mDatabase = mDatabase;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return productList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(listLayoutRes, null);

        //getting employee of the specified position
        Product product = productList.get(position);

        //getting views
        TextView textViewName = view.findViewById(R.id.productName);
        TextView textViewDept = view.findViewById(R.id.productKey);

        //adding data to views
        textViewName.setText(product.getName());
        textViewDept.setText(product.getKeyy());
        //textViewSalary.setText(String.valueOf(employee.getCategory()));

        //we will use these buttons later for update and delete operation

        textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProduct(product);
            }
        });
        return view;

    }

    private void updateProduct(final Product product) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.dialog_update_product, null);
        builder.setView(view);

        final EditText edUnit = view.findViewById(R.id.edUnit);
        final EditText edRateUnit = view.findViewById(R.id.edRateUnit);
        final EditText edMRP = view.findViewById(R.id.edMRP);
        final EditText edWeight = view.findViewById(R.id.edWeight);

        edMRP.setText(product.getRegularPrice());
        edWeight.setText(product.getWeight());

        final AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.buttonUpdateProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String unit = edUnit.getText().toString().trim();
                String rate = edRateUnit.getText().toString().trim();
                String mrp = edMRP.getText().toString().trim();
                String weight = edWeight.getText().toString().trim();

                if (unit.isEmpty()) {
                    edUnit.setError("Unit can't be blank");
                    edUnit.requestFocus();
                    return;
                }

                if (rate.isEmpty()) {
                    edRateUnit.setError("Rate can't be blank");
                    edRateUnit.requestFocus();
                    return;
                }

                String sql = "UPDATE products \n" +
                        "SET stock_qnt = ?, \n" +
                        "cost_price = ?, \n" +
                        "regular_price = ?, \n" +
                        "weight = ? \n" +
                        "WHERE id = ?;\n";

                mDatabase.execSQL(sql, new String[]{unit, rate, mrp, weight, String.valueOf(product.getId())});
                Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();
                //reloadEmployeesFromDatabase();

                dialog.dismiss();
            }
        });


    }

    private void reloadEmployeesFromDatabase() {
        Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM products", null);
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
        notifyDataSetChanged();
    }
}

