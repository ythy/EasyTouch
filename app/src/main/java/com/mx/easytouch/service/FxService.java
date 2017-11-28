package com.mx.easytouch.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
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
import android.widget.TextView;
import android.widget.Toast;

import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.Settings;
import com.mx.easytouch.utils.ShellBase;
import com.mx.easytouch.utils.TimeCount;
import com.mx.easytouch.activity.FuncActivity;
import com.mx.easytouch.R;

import java.util.Date;

public class FxService extends Service {

	// 定义浮动窗口布局
	FrameLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	// 创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;

	TextView mTvFloat;
	Button mBtnFloat;
	Point mLastPoint = new Point();
	private static final String TAG = "FxService";
	private long mLongPressTime = 500;

	private Notification  floatNotification = null;
	private static final int  NOTIFICATION_ID = 45148;
	private PowerManager.WakeLock mWakeLock;
	ShellBase mShellBase;

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
		mShellBase = new ShellBase();
		if(mFloatLayout == null)
		{
			if(intent != null){
				if(intent.getBooleanExtra("autoclick", false)){
					createFloatView(0, 0);
					this.startAutoClick(intent.getIntExtra("position_x", 0), intent.getIntExtra("position_y", 0),
							intent.getIntExtra("frequency", 10), intent.getIntArrayExtra("timer"));
				}else if(intent.getStringExtra("hyAuto") != null){
					createFloatView(intent.getIntExtra("position_x", 0), intent.getIntExtra("position_y", 0));
					this.startHYAuto(intent.getStringExtra("hyAuto"));
				}else if(intent.getBooleanExtra("screenshot", false)){
					this.startScreenShot(intent.getIntExtra("position_x", 0), intent.getIntExtra("position_y", 0));
				}else
					createFloatView(intent.getIntExtra("position_x", 0), intent.getIntExtra("position_y", 0));
			}
			else
				createFloatView(0, 0);
		}

		if(CommonUtils.getSPType(this, Settings.SP_NOTIFICATION)){
			this.floatNotification = new Notification.Builder(getApplicationContext())
					.setContentTitle("ythy")
					.setContentText("EasyTouch")
					.setSmallIcon(R.drawable.bar)
					.build();
			floatNotification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
			startForeground(NOTIFICATION_ID, floatNotification);
		}

