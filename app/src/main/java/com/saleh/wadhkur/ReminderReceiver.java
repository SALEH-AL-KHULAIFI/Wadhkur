package com.saleh.wadhkur;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TYPE =
            "reminder_type";

    private static final String CHANNEL_ID =
            "wadhkur_daily_reminders";

    private static final int MORNING_NOTIFICATION_ID =
            8801;

    private static final int EVENING_NOTIFICATION_ID =
            8802;

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        String type =
                intent.getStringExtra(
                        EXTRA_TYPE
                );

        if (type == null) {
            return;
        }

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                );

        boolean enabled;

        if (ReminderScheduler.TYPE_MORNING.equals(type)) {

            enabled =
                    prefs.getBoolean(
                            "morning_enabled",
                            true
                    );

        } else if (
                ReminderScheduler.TYPE_EVENING.equals(type)
        ) {

            enabled =
                    prefs.getBoolean(
                            "evening_enabled",
                            true
                    );

        } else {

            return;
        }

        /*
         * إذا أوقف المستخدم التنبيه
         * لا نعيد جدولتَه.
         */
        if (!enabled) {
            return;
        }

        /*
         * نعيد جدولة التنبيه أولًا.
         *
         * هذا مهم جدًا:
         * حتى إذا كانت صلاحية الإشعارات مرفوضة،
         * يبقى التنبيه اليومي مجدولًا لليوم التالي.
         */
        if (ReminderScheduler.TYPE_MORNING.equals(type)) {

            ReminderScheduler.scheduleMorning(
                    context
            );

        } else {

            ReminderScheduler.scheduleEvening(
                    context
            );
        }

        /*
         * Android 13+
         * يحتاج POST_NOTIFICATIONS.
         */
        if (Build.VERSION.SDK_INT >= 33) {

            if (
                    context.checkSelfPermission(
                            Manifest.permission.POST_NOTIFICATIONS
                    )
                    != PackageManager.PERMISSION_GRANTED
            ) {
                return;
            }
        }

        createChannel(context);

        DhikrData.Dhikr[] data;

        String title;

        int notificationId;

        if (
                ReminderScheduler.TYPE_MORNING.equals(type)
        ) {

            data = DhikrData.MORNING;

            title =
                    "🌅 أذكار الصباح";

            notificationId =
                    MORNING_NOTIFICATION_ID;

        } else {

            data = DhikrData.EVENING;

            title =
                    "🌆 أذكار المساء";

            notificationId =
                    EVENING_NOTIFICATION_ID;
        }

        if (
                data == null ||
                data.length == 0
        ) {
            return;
        }

        /*
         * اختيار ذكر كامل من القائمة.
         */
        int index =
                new Random().nextInt(
                        data.length
                );

        String text =
                data[index].text;

        Intent openIntent =
                new Intent(
                        context,
                        MainActivity.class
                );

        openIntent.putExtra(
                "open_section",
                type
        );

        openIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent openPendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                |
                        PendingIntent.FLAG_IMMUTABLE
                );

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= 26) {

            builder =
                    new Notification.Builder(
                            context,
                            CHANNEL_ID
                    );

        } else {

            builder =
                    new Notification.Builder(
                            context
                    );
        }

        builder
                .setSmallIcon(
                        android.R.drawable.ic_popup_reminder
                )
                .setContentTitle(
                        title
                )
                .setContentText(
                        text
                )
                .setStyle(
                        new Notification.BigTextStyle()
                                .bigText(text)
                )
                .setAutoCancel(
                        true
                )
                .setContentIntent(
                        openPendingIntent
                )
                .setPriority(
                        Notification.PRIORITY_DEFAULT
                )
                .setVibrate(
                        new long[]{
                                0,
                                180,
                                100,
                                180
                        }
                );

        manager.notify(
                notificationId,
                builder.build()
        );
    }

    private void createChannel(
            Context context
    ) {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "تذكيرات وذكر",
                        NotificationManager.IMPORTANCE_DEFAULT
                );

        channel.setDescription(
                "تنبيهات أذكار الصباح والمساء"
        );

        channel.enableVibration(
                true
        );

        manager.createNotificationChannel(
                channel
        );
    }
}