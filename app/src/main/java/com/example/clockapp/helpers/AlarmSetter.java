package com.example.clockapp.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.clockapp.AlarmService;
import com.example.clockapp.db.entities.Alarm;

import java.util.Calendar;

public class AlarmSetter {
    public static long WEEK_INTERVAL = AlarmManager.INTERVAL_DAY * 7;
    public static long MONTH_INTERVAL = 2_629_800_000L;

    public static void setAlarm(Alarm alarm, Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction("alarm");
        Bundle bundle = new Bundle();
        bundle.putSerializable("alarm", alarm);
        intent.putExtra("alarm_bundle", bundle);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context.getApplicationContext(), (int) alarm.getUid(), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        AlarmSetter.setCalendar(calendar, alarm.getTime());
        String repeatMode = alarm.getRepeatMode();
        if (repeatMode.equals("")) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            long interval = 0;
            if (repeatMode.equals("daily")) {
                interval = AlarmManager.INTERVAL_DAY;
            } else if (repeatMode.equals("weekly")) {
                interval = WEEK_INTERVAL;
            } else if (repeatMode.equals("monthly")) {
                interval = MONTH_INTERVAL;
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
        }
    }

    public static void cancelAlarm(Alarm alarm, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction("alarm");
        PendingIntent pendingIntent = PendingIntent.getForegroundService(context.getApplicationContext(), (int) alarm.getUid(), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private static void setCalendar(Calendar calendar, String time) {
        int alarmHour = Integer.parseInt(time.charAt(0) + Character.toString(time.charAt(1)));
        int alarmMinute = Integer.parseInt(time.charAt(3) + Character.toString(time.charAt(4)));
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
        calendar.set(Calendar.MINUTE, alarmMinute);
        calendar.set(Calendar.SECOND, 0);
        long alarmTime = Helpers.convertTimeToMs(alarmHour, alarmMinute);
        long currentTime = Helpers.convertTimeToMs(currentHour, currentMinute);
        if (currentTime > alarmTime) {
            calendar.add(Calendar.DATE, 1);
        }
    }
}
