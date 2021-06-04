package com.clientzp.Shop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.clientzp.ActivityDrawer;
import com.clientzp.R;

public class ActivityShopHome extends ActivityDrawer implements View.OnClickListener {

    Button shopCategory, shopHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_shop_home, null, false);
        frameLayout.addView(activityView);

        shopCategory = findViewById(R.id.shop_category);
        shopHistory = findViewById(R.id.shop_history);

        shopHistory.setOnClickListener(this);
        shopCategory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.shop_category) {
            //take user to https://zippe.in/en/shop-by-category/ url
            String shopUrl = "https://zippe.in/en/shop-by-category/";
            Intent shopIntent = new Intent(Intent.ACTION_VIEW);
            shopIntent.setData(Uri.parse(shopUrl));
            startActivity(shopIntent);
        }
        if (id == R.id.shop_history) {
            //take user to https://zippe.in/en/my-account/orders/ url
            String connectUrl = "https://zippe.in/en/my-account/orders/";
            Intent connectIntent = new Intent(Intent.ACTION_VIEW);
            connectIntent.setData(Uri.parse(connectUrl));
            startActivity(connectIntent);
            /*Intent history = new Intent(ActivityShopHome.this, ActivityShopHistoryList.class);
            startActivity(history);
            finish();*/
        }

    }
}