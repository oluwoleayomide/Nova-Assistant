package com.nova.assistant.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import com.nova.assistant.utils.PrefsManager;
import java.util.Locale;

public class TTSHandler {

    public interface TTSListener {
        void onStart();
        void onDone();
        void onError();
    }

    private TextToSpeech tts;
    private boolean isReady = false;
    private TTSListener listener;

    public TTSHandler(Context context, TTSListener listener) {
        this.listener = listener;
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                applySettings();
                isReady = true;
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {
                if (TTSHandler.this.listener != null) TTSHandler.this.listener.onStart();
            }
            @Override public void onDone(String utteranceId) {
                if (TTSHandler.this.listener != null) TTSHandler.this.listener.onDone();
            }
            @Override public void onError(String utteranceId) {
                if (TTSHandler.this.listener != null) TTSHandler.this.listener.onError();
            }
        });
    }

    public void speak(String text) {
        if (!isReady || text == null || text.isEmpty()) return;
        applySettings();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NOVA_UTT_" + System.currentTimeMillis());
    }

    public void speakAdd(String text) {
        if (!isReady || text == null || text.isEmpty()) return;
        applySettings();
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "NOVA_UTT_" + System.currentTimeMillis());
    }

    private void applySettings() {
        tts.setSpeechRate(PrefsManager.getVoiceSpeed());
        tts.setPitch(PrefsManager.getVoicePitch());
    }

    public void stop() {
        if (tts != null && tts.isSpeaking()) tts.stop();
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isReady = false;
        }
    }
}
