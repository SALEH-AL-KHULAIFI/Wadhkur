package com.saleh.wadhkur;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TYPE =
            "reminder_type";

    private static final String CHANNEL_ID =
            "wadhkur_daily_reminders";

    private static final int GENERAL_NOTIFICATION_ID =
            8800;

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
                intent.getStringExtra(EXTRA_TYPE);

        if (type == null) {
            return;
        }

        SharedPreferences prefs =
                context.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                );

        boolean enabled;

        if (ReminderScheduler.TYPE_GENERAL.equals(type)) {

            enabled =
                    prefs.getBoolean(
                            "general_enabled",
                            true
                    );

        } else if (
                ReminderScheduler.TYPE_MORNING.equals(type)
        ) {

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
         * إذا كان التنبيه معطلاً،
         * لا نعرض إشعارًا ولا نعيد جدولتَه.
         */
        if (!enabled) {
            return;
        }

        /*
         * إعادة جدولة التنبيه لليوم التالي.
         *
         * هذا يجعل التنبيه مستمرًا حتى بعد تشغيل
         * الإشعار الحالي.
         */
        if (ReminderScheduler.TYPE_GENERAL.equals(type)) {

            ReminderScheduler.scheduleGeneral(
                    context
            );

        } else if (
                ReminderScheduler.TYPE_MORNING.equals(type)
        ) {

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
         * يحتاج صلاحية POST_NOTIFICATIONS.
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

        /*
         * التنبيه العام:
         * يختار ذكرًا كاملًا عشوائيًا.
         */
        if (
                ReminderScheduler.TYPE_GENERAL.equals(type)
        ) {

            data = getGeneralDhikr();

            title =
                    "🔔 وذكر";

            notificationId =
                    GENERAL_NOTIFICATION_ID;

        } else if (
                ReminderScheduler.TYPE_MORNING.equals(type)
        ) {

            data =
                    DhikrData.MORNING;

            title =
                    "🌅 أذكار الصباح";

            notificationId =
                    MORNING_NOTIFICATION_ID;

        } else {

            data =
                    DhikrData.EVENING;

            title =
                    "🌙 أذكار المساء";

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
         * اختيار ذكر كامل عشوائيًا.
         */
        int index =
                new Random().nextInt(
                        data.length
                );

        DhikrData.Dhikr selected =
                data[index];

        if (selected == null) {
            return;
        }

        String text =
                selected.text;

        if (
                text == null ||
                text.trim().isEmpty()
        ) {
            return;
        }

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

    /**
     * إنشاء مجموعة الأذكار الخاصة بالتنبيه العام.
     *
     * نجمع أذكار الصباح والمساء في قائمة واحدة،
     * وبالتالي يستطيع التنبيه العام اختيار ذكر
     * كامل وعشوائي من المجموعتين.
     */
    private DhikrData.Dhikr[] getGeneralDhikr() {

        int morningLength =
                DhikrData.MORNING == null
                        ? 0
                        : DhikrData.MORNING.length;

        int eveningLength =
                DhikrData.EVENING == null
                        ? 0
                        : DhikrData.EVENING.length;

        int total =
                morningLength +
                eveningLength;

        if (total == 0) {
            return new DhikrData.Dhikr[0];
        }

        DhikrData.Dhikr[] result =
                new DhikrData.Dhikr[total];

        int position = 0;

        if (DhikrData.MORNING != null) {

            for (
                    DhikrData.Dhikr dhikr
                    : DhikrData.MORNING
            ) {

                result[position++] =
                        dhikr;
            }
        }

        if (DhikrData.EVENING != null) {

            for (
                    DhikrData.Dhikr dhikr
                    : DhikrData.EVENING
            ) {

                result[position++] =
                        dhikr;
            }
        }

        return result;
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
                "تنبيهات الأذكار العامة وأذكار الصباح والمساء"
        );

        channel.enableVibration(
                true
        );

        manager.createNotificationChannel(
                channel
        );
    }
}