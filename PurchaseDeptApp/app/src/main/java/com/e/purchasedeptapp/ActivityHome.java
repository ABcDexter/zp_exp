package com.e.purchasedeptapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;


public class ActivityHome extends ActivityDrawer implements View.OnClickListener {
    private static final String TAG = "ActivityHome";
    public static final String AADHAR = "Aadhar";
    public static final String AUTH_KEY = "Auth";
    public static final String AUTH_COOKIE = "com.purchasedeptapp.cookie";

    private static ActivityHome instance;
    ActivityHome a = ActivityHome.this;
    Map<String, String> params = new HashMap();

    Dialog myDialog;
    ScrollView scrollView;
    String aadhar, auth, strAuth;
    Vibrator vibrator;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE};

    Button btnSpices, btnFlour, btnOil, btnSugar, btnDairy, btnSnacks, btnConfectionary,
            btnBreakfastEss, btnBeverages, btnPremixes, btnDryFruits, btnCookingPaste, btnBaking,
            btnReadyMeals, btnPackaged, btnFrozenVeg, btnFrozenNonVeg, btnPaperTowels, btnMiscHousehold,
            btnSkinCare, btnHairCare, btnOralCare, btnDeos, btnShaving, btnBabyCare, btnDiaperWipes,
            btnFemaleHydiene, btnEverydayMeds, btnCleaners;

    //When an Activity first call or launched then onCreate(Bundle savedInstanceState) method is responsible to create the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don’t set any content view here, since its already set in ActivityDrawer
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_home, null, false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        instance = this;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);// initializing vibration service for this activity

        SharedPreferences cookie = getSharedPreferences(AUTH_COOKIE, Context.MODE_PRIVATE);
        strAuth = cookie.getString(AUTH_KEY, ""); // retrieve auth value stored locally and assign it to String auth
        aadhar = cookie.getString(AADHAR, "");// retrieve aadhaar value stored locally and assign it to String aadhar

        //initializing variables

        scrollView = findViewById(R.id.scrollLayout);

        myDialog = new Dialog(this);
        auth = strAuth;
        btnSpices = findViewById(R.id.cat_spices_masala_herbs);
        btnFlour = findViewById(R.id.cat_flour_grains_pulses);
        btnOil = findViewById(R.id.cat_oil_ghee);
        btnSugar = findViewById(R.id.cat_sugar_sweetners);
        btnDairy = findViewById(R.id.cat_dairy);
        btnSnacks = findViewById(R.id.cat_snacks);
        btnConfectionary = findViewById(R.id.cat_confectionary_dessert);
        btnBreakfastEss = findViewById(R.id.cat_breakfast_essentials_cereals);
        btnBeverages = findViewById(R.id.cat_beverages);
        btnPremixes = findViewById(R.id.cat_premixes);
        btnDryFruits = findViewById(R.id.cat_dry_fruit);
        btnCookingPaste = findViewById(R.id.cat_cooking_paste);
        btnBaking = findViewById(R.id.cat_baking_essentials);
        btnReadyMeals = findViewById(R.id.cat_ready_meals_mixes);
        btnPackaged = findViewById(R.id.cat_packaged_essentials);
        btnFrozenVeg = findViewById(R.id.cat_frozen_packaged_veg);
        btnFrozenNonVeg = findViewById(R.id.cat_frozen_packaged_non_Veg);
        btnCleaners = findViewById(R.id.cat_cleaners);
        btnPaperTowels = findViewById(R.id.cat_paper_towels);
        btnMiscHousehold = findViewById(R.id.cat_misc_household);
        btnSkinCare = findViewById(R.id.cat_skin_care);
        btnHairCare = findViewById(R.id.cat_hair_care);
        btnOralCare = findViewById(R.id.cat_oral_care);
        btnDeos = findViewById(R.id.cat_deos_perfumes);
        btnShaving = findViewById(R.id.cat_shaving_hair_removal);
        btnBabyCare = findViewById(R.id.cat_baby_care);
        btnDiaperWipes = findViewById(R.id.cat_diapers_wipes);
        btnFemaleHydiene = findViewById(R.id.cat_female_hygiene);
        btnEverydayMeds = findViewById(R.id.cat_everyday_medicine_sexual_wellness);

        btnSpices.setOnClickListener(this);
        btnFlour.setOnClickListener(this);
        btnOil.setOnClickListener(this);
        btnSugar.setOnClickListener(this);
        btnDairy.setOnClickListener(this);
        btnSnacks.setOnClickListener(this);
        btnConfectionary.setOnClickListener(this);
        btnBreakfastEss.setOnClickListener(this);
        btnBeverages.setOnClickListener(this);
        btnPremixes.setOnClickListener(this);
        btnDryFruits.setOnClickListener(this);
        btnCookingPaste.setOnClickListener(this);
        btnBaking.setOnClickListener(this);
        btnReadyMeals.setOnClickListener(this);
        btnPackaged.setOnClickListener(this);
        btnFrozenVeg.setOnClickListener(this);
        btnFrozenNonVeg.setOnClickListener(this);
        btnCleaners.setOnClickListener(this);
        btnPaperTowels.setOnClickListener(this);
        btnMiscHousehold.setOnClickListener(this);
        btnSkinCare.setOnClickListener(this);
        btnHairCare.setOnClickListener(this);
        btnOralCare.setOnClickListener(this);
        btnDeos.setOnClickListener(this);
        btnShaving.setOnClickListener(this);
        btnBabyCare.setOnClickListener(this);
        btnDiaperWipes.setOnClickListener(this);
        btnFemaleHydiene.setOnClickListener(this);
        btnEverydayMeds.setOnClickListener(this);

    }

    public static ActivityHome getInstance() {
        return instance;
    }

    //method to check if permissions are granted to the app by the driver

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cat_spices_masala_herbs) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivitySpicesMasalaHerbs.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_flour_grains_pulses) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityFlourGrainsPulses.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_oil_ghee) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityOilGhee.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_sugar_sweetners) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivitySugarSweetners.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_dairy) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityDairy.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_snacks) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivitySnacks.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_confectionary_dessert) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityConfectionary.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_breakfast_essentials_cereals) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityBreakfastEss.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_beverages) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityBeverages.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_premixes) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityPremixes.class);
            startActivity(newOrderIntent);
            finish();

        } else if (id == R.id.cat_dry_fruit) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityDryFruits.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_cooking_paste) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityCookingPaste.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_baking_essentials) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityBaking.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_ready_meals_mixes) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityReadyMeals.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_packaged_essentials) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityPackagedEss.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_frozen_packaged_veg) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityFrozenVeg.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_frozen_packaged_non_Veg) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityFrozenNonVeg.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_cleaners) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityCleaners.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_paper_towels) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityPaperTowel.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_misc_household) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityMiscHousehold.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_skin_care) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivitySkinCare.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_hair_care) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityHairCare.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_oral_care) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityOralCare.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_deos_perfumes) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityDeos.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_shaving_hair_removal) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityShavingHair.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_baby_care) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityBabyCare.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_diapers_wipes) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityDiapers.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_female_hygiene) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityFemaleHygiene.class);
            startActivity(newOrderIntent);
            finish();
        } else if (id == R.id.cat_everyday_medicine_sexual_wellness) {
            Intent newOrderIntent = new Intent(ActivityHome.this, ActivityEverydayMeds.class);
            startActivity(newOrderIntent);
            finish();
        }
    }
}
