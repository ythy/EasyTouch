package com.mx.easytouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.mx.easytouch.service.FxService;

public class ActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	 
		Intent smsIntent = new Intent(context,
				FxService.class);
		context.startService(smsIntent);
	}

}

