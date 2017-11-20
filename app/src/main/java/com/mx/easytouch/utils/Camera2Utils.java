package com.mx.easytouch.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by maoxin on 2017/11/20.
 */

public class Camera2Utils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean  torchSwitch(Context context, boolean isLightOn){
        boolean result = false;
        try {
            android.hardware.camera2.CameraManager manager = (android.hardware.camera2.CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            manager.setTorchMode("0", isLightOn ? false: true);
            result = true;
        } catch (android.hardware.camera2.CameraAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

}
