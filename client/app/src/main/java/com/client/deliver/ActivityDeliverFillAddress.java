package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.client.R;

public class ActivityDeliverFillAddress extends AppCompatActivity implements View.OnClickListener {
    TextView txtAddress;
    EditText buildingAddress, pinCode, landmark, mobile;
    String choose = "";
    ImageButton confirm;
    private static final String TAG = "ActivityDeliverFillAddress";

    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String ADDRESS_PICK = "com.client.ride.AddressPick";
    public static final String ADDRESS_DROP = "com.client.ride.AddressDrop";
    public static final String PICK_LANDMARK = "com.client.ride.PickLandmark";
    public static final String DROP_LANDMARK = "com.client.ride.DropLandmark";
    public static final String PICK_PIN = "com.client.ride.PickPin";
    public static final String DROP_PIN = "com.client.ride.DropPin";
    public static final String PICK_MOBILE = "com.client.ride.PickMobile";
    public static final String DROP_MOBILE = "com.client.ride.DropMobile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_fill_address);

        //initializing views
        txtAddress = findViewById(R.id.txt_address);
        buildingAddress = findViewById(R.id.address);
        pinCode = findViewById(R.id.pin_code);
        landmark = findViewById(R.id.landmark);
        mobile = findViewById(R.id.mobile);
        confirm = findViewById(R.id.confirm_address);

        confirm.setOnClickListener(this);

        Intent intent = getIntent();
        String fillRequest = intent.getStringExtra("FillPick");
        assert fillRequest != null;
        if (fillRequest.equals("pick")) {
            choose = "pick";
            txtAddress.setText("PICKUP ADDRESS");

        } else if (fillRequest.equals("drop")) {
            choose = "drop";
            txtAddress.setText("Delivery Address");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_address) {

            if (choose.equals("pick")){
                SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(ADDRESS_PICK, buildingAddress.getText().toString());
                editor.putString(PICK_PIN, pinCode.getText().toString());
                editor.putString(PICK_LANDMARK,  landmark.getText().toString());
                editor.putString(PICK_MOBILE, mobile.getText().toString());
                editor.apply();
                Log.d(TAG,"choose = pick");
            }
            if (choose.equals("drop")){
                SharedPreferences pref = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(ADDRESS_DROP, buildingAddress.getText().toString());
                editor.putString(DROP_PIN, pinCode.getText().toString());
                editor.putString(DROP_LANDMARK,  landmark.getText().toString());
                editor.putString(DROP_MOBILE, mobile.getText().toString());
                editor.apply();
                Log.d(TAG,"choose = drop");
            }
            Intent addIntent = new Intent(ActivityDeliverFillAddress.this, ActivityDeliverHome.class);
            startActivity(addIntent);
        }
    }
}
