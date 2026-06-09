package com.nova.assistant.commands;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class TaskScheduler {

    private final Context context;

    public TaskScheduler(Context context) {
        this.context = context;
    }

    public String schedule(String appTarget, String taskBody, long delayMs) {
        if (delayMs <= 0) return "When should I do this?";

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_TASK);
        intent.putExtra(AlarmReceiver.EXTRA_APP,  appTarget);
        intent.putExtra(AlarmReceiver.EXTRA_BODY, taskBody);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, pi);

        long minutes = delayMs / 60_000;
        String timeStr = minutes > 0 ? minutes + " minute" + (minutes != 1 ? "s" : "") :
                         (delayMs / 1000) + " seconds";
        return "Got it. I'll do that in " + timeStr + ".";
    }
}
