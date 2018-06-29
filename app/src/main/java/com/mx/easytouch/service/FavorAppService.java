package com.mx.easytouch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mx.aidl.easytouch.IRemoteFavService;
import com.mx.aidl.easytouch.FavorApp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by maoxin on 2018/6/28.
 */

public class FavorAppService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IRemoteFavService.Stub mBinder = new IRemoteFavService.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
        }

        @Override
        public void addFavor(String name) throws RemoteException {
        }

        @Override
        public List<FavorApp> getFavor() throws RemoteException {
            FavorApp info = new FavorApp("maoxin");
            List<FavorApp> result = new ArrayList<FavorApp>();
            result.add(info);
            return result;
        }

    };

}