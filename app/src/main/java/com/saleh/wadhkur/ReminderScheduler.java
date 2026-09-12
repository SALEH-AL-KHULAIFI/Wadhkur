package com.saleh.wadhkur;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public final class ReminderScheduler {

    public static final String TYPE_MORNING = "morning";
    public static final String TYPE_EVENING = "evening";

    private static final int MORNING_REQUEST_CODE = 7701;
    private static final int EVENING_REQUEST_CODE = 7702;

    private ReminderScheduler() {
    }

    public static void scheduleAll(Context context) {

        scheduleMorning(context);
        scheduleEvening(context);
    }

    public static void scheduleMorning(Context context) {

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                );

        int hour =
                prefs.getInt(
                        "morning_hour",
                        6
                );

        int minute =
                prefs.getInt(
                        "morning_minute",
                        0
                );

        schedule(
                context,
                TYPE_MORNING,
                hour,
                minute
        );
    }

    public static void scheduleEvening(Context context) {

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                );

        int hour =
                prefs.getInt(
                        "evening_hour",
                        17
                );

        int minute =
                prefs.getInt(
                        "evening_minute",
                        0
                );

        schedule(
                context,
                TYPE_EVENING,
                hour,
                minute
        );
    }

    public static void schedule(
            Context context,
            String type,
            int hour,
            int minute
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        int requestCode =
                TYPE_MORNING.equals(type)
                        ? MORNING_REQUEST_CODE
                        : EVENING_REQUEST_CODE;

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(
                "com.saleh.wadhkur.REMINDER_" + type
        );

        intent.putExtra(
                ReminderReceiver.EXTRA_TYPE,
                type
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        Calendar next =
                Calendar.getInstance();

        next.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        next.set(
                Calendar.MINUTE,
                minute
        );

        next.set(
                Calendar.SECOND,
                0
        );

        next.set(
                Calendar.MILLISECOND,
                0
        );

        if (next.getTimeInMillis()
                <= System.currentTimeMillis()) {

            next.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );
        }

        long trigger =
                next.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pendingIntent
            );

        } else {

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pendingIntent
            );
        }
    }

    public static void cancelMorning(
            Context context
    ) {

        cancel(
                context,
                TYPE_MORNING,
                MORNING_REQUEST_CODE
        );
    }

    public static void cancelEvening(
            Context context
    ) {

        cancel(
                context,
                TYPE_EVENING,
                EVENING_REQUEST_CODE
        );
    }

    public static void cancelAll(
            Context context
    ) {

        cancelMorning(context);
        cancelEvening(context);
    }

    private static void cancel(
            Context context,
            String type,
            int requestCode
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(
                "com.saleh.wadhkur.REMINDER_" + type
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        alarmManager.cancel(
                pendingIntent
        );

        pendingIntent.cancel();
    }
}