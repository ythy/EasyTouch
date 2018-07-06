package com.mx.testaidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mx.aidl.easytouch.IRemoteFavService;
import com.mx.aidl.easytouch.FavorApp;

public class MainActivity extends AppCompatActivity {

    private IRemoteFavService service;
    private RemoteServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectService();
    }

    private void connectService() {
        serviceConnection = new RemoteServiceConnection();
        Intent i = new Intent("com.mx.easytouch.service.FavorAppService");
        i.setPackage("com.mx.easytouch");
        boolean ret = bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }



    class RemoteServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binderservice) {
            service = IRemoteFavService.Stub.asInterface((IBinder) binderservice);
            try {
                Toast.makeText(MainActivity.this, service.getFavor().get(0).getName(), Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
