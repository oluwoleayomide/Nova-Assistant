package com.nova.assistant.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import com.nova.assistant.utils.PrefsManager;

public class ProactiveMonitor {

    private static final long CHECK_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutes
    private static final int  BATTERY_THRESHOLD  = 20;             // %
    private static final float TEMP_THRESHOLD    = 42.0f;          // Celsius
    private static final float STORAGE_THRESHOLD = 0.90f;          // 90% used

    private final Context context;
    private final AlertManager alertManager;
    private final Handler handler;

    private boolean batteryAlerted  = false;
    private boolean tempAlerted     = false;
    private boolean storageAlerted  = false;
    private boolean running         = false;

    public ProactiveMonitor(Context context) {
        this.context      = context;
        this.alertManager = new AlertManager(context);
        this.handler      = new Handler(Looper.getMainLooper());
    }

    public void start() {
        running = true;
        scheduleCheck();
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void scheduleCheck() {
        handler.postDelayed(() -> {
            if (running) {
                runChecks();
                scheduleCheck();
            }
        }, CHECK_INTERVAL_MS);
    }

    private void runChecks() {
        checkBattery();
        checkStorage();
        // Temperature check via battery intent
        checkTemperature();
    }

    private void checkBattery() {
        if (!PrefsManager.isBatteryAlertEnabled()) return;

        Intent batteryIntent = context.registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) return;

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) return;

        int pct = (int)(100f * level / scale);
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL;

        if (pct <= BATTERY_THRESHOLD && !charging && !batteryAlerted) {
            batteryAlerted = true;
            alertManager.sendBatteryAlert(pct);
        } else if (pct > BATTERY_THRESHOLD + 5) {
            batteryAlerted = false; // Reset when charged
        }
    }

    private void checkTemperature() {
        if (!PrefsManager.isTempAlertEnabled()) return;

        Intent batteryIntent = context.registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) return;

        int tempTenths = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        if (tempTenths < 0) return;

        float tempC = tempTenths / 10.0f;
        if (tempC > TEMP_THRESHOLD && !tempAlerted) {
            tempAlerted = true;
            alertManager.sendTempAlert();
        } else if (tempC < TEMP_THRESHOLD - 3) {
            tempAlerted = false;
        }
    }

    private void checkStorage() {
        if (!PrefsManager.isStorageAlertEnabled()) return;

        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long total     = stat.getBlockCountLong() * stat.getBlockSizeLong();
        long available = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        long used      = total - available;

        if (total <= 0) return;
        float usedRatio = (float) used / total;

        if (usedRatio >= STORAGE_THRESHOLD && !storageAlerted) {
            storageAlerted = true;
            alertManager.sendStorageAlert();
        } else if (usedRatio < STORAGE_THRESHOLD - 0.05f) {
            storageAlerted = false;
        }
    }
}
