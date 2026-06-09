package com.nova.assistant.commands;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import com.nova.assistant.utils.PermissionHelper;

public class PhoneController {

    private final Context context;

    public PhoneController(Context context) {
        this.context = context;
    }

    public String call(String contactName) {
        if (!PermissionHelper.hasCallPermission(context)) {
            return "I need permission to make calls. Please grant it in Settings.";
        }
        if (!PermissionHelper.hasContactsPermission(context)) {
            return "I need access to your contacts to call " + contactName + ".";
        }

        String number = resolveContactNumber(contactName);
        if (number == null) {
            return "I couldn't find " + contactName + " in your contacts.";
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
        return "Calling " + contactName + " now.";
    }

    public String sendSms(String contactName, String body) {
        if (!PermissionHelper.hasSmsPermission(context)) {
            return "I need SMS permission to send messages.";
        }

        String number = resolveContactNumber(contactName);
        if (number == null) {
            return "I couldn't find " + contactName + " in your contacts.";
        }

        if (body == null || body.isEmpty()) {
            return "What should I say to " + contactName + "?";
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, body, null, null);
            return "Message sent to " + contactName + ": \"" + body + "\"";
        } catch (Exception e) {
            return "Failed to send message: " + e.getMessage();
        }
    }

    private String resolveContactNumber(String name) {
        if (name == null || name.isEmpty()) return null;

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            + " LIKE ?";
        String[] selArgs = {"%" + name + "%"};

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                return idx >= 0 ? cursor.getString(idx) : null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
}
