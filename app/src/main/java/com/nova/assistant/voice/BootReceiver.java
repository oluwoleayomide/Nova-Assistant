package com.nova.assistant.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.nova.assistant.utils.PrefsManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            PrefsManager.init(context);

            if (PrefsManager.isOnboarded()) {
                Intent serviceIntent = new Intent(context, VoiceService.class);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}
