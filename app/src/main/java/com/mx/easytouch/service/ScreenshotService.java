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
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.mx.easytouch.utils.CommonUtils;
import com.mx.easytouch.utils.FileUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by maoxin on 2017/11/16.
 */

@SuppressLint("NewApi")
public class ScreenshotService extends Service {

    private static final String SCREEN_CAP_NAME = "screen_cap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
    private static Intent mResultData = null;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay; // 捕获屏幕
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

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
        DisplayMetrics mMetrics = new DisplayMetrics();
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
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
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
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREEN_CAP_NAME,
                mScreenWidth, mScreenHeight, mScreenDensity,
                VIRTUAL_DISPLAY_FLAGS,
                mImageReader.getSurface(), null, null);
    }

    ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "in OnImageAvailable");
            FileOutputStream fos = null;
            Bitmap bitmap = null;
            try (Image image = reader.acquireLatestImage()) {
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    if (planes[0].getBuffer() == null) {
                        return;
                    }
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int pixelStride = planes[0].getPixelStride(); // 像素宽度
                    int rowStride = planes[0].getRowStride(); // 每行的宽度
                    int rowPadding = rowStride - pixelStride * width; //每行的像素外边距
                    ByteBuffer buffer = planes[0].getBuffer();
                    bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    //此处应先通过copyPixelsFromBuffer把像素导入，再进行处理。然后通过判断像素值为0去除左右两边空白部分(原因未明)
                    bitmap.copyPixelsFromBuffer(buffer);
                    //此处开始处理透明冗余图片区域
                    int start = -1;
                    int end = bitmap.getWidth() - 1;
                    int top = 0;
                    int middle = bitmap.getWidth() * pixelStride / 2; // 每行中间像素位置， 如果是0换下一行
                    int offset = 0;

                    for (int i = 0; i < bitmap.getHeight(); ++i) {
                        if(buffer.get(middle) == 0){
                            top++;
                            offset += rowStride;
                            middle += rowStride;
                            continue;
                        }
                        for (int j = 0; j < bitmap.getWidth(); ++j) {
                            if(buffer.get(offset) != 0 && start == -1){
                                start = j;
                                offset += pixelStride;
                                continue;
                            }
                            if(buffer.get(offset) == 0
                                    && buffer.get(offset + 1) == 0
                                    && buffer.get(offset + 2) == 0
                                    && buffer.get(offset + 3) == 0
                                    && start != -1){
                                end = j;
                                break;
                            }
                            offset += pixelStride;
                        }
                        break;
                    }
                    Log.d("ScreenCapture", " start : " + start + " - " + end + " ,  top:  " + top  );
                    bitmap = Bitmap.createBitmap( bitmap, start, top, end + 1 - start, height - top);
                    //处理结束
                    image.close();

                    // write bitmap to a file
                    final String fileName = FileUtil.getScreenShotsName(getApplicationContext());
                    fos = new FileOutputStream(fileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(getApplicationContext(), "screenshot captured", Toast.LENGTH_SHORT).show();
                    CommonUtils.mediaScan(getApplicationContext(), new File(fileName));
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
