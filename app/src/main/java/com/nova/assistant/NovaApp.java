package com.nova.assistant;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.nova.assistant.utils.PrefsManager;
import com.nova.assistant.utils.ThemeManager;

public class NovaApp extends Application {

    public static final String CHANNEL_ID_SERVICE = "nova_service";
    public static final String CHANNEL_ID_ALERTS  = "nova_alerts";

    @Override
    public void onCreate() {
        super.onCreate();
        PrefsManager.init(this);
        ThemeManager.applyTheme(PrefsManager.getTheme());
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // Persistent service channel
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID_SERVICE,
                "NOVA Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("NOVA runs in background to listen for your wake word");
            serviceChannel.setShowBadge(false);
            nm.createNotificationChannel(serviceChannel);

            // Alert notifications channel
            NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ID_ALERTS,
                "NOVA Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            alertChannel.setDescription("Battery, temperature and storage alerts");
            alertChannel.enableVibration(true);
            nm.createNotificationChannel(alertChannel);
        }
    }
}
