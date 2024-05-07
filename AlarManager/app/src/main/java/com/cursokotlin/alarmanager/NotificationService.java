package com.cursokotlin.alarmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    private static final String EXTRA_DELAY = "delay";

    private Timer timer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Obtener el tiempo de espera del extra del intent
        System.out.println("entre");
        long delay = intent.getLongExtra(EXTRA_DELAY, 15000); // 15 segundos por defecto


        startNotificationServiceAfterDelay(delay);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startNotificationServiceAfterDelay(long delayMillis) {
        timer = new Timer();
        System.out.println("cargando explosion");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showNotification();
            }
        }, delayMillis);
    }

    private void showNotification() {
        System.out.println("ola");
        Uri sound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.sound);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default_notification_channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Test")
                .setSound(sound)
                .setContentText("Hello! This is my first push notification");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("NOTIFICATION_CHANNEL_ID", "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.setSound(sound, audioAttributes);
            mBuilder.setChannelId("NOTIFICATION_CHANNEL_ID");
            mNotificationManager.createNotificationChannel(notificationChannel);

        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
        System.out.println("ola2");
    }

    public void cancelNotificationService() {
        if (timer != null) {
            System.out.println("DETENER");
            timer.cancel();
            stopSelf(); // Detener el servicio
        } else {
            System.out.println("no detener");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}