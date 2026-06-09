package com.nova.assistant.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final String[] ALL_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    };

    public static boolean hasPermission(Context ctx, String permission) {
        return ContextCompat.checkSelfPermission(ctx, permission)
               == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasMicPermission(Context ctx) {
        return hasPermission(ctx, Manifest.permission.RECORD_AUDIO);
    }

    public static boolean hasContactsPermission(Context ctx) {
        return hasPermission(ctx, Manifest.permission.READ_CONTACTS);
    }

    public static boolean hasCallPermission(Context ctx) {
        return hasPermission(ctx, Manifest.permission.CALL_PHONE);
    }

    public static boolean hasSmsPermission(Context ctx) {
        return hasPermission(ctx, Manifest.permission.SEND_SMS);
    }

    public static boolean hasCameraPermission(Context ctx) {
        return hasPermission(ctx, Manifest.permission.CAMERA);
    }

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static void requestAllPermissions(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, ALL_PERMISSIONS, requestCode);
    }

    // Request codes
    public static final int RC_AUDIO    = 100;
    public static final int RC_CONTACTS = 101;
    public static final int RC_CALL     = 102;
    public static final int RC_SMS      = 103;
    public static final int RC_STORAGE  = 104;
    public static final int RC_CAMERA   = 105;
    public static final int RC_ALL      = 200;
}
