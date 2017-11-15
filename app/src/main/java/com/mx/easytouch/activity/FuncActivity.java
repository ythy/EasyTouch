package com.mx.easytouch.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.mx.easytouch.R;
import com.mx.easytouch.db.Providerdata;
import com.mx.easytouch.service.FxService;
import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.DBHelper;
import com.mx.easytouch.vo.InstallPackage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTouch;

public class FuncActivity extends Activity {

    @BindView(R.id.etClick)
    EditText etClick;

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
    private int mPx;
    private int mPy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPx = getIntent().getIntExtra("position_x", 0);
        mPy = getIntent().getIntExtra("position_y", 0);
        mDBHelper = new DBHelper(this, Providerdata.DATABASE_NAME,
                null, Providerdata.DATABASE_VERSION);
        if(getIntent().getIntExtra("timecount", 0) > 0)
            onBack();
        else{
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_func);
            ButterKnife.bind(this);
            setLayout();
        }
    }

    private void setLayout(){
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


        if(CommonUtils.getSPType(this, com.mx.easytouch.utils.Settings.SP_AUTO_CLICK))
            llAutoclick.setVisibility(View.VISIBLE);
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


    public void onBack() {
        Intent intent = new Intent(FuncActivity.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        startService(intent);
        this.finish();
    }

    public void onBackAutoClick() {
        Intent intent = new Intent(FuncActivity.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        intent.putExtra("autoclick", true);
        intent.putExtra("frequency", Integer.parseInt(String.valueOf(etClick.getText())));
        startService(intent);
        this.finish();
    }

    public void onBackHackClick() {

        Intent intent = new Intent(FuncActivity.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        intent.putExtra("hyAuto", spinnerName.getSelectedItem().toString());
        startService(intent);
        this.finish();
    }

    public void onBackScreenShotClick() {

        Intent intent = new Intent(FuncActivity.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        intent.putExtra("screenshot", true);
        startService(intent);
        this.finish();
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.Close();
    }


}
