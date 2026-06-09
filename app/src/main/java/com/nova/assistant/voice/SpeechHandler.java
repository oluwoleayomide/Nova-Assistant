package com.nova.assistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechHandler {

    public interface SpeechListener {
        void onListening();
        void onResult(String text);
        void onError(int errorCode);
    }

    private SpeechRecognizer recognizer;
    private final Context context;
    private SpeechListener listener;
    private boolean isListening = false;

    public SpeechHandler(Context context, SpeechListener listener) {
        this.context  = context;
        this.listener = listener;
        init();
    }

    private void init() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context);
            recognizer.setRecognitionListener(new RecognitionListener() {

                @Override public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    if (listener != null) listener.onListening();
                }

                @Override public void onBeginningOfSpeech() {}

                @Override public void onRmsChanged(float rmsdB) {}

                @Override public void onBufferReceived(byte[] buffer) {}

                @Override public void onEndOfSpeech() {
                    isListening = false;
                }

                @Override public void onError(int error) {
                    isListening = false;
                    if (listener != null) listener.onError(error);
                }

                @Override public void onResults(Bundle results) {
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        if (listener != null) listener.onResult(matches.get(0));
                    } else {
                        if (listener != null) listener.onError(SpeechRecognizer.ERROR_NO_MATCH);
                    }
                }

                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    public void startListening() {
        if (recognizer == null) {
            init();
            if (recognizer == null) return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizer.startListening(intent);
    }

    public void stopListening() {
        if (recognizer != null && isListening) {
            recognizer.stopListening();
            isListening = false;
        }
    }

    public boolean isListening() { return isListening; }

    public void destroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
    }
}
