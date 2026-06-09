package com.nova.assistant.commands;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppLauncher {

    // Common app name to package mapping
    private static final Map<String, String> APP_PACKAGES = new HashMap<String, String>() {{
        put("whatsapp",   "com.whatsapp");
        put("instagram",  "com.instagram.android");
        put("facebook",   "com.facebook.katana");
        put("twitter",    "com.twitter.android");
        put("x",          "com.twitter.android");
        put("youtube",    "com.google.android.youtube");
        put("spotify",    "com.spotify.music");
        put("netflix",    "com.netflix.mediaclient");
        put("chrome",     "com.android.chrome");
        put("gmail",      "com.google.android.gm");
        put("maps",       "com.google.android.apps.maps");
        put("google maps","com.google.android.apps.maps");
        put("camera",     "com.android.camera2");
        put("gallery",    "com.android.gallery3d");
        put("settings",   "com.android.settings");
        put("calculator", "com.android.calculator2");
        put("calendar",   "com.android.calendar");
        put("clock",      "com.android.deskclock");
        put("files",      "com.google.android.apps.nbu.files");
        put("tiktok",     "com.zhiliaoapp.musically");
        put("snapchat",   "com.snapchat.android");
        put("telegram",   "org.telegram.messenger");
        put("discord",    "com.discord");
        put("uber",       "com.ubercab");
        put("amazon",     "com.amazon.mShop.android.shopping");
        put("playstore",  "com.android.vending");
        put("play store", "com.android.vending");
    }};

    private final Context context;

    public AppLauncher(Context context) {
        this.context = context;
    }

    public String launch(String appName) {
        if (appName == null || appName.isEmpty()) return "Which app should I open?";

        String lower = appName.toLowerCase(Locale.US);

        // Try exact match first
        String pkg = APP_PACKAGES.get(lower);

        // Try partial match
        if (pkg == null) {
            for (Map.Entry<String, String> entry : APP_PACKAGES.entrySet()) {
                if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                    pkg = entry.getValue();
                    break;
                }
            }
        }

        // Try to find via PackageManager
        if (pkg == null) {
            pkg = findPackageByName(appName);
        }

        if (pkg != null) {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                return "Opening " + capitalize(appName);
            }
        }

        return "I couldn't find " + appName + " installed on your device.";
    }

    private String findPackageByName(String appName) {
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        String lower = appName.toLowerCase(Locale.US);
        for (ResolveInfo ri : apps) {
            String label = ri.loadLabel(pm).toString().toLowerCase(Locale.US);
            if (label.contains(lower) || lower.contains(label)) {
                return ri.activityInfo.packageName;
            }
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
