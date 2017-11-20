package com.mx.easytouch.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.mx.easytouch.R;
import com.mx.easytouch.receiver.TorchWidgetProvider;

/**
 * Created by maoxin on 2017/11/20.
 */

public class TorchService extends Service {

    private Notification  torchNotification = null;
    private static final int  NOTIFICATION_ID = 14348;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTorchNotification();
        return START_NOT_STICKY;
    }

    private void startTorchNotification(){
        Intent notificationIntent = new Intent(this, TorchWidgetProvider.class);
        notificationIntent.setAction(Intent.ACTION_VIEW);
        notificationIntent.setAction(TorchWidgetProvider.RECEIVE_FLASH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);

        this.torchNotification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Torch turned on")
                .setContentText("Tap to turn off.")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.won)
                .build();
        torchNotification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(NOTIFICATION_ID, torchNotification);
    }
}
