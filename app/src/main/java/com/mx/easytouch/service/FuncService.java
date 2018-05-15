package com.mx.easytouch.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mx.easytouch.R;
import com.mx.easytouch.activity.MediaActivity;
import com.mx.easytouch.db.Providerdata;
import com.mx.easytouch.dialog.DialogAutoClick;
import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.DBHelper;
import com.mx.easytouch.vo.InstallPackage;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by maoxin on 2018/5/10.
 */

public class FuncService extends Service {

    // 定义浮动窗口布局
    FrameLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    // 创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    @BindView(R.id.tvAutoClick)
    TextView tvClick;

    @OnClick(R.id.tvAutoClick)
    void onAutoClicSetkBtnClickHandler(View v){
        DialogAutoClick.show(FuncService.this, mainHandler, mAutoClickDuration, mAutoClickTime);
    }

    @OnClick(R.id.ll_parent)
    void onllParentClickHandler(View v){
        onBack();
    }

    @OnTouch(R.id.ll_main)
    boolean onllMainClickHandler(View v, MotionEvent event){
        return true;
    }

    @OnClick(R.id.btnHome)
    void onHomeBtnClickHandler(View v){
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);
        onBack();
    }
    @OnClick(R.id.btn_click)
    void onAutoClickBtnClickHandler(View v){
        onBackAutoClick();
    }

    @OnClick(R.id.btnHack)
    void onHackClickBtnClickHandler(View v){
        onBackHackClick();
    }

    @OnClick(R.id.btnScreenshot)
    void onScreenShotClickBtnClickHandler(View v){
        onBackScreenShotClick();
    }

    @BindView(R.id.spinnerName)
    Spinner spinnerName;

    @BindView(R.id.llTaskBar)
    LinearLayout llTaskBar;

    @BindView(R.id.llFavApp)
    LinearLayout llFavApp;

    @BindView(R.id.ll_main)
    LinearLayout llmain;

    @BindView(R.id.hsViewTask)
    HorizontalScrollView hsTask;

    @BindView(R.id.hsFavApp)
    HorizontalScrollView hsFav;

    @BindView(R.id.ll_autoclick)
    LinearLayout llAutoclick;

    @BindView(R.id.ll_hy)
    LinearLayout llhy;

    @BindView(R.id.ll_screenshot)
    LinearLayout llscreenshot;

    @BindView(R.id.llVolumn)
    LinearLayout llVolumn;

    @BindView(R.id.seebarVolumnMusic)
    SeekBar seekBarVolumnMusic;

    @BindView(R.id.seebarVolumnRing)
    SeekBar seekBarVolumnRing;

    DBHelper mDBHelper;
    public static final int REQUEST_MEDIA_PROJECTION = 155;
    public static final String TAG = FuncService.class.getName();
    private int mPx;
    private int mPy;

    private int mAutoClickDuration = 10;
    private int[] mAutoClickTime = new int[]{7,18,00};


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPx = intent.getIntExtra("position_x", 0);
        mPy = intent.getIntExtra("position_y", 0);
        mDBHelper = new DBHelper(this, Providerdata.DATABASE_NAME,
                null, Providerdata.DATABASE_VERSION);
        if(intent.getIntExtra("timecount", 0) > 0)
            onBack();
        else{
            setWindow();
            setLayout();
        }
        return Service.START_NOT_STICKY;
    }

    private void setWindow(){
        wmParams = new WindowManager.LayoutParams();
        // 获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(
                getApplication().WINDOW_SERVICE);
        // 设置window type
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        else
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity

        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        // 获取浮动窗口视图所在布局
        mFloatLayout = (FrameLayout) inflater.inflate(R.layout.window_func,
                null);
        // 添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
    }

    private void setLayout(){
        ButterKnife.bind(this, mFloatLayout);
        llmain.post(new Runnable() {
            @Override
            public void run() {
                setTaskBoxLayout();
                if(CommonUtils.getSPType(getApplicationContext(), com.mx.easytouch.utils.Settings.SP_RECENT_APP)){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                        getTaskList();
                    else
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getTaskListH();
                            }
                        }).start();
                }
                if(CommonUtils.getSPType(getApplicationContext(), com.mx.easytouch.utils.Settings.SP_FAVORITE)){
                    getFavList();
                }
            }
        });


        if(CommonUtils.getSPType(this, com.mx.easytouch.utils.Settings.SP_AUTO_CLICK)){
            llAutoclick.setVisibility(View.VISIBLE);
            tvClick.setText(mAutoClickDuration + " " + ( mAutoClickTime == null ? "" : mAutoClickTime[0] + ":"  + mAutoClickTime[1] ) );
        }
        else
            llAutoclick.setVisibility(View.GONE);

        if(CommonUtils.getSPType(this, com.mx.easytouch.utils.Settings.SP_HY_AUTO)) {
            llhy.setVisibility(View.VISIBLE);
            spinnerName.setSelection(0);
        }else
            llhy.setVisibility(View.GONE);

        if(CommonUtils.getSPType(this, com.mx.easytouch.utils.Settings.SP_SCREENSHOT))
            llscreenshot.setVisibility(View.VISIBLE);
        else
            llscreenshot.setVisibility(View.GONE);

        if(CommonUtils.getSPType(this, com.mx.easytouch.utils.Settings.SP_VOLUMN)){
            llVolumn.setVisibility(View.VISIBLE);
            setVolumn(seekBarVolumnMusic, AudioManager.STREAM_MUSIC);
            setVolumn(seekBarVolumnRing, AudioManager.STREAM_RING);
        }
        else
            llVolumn.setVisibility(View.GONE);
    }

    private void setVolumn(SeekBar seekBar, final int type){
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(type);
        int currentVolume = audioManager.getStreamVolume(type);

        seekBar.setMax(maxVolume);
        seekBar.setProgress(currentVolume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(type, arg1, 0);
            }
        });


    }

    private void setTaskBoxLayout() {
        //设置容器宽度
        final int minWidth = CommonUtils.dip2px(this, 240);
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams( Math.max(minWidth,llmain.getMeasuredWidth())
                , RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (CommonUtils.getSPType(getApplicationContext(), com.mx.easytouch.utils.Settings.SP_RECENT_APP)){
            lps.addRule(RelativeLayout.BELOW, R.id.ll_main);
            lps.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            this.hsTask.setLayoutParams(lps);
            setFavBoxLayout(true);
        }else
            setFavBoxLayout(false);
    }

    private void setFavBoxLayout(boolean flag) {
        final int minWidth = CommonUtils.dip2px(this, 240);
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams( Math.max(minWidth,llmain.getMeasuredWidth())
                , RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (CommonUtils.getSPType(getApplicationContext(), com.mx.easytouch.utils.Settings.SP_FAVORITE)){
            lps.addRule(RelativeLayout.BELOW, flag ? R.id.hsViewTask : R.id.ll_main);
            lps.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            this.hsFav.setLayoutParams(lps);
        }
    }


    public void getFavList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<InstallPackage> current = mDBHelper.queryFavApp();
                Message msg = Message.obtain();
                msg.what = 3;
                msg.obj = current;
                mainHandler.sendMessage(msg);
            }
        }).start();
    }

    public void getTaskList() {
        ImageButton ivIcon;
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        PackageManager pm = this.getPackageManager();
        try {
            List<ActivityManager.RecentTaskInfo> list = am.getRecentTasks(12, 0);
            llTaskBar.removeAllViews();
            for (ActivityManager.RecentTaskInfo ti : list) {
                final Intent intent = ti.baseIntent;
                ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
                if (resolveInfo != null) {
                    ivIcon = getAppIcon(intent.getComponent().getPackageName(), intent);
                    llTaskBar.addView(ivIcon);
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void getTaskListH(){
        final long currentTime = System.currentTimeMillis(); // Get current time in milliseconds
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1); // Set year to beginning of desired period.
        final long beginTime = cal.getTimeInMillis(); // Get begin time in milliseconds

        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, currentTime);
        Collections.sort(queryUsageStats, new Comparator<UsageStats>() {
            @Override
            public int compare(UsageStats o1, UsageStats o2) {
                return (int) (o2.getLastTimeStamp() - o1.getLastTimeStamp());
            };
        });
        mainHandler.sendEmptyMessage(2);
        ImageButton ivIcon;
        final PackageManager pm = this.getPackageManager();
        for (final UsageStats us : queryUsageStats) {
            ivIcon = getAppIcon(us.getPackageName(), pm.getLaunchIntentForPackage(us.getPackageName()));
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = ivIcon;
            mainHandler.sendMessage(msg);
        }
    }



    public void onBackAutoClick() {
        Intent intent = new Intent(FuncService.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        intent.putExtra("autoclick", true);
        intent.putExtra("frequency", mAutoClickDuration);
        intent.putExtra("timer", mAutoClickTime);
        startService(intent);
        this.stopSelf();
    }

    public void onBackHackClick() {

        Intent intent = new Intent(FuncService.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        intent.putExtra("hyAuto", spinnerName.getSelectedItem().toString());
        startService(intent);
        this.stopSelf();
    }

    public void onBackScreenShotClick() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Intent intent = new Intent(FuncService.this, MediaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("position_x", mPx);
            intent.putExtra("position_y", mPy);
            startActivity(intent);
        }else{
            Intent intent = new Intent(FuncService.this, FxService.class);
            intent.putExtra("position_x", mPx);
            intent.putExtra("position_y", mPy);
            intent.putExtra("screenshot", true);
            startService(intent);
        }
        this.stopSelf();
    }

    Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                ImageButton imageButton = (ImageButton) msg.obj;
                llTaskBar.addView(imageButton);
            }else if(msg.what == 2){
                llTaskBar.removeAllViews();
            }else if(msg.what == 3){
                List<InstallPackage> current = (List<InstallPackage>) msg.obj;
                setFavAppList(current);
            }else if(msg.what == 4){
                Bundle data = msg.getData();
                mAutoClickDuration = data.getInt("duration");
                mAutoClickTime = data.getIntArray("timer");
                tvClick.setText(mAutoClickDuration + " " + ( mAutoClickTime == null ? "" : mAutoClickTime[0] + ":"  + mAutoClickTime[1] ) );
            }
        }
    };

    private ImageButton getAppIcon(String packageName, final Intent ivIntent){
        ImageButton ivIcon;
        final PackageManager pm = getPackageManager();
        ivIcon = new ImageButton(this);
        try {
            ivIcon.setImageDrawable(pm.getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ivIcon.setFocusable(true);
        ivIcon.setClickable(true);
        ivIcon.setEnabled(true);
        ivIcon.setScaleType(ImageView.ScaleType.CENTER);
        ivIcon.setLayoutParams(new LinearLayout.LayoutParams(CommonUtils.dip2px(this, 60), CommonUtils.dip2px(this, 60)));
        if (ivIntent != null) {
            ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ivIntent);
                    onBack();
                }
            });
        }
        return ivIcon;
    }

    private void setFavAppList(List<InstallPackage> current){
        llFavApp.removeAllViews();
        ImageButton ivIcon;
        for (final InstallPackage us : current) {
            ivIcon = getAppIcon(us.getPackageName(), getPackageManager().getLaunchIntentForPackage(us.getPackageName()));
            llFavApp.addView(ivIcon);
        }
    }

    public void onBack() {
        Intent intent = new Intent(FuncService.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        startService(intent);
        this.stopSelf();
        if (mFloatLayout != null) {
            // 移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDBHelper.close();
    }
}
