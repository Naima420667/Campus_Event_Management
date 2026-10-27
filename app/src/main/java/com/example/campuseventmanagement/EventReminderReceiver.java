package com.example.campuseventmanagement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "eventnest_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String eventName = intent.getStringExtra("event_name");
        int eventId = intent.getIntExtra("event_id", 1);

        if (eventName == null) {
            eventName = "Upcoming Event";
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("Reminders for upcoming EventNest events");
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, CalendarActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                eventId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        android.app.Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Event Reminder")
                        .setContentText(eventName + " is coming up soon!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(eventName +
                                        " is scheduled for tomorrow. Don't forget!"))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        try {
            NotificationManagerCompat.from(context)
                    .notify(eventId, notification);
        } catch (SecurityException ignored) {
        }
    }
}