package com.clientzp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AadharCardUpload extends ActivityDrawer implements View.OnClickListener {
    private ImageView imgAadharFront, imgAadharBack;
    private static final String TAG = "AadharCardUpload";
    ScrollView scrollView;
    private ImageButton upload;
    public static final String AUTH_KEY = "AuthKey";
    public static final String SESSION_COOKIE = "com.clientzp.ride.Cookie";
    String aadhar_f, aadhar_b;
    Bitmap bitmap;
    ProgressBar simpleProgressBar;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET};
    String stringAuth;
    SharedPreferences prefAuth;

    public void onSuccess(JSONObject response) throws JSONException {
        //Log.d(TAG + "jsObjRequest", "RESPONSE:" + response);
        //response on hitting auth-aadhaar-save API
        Intent home = new Intent(AadharCardUpload.this, UserProfileActivity.class);
        startActivity(home);
        finish();
    }

    public void onFailure(VolleyError error) {
        /*Log.d(TAG, "onErrorResponse: " + error.toString());
        Log.d(TAG, "Error:" + error.toString());*/
        Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        simpleProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = findViewById(R.id.activity_frame);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View activityView = layoutInflater.inflate(R.layout.activity_aadhar_card_upload, null, false);
        frameLayout.addView(activityView);

        prefAuth = getSharedPreferences(SESSION_COOKIE, Context.MODE_PRIVATE);
        stringAuth = prefAuth.getString(AUTH_KEY, "");

        scrollView = findViewById(R.id.mainLayout);
        upload = findViewById(R.id.uploadImages);
        imgAadharBack = findViewById(R.id.aadharBackImg);
        imgAadharBack.setOnClickListener(this);
        imgAadharFront = findViewById(R.id.aadharFrontImg);
        imgAadharFront.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.aadharFrontImg) {
            Log.d(TAG, "aadharFrontImg clicked");
            selectImage(AadharCardUpload.this, 1);

        } else if (id == R.id.aadharBackImg) {
            selectImage(AadharCardUpload.this, 2);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void selectImage(Context context, int fromRID) {
        Log.d(TAG, " inside selectImage");
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d(TAG, " inside else statement of selectImage");
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (fromRID == 1) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 1);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 2);

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                    if (fromRID == 2) {
                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 3);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 4);//one can be replaced with any action code

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                }
            });
            builder.show();
        }
    }

    private void nextActivity(String aadharF, String aadharB) {

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

                //Log.d(TAG, "Control came to nextActivity()");
                simpleProgressBar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap();
                String auth = stringAuth;
                params.put("auth", auth);
                params.put("aadhaarFront", aadharF);
                params.put("aadhaarBack", aadharB);
                JSONObject parameters = new JSONObject(params);
                AadharCardUpload a = AadharCardUpload.this;
                /*Log.d(TAG, "Values: aadhaarFront=" + aadharF + " aadhaarBack=" + aadharB+ " auth="+auth);
                Log.d(TAG, "UtilityApiRequestPost.doPOST API NAME auth-aadhaar-save");*/
                UtilityApiRequestPost.doPOST(a, "auth-aadhaar-save", parameters, 30000, 0, response -> {
                    try {
                        a.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, a::onFailure);
            }
        });
    }

    public void convertAndUpload(int identify) {

        if (identify == 1 || identify == 2) {

            imgAadharFront.buildDrawingCache();
            bitmap = imgAadharFront.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_f = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            /*Log.d(TAG, "aadhar front converted to Base64" + aadhar_f);
            Log.d(TAG, "Control moved to nextActivity()");*/
        }
        if (identify == 3 || identify == 4) {
            imgAadharBack.buildDrawingCache();
            bitmap = imgAadharBack.getDrawingCache();
            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            aadhar_b = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            /*Log.d(TAG, "aadhar back converted to Base64");
            Log.d(TAG, "Control moved to nextActivity()");*/
        }
        nextActivity(aadhar_f, aadhar_b);
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharFront.setImageBitmap(selectedImage);
                        convertAndUpload(1);
                    }
                    break;
                case 2:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            imgAadharFront.setImageBitmap(selectedImage);
                            convertAndUpload(2);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(AadharCardUpload.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(AadharCardUpload.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
                    }
                    /*if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imgAadharFront.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                convertAndUpload(2);
                            }
                        }
                    }*/
                    break;
                case 3:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imgAadharBack.setImageBitmap(selectedImage);
                        convertAndUpload(3);
                    }

                    break;
                case 4:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            imgAadharBack.setImageBitmap(selectedImage);
                            convertAndUpload(4);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(AadharCardUpload.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(AadharCardUpload.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
                    }
                    /*if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                //Log.d(TAG, "image set");
                                imgAadharBack.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                convertAndUpload(4);
                                cursor.close();
                            }
                        }
                    }*/
                    break;
            }
        }

    }

}
