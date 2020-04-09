package com.mx.easytouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.mx.easytouch.service.FxService;

public class ActionReceiver extends BroadcastReceiver {

	private static String TAG = ActionReceiver.class.getName();
	public static String ACTION_ALARM = "com.mx.easytouch.alarm";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "onReceive " + (intent.getAction() == null ? "none" : intent.getAction() ) );
	 	if(intent.getAction().equals(Intent.ACTION_USER_PRESENT) || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
				intent.getAction().equals(ACTION_ALARM)) {
			int x = intent.getIntExtra("x", 0);
			int y = intent.getIntExtra("y", 0);
			Log.e(TAG, "onReceive " + x + " : "  + y );
			startFxService(context, x, y);
		}
	}

	private void startFxService(Context context, int x, int y){
		Intent fxIntent = new Intent(context, FxService.class);
		Bundle extras = new Bundle();
		extras.putInt("position_x", x);
		extras.putInt("position_y", y);
		fxIntent.putExtras(extras);
		//context.stopService(new Intent(context, FuncService.class)); //防止两个service同时存在，先关闭再开启
		context.startService(fxIntent);
	}

}

