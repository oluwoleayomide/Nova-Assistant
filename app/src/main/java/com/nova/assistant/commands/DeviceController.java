package com.nova.assistant.commands;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.provider.AlarmClock;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceController {

    private final Context context;
    private static boolean flashOn = false;

    public DeviceController(Context context) {
        this.context = context;
    }

    // ---- ALARM ----
    public String setAlarm(String timeString) {
        try {
            int[] parsed = parseTime(timeString);
            int hour   = parsed[0];
            int minute = parsed[1];

            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "NOVA Alarm");
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            String amPm = hour < 12 ? "AM" : "PM";
            int h12 = hour % 12;
            if (h12 == 0) h12 = 12;
            return String.format(Locale.US, "Alarm set for %d:%02d %s", h12, minute, amPm);
        } catch (Exception e) {
            return "I had trouble setting that alarm. Please try again.";
        }
    }

    // ---- TIMER ----
    public String setTimer(long durationMs) {
        if (durationMs <= 0) return "How long should I set the timer for?";
        try {
            long seconds = durationMs / 1000;
            Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
            intent.putExtra(AlarmClock.EXTRA_LENGTH, (int) seconds);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Timer set for " + formatDuration(durationMs);
        } catch (Exception e) {
            return "Couldn't set the timer. Please try again.";
        }
    }

    // ---- FLASHLIGHT ----
    public String toggleFlashlight(boolean on) {
        try {
            CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] ids = cm.getCameraIdList();
            if (ids.length > 0) {
                cm.setTorchMode(ids[0], on);
                flashOn = on;
                return on ? "Flashlight on." : "Flashlight off.";
            }
        } catch (Exception e) {
            return "Couldn't control flashlight: " + e.getMessage();
        }
        return "Flashlight not available.";
    }

    // ---- VOLUME ----
    public String adjustVolume(boolean up) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (up) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            return "Volume up.";
        } else {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            return "Volume down.";
        }
    }

    public String mute() {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.adjustStreamVolume(AudioManager.STREAM_RING,
            AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
        return "Phone muted.";
    }

    // ---- LOCK SCREEN ----
    public String scheduleLockScreen(long delayMs) {
        if (delayMs <= 0) {
            // Immediate lock via DevicePolicyManager (needs admin — use intent instead)
            lockNow();
            return "Locking screen now.";
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.nova.assistant.ACTION_LOCK");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerAt = System.currentTimeMillis() + delayMs;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        return "Screen will lock in " + formatDuration(delayMs) + ".";
    }

    private void lockNow() {
        Intent intent = new Intent(Intent.ACTION_SCREEN_OFF);
        context.sendBroadcast(intent);
    }

    // ---- Helpers ----

    private int[] parseTime(String timeStr) {
        int hour = 8, minute = 0;
        if (timeStr == null) return new int[]{hour, minute};

        Pattern p = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(timeStr);
        if (m.find()) {
            hour = Integer.parseInt(m.group(1));
            if (m.group(2) != null) minute = Integer.parseInt(m.group(2));
            String amPm = m.group(3);
            if (amPm != null) {
                if (amPm.equalsIgnoreCase("pm") && hour < 12) hour += 12;
                if (amPm.equalsIgnoreCase("am") && hour == 12) hour = 0;
            }
        }
        return new int[]{hour, minute};
    }

    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long hours    = totalSec / 3600;
        long minutes  = (totalSec % 3600) / 60;
        long seconds  = totalSec % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0) sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        if (seconds > 0 && hours == 0) sb.append(seconds).append(" seconds");
        return sb.toString().trim();
    }
}
