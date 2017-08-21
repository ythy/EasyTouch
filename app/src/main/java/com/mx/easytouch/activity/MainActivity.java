package com.mx.easytouch.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mx.easytouch.R;
import com.mx.easytouch.service.FxService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        showAlertWindows();
    }

    private void showAlertWindows()
    {
        Intent intent = new Intent(MainActivity.this, FxService.class);
        //启动FxService
        startService(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