		if(intent.getBooleanExtra("screenshot", false))
			return START_NOT_STICKY;
		else
			return START_REDELIVER_INTENT;
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
		mTvFloat = (TextView) mFloatLayout.findViewById(R.id.tvFloat);
		mBtnFloat.getBackground().setAlpha(170);
		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		// 设置监听浮动窗口的触摸移动
		mBtnFloat.setOnTouchListener(new OnTouchListener() {
			boolean moveFlag = false;
			PointF start = new PointF();
			PointF startRaw = new PointF();
			long lastEventTime;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						start.set(event.getX(), event.getY());
						startRaw.set(event.getRawX(), event.getRawY());
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
						if (deltaX > CommonUtils.getMoveDelta(getApplicationContext())) {
							moveFlag = true;
							DisplayMetrics dm = new DisplayMetrics();
							mWindowManager.getDefaultDisplay().getMetrics(dm);
							int xPoint = Math.round ( event.getRawX()  -  mBtnFloat.getMeasuredWidth()/2 );
							int yPoint = Math.round ( event.getRawY()  -  mBtnFloat.getMeasuredHeight()/2 ) - getStatusBarHeight() ;
							if( xPoint < 0 )
								xPoint = 0;
							else if(xPoint > dm.widthPixels - mBtnFloat.getMeasuredWidth())
								xPoint = dm.widthPixels - mBtnFloat.getMeasuredWidth();
							wmParams.x =  xPoint;
							if( yPoint < 0 )
								yPoint = 0;
							else if(yPoint > dm.heightPixels - getStatusBarHeight() - mBtnFloat.getMeasuredHeight())
								yPoint = dm.heightPixels - getStatusBarHeight() -  mBtnFloat.getMeasuredHeight();
							wmParams.y = yPoint;
							// 刷新
							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
						}

						break;
				}
				return false;
			}

		});

	}

	Handler hyHandler  = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){
				mTvFloat.setText(String.valueOf(msg.arg1));
			}
		}
	};

	private void startAutoClick(int x, int y, int frequency, int[] timer){
		mAutoClickX = x;
		mAutoClickY = y;
		mAutoFrequency = frequency > 10 ? 10 : frequency;

		if(timer == null){
			startAutoClickRunnable((int) (60 * 10 * mAutoFrequency) );
		}else{
			mWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
			mWakeLock.acquire();
			Date now = new Date();
			long timerDate = new Date(now.getYear(), now.getMonth(), now.getDate(), timer[0], timer[1], timer[2]).getTime();
			long total = timerDate - now.getTime();
			mTvFloat.setText(timer[0] + ":" + timer[1] + ":" + timer[2]);
			if(total > 0){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						startAutoClickRunnable((int) (90 * mAutoFrequency));
					}
				}, total);
			}
		}

	}

	int mAutoClickX;
	int mAutoClickY;
	int mAutoFrequency;
	Runnable mAutoClickRunnable = new Runnable() {
		@Override
		public void run() {
			if(TimeCount.getInstance().subtractCount() >= 0){
				mShellBase.execShellCmd("input tap " + (mAutoClickX + mBtnFloat.getMeasuredWidth()/2) + " " +
						(mAutoClickY + mBtnFloat.getMeasuredHeight()/2 + getStatusBarHeight() ));
				if(TimeCount.getInstance().getHackCount() % mAutoFrequency == 0){
					Message msg = Message.obtain();
					msg.what = 1;
					msg.arg1 = TimeCount.getInstance().getHackMaxCount() - TimeCount.getInstance().getHackCount();
					mAutoclickHandler.sendMessage(msg);
				}
				if(TimeCount.getInstance().getHackCount() <= 0){
					TimeCount.getInstance().setHackCount(1);
					onShow();
				}
			}
		}
	};

	Handler mAutoclickHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){
				mTvFloat.setText(String.valueOf(msg.arg1));
			}
		}
	};


	private void startAutoClickRunnable(int max){
		TimeCount.getInstance().setHackCount(max);
		int i = 0;
		while(i++ < max){
			mAutoclickHandler.postDelayed(mAutoClickRunnable,
					1000/mAutoFrequency * i);
		}
	}

	private void  startHYAuto(String type){
		if("moyao".equals(type))
			startMoyao();
		else if("duilian".equals(type))
			startDuilian();
		else if("yigong".equals(type))
			startYigong();
	}

	private void startMoyao(){
		TimeCount.getInstance().setHackCount(Integer.MAX_VALUE);
		final int duration = 3;
		new Thread(new Runnable() {
			@Override
			public void run() {
				int turn = duration;
				mShellBase.sendKeyEventCode("chen gc");
				mShellBase.sendKeyEventCode("e");
				mShellBase.sendKeyEventCode("e");
				mShellBase.sendKeyEventCode("n");
				while(TimeCount.getInstance().subtractCount() > 0){
					mShellBase.sendKeyEventCode("ask ping about job");
					mShellBase.sendKeyEventCode("moyao", 8000);
					if(--turn == 0){
						turn = duration;
						mShellBase.sendKeyEventCode("xiuxi beg", 3000);
					}
				}
			}
		}
		).start();
	}

	private void startDuilian(){
		TimeCount.getInstance().setHackCount(Integer.MAX_VALUE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				mShellBase.sendKeyEventCode("remove all");
				mShellBase.sendKeyEventCode("wear all");
				while(TimeCount.getInstance().subtractCount() > 0){
					setFloatText();
					mShellBase.sendKeyEventCode("chen dl");
					mShellBase.sendKeyEventCode("n");
					mShellBase.sendKeyEventCode("n");
					mShellBase.sendKeyEventCode("e");
					mShellBase.sendKeyEventCode("n");
					excuteDuilian(new DuilianJia());
					excuteDuilian(new DuilianZhu());
					excuteDuilian(new DuilianFu());
					excuteDuilian(new DuilianWei());
					mShellBase.sendKeyEventCode("xiuxi beg", 3000);
				}
			}
		}
		).start();
	}

	private void setFloatText(){
		Message msg = Message.obtain();
		msg.arg1 = Integer.MAX_VALUE - TimeCount.getInstance().getHackCount();
		msg.what = 1;
		hyHandler.sendMessage(msg);
	}

	private void excuteDuilian(IDuilian duilian){
		int dlTurn = 7;
		mShellBase.sendKeyEventCode("job");
		duilian.go();
		while(dlTurn-- > 0 ){
			duilian.duilian();
		}
		duilian.back();
	}

	private void startYigong(){
		TimeCount.getInstance().setHackCount(Integer.MAX_VALUE);
		final int duration = 25;
		new Thread(new Runnable() {
			@Override
			public void run() {
				mShellBase.sendKeyEventCode("remove all");
				mShellBase.sendKeyEventCode("wear all");
				mShellBase.sendKeyEventCode("chen dl");
				mShellBase.sendKeyEventCode("n");
				mShellBase.sendKeyEventCode("n");
				mShellBase.sendKeyEventCode("e");
				mShellBase.sendKeyEventCode("n");
				mShellBase.sendKeyEventCode("enter");
				mShellBase.sendKeyEventCode("s");
				mShellBase.sendKeyEventCode("enter");
				mShellBase.sendKeyEventCode("ne");
				mShellBase.sendKeyEventCode("get huachu");
				mShellBase.sendKeyEventCode("wield huachu");
				int xiuxiDuration = duration;
				while(TimeCount.getInstance().subtractCount() > 0){
					setFloatText();
					mShellBase.sendKeyEventCode(1000, "ask huajiang about job");
					int dur = 6;
					while(dur-- > 0) {
						if(xiuxiDuration-- < 0){
							xiuxiDuration = duration;
							mShellBase.sendKeyEventCode("xiuxi beg", 3000);
						}
						mShellBase.sendKeyEventCode("work", 1000);
					}
				}
			}
		}
		).start();
	}

	//Environment.getExternalStorageDirectory()
	private void startScreenShot(int x, int y){
		String command = "/system/bin/screencap -p ";
		String fileName = "/sdcard"  + Settings.SRC_PATH + new Date().getTime() + "screenshot.png";
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.mShellBase.execShellCmd(command + fileName);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "screenshot captured", Toast.LENGTH_SHORT).show();
		createFloatView(x, y);
	}

	private void onShow() {
		Intent intent = new Intent(FxService.this, FuncActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("position_x", wmParams.x);
		intent.putExtra("position_y", wmParams.y);
		intent.putExtra("timecount", TimeCount.getInstance().getHackCount());
		TimeCount.getInstance().setHackCount(0);
		startActivity(intent);
		stopSelf();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		TimeCount.getInstance().setHackCount(0);
		stopForeground(true);
		if (mFloatLayout != null) {
			// 移除悬浮窗口
			mWindowManager.removeView(mFloatLayout);
		}
		if(mWakeLock != null){
			mWakeLock.release();
		}
		mAutoclickHandler.removeCallbacksAndMessages(null);
	}

	private  int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/// 以下 内部class

	interface IDuilian{
		void go();
		void duilian();
		void back();
	}

	class DuilianZhu extends ShellBase implements IDuilian {

		@Override
		public void go() {
		}

		@Override
		public void duilian() {
			sendKeyEventCode("duilian zhu wanli", 1500);
		}

		@Override
		public void back() {
		}
	};

	class DuilianWei extends ShellBase implements IDuilian {

		@Override
		public void go() {
		}

		@Override
		public void duilian() {
			sendKeyEventCode("duilian wei shi", 1500);
		}

		@Override
		public void back() {
		}
	};

	class DuilianJia extends ShellBase implements IDuilian {

		@Override
		public void go() {
			sendKeyEventCode("enter");
		}

		@Override
		public void duilian() {
			sendKeyEventCode("duilian jia ding", 1500);
		}

		@Override
		public void back() {
			sendKeyEventCode("out");
		}
	};

	class DuilianFu extends ShellBase implements IDuilian {

		@Override
		public void go() {
			sendKeyEventCode("s");
		}

		@Override
		public void duilian() {
			sendKeyEventCode("duilian fu sigui", 1500);
		}

		@Override
		public void back() {
			sendKeyEventCode( "n");
		}
	};

}



