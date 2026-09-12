package com.saleh.wadhkur;
import android.content.*;
public class BootReceiver extends BroadcastReceiver{public void onReceive(Context c,Intent i){if(i==null||i.getAction()==null)return;String a=i.getAction();if(!Intent.ACTION_BOOT_COMPLETED.equals(a)&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(a))return;android.content.SharedPreferences p=c.getSharedPreferences("settings",Context.MODE_PRIVATE);if(!p.getBoolean("enabled",false))return;long m=Math.max(1,Math.min(60,p.getLong("minutes",30)));ReminderScheduler.schedule(c,m);}}
