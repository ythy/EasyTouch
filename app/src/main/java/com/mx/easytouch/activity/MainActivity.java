package com.mx.easytouch.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mx.easytouch.R;
import com.mx.easytouch.adapter.FavAppAdapter;
import com.mx.easytouch.db.Providerdata;
import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.DBHelper;
import com.mx.easytouch.utils.ServiceUtil;
import com.mx.easytouch.utils.Settings;
import com.mx.easytouch.utils.StepConnection;
import com.mx.easytouch.utils.StepCountReporter;
import com.mx.easytouch.vo.InstallPackage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @OnClick(R.id.btnSetAccess)
    void onBtnSetAccessClickHandler(){
        Intent intent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @OnClick(R.id.btnSetApp)
    void onBtnSetAppClickHandler(){
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS);
        startActivity(intent);
    }


    @OnCheckedChanged({R.id.chkAuto, R.id.chkShot, R.id.chkRecent, R.id.chkNotification, R.id.chkFavorite, R.id.chkVolumn })
    void onCheckedAutoChanged(CompoundButton arg0, boolean arg1) {
        CommonUtils.setSPType(this, arg0.getTag().toString(), arg1);
    }

    @OnClick(R.id.btnAddFav)
    void onAddBtnClick(){
        showInstalledAppDialog();
    }

    @OnClick(R.id.btnFloat)
    void onFloatBtnClick(){
        this.showAlertWindows();
    }

    @BindView(R.id.listFav)
    ListView mListViewFav;

    @BindView(R.id.chkAuto)
    CheckBox mCheckAuto;

    @BindView(R.id.chkShot)
    CheckBox mCheckShot;

    @BindView(R.id.chkRecent)
    CheckBox mCheckRecent;

    @BindView(R.id.chkNotification)
    CheckBox mCheckNotification;

    @BindView(R.id.chkFavorite)
    CheckBox mCheckFavorite;

    @BindView(R.id.chkVolumn)
    CheckBox mCheckVolume;

    @BindView(R.id.seekBarFlip)
    SeekBar mSeekBarFlip;

    @BindView(R.id.tvSeekBarFlip)
    TextView tvSeekBarFlip;

    @BindView(R.id.tvStepCount)
    TextView tvStepCount;

    DBHelper mDBHelper;
    StepConnection mStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        showAlertWindows();
        initAllCheckBox();
        initSlipSeekBar();
        mDBHelper = new DBHelper(this, Providerdata.DATABASE_NAME,
                null, Providerdata.DATABASE_VERSION);
        refreshListView();

        mStep = new StepConnection(this, new StepCountReporter.StepCountObserver() {
            @Override
            public void onChanged(int count) {
                tvStepCount.setText("Step: " + count);
            }
        });

    }

    private void initSlipSeekBar(){
        int current = CommonUtils.getMoveDelta(this);
        mSeekBarFlip.setMax(50);
        mSeekBarFlip.setProgress(current);
        tvSeekBarFlip.setText(String.valueOf(current));
        mSeekBarFlip.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                tvSeekBarFlip.setText(String.valueOf(arg0.getProgress()));
                CommonUtils.setMoveDelta(getBaseContext(), arg0.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

            }
        });
    }

    private void initAllCheckBox(){
        initCheckBox(mCheckAuto, Settings.INSTANCE.getSP_AUTO_CLICK());
        initCheckBox(mCheckShot, Settings.INSTANCE.getSP_SCREENSHOT());
        initCheckBox(mCheckRecent, Settings.INSTANCE.getSP_RECENT_APP());
        initCheckBox(mCheckNotification, Settings.INSTANCE.getSP_NOTIFICATION());
        initCheckBox(mCheckFavorite, Settings.INSTANCE.getSP_FAVORITE());
        initCheckBox(mCheckVolume, Settings.INSTANCE.getSP_VOLUMN());
    }

    private void initCheckBox(CheckBox chk, String set){
        chk.setTag(set);
        chk.setChecked(CommonUtils.getSPType(this, set));
    }

    private void showInstalledAppDialog(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<InstallPackage> packages = new ArrayList<>();
                PackageManager pm = getPackageManager();
                List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
                for(PackageInfo ps : packageInfoList){
                    String appName = ps.applicationInfo.loadLabel(pm).toString();
                    if(!CommonUtils.isSystemApp(ps) && !CommonUtils.isSystemUpdateApp(ps))
                        packages.add(new InstallPackage( appName,  ps.packageName ));
                }
                Message msg = Message.obtain();
                msg.what = 3;
                Bundle data = new Bundle();
                data.putParcelableArrayList("packages", packages);
                msg.setData(data);
                dbHandler.sendMessage(msg);
            }
        }).start();
    }

    private void setAppListDialog(final List<InstallPackage> packages){
        //Create sequence of items
        final CharSequence[] dialogInfoList = new String[packages.size()];
        for(int i = 0; i < packages.size(); i++){
            dialogInfoList[i] =  packages.get(i).getAppName();
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Installed APP");
        dialogBuilder.setItems(dialogInfoList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int item) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long nid = mDBHelper.addFavApp(new InstallPackage(packages.get(item).getAppName(),
                            packages.get(item).getPackageName()));
                    if(nid > -1)
                        refreshListView();
                }
            }).start();
            }
        });
        AlertDialog alertDialogObject = dialogBuilder.create();
        alertDialogObject.show();
    }


    private void refreshListView(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<InstallPackage> current = mDBHelper.queryFavApp();
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle data = new Bundle();
                data.putParcelableArrayList("packages", current);
                msg.setData(data);
                dbHandler.sendMessage(msg);
            }
        }).start();

    }

    FavAppAdapter.FavAppTouchListener favTouchListener = new FavAppAdapter.FavAppTouchListener(){
        @Override
        public void onDelBtnClickListener(final InstallPackage info) {
            // TODO Auto-generated method stub
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDBHelper.delFavApp(info);
                    refreshListView();
                }
            }).start();
        }
    };

    static class  DBHandler extends Handler{

        WeakReference<MainActivity> weakReference;

        DBHandler(MainActivity activity){
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                List<InstallPackage> current = msg.getData().getParcelableArrayList("packages");
                FavAppAdapter adapter = new FavAppAdapter(weakReference.get().getBaseContext(), current);
                adapter.setFavAppTouchListener(weakReference.get().favTouchListener);
                weakReference.get().mListViewFav.setAdapter(adapter);
            }else if(msg.what == 3){
                List<InstallPackage> packages = msg.getData().getParcelableArrayList("packages");
                assert packages != null;
                weakReference.get().setAppListDialog(packages);
            }
        }
    }

    Handler dbHandler = new DBHandler(this);

    private void showAlertWindows()
    {
        ServiceUtil.Companion.startFxService(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.Close();
        mStep.disconnect();
    }
}