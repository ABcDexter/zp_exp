package com.client.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.client.ActivityDrawer;
import com.client.R;

public class ActivityDeliverItemDetails extends ActivityDrawer implements View.OnClickListener {

    private static final String TAG = "ActivityDeliverItemDetails";
    public static final String PREFS_ADDRESS = "com.client.ride.Address";
    public static final String FLAMMABLE = "flammable";
    public static final String FRAGILE = "fragile";
    public static final String LIQUID = "liquid";
    public static final String KEEP_WARM = "keep_warm";
    public static final String KEEP_COLD = "keep_cold";
    public static final String KEEP_DRY = "keep_dry";
    public static final String ADD_INFO = "";

    EditText details;
    ImageButton confirm;
    String Fr = "0", Fl = "0", Li = "0", Kc = "0", Kd = "0", Kw = "0";
    Button btn1, btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_deliver_item_details, null, false);
        frameLayout.addView(activityView);

        //initializing views
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        details = findViewById(R.id.add_details);
        confirm = findViewById(R.id.confirm_deliver);

        confirm.setOnClickListener(this);

        imageDialog = new Dialog(this);
        dialog1 = new Dialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_deliver:
                storeData();
                Intent confirm = new Intent(ActivityDeliverItemDetails.this, ActivityDeliverConfirm.class);
                startActivity(confirm);
                break;
            case R.id.btn1:
                PopupContentType();
                break;
            case R.id.btn2:
                PopupContentType2();
                break;
            case R.id.ride_rl_1:
                btn2.setText("Keep Dry");
                dialog1.dismiss();
                Kd = "1";
                break;
            case R.id.ride_rl_2:
                btn2.setText("Keep Cold");
                dialog1.dismiss();
                Kc = "1";
                break;
            case R.id.ride_rl_3:
                btn2.setText("Keep Warm");
                dialog1.dismiss();
                Kw = "1";
                break;
            case R.id.rent_rl_1:
                btn1.setText("Fragile");
                imageDialog.dismiss();
                Fr = "1";
                break;
            case R.id.rent_rl_2:
                btn1.setText("Flammable");
                imageDialog.dismiss();
                Fl = "1";
                break;
            case R.id.rent_rl_3:
                btn1.setText("Contains Liquid");
                imageDialog.dismiss();
                Li = "1";
                break;

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

    Dialog imageDialog, dialog1;

    private void PopupContentType() {

        imageDialog.setContentView(R.layout.popup_rent_vehicles);
        RelativeLayout rl1 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_1);
        RelativeLayout rl2 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_2);
        RelativeLayout rl3 = (RelativeLayout) imageDialog.findViewById(R.id.rent_rl_3);

        TextView head = (TextView) imageDialog.findViewById(R.id.txt_head);
        TextView t1 = (TextView) imageDialog.findViewById(R.id.txt1);
        TextView t2 = (TextView) imageDialog.findViewById(R.id.txt2);
        TextView t3 = (TextView) imageDialog.findViewById(R.id.txt3);

        ImageView i1 = (ImageView) imageDialog.findViewById(R.id.img1);
        ImageView i2 = (ImageView) imageDialog.findViewById(R.id.img2);
        ImageView i3 = (ImageView) imageDialog.findViewById(R.id.img3);

        head.setText("Choose");

        t1.setText("Fragile");
        t2.setText("Flammable");
        t3.setText("Contains Liquid");

        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);

        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = imageDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        imageDialog.show();
        Window window = imageDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        imageDialog.setCanceledOnTouchOutside(true);
    }

    private void PopupContentType2() {

        dialog1.setContentView(R.layout.popup_vehicles);
        RelativeLayout rl1 = (RelativeLayout) dialog1.findViewById(R.id.ride_rl_1);
        RelativeLayout rl2 = (RelativeLayout) dialog1.findViewById(R.id.ride_rl_2);
        RelativeLayout rl3 = (RelativeLayout) dialog1.findViewById(R.id.ride_rl_3);

        TextView head = (TextView) dialog1.findViewById(R.id.txt_head);
        TextView t1 = (TextView) dialog1.findViewById(R.id.txt1);
        TextView t2 = (TextView) dialog1.findViewById(R.id.txt2);
        TextView t3 = (TextView) dialog1.findViewById(R.id.txt3);

        ImageView i1 = (ImageView) dialog1.findViewById(R.id.img1);
        ImageView i2 = (ImageView) dialog1.findViewById(R.id.img2);
        ImageView i3 = (ImageView) dialog1.findViewById(R.id.img3);

        head.setText("Choose");

        t1.setText("Keep Dry");
        t2.setText("Keep Cold");
        t3.setText("Keep Warm");

        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);

        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = dialog1.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        dialog1.show();
        Window window = dialog1.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog1.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityDeliverItemDetails.this, ActivityDeliverHome.class));
        finish();
    }
}
