package com.mx.easytouch.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


import com.mx.easytouch.service.FuncService;
import com.mx.easytouch.service.FxService;
import com.mx.easytouch.utils.CommonUtils;

public class ActionReceiver extends BroadcastReceiver {

	private static String TAG = ActionReceiver.class.getName();
	public static String ACTION_ALARM = "com.mx.easytouch.alarm";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "onReceive " + (intent.getAction() == null ? "none" : intent.getAction() ) );
	 	if(intent.getAction().equals(Intent.ACTION_USER_PRESENT) || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
				intent.getAction().equals(ACTION_ALARM)) {
			this.startFloatService(context);
		}
	}

	public static void setFloatButton(Context context)
	{
		Intent intent = new Intent(context, ActionReceiver.class);
		intent.setAction(ACTION_ALARM);
		context.sendBroadcast(intent);
//		AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
//		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 50, pi);
	}

	private void startFloatService(Context context){
		Intent fxIntent = new Intent(context, FxService.class);
		if(!CommonUtils.isMyServiceRunning(context, FuncService.class) && !CommonUtils.isMyServiceRunning(context, FxService.class))
			context.startService(fxIntent);
	}

}

