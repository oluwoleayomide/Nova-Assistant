package com.nova.assistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import com.nova.assistant.utils.PrefsManager;
import java.util.ArrayList;
import java.util.Locale;

public class WakeWordDetector {

    public interface WakeWordListener {
        void onWakeWordDetected();
    }

    private SpeechRecognizer recognizer;
    private final Context context;
    private WakeWordListener listener;
    private boolean isRunning = false;
    private boolean isPaused  = false;

    public WakeWordDetector(Context context, WakeWordListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        isPaused  = false;
        startCycle();
    }

    public void stop() {
        isRunning = false;
        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.destroy();
            recognizer = null;
        }
    }

    public void pause() { isPaused = true; }
    public void resume() {
        isPaused = false;
        if (isRunning) startCycle();
    }

    private void startCycle() {
        if (!isRunning || isPaused) return;

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override public void onError(int error) {
                if (isRunning && !isPaused) {
                    // Small delay then restart to avoid tight loops
                    new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> startCycle(), 1500);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String wakeWord = PrefsManager.getWakeWord().toLowerCase(Locale.US);
                    for (String match : matches) {
                        if (match.toLowerCase(Locale.US).contains(wakeWord)) {
                            if (listener != null) listener.onWakeWordDetected();
                            break;
                        }
                    }
                }
                // Restart cycle after result
                if (isRunning && !isPaused) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> startCycle(), 500);
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {
                // Check partial results for faster wake word response
                ArrayList<String> partial = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null) {
                    String wakeWord = PrefsManager.getWakeWord().toLowerCase(Locale.US);
                    for (String match : partial) {
                        if (match.toLowerCase(Locale.US).contains(wakeWord)) {
                            if (listener != null) listener.onWakeWordDetected();
                            break;
                        }
                    }
                }
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);

        recognizer.startListening(intent);
    }
}
