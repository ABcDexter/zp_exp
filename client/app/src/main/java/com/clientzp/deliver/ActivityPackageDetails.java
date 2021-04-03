package com.clientzp.deliver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.clientzp.ActivityDrawer;
import com.clientzp.R;
import com.google.android.material.snackbar.Snackbar;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityPackageDetails extends ActivityDrawer implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static final Integer[] IMAGES = {R.drawable.delivery_man};
    private final ArrayList<Integer> ImagesArray = new ArrayList<Integer>();
    String ContentType = "";
    String ContentSize = "";
    String ctype = "", csize="";//TODO find better way
    Dialog careDialog, contentDialog, sizeDialog;
    EditText specify;
    RelativeLayout rlSpecify;
    ImageView check;
    Button packageDetails, packageSize, packageCare;
    Vibrator vibrator;
    ImageButton next;
    public static final String REVIEW = "com.delivery.Review";//TODO find better way
    public static final String R_C_TYPE = "CTYPE";
    public static final String R_C_SIZE = "CSIZE";
    public static final String R_C_FRAGILE = "CFRAGILE";
    public static final String R_C_LIQUID = "CLIQUID";
    public static final String R_C_COLD = "CCOLD";
    public static final String R_C_WARM = "CWARM";
    public static final String R_C_PERISHABLE = "CPERISHABLE";
    public static final String R_C_NONE = "CNONE";
    public static final String CONTENT_TYPE = "com.delivery.ride.ContentType";
    public static final String CONTENT_DIM = "com.delivery.ride.ContentDimensions";
    public static final String AUTH_KEY = "AuthKey";
    public static final String AN_KEY = "AadharKey";
    public static final String FRAGILE = "fragile";
    public static final String LIQUID = "liquid";
    public static final String KEEP_WARM = "keep_warm";
    public static final String KEEP_COLD = "keep_cold";
    public static final String PERISHABLE = "Perishable";
    public static final String NONE = "None";
    public static final String PREFS_ADDRESS = "com.clientzp.ride.Address";
    String Fr = "0", Li = "0", Kc = "0", Kw = "0", Pr = "0", None = "0";
    String rFr = "", rLi = "", rKc = "", rKw = "", rPr = "", rNone = "";
    CheckBox fragile, cold, warm, liquid, perishable, none;
    ScrollView scrollView;
    boolean care = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_package_details, null, false);
        frameLayout.addView(activityView);
        init();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        scrollView = findViewById(R.id.scrollPackageDelivery);
        packageDetails = findViewById(R.id.content_type);
        packageSize = findViewById(R.id.content_size);
        packageCare = findViewById(R.id.add_on);
        next = findViewById(R.id.next_deliver);

        packageDetails.setOnClickListener(this);
        packageSize.setOnClickListener(this);
        packageCare.setOnClickListener(this);
        next.setOnClickListener(this);

        contentDialog = new Dialog(this);
        sizeDialog = new Dialog(this);
        careDialog = new Dialog(this);
        //retriveData();

    }

    private void init() {
        for (int i = 0; i < IMAGES.length; i++)
            ImagesArray.add(IMAGES[i]);

        mPager = (ViewPager) findViewById(R.id.pager);


        mPager.setAdapter(new SlidingImage_Adapter(ActivityPackageDetails.this, ImagesArray));


        CirclePageIndicator indicator = (CirclePageIndicator)
                findViewById(R.id.indicator);

        indicator.setViewPager(mPager);

        final float density = getResources().getDisplayMetrics().density;

        //Set circle indicator radius
        indicator.setRadius(5 * density);

        NUM_PAGES = IMAGES.length;

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 5000, 10000);

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.content_type) {
            PopupContent();
        } else if (id == R.id.rl_content_1) {
            packageDetails.setText(R.string.documents_books);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "DOC";
            ctype = "Documents / Books";
            contentDialog.dismiss();
        } else if (id == R.id.rl_content_2) {
            packageDetails.setText(R.string.restaurant_orders);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "FOO";
            ctype = "Restaurant Orders";
            contentDialog.dismiss();
        } else if (id == R.id.rl_content_3) {
            packageDetails.setText(R.string.household_items);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "HOU";
            ctype = "Household Items";
            contentDialog.dismiss();
        } else if (id == R.id.rl_content_4) {
            packageDetails.setText(R.string.electronics_electrical);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "ELE";
            ctype = "Electronics / electrical";
            contentDialog.dismiss();
        } else if (id == R.id.rl_content_5) {
            packageDetails.setText(R.string.clothes_accessories);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "CLO";
            ctype = "Clothes / Accessories";
            contentDialog.dismiss();
        } else if (id == R.id.rl_content_7) {
            packageDetails.setText(R.string.medicines);
            packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentType = "MED";
            ctype = "Medicines";
            contentDialog.dismiss();
        } else if (id == R.id.other) {
            rlSpecify.setVisibility(View.VISIBLE);
            contentDialog.setCanceledOnTouchOutside(false);
        } else if (id == R.id.checkTick) {
            String details = specify.getText().toString();
            if (!details.isEmpty()) {
                packageDetails.setText(details);
                packageDetails.setBackgroundResource(R.drawable.rect_box_outline_color_change);
                ContentType = details;
                ctype = details;
                contentDialog.dismiss();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                    packageDetails.setBackgroundResource(R.drawable.rect_box_outline);
                }
            }
        } else if (id == R.id.content_size) {
            PopupSize();
        } else if (id == R.id.s_1) {
            packageSize.setText(R.string.small);
            packageSize.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentSize = "S";
            csize = "Small";
            sizeDialog.dismiss();
        } else if (id == R.id.s_2) {
            packageSize.setText(R.string.medium);
            packageSize.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentSize = "M";
            csize = "Medium";
            sizeDialog.dismiss();
        } else if (id == R.id.s_3) {
            packageSize.setText(R.string.large);
            packageSize.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentSize = "L";
            csize = "Large";
            sizeDialog.dismiss();
        } else if (id == R.id.s_4) {
            packageSize.setText(R.string.x_large);
            packageSize.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentSize = "XL";
            csize = "X-Large";
            sizeDialog.dismiss();
        } else if (id == R.id.s_5) {
            packageSize.setText(R.string.x_x_large);
            packageSize.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            ContentSize = "XXL";
            csize = "XX-Large";
            sizeDialog.dismiss();
        } else if (id == R.id.add_on) {
            PopupCare();
        } else if (id == R.id.confirm) {

            packageCare.setBackgroundResource(R.drawable.rect_box_outline_color_change);
            careDialog.dismiss();
            storeCareData();
        } else if (id == R.id.next_deliver) {
            if (ContentType.equals("") || ContentSize.equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
                Log.d("PackageDetails", " ContentType=" + ContentType + " ContentSize=" + ContentSize);
                Snackbar snackbar = Snackbar.make(scrollView, "All Fields Mandatory ", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();

            } else {
                storeData();
                Intent confirmIntent = new Intent(ActivityPackageDetails.this, ActivityFillPickDetails.class);
                startActivity(confirmIntent);
            }
        } else if (id == R.id.slidingImage) {
            Intent full = new Intent(ActivityPackageDetails.this, ActivitySlidingFullPage.class);
            startActivity(full);
        }
    }

    private void retriveData() {
        SharedPreferences prefDetails = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        String fr = prefDetails.getString(FRAGILE, "");
        String li = prefDetails.getString(LIQUID, "");
        String kw = prefDetails.getString(KEEP_WARM, "");
        String pe = prefDetails.getString(PERISHABLE, "");
        String kc = prefDetails.getString(KEEP_COLD, "");
        String no = prefDetails.getString(NONE, "");
        SharedPreferences review = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
        String type = review.getString(R_C_TYPE, "");
        String dim = review.getString(R_C_SIZE, "");

        Log.d("ActivityPackageDetails storeCareData()", " Fr" + fr + " Li" +
                li + " Kw" + kw + " Pr" + pe + " Kc" + kc + " none" + no);

        if (fr.equals("1") || li.equals("1") || kw.equals("1") || pe.equals("1") || kc.equals("1")) {
            packageCare.setText(R.string.click_to_view);
        }
        packageDetails.setText(type);
        packageSize.setText(dim);

    }

    private void storeCareData() {
        //TODO find better way
        SharedPreferences reviewPref = this.getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
        SharedPreferences.Editor reditor = reviewPref.edit();
        reditor.putString(R_C_FRAGILE, rFr);
        reditor.putString(R_C_LIQUID, rLi);
        reditor.putString(R_C_WARM, rKw);
        reditor.putString(R_C_PERISHABLE, rPr);
        reditor.putString(R_C_COLD, rKc);
        reditor.putString(R_C_NONE, rNone);
        reditor.apply();

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FRAGILE, Fr);
        editor.putString(LIQUID, Li);
        editor.putString(KEEP_WARM, Kw);
        editor.putString(PERISHABLE, Pr);
        editor.putString(KEEP_COLD, Kc);
        editor.putString(NONE, None);
        editor.apply();

        Log.d("ActivityPackageDetails storeCareData()", " Fr" + Fr + " Li" +
                Li + " Kw" + Kw + " Pr" + Pr + " Kc" + Kc + " none" + None);

        if (Fr.equals("1") || Li.equals("1") || Kw.equals("1") || Kc.equals("1") || Pr.equals("1")) {

            packageCare.setText(R.string.click_to_view);
        }
    }

    private void storeData() {
        //String DETAILS = details.getText().toString();
        //DETAILS = DETAILS.replaceAll("[^a-zA-Z0-9]", "");
        if (!care) {
            None = "1";
        }
        SharedPreferences reviewPref = getSharedPreferences(REVIEW, Context.MODE_PRIVATE);
        SharedPreferences.Editor reditor = reviewPref.edit();
        reditor.putString(R_C_TYPE, ctype);
        reditor.putString(R_C_SIZE, csize);
        reditor.apply();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTENT_TYPE, ContentType);
        editor.putString(CONTENT_DIM, ContentSize);
        editor.apply();

        Log.d("ActivityPackageDetails", " Fr" + Fr + " Li" + Li + " Kw" + Kw + " Pr" + Pr + " Kc"
                + Kc + " none" + None + " CONTENT_TYPE" + ContentType + " CONTENT_DIM" + ContentSize);
    }

    private void PopupCare() {
        SharedPreferences prefCare = getSharedPreferences(PREFS_ADDRESS, Context.MODE_PRIVATE);
        String careFr = prefCare.getString(FRAGILE, "");
        String careLi = prefCare.getString(LIQUID, "");
        String careKc = prefCare.getString(KEEP_COLD, "");
        String careKw = prefCare.getString(KEEP_WARM, "");
        String carePe = prefCare.getString(PERISHABLE, "");
        String careNo = prefCare.getString(NONE, "");

        careDialog.setContentView(R.layout.popup_checkbox_care);
        //breakable = careDialog.findViewById(R.id.breakable);
        fragile = careDialog.findViewById(R.id.fragile);
        liquid = careDialog.findViewById(R.id.liquid);
        perishable = careDialog.findViewById(R.id.perishable);
        cold = careDialog.findViewById(R.id.cold);
        warm = careDialog.findViewById(R.id.warm);
        none = careDialog.findViewById(R.id.non_of_the_above);
        TextView confirm = careDialog.findViewById(R.id.confirm);

        confirm.setOnClickListener(this);
        if (careFr.equals("1")) {
            fragile.setChecked(true);
            none.setChecked(false);
        }
        if (careLi.equals("1")) {
            liquid.setChecked(true);
            none.setChecked(false);
        }
        if (careKc.equals("1")) {
            cold.setChecked(true);
            none.setChecked(false);
        }
        if (careKw.equals("1")) {
            warm.setChecked(true);
            none.setChecked(false);
        }
        if (carePe.equals("1")) {
            perishable.setChecked(true);
            none.setChecked(false);
        }
        if (careNo.equals("1")) {
            none.setChecked(true);
            fragile.setChecked(false);
            liquid.setChecked(false);
            cold.setChecked(false);
            warm.setChecked(false);
            perishable.setChecked(false);
        }
        fragile.setOnCheckedChangeListener(this);
        liquid.setOnCheckedChangeListener(this);
        perishable.setOnCheckedChangeListener(this);
        cold.setOnCheckedChangeListener(this);
        warm.setOnCheckedChangeListener(this);
        none.setOnCheckedChangeListener(this);

        careDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = careDialog.getWindow().getAttributes();

        wmlp.y = 80;   //y position
        careDialog.show();
        Window window = careDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        careDialog.setCanceledOnTouchOutside(false);
    }

    private void PopupContent() {

        contentDialog.setContentView(R.layout.popup_content_type);
        RelativeLayout rl1 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_1);
        RelativeLayout rl2 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_2);
        RelativeLayout rl3 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_3);
        RelativeLayout rl4 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_4);
        RelativeLayout rl5 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_5);
        RelativeLayout rl7 = (RelativeLayout) contentDialog.findViewById(R.id.rl_content_7);
        RelativeLayout other = (RelativeLayout) contentDialog.findViewById(R.id.other);
        rlSpecify = (RelativeLayout) contentDialog.findViewById(R.id.rl_specify);
        specify = (EditText) contentDialog.findViewById(R.id.specify_content);
        check = (ImageView) contentDialog.findViewById(R.id.checkTick);
        rl1.setOnClickListener(this);
        rl2.setOnClickListener(this);
        rl3.setOnClickListener(this);
        rl4.setOnClickListener(this);
        rl5.setOnClickListener(this);
        rl7.setOnClickListener(this);
        other.setOnClickListener(this);
        check.setOnClickListener(this);
        contentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = contentDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        contentDialog.show();
        Window window = contentDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        contentDialog.setCanceledOnTouchOutside(true);
    }

    private void PopupSize() {

        sizeDialog.setContentView(R.layout.popup_content_size);
        RelativeLayout s1 = (RelativeLayout) sizeDialog.findViewById(R.id.s_1);
        RelativeLayout s2 = (RelativeLayout) sizeDialog.findViewById(R.id.s_2);
        RelativeLayout s3 = (RelativeLayout) sizeDialog.findViewById(R.id.s_3);
        RelativeLayout s4 = (RelativeLayout) sizeDialog.findViewById(R.id.s_4);
        RelativeLayout s5 = (RelativeLayout) sizeDialog.findViewById(R.id.s_5);

        TextView t1 = (TextView) sizeDialog.findViewById(R.id.size1);
        TextView t2 = (TextView) sizeDialog.findViewById(R.id.size2);
        TextView t3 = (TextView) sizeDialog.findViewById(R.id.size3);
        TextView t4 = (TextView) sizeDialog.findViewById(R.id.size4);
        TextView t5 = (TextView) sizeDialog.findViewById(R.id.size5);
        Switch switchUnit = (Switch) sizeDialog.findViewById(R.id.size_switch);
        TextView inch = sizeDialog.findViewById(R.id.inch);
        switchUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inch.setTextColor(ContextCompat.getColor(ActivityPackageDetails.this, R.color.colorPrimaryDark));
                    t1.setText("(14 x 10 x 5 inch)");
                    t2.setText("(28 x 20 x 10 inch)");
                    t3.setText("(42 x 30 x 15 inch)");
                    t4.setText("(42 x 40 x 20 inch)");
                    t5.setText("(69 x 50 x 25 inch)");
                } else {
                    inch.setTextColor(Color.WHITE);

                    t1.setText("(35 x 25 x 13 cm)");
                    t2.setText("(70 x 50 x 26 cm)");
                    t3.setText("(105 x 75 x 39 cm)");
                    t4.setText("(104 x 100 x 52 cm)");
                    t5.setText("(175 x 125 x 65 cm)");

                }
            }
        });

        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);
        s5.setOnClickListener(this);

        sizeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = contentDialog.getWindow().getAttributes();

        //wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        //wmlp.x = 100;   //x position
        wmlp.y = 80;   //y position
        sizeDialog.show();
        Window window = sizeDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        sizeDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        if (id == R.id.fragile) {
            if (fragile.isChecked()) {
                care = true;
                Fr = "1";
                none.setChecked(false);
                None = "0";
                rFr = "fragile";
            } else {
                Fr = "0";
                rFr = "";
            }
        }
        if (id == R.id.liquid) {
            if (liquid.isChecked()) {
                care = true;
                Li = "1";
                none.setChecked(false);
                None = "0";
                rLi = "contains liquid";
            } else {
                Li = "0";
                rLi = "";
            }
        }
        if (id == R.id.perishable) {
            if (perishable.isChecked()) {
                care = true;
                Pr = "1";
                none.setChecked(false);
                None = "0";
                rPr = "perishable";
            } else {
                Pr = "0";
                rPr = "";
            }
        }
        if (id == R.id.cold) {
            if (cold.isChecked()) {
                warm.setChecked(false);
                care = true;
                Kc = "1";
                Kw = "0";
                none.setChecked(false);
                None = "0";
                rKc = "Keep cold";
                rKw = "";
            } else {
                Kc = "0";
                rKc = "";
            }
        }
        if (id == R.id.warm) {
            if (warm.isChecked()) {
                cold.setChecked(false);
                care = true;
                Kw = "1";
                Kc = "0";
                none.setChecked(false);
                None = "0";
                rKw = "keep warm";
                rKc = "";

            } else {
                Kw = "";
                rKw = "";
            }
        }
        if (id == R.id.non_of_the_above) {
            if (none.isChecked()) {
                care = true;
                None = "1";
                fragile.setChecked(false);
                liquid.setChecked(false);
                perishable.setChecked(false);
                cold.setChecked(false);
                warm.setChecked(false);
                rNone = "none";
            } else {
                None = "0";
                rNone = "";
            }
        }
    }
}
