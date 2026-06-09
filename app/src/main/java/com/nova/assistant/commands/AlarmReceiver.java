package com.nova.assistant.commands;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.nova.assistant.voice.VoiceService;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_TASK  = "com.nova.assistant.ACTION_TASK";
    public static final String ACTION_LOCK  = "com.nova.assistant.ACTION_LOCK";
    public static final String EXTRA_APP    = "extra_app";
    public static final String EXTRA_BODY   = "extra_body";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (ACTION_TASK.equals(action)) {
            String app  = intent.getStringExtra(EXTRA_APP);
            String body = intent.getStringExtra(EXTRA_BODY);
            handleScheduledTask(context, app, body);
        } else if (ACTION_LOCK.equals(action)) {
            // Lock screen
            Intent lockIntent = new Intent(Intent.ACTION_SCREEN_OFF);
            context.sendBroadcast(lockIntent);
        }
    }

    private void handleScheduledTask(Context context, String app, String body) {
        // Build the command and send it to VoiceService
        String command = body != null ? body : (app != null ? "open " + app : "");
        if (!command.isEmpty()) {
            Intent serviceIntent = new Intent(context, VoiceService.class);
            serviceIntent.setAction(VoiceService.ACTION_COMMAND);
            serviceIntent.putExtra(VoiceService.EXTRA_COMMAND_TEXT, command);
            context.startForegroundService(serviceIntent);
        }
    }
}
