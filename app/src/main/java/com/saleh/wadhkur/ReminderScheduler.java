package com.saleh.wadhkur;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public final class ReminderScheduler {

    public static final String TYPE_GENERAL = "general";
    public static final String TYPE_MORNING = "morning";
    public static final String TYPE_EVENING = "evening";

    private static final String PREFS = "settings";

    private static final int GENERAL_REQUEST_CODE = 7700;
    private static final int MORNING_REQUEST_CODE = 7701;
    private static final int EVENING_REQUEST_CODE = 7702;

    private ReminderScheduler() {
    }

    /*
     * جدولة جميع التذكيرات المفعلة
     */
    public static void scheduleAll(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        if (prefs.getBoolean(
                "general_enabled",
                true
        )) {

            scheduleGeneral(context);
        }

        if (prefs.getBoolean(
                "morning_enabled",
                true
        )) {

            scheduleMorning(context);
        }

        if (prefs.getBoolean(
                "evening_enabled",
                true
        )) {

            scheduleEvening(context);
        }
    }

    /*
     * التذكير العام
     */
    public static void scheduleGeneral(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        int hour =
                prefs.getInt(
                        "general_hour",
                        12
                );

        int minute =
                prefs.getInt(
                        "general_minute",
                        0
                );

        schedule(
                context,
                TYPE_GENERAL,
                hour,
                minute
        );
    }

    /*
     * تذكير أذكار الصباح
     */
    public static void scheduleMorning(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
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

    /*
     * تذكير أذكار المساء
     */
    public static void scheduleEvening(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
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

    /*
     * جدولة تنبيه يومي
     */
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
                getRequestCode(type);

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

        /*
         * إذا مر وقت التنبيه اليوم،
         * نضعه في اليوم التالي.
         */
        if (next.getTimeInMillis()
                <= System.currentTimeMillis()) {

            next.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );
        }

        long trigger =
                next.getTimeInMillis();

        /*
         * لا نستخدم Exact Alarm هنا حتى لا نحتاج
         * إلى إضافة SCHEDULE_EXACT_ALARM في هذه المرحلة.
         *
         * setAndAllowWhileIdle مناسب لتذكيرات يومية
         * ويعمل أثناء وضع توفير الطاقة مع السماح
         * للنظام بهامش بسيط في وقت التنفيذ.
         */
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

    /*
     * إلغاء التذكير العام
     */
    public static void cancelGeneral(
            Context context
    ) {

        cancel(
                context,
                TYPE_GENERAL,
                GENERAL_REQUEST_CODE
        );
    }

    /*
     * إلغاء تذكير الصباح
     */
    public static void cancelMorning(
            Context context
    ) {

        cancel(
                context,
                TYPE_MORNING,
                MORNING_REQUEST_CODE
        );
    }

    /*
     * إلغاء تذكير المساء
     */
    public static void cancelEvening(
            Context context
    ) {

        cancel(
                context,
                TYPE_EVENING,
                EVENING_REQUEST_CODE
        );
    }

    /*
     * إلغاء جميع التذكيرات
     */
    public static void cancelAll(
            Context context
    ) {

        cancelGeneral(context);
        cancelMorning(context);
        cancelEvening(context);
    }

    /*
     * إلغاء تنبيه محدد
     */
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

        alarmManager.cancel(
                pendingIntent
        );

        pendingIntent.cancel();
    }

    /*
     * الحصول على Request Code حسب نوع التذكير
     */
    private static int getRequestCode(
            String type
    ) {

        if (TYPE_GENERAL.equals(type)) {

            return GENERAL_REQUEST_CODE;
        }

        if (TYPE_MORNING.equals(type)) {

            return MORNING_REQUEST_CODE;
        }

        if (TYPE_EVENING.equals(type)) {

            return EVENING_REQUEST_CODE;
        }

        /*
         * حماية من أي نوع غير معروف.
         */
        return GENERAL_REQUEST_CODE;
    }
}