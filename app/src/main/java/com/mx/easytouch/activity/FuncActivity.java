package com.mx.easytouch.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mx.easytouch.R;
import com.mx.easytouch.service.FxService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class FuncActivity extends Activity {

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

    private int mPx;
    private int mPy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_func);

        ButterKnife.bind(this);
        mPx = getIntent().getIntExtra("position_x", 0);
        mPy = getIntent().getIntExtra("position_y", 0);
    }

    public void onBack() {
        Intent intent = new Intent(FuncActivity.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        startService(intent);
        this.finish();
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
    }
}
