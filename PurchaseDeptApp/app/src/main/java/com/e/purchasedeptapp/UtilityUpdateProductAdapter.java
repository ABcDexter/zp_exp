package com.e.purchasedeptapp;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class UtilityUpdateProductAdapter extends ArrayAdapter<ProductFromApp> {
    Context mCtx;
    int listLayoutRes;
    List<ProductFromApp> productList;
    SQLiteDatabase mDatabase;
    UpdateProductDatabaseHandler db;

    public UtilityUpdateProductAdapter(Context mCtx, int listLayoutRes, List<ProductFromApp> productList, SQLiteDatabase mDatabase) {
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
        ProductFromApp product = productList.get(position);
       // Product product_List = product_list.get(position);

        //getting views
        TextView textViewName = view.findViewById(R.id.txtUplName);
        TextView textViewUnit = view.findViewById(R.id.txtUplUnit);
        TextView textViewRate = view.findViewById(R.id.txtUplRate);
        TextView textViewMRP = view.findViewById(R.id.txtUplMRP);
        TextView textViewWeight = view.findViewById(R.id.txtUplWeight);

        //adding data to views
        textViewName.setText(product.get_name());
        textViewUnit.setText(product.get_regular_price());
        textViewRate.setText(product.get_weight());
        textViewMRP.setText(product.get_key());
        textViewWeight.setText(product.get_key());
        /*textViewName.setText(product.get_name());
        textViewUnit.setText(product.get_stock_quantity());
        textViewRate.setText(product.get_cost_price());
        textViewMRP.setText(product.get_regular_price());
        textViewWeight.setText(product.get_weight());*/

        //we will use these buttons later for update and delete operation

        textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProduct(product);
            }
        });
        db = new UpdateProductDatabaseHandler(mCtx);
        return view;
    }
    private void updateProduct(final ProductFromApp product) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx, R.style.MyAlertDialogStyle);

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.dialog_update_product, null);
        builder.setView(view);

        final TextView txName = view.findViewById(R.id.nameProduct);
        final EditText edUnit = view.findViewById(R.id.edUnit);
        final EditText edRateUnit = view.findViewById(R.id.edRateUnit);
        final EditText edMRP = view.findViewById(R.id.edMRP);
        final EditText edWeight = view.findViewById(R.id.edWeight);

        txName.setText(product.get_name());
        edMRP.setText(product.get_regular_price());
        edWeight.setText(product.get_weight());

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

                Log.d("UtilityUpdateProductAdapter", " name = " + product.get_name() + " id = " + product.getID() + " key = " + product.get_key());
                db.addProduct(new ProductFromApp(product.get_name(), String.valueOf(product.get_key()), unit, rate, mrp, weight));
                Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });
    }
}

