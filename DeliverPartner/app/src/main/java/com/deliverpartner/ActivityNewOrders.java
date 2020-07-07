package com.deliverpartner;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.deliverpartner.App.CHANNEL_ID;

public class ActivityNewOrders extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);
        notificationManager = NotificationManagerCompat.from(this);


    }

    public void showNotification(View v) {
        Log.d("dfghj", "inside showNotification()");
        RemoteViews collapsedView = new RemoteViews(getPackageName(),
                R.layout.notification_collapsed);
        RemoteViews expandedView = new RemoteViews(getPackageName(),
                R.layout.notification_expanded);
        Intent clickIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this,
                0, clickIntent, 0);
        collapsedView.setTextViewText(R.id.text_view_collapsed_1, "Hello World!");
        expandedView.setImageViewResource(R.id.image_view_expanded, R.drawable.ic_home_black);
        expandedView.setOnClickPendingIntent(R.id.image_view_expanded, clickPendingIntent);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .build();
        notificationManager.notify(1, notification);
    }

}
