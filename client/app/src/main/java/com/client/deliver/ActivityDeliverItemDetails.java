package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import com.client.ActivityDrawer;
import com.client.R;

public class ActivityDeliverItemDetails extends ActivityDrawer implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ActivityDeliverItemDetails";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String FLAMMABLE = "flammable";
    public static final String FRAGILE = "fragile";
    public static final String LIQUID = "liquid";
    public static final String KEEP_WARM = "keep_warm";
    public static final String KEEP_COLD = "keep_cold";
    public static final String KEEP_DRY = "keep_dry";
    public static final String ADD_INFO = "";

    CheckBox fr, fl, li, kc, kd, kw;
    EditText details;
    ImageButton confirm;
    String Fr, Fl, Li, Kc, Kd, Kw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_item_details);

        //initializing views
        fr = findViewById(R.id.fr);
        fl = findViewById(R.id.fl);
        li = findViewById(R.id.li);
        kc = findViewById(R.id.kc);
        kd = findViewById(R.id.kd);
        kw = findViewById(R.id.kw);
        details = findViewById(R.id.add_details);
        confirm = findViewById(R.id.confirm_deliver);

        confirm.setOnClickListener(this);

        /*if (fr.isChecked())
            {Fr = "1";}
        else {Fr = "0";}
        if (fl.isChecked())
            {Fl = "1";}
        else {Fl = "0";}
        if (li.isChecked())
            {Li = "1";}
        else {Li = "0";}
        if (kc.isChecked())
            {Kc = "1";}
        else {Kc = "0";}
        if (kd.isChecked())
            {Kd = "1";}
        else {Kd = "0";}
        if (kw.isChecked())
            {Kw = "1";}
        else {Kw = "0";}*/
        fr.setOnCheckedChangeListener(this);
        fl.setOnCheckedChangeListener(this);
        li.setOnCheckedChangeListener(this);
        kc.setOnCheckedChangeListener(this);
        kd.setOnCheckedChangeListener(this);
        kw.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_deliver:
                /*if (fr.isChecked())
                    Fr = "1";
                else Fr = "0";
                if (fl.isChecked())
                    Fl = "1";
                else Fl = "0";
                if (li.isChecked())
                    Li = "1";
                else Li = "0";
                if (kc.isChecked())
                    Kc = "1";
                else Kc = "0";
                if (kd.isChecked())
                    Kd = "1";
                else Kd = "0";
                if (kw.isChecked())
                    Kw = "1";
                else Kw = "0";*/

                storeData();

                Intent confirm = new Intent(ActivityDeliverItemDetails.this, ActivityDeliverConfirm.class);
                startActivity(confirm);
        }
    }

    private void storeData() {
        String DETAILS = details.getText().toString();
        DETAILS = DETAILS.replaceAll("[^a-zA-Z0-9]", "");
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FRAGILE, Fr);
        editor.putString(FLAMMABLE, Fl);
        editor.putString(LIQUID, Li);
        editor.putString(KEEP_WARM, Kw);
        editor.putString(KEEP_DRY, Kd);
        editor.putString(KEEP_COLD, Kc);
        editor.putString(ADD_INFO, DETAILS);
        editor.apply();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (fr.isChecked())
        {Fr = "1";}
        else {Fr = "0";}
        if (fl.isChecked())
        {Fl = "1";}
        else {Fl = "0";}
        if (li.isChecked())
        {Li = "1";}
        else {Li = "0";}
        if (kc.isChecked())
        {Kc = "1";}
        else {Kc = "0";}
        if (kd.isChecked())
        {Kd = "1";}
        else {Kd = "0";}
        if (kw.isChecked())
        {Kw = "1";}
        else {Kw = "0";}

    }
}
