package com.nova.assistant.utils;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static void applyTheme(String theme) {
        switch (theme) {
            case PrefsManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case PrefsManager.THEME_AUTO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case PrefsManager.THEME_DARK:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }
}
