package com.mx.easytouch.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.mx.easytouch.activity.FuncActivity;
import com.mx.easytouch.R;

public class FxService extends Service {

	// 定义浮动窗口布局
	FrameLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	// 创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;

	Button mBtnFloat;
	Point mLastPoint = new Point();
	private static final String TAG = "FxService";
	private long mLongPressTime = 500;

	private Notification updateNotification = null;
	private static final int  NOTIFICATION_ID = 55188;

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

		if(mFloatLayout == null)
		{
			if(intent != null)
				createFloatView(intent.getIntExtra("position_x", 0), intent.getIntExtra("position_y", 0));
			else
				createFloatView(0, 0);
		}

//		this.updateNotification = new Notification();
//	    updateNotification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
//		startForeground(NOTIFICATION_ID, updateNotification);

		return START_STICKY;
	}

	private void createFloatView(int px, int py) {
		wmParams = new WindowManager.LayoutParams();
		// 获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication().getSystemService(
				getApplication().WINDOW_SERVICE);
		Log.i(TAG, "mWindowManager--->" + mWindowManager);
		// 设置window type
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			wmParams.type = LayoutParams.TYPE_TOAST;
		else
			wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = px;
		wmParams.y = py;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatLayout = (FrameLayout) inflater.inflate(R.layout.widow_float,
				null);
		// 添加mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);
		// 浮动窗口按钮
		mBtnFloat = (Button) mFloatLayout.findViewById(R.id.btnFloat);
		mBtnFloat.getBackground().setAlpha(170);
		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		// 设置监听浮动窗口的触摸移动
		mBtnFloat.setOnTouchListener(new OnTouchListener() {
			boolean moveFlag = false;
			PointF start = new PointF();
			long lastEventTime;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					start.set(event.getX(), event.getY());
					moveFlag = false;
					lastEventTime = event.getEventTime();
					break;
				case MotionEvent.ACTION_UP:
					if(!moveFlag)
					{
						if(event.getEventTime() - lastEventTime > mLongPressTime)
							stopSelf();
						else
							onShow();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					double deltaX = Math.sqrt((event.getX() - start.x)
							* (event.getX() - start.x)
							+ (event.getY() - start.y)
							* (event.getY() - start.y));
					if (deltaX > 10) {
						moveFlag = true;
						wmParams.x = (int) event.getRawX()
								- mBtnFloat.getMeasuredWidth() / 2;
						// 减25为状态栏的高度
						wmParams.y = (int) event.getRawY()
								- mBtnFloat.getMeasuredHeight() / 2 - 25;
						// 刷新
						mWindowManager.updateViewLayout(mFloatLayout, wmParams);
					}

					break;
				}
				return false;
			}

		});

	}

	private void onShow() {
		Intent intent = new Intent(FxService.this, FuncActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("position_x", wmParams.x);
		intent.putExtra("position_y", wmParams.y);
		startActivity(intent);
		stopSelf();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopForeground(true);
		if (mFloatLayout != null) {
			// 移除悬浮窗口
			mWindowManager.removeView(mFloatLayout);
		}
	}

}