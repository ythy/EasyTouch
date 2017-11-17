package com.mx.easytouch.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by maoxin on 2017/11/10.
 */

public class CommonUtils {

    public static void setSPType(Context context, String type, boolean flag) {
        SharedPreferences sp = context.getSharedPreferences("commonset", Context.MODE_PRIVATE);
        sp.edit().putBoolean(type, flag).commit();
    }

    public static boolean getSPType(Context context, String type) {
        SharedPreferences sp = context.getSharedPreferences("commonset", Context.MODE_PRIVATE);
        return sp.getBoolean(type, false);
    }

    public static void setMoveDelta(Context context, int length) {
        SharedPreferences sp = context.getSharedPreferences("commonset", Context.MODE_PRIVATE);
        sp.edit().putInt("moveDeltaValue", length).commit();
    }

    public static int getMoveDelta(Context context) {
        SharedPreferences sp = context.getSharedPreferences("commonset", Context.MODE_PRIVATE);
        return sp.getInt("moveDeltaValue", 10);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 是否是系统软件或者是系统软件的更新软件
     * @return
     */
    public static boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static boolean isSystemUpdateApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static void refreshMediaScanner(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//如果是4.4及以上版本
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file); //out is your output file
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }
}
