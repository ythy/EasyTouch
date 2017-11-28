package com.mx.easytouch.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.mx.easytouch.R;


public class DialogAutoClick {




	public static void show(final Context context, final Handler handler,
							int mDuration, int[] mTime) {
		final AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		window.setContentView(R.layout.dialog_autoclick);

		final CheckBox checkBox = (CheckBox) window.findViewById(R.id.chkDelay);
		checkBox.setChecked(mTime != null);
		final int[] time = mTime == null ?  new int[]{7,18,00} : mTime;
		final EditText editText = (EditText) window.findViewById(R.id.etClick);
		editText.setText(String.valueOf(mDuration));

		TimePicker timepicker = (TimePicker) window.findViewById(R.id.timePicker);
		timepicker.setCurrentHour(time[0]);
		timepicker.setCurrentMinute(time[1]);;
		timepicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener(){
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				time[0] = hourOfDay;
				time[1] = minute;
			}
		});

		TimePicker timepickerSec = (TimePicker) window.findViewById(R.id.timePickerSec);
		timepickerSec.setCurrentMinute(time[2]);
		timepickerSec.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener(){
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				time[2] = minute;
			}
		});
		timepickerSec.setIs24HourView(true);
		timepicker.setIs24HourView(true);

		Button btnConfirm = (Button) window.findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = Message.obtain();
				msg.what = 4;
				Bundle data = new Bundle();
				data.putInt("duration", Integer.parseInt(editText.getText().toString()));
				data.putIntArray("timer", checkBox.isChecked() ? time : null);
				msg.setData(data);
				handler.sendMessage(msg);
				dlg.dismiss();
			}
		});

	}

}
