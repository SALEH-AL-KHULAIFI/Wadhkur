package com.saleh.wadhkur;
import android.app.*;import android.content.*;import android.os.SystemClock;
public final class ReminderScheduler{private static final int REQUEST=7711;private ReminderScheduler(){}
 public static void schedule(Context c,long m){cancel(c);AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(a==null)return;long x=Math.max(1,Math.min(60,m))*60000L;Intent i=new Intent(c,ReminderReceiver.class);PendingIntent p=PendingIntent.getBroadcast(c,REQUEST,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);a.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+x,x,p);}
 public static void cancel(Context c){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(a==null)return;Intent i=new Intent(c,ReminderReceiver.class);PendingIntent p=PendingIntent.getBroadcast(c,REQUEST,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);a.cancel(p);p.cancel();}}
