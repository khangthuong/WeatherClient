package com.example.khangnt.weatherclient.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlarmHelper {

    private Context context;
    private static AlarmHelper mInstance;

    private AlarmHelper(Context context) {
        this.context = context;
    }

    public static synchronized AlarmHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AlarmHelper(context);
        }
        return mInstance;
    }

    public void setAlarm() {
        Intent alarmIntent = new Intent(context,
                AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to start at 10:00 AM
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 14);
        calendar1.set(Calendar.MINUTE, 16);
        calendar1.set(Calendar.SECOND, 0);
        if (calendar1.before(Calendar.getInstance())) {
            calendar1.add(Calendar.DAY_OF_MONTH, 1);
        }
        Log.d("2222: ", calendar1.getTimeInMillis()+"");

        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar1.getTimeInMillis(),
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS),
                pendingIntent);
        Toast.makeText(context.getApplicationContext(), "Notification will be show every day at 7:00 AM",
                Toast.LENGTH_LONG).show();
    }

    public void cancelAlarm() {
        Intent alarmIntent = new Intent(context,
                AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(context.getApplicationContext(), "Notification will be off",
                Toast.LENGTH_LONG).show();
    }
}
