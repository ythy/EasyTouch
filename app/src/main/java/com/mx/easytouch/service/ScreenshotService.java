package com.mx.easytouch.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.mx.easytouch.activity.FuncActivity;
import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by maoxin on 2017/11/16.
 */

@SuppressLint("NewApi")
public class ScreenshotService extends Service {

    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static Intent mResultData = null;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay; // 捕获屏幕
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private DisplayMetrics mMetrics ;

    private int mPx;
    private int mPy;

    private final static String TAG = ScreenshotService.class.getName();

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
        mPx = intent.getIntExtra("position_x", 0);
        mPy = intent.getIntExtra("position_y", 0);
        initParams();
        startScreenShot();
        return START_NOT_STICKY;
    }

    public static void setResultData(Intent mResultData) {
        ScreenshotService.mResultData = mResultData;
    }

    private void initParams(){
        mMetrics = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mScreenDensity = mMetrics.densityDpi;
        mScreenWidth = mMetrics.widthPixels;
        mScreenHeight = mMetrics.heightPixels;
        createImageReader();
    }

    private void startScreenShot() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startVirtual();
            }
        }, 500);
    }

    private void createImageReader() {
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);
    }

    private void startVirtual() {
        setUpMediaProjection();
        virtualDisplay();
    }

    private void setUpMediaProjection() {
        mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME,
                mScreenWidth, mScreenHeight, mScreenDensity,
                VIRTUAL_DISPLAY_FLAGS,
                mImageReader.getSurface(), null, null);
    }

    ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "in OnImageAvailable");
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    if (planes[0].getBuffer() == null) {
                        return;
                    }
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    int offset = 0;
                    bitmap = Bitmap.createBitmap(mMetrics, width, height, Bitmap.Config.ARGB_8888);
                    ByteBuffer buffer = planes[0].getBuffer();
                    for (int i = 0; i < height; ++i) {
                        for (int j = 0; j < width; ++j) {
                            int pixel = 0;
                            pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                            pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                            pixel |= (buffer.get(offset + 2) & 0xff);       // B
                            pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                            bitmap.setPixel(j, i, pixel);
                            offset += pixelStride;
                        }
                        offset += rowPadding;
                    }
                    // write bitmap to a file
                    String fileName = FileUtil.getScreenShotsName(getApplicationContext());
                    fos = new FileOutputStream(fileName);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Toast.makeText(getApplicationContext(), "screenshot captured", Toast.LENGTH_SHORT).show();
                    CommonUtils.refreshMediaScanner(getApplicationContext(), new File(fileName));
                    onBack();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (image != null) {
                    image.close();
                }
                stopImageReader();
            }
        }
    };

    private void onBack(){
        Intent intent = new Intent(ScreenshotService.this, FxService.class);
        intent.putExtra("position_x", mPx);
        intent.putExtra("position_y", mPy);
        startService(intent);
        stopSelf();
    }

    private void stopMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    private void stopImageReader(){
        if(mImageReader != null){
            mImageReader.setOnImageAvailableListener(null, null);
            mImageReader.close();
        }
    }

    @Override
    public void onDestroy() {
        // to remove mFloatLayout from windowManager
        super.onDestroy();
        stopVirtual();
        stopMediaProjection();
    }

}
