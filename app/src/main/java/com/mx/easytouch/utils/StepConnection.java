package com.mx.easytouch.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Collections;
import java.util.Map;

/**
 * Created by maoxin on 2018/7/6.
 */

public class StepConnection {

    private static final String TAG = "StepConnection";
    private HealthDataStore mStore;
    private StepCountReporter mReporter;
    private Activity mContext;
    private StepCountReporter.StepCountObserver mStepCountObserver;

    public StepConnection(Activity context, StepCountReporter.StepCountObserver observer){
        this.mContext = context;
        this.mStepCountObserver = observer;
        connectHealth();
    }

    public void disconnect(){
        if(mStore != null)
            mStore.disconnectService();
    }

    private  void connectHealth(){
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(mContext, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();
    }



    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            mReporter = new StepCountReporter(mStore);
            if (isPermissionAcquired()) {
                mReporter.start(mStepCountObserver);
            } else {
                requestPermission();
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(TAG, "Health data service is not available.");
        }

        @Override
        public void onDisconnected() {
        }
    };

    private boolean isPermissionAcquired() {
        HealthPermissionManager.PermissionKey permKey = new HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Check whether the permissions that this application needs are acquired
            Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(Collections.singleton(permKey));
            return resultMap.get(permKey);
        } catch (Exception e) {
            Log.e(TAG, "Permission request fails.", e);
        }
        return false;
    }

    private void requestPermission() {
        HealthPermissionManager.PermissionKey permKey = new HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(Collections.singleton(permKey), mContext)
                    .setResultListener(new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>(){
                        @Override
                        public void onResult(HealthPermissionManager.PermissionResult result) {
                            Log.d(TAG, "Permission callback is received.");
                            Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                            if (resultMap.containsValue(Boolean.FALSE)) {
                                Log.d(TAG, "Permission callback Fail.");
                            } else {
                                // Get the current step count and display it
                                mReporter.start(mStepCountObserver);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Permission setting fails.", e);
        }
    }

}
