package com.nova.assistant.voice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.nova.assistant.MainActivity;
import com.nova.assistant.NovaApp;
import com.nova.assistant.R;
import com.nova.assistant.commands.CommandExecutor;
import com.nova.assistant.home.HomeFragment;
import com.nova.assistant.utils.PrefsManager;

public class VoiceService extends Service {

    public static final String ACTION_WAKE    = "com.nova.assistant.ACTION_WAKE";
    public static final String ACTION_COMMAND = "com.nova.assistant.ACTION_COMMAND";
    public static final String ACTION_STOP    = "com.nova.assistant.ACTION_STOP";
    public static final String EXTRA_COMMAND_TEXT = "command_text";

    private static final int NOTIF_ID = 1001;

    private TTSHandler ttsHandler;
    private SpeechHandler speechHandler;
    private WakeWordDetector wakeWordDetector;
    private CommandExecutor commandExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private enum ServiceState { IDLE, LISTENING, PROCESSING, SPEAKING }
    private ServiceState state = ServiceState.IDLE;

    @Override
    public void onCreate() {
        super.onCreate();
        commandExecutor = new CommandExecutor(this);

        ttsHandler = new TTSHandler(this, new TTSHandler.TTSListener() {
            @Override public void onStart() { setState(ServiceState.SPEAKING); }
            @Override public void onDone()  { onSpokenDone(); }
            @Override public void onError() { setState(ServiceState.IDLE); }
        });

        speechHandler = new SpeechHandler(this, new SpeechHandler.SpeechListener() {
            @Override public void onListening() { setState(ServiceState.LISTENING); }
            @Override public void onResult(String text) { processCommand(text); }
            @Override public void onError(int errorCode) {
                ttsHandler.speak(getString(R.string.error_no_match));
            }
        });

        wakeWordDetector = new WakeWordDetector(this, () -> mainHandler.post(this::onWakeWordDetected));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_WAKE.equals(action)) {
                onWakeWordDetected();
            } else if (ACTION_COMMAND.equals(action)) {
                String cmd = intent.getStringExtra(EXTRA_COMMAND_TEXT);
                if (cmd != null) processCommand(cmd);
            } else if (ACTION_STOP.equals(action)) {
                stopSelf();
            } else {
                // Normal start - begin wake word listening
                wakeWordDetector.start();
            }
        } else {
            wakeWordDetector.start();
        }

        return START_STICKY;
    }

    private void onWakeWordDetected() {
        wakeWordDetector.pause();
        ttsHandler.speak(getString(R.string.wake_prompt));
        // After TTS greeting, start listening (handled in onSpokenDone)
    }

    private void onSpokenDone() {
        if (state == ServiceState.SPEAKING) {
            setState(ServiceState.LISTENING);
            speechHandler.startListening();
        } else {
            setState(ServiceState.IDLE);
            wakeWordDetector.resume();
        }
    }

    private void processCommand(String text) {
        setState(ServiceState.PROCESSING);
        broadcastTranscript(text);

        String response = commandExecutor.execute(text);
        ttsHandler.speak(response);
        wakeWordDetector.resume();
    }

    private void setState(ServiceState newState) {
        state = newState;
        broadcastOrbState(newState.name());
    }

    private void broadcastOrbState(String stateName) {
        Intent intent = new Intent(HomeFragment.ACTION_ORB_STATE);
        intent.putExtra(HomeFragment.EXTRA_STATE, stateName);
        sendBroadcast(intent);
    }

    private void broadcastTranscript(String text) {
        Intent intent = new Intent(HomeFragment.ACTION_TRANSCRIPT);
        intent.putExtra(HomeFragment.EXTRA_TEXT, "\"" + text + "\"");
        sendBroadcast(intent);
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NovaApp.CHANNEL_ID_SERVICE)
            .setContentTitle("NOVA is active")
            .setContentText(getString(R.string.notif_running))
            .setSmallIcon(R.drawable.ic_nova_logo)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeWordDetector != null) wakeWordDetector.stop();
        if (speechHandler != null) speechHandler.destroy();
        if (ttsHandler != null) ttsHandler.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
