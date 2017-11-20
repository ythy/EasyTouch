package com.mx.easytouch.receiver;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.RemoteViews;

import com.mx.easytouch.R;
import com.mx.easytouch.service.TorchService;
import com.mx.easytouch.utils.Camera2Utils;

/**
 * Created by maoxin on 2017/11/20.
 */


public class TorchWidgetProvider extends AppWidgetProvider {

    private final static String TAG = TorchWidgetProvider.class.getName();
    public final static String RECEIVE_FLASH = "COM_FLASHLIGHT";
    private  static boolean isLightOn = false;
    private static Camera camera = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Intent receiver = new Intent(context, TorchWidgetProvider.class);
        receiver.setAction(Intent.ACTION_VIEW);
        receiver.setAction(RECEIVE_FLASH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_torch);
        views.setOnClickPendingIntent(R.id.btnTorch, pendingIntent);
        if(isLightOn) {
            views.setImageViewResource(R.id.btnTorch, R.drawable.flash_on);
            startTorchService(context, true);
        } else {
            views.setImageViewResource(R.id.btnTorch, R.drawable.flash_off);
            startTorchService(context, false);
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, intent.getAction());
        if(RECEIVE_FLASH.equals(intent.getAction())){
            handleCamera(context);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
            ComponentName thisWidget = new ComponentName(context.getApplicationContext(), TorchWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    private void handleCamera(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handleCameraOld();
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handleCameraNew(context);
        }
    }

    private void handleCameraOld(){
        if (isLightOn) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
                isLightOn = false;
            }
        } else {
            camera = Camera.open();
            if(camera != null) {
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                try {
                    camera.setParameters(param);
                    camera.startPreview();
                    isLightOn = true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void handleCameraNew(Context context){
        boolean result = Camera2Utils.torchSwitch(context, isLightOn);
        if(result)
            isLightOn = !isLightOn;
    }

    private void startTorchService(Context context, boolean flag){
        Intent intent = new Intent(context, TorchService.class);
        intent.putExtra("isOpen", flag);
        if(flag)
            context.startService(intent);
        else
            context.stopService(intent);
    }

}
