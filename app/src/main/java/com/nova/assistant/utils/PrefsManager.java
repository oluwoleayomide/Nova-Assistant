package com.nova.assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREFS_NAME = "nova_prefs";

    private static final String KEY_ONBOARDED     = "onboarded";
    private static final String KEY_USER_NAME     = "user_name";
    private static final String KEY_CALL_NAME     = "call_name";
    private static final String KEY_THEME         = "theme";
    private static final String KEY_WAKE_WORD     = "wake_word";
    private static final String KEY_VOICE_GENDER  = "voice_gender";
    private static final String KEY_VOICE_SPEED   = "voice_speed";
    private static final String KEY_VOICE_PITCH   = "voice_pitch";
    private static final String KEY_API_KEY       = "api_key";
    private static final String KEY_API_ENDPOINT  = "api_endpoint";
    private static final String KEY_ALERT_BATTERY = "alert_battery";
    private static final String KEY_ALERT_TEMP    = "alert_temp";
    private static final String KEY_ALERT_STORAGE = "alert_storage";

    public static final String THEME_DARK  = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_AUTO  = "auto";

    public static final String GENDER_FEMALE = "female";
    public static final String GENDER_MALE   = "male";

    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- Onboarding ---
    public static boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }
    public static void setOnboarded(boolean v) {
        prefs.edit().putBoolean(KEY_ONBOARDED, v).apply();
    }

    // --- User info ---
    public static String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    public static void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }
    public static String getCallName() {
        return prefs.getString(KEY_CALL_NAME, getUserName());
    }
    public static void setCallName(String name) {
        prefs.edit().putString(KEY_CALL_NAME, name).apply();
    }

    // --- Theme ---
    public static String getTheme() {
        return prefs.getString(KEY_THEME, THEME_DARK);
    }
    public static void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    // --- Wake word ---
    public static String getWakeWord() {
        return prefs.getString(KEY_WAKE_WORD, "Hey Nova");
    }
    public static void setWakeWord(String word) {
        prefs.edit().putString(KEY_WAKE_WORD, word).apply();
    }

    // --- Voice ---
    public static String getVoiceGender() {
        return prefs.getString(KEY_VOICE_GENDER, GENDER_FEMALE);
    }
    public static void setVoiceGender(String gender) {
        prefs.edit().putString(KEY_VOICE_GENDER, gender).apply();
    }
    public static float getVoiceSpeed() {
        return prefs.getFloat(KEY_VOICE_SPEED, 1.0f);
    }
    public static void setVoiceSpeed(float speed) {
        prefs.edit().putFloat(KEY_VOICE_SPEED, speed).apply();
    }
    public static float getVoicePitch() {
        return prefs.getFloat(KEY_VOICE_PITCH, 1.0f);
    }
    public static void setVoicePitch(float pitch) {
        prefs.edit().putFloat(KEY_VOICE_PITCH, pitch).apply();
    }

    // --- API ---
    public static String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }
    public static void setApiKey(String key) {
        prefs.edit().putString(KEY_API_KEY, key).apply();
    }
    public static String getApiEndpoint() {
        return prefs.getString(KEY_API_ENDPOINT,
               "https://api.groq.com/openai/v1/chat/completions");
    }
    public static void setApiEndpoint(String url) {
        prefs.edit().putString(KEY_API_ENDPOINT, url).apply();
    }

    // --- Alerts ---
    public static boolean isBatteryAlertEnabled() {
        return prefs.getBoolean(KEY_ALERT_BATTERY, true);
    }
    public static void setBatteryAlert(boolean v) {
        prefs.edit().putBoolean(KEY_ALERT_BATTERY, v).apply();
    }
    public static boolean isTempAlertEnabled() {
        return prefs.getBoolean(KEY_ALERT_TEMP, true);
    }
    public static void setTempAlert(boolean v) {
        prefs.edit().putBoolean(KEY_ALERT_TEMP, v).apply();
    }
    public static boolean isStorageAlertEnabled() {
        return prefs.getBoolean(KEY_ALERT_STORAGE, true);
    }
    public static void setStorageAlert(boolean v) {
        prefs.edit().putBoolean(KEY_ALERT_STORAGE, v).apply();
    }
}
