package com.saleh.wadhkur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        String action =
                intent.getAction();

        /*
         * إعادة جدولة التنبيهات في حالتين:
         *
         * 1. بعد إعادة تشغيل الهاتف.
         * 2. بعد تحديث التطبيق.
         */
        if (
                !Intent.ACTION_BOOT_COMPLETED.equals(action)
                        &&
                !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
        ) {
            return;
        }

        /*
         * ReminderScheduler يقرأ بنفسه حالة
         * كل نوع من SharedPreferences:
         *
         * general_enabled
         * morning_enabled
         * evening_enabled
         *
         * لذلك نستخدم scheduleAll()
         * حتى تتم إعادة جميع التنبيهات المفعلة.
         */
        ReminderScheduler.scheduleAll(
                context
        );
    }
}