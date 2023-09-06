package com.example.SuperTrack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;



import java.util.concurrent.TimeUnit;

public class CountdownService extends Service {

    private boolean isCountingdown = false;

    private boolean isStartingLater = false;
    private Handler countdownHandler = new Handler();
    private long remainingMillis;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("MusicPlayer","onReceive");
                Log.i("MusicPlayer","Start Service");
                // Device went to sleep or screen locked
                // Start the CountdownService here
            startService(intent);
            }
        }
    };





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
public void NotificationSetup(long remainingMillis){
    // Create a notification channel (required for Android Oreo and above)
    Log.i("MusicPlayer","NotificationSetup");

    // Inside NotificationSetup()
    builder = new NotificationCompat.Builder(this, "countdown_channel")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Countdown")
            .setContentText("Playback begins: " + convertToMMSS(String.valueOf(remainingMillis)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel("countdown_channel", "Countdown Channel", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

// Create the notification
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "countdown_channel")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Countdown")
            .setContentText("Playback will start in: " + convertToMMSS(String.valueOf(remainingMillis)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    Notification notification = builder.build();

// Start the service as a foreground service with the notification
    startForeground(1, notification);
}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MusicPlayer","onStartCommand");

        if (intent != null && intent.getAction() != null) {
            Log.i("MusicPlayer","intent != null && intent.getAction() != null");

            if (intent.getAction().equals("START_COUNTDOWN")) {
                // Start the countdown with the provided delayMillis
                long delayMillis = intent.getLongExtra("DELAY_MILLIS", 0);
                Log.i("MusicPlayer","intent.getAction().equals(\"START_COUNTDOWN\"");

                NotificationSetup(delayMillis);
                //startCountdown(delayMillis);
            }
            if(intent.getAction().equals("ISLater")){isStartingLater= intent.getBooleanExtra("ISLater",false);}
            if(intent.getAction().equals("IsCountingdown")){isCountingdown= intent.getBooleanExtra("IsCountingdown",false);}

        }
        // Register the screenStateReceiver
        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, screenOffFilter);

        return START_STICKY;
    }

    private Runnable countdownRunnable = new Runnable() {

        @Override
        public void run() {
            Log.i("MusicPlayer","countdownRunnable run() ");

            if (isCountingdown && remainingMillis > 0) {
                // Update notification content text
                String remainingTime = convertToMMSS(String.valueOf(remainingMillis));
                builder.setContentText("Playback will start in: " + remainingTime);
                notificationManager.notify(1, builder.build()); // Update the existing notification

                remainingMillis -= 1000;
                countdownHandler.postDelayed(this, 1000);
            } else {
                // Countdown complete, perform necessary actions
                stopSelf();
            }
        }
    };
    public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCountingdown = false;
        countdownHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(screenStateReceiver);
        // TODO: Remove notification here
    }
}
