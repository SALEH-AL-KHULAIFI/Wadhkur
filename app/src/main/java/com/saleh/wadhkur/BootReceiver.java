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

        if (
                !Intent.ACTION_BOOT_COMPLETED.equals(action)
                        &&
                !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
        ) {
            return;
        }

        android.content.SharedPreferences prefs =
                context.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                );

        boolean morningEnabled =
                prefs.getBoolean(
                        "morning_enabled",
                        true
                );

        boolean eveningEnabled =
                prefs.getBoolean(
                        "evening_enabled",
                        true
                );

        if (morningEnabled) {

            ReminderScheduler.scheduleMorning(
                    context
            );
        }

        if (eveningEnabled) {

            ReminderScheduler.scheduleEvening(
                    context
            );
        }
    }
}