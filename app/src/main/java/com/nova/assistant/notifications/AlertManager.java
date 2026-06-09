package com.nova.assistant.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.nova.assistant.MainActivity;
import com.nova.assistant.NovaApp;
import com.nova.assistant.R;

public class AlertManager {

    private static final int NOTIF_BATTERY  = 2001;
    private static final int NOTIF_TEMP     = 2002;
    private static final int NOTIF_STORAGE  = 2003;

    private final Context context;
    private final NotificationManager nm;

    public AlertManager(Context context) {
        this.context = context;
        this.nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void sendBatteryAlert(int level) {
        String msg = String.format("Battery at %d%%. Enable battery saver?", level);
        send(NOTIF_BATTERY, "⚡ Battery Low", msg);
    }

    public void sendTempAlert() {
        send(NOTIF_TEMP, "🌡️ Phone Getting Hot", "Your phone is overheating. Close heavy apps?");
    }

    public void sendStorageAlert() {
        send(NOTIF_STORAGE, "💾 Storage Almost Full", "Storage is running low. Clear cache?");
    }

    private void send(int id, String title, String message) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(context, NovaApp.CHANNEL_ID_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_nova_logo)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build();

        nm.notify(id, notif);
    }

    public void cancelAll() {
        nm.cancelAll();
    }
}
