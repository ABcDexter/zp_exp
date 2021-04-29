package com.clientzp.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.clientzp.ActivityWelcome;
import com.clientzp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    NotificationManager notificationManager = null;
    String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //Log.d("NEW_TOKEN", s);
    }

   /* Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            sendNotification(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /*super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null)
            getImage(remoteMessage);*/
        Intent notificationIntent;
        if (ActivityWelcome.isAppRunning) {
            notificationIntent = new Intent(this, ActivityWelcome.class);
        } else {
            notificationIntent = new Intent(this, ActivityWelcome.class);
        }
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationId = new Random().nextInt(60000);
        //Bitmap bitmap = getBitmapfromUrl(remoteMessage.getData().get("imageUrl")); //obtain the image
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.zippe_logo__);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setLargeIcon(icon) //set it in the notification
                .setSmallIcon(R.drawable.zippe_logo__)  //a resource for your custom small icon
                .setContentTitle(remoteMessage.getData().get("title")) //the "title" value you sent in your notification
                .setContentText(remoteMessage.getData().get("message")) //ditto
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());

    }

    //Simple method for image downloading
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

}