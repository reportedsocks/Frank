package com.app.frank.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.frank.R;
import com.app.frank.SplashScreen;

import java.util.Random;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Chooses random text and title for notification and shows it to user
        //Important: title array and text array have to be the same length
        String[] titles = intent.getStringArrayExtra("Notification title");
        String[] texts = intent.getStringArrayExtra("Notification text");
        int random = new Random().nextInt(titles.length);

        Intent notificationIntent = new Intent(context, SplashScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Chanel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titles[random])
                .setContentText(texts[random])
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1234567, builder.build());
    }

}
