package com.mx.easytouch.service

import android.app.Application
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

import com.mx.easytouch.receiver.ActionReceiver
import com.mx.easytouch.utils.CommonUtils
import com.mx.easytouch.utils.Settings
import com.mx.easytouch.utils.ShellBase
import com.mx.easytouch.utils.TimeCount
import com.mx.easytouch.R
import com.mx.easytouch.components.FXAutoClick
import com.mx.easytouch.components.FXJiangShan

import java.util.Date

class FxService : Service() {

    // 定义浮动窗口布局
    private var mFloatLayout: FrameLayout? = null
    lateinit private var wmParams: WindowManager.LayoutParams
    // 创建浮动窗口设置布局参数的对象
    lateinit private var mWindowManager: WindowManager
    lateinit private var mTvFloat: TextView
    lateinit private var mBtnFloat: Button

    lateinit private var floatNotification: Notification
    private var mWakeLock: PowerManager.WakeLock? = null
    lateinit private var mShellBase: ShellBase
    private var mEndSelf: Boolean = false//个人关闭与否

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(sIntent: Intent?, flags: Int, startId: Int): Int {
        mShellBase = ShellBase()

        if (mFloatLayout == null) {
            if (sIntent != null && sIntent.extras != null) {
                val bundle = sIntent.extras
                val positionX = bundle.getInt("position_x", 0)
                val positionY = bundle.getInt("position_y", 0)

                if (bundle.getBoolean("autoclick", false)) {
                    createFloatView(0, 0)
                    setWackLock()
                    FXAutoClick(positionX + mBtnFloat.measuredWidth.div(2),
                            positionY + mBtnFloat.measuredHeight.div(2) + statusBarHeight,
                            bundle.getInt("frequency", 10), bundle.getIntArray("timer"), object : FXAutoClick.AutoClickHandle {
                        override fun floatText(input: String) {
                            mTvFloat.text = input
                        }

                        override fun end() {
                            onShow()
                        }
                    })
                } else if (bundle.getString("hyAuto") != null) {
                    createFloatView(positionX, positionY)
                    setWackLock()
                    FXJiangShan(object : FXJiangShan.JSHandle{
                        override fun floatText(input: String) {
                            mTvFloat.text = input
                        }
                    })
                } else if (bundle.getBoolean("screenshot", false)) {
                    this.startScreenShot(positionX, positionY)
                } else
                    createFloatView(positionX, positionY)
            } else
                createFloatView(0, 0)
        }

        if (CommonUtils.getSPType(this, Settings.SP_NOTIFICATION)) {
            this.floatNotification = Notification.Builder(applicationContext)
                    .setContentTitle("ythy")
                    .setContentText("EasyTouch")
                    .setSmallIcon(R.drawable.bar)
                    .build()
            floatNotification!!.flags = floatNotification!!.flags or Notification.FLAG_FOREGROUND_SERVICE
            startForeground(NOTIFICATION_ID, floatNotification)
        }

        if (sIntent!!.extras != null && sIntent.extras.getBoolean("screenshot", false))
            return Service.START_NOT_STICKY
        else
            return Service.START_REDELIVER_INTENT
    }

    private fun createFloatView(px: Int, py: Int) {
        wmParams = WindowManager.LayoutParams()
        // 获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = application.getSystemService(
                Application.WINDOW_SERVICE) as WindowManager
        Log.i(TAG, "mWindowManager--->" + mWindowManager)
        // 设置window type
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            wmParams.type = LayoutParams.TYPE_TOAST
        else
            wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT or Gravity.TOP
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = px
        wmParams.y = py

        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        val inflater = LayoutInflater.from(application)
        // 获取浮动窗口视图所在布局
        mFloatLayout = inflater.inflate(R.layout.window_float, null) as FrameLayout
        // 添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams)
        // 浮动窗口按钮
        mBtnFloat = mFloatLayout!!.findViewById(R.id.btnFloat) as Button
        mTvFloat = mFloatLayout!!.findViewById(R.id.tvFloat) as TextView
        mBtnFloat.background.alpha = 170
        mFloatLayout!!.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        // 设置监听浮动窗口的触摸移动
        mBtnFloat.setOnTouchListener(object : OnTouchListener {
            internal var moveFlag = false
            internal var start = PointF()
            internal var startRaw = PointF()
            internal var lastEventTime: Long = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        start.set(event.x, event.y)
                        startRaw.set(event.rawX, event.rawY)
                        moveFlag = false
                        lastEventTime = event.eventTime
                    }
                    MotionEvent.ACTION_UP -> if (!moveFlag) {
                        if (event.eventTime - lastEventTime > LongPressTime) {
                            mEndSelf = true
                            clean()
                            stopSelf()
                        } else
                            onShow()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = Math.sqrt(((event.x - start.x) * (event.x - start.x) + (event.y - start.y) * (event.y - start.y)).toDouble())
                        if (deltaX > CommonUtils.getMoveDelta(applicationContext)) {
                            moveFlag = true
                            val dm = DisplayMetrics()
                            mWindowManager.defaultDisplay.getMetrics(dm)
                            var xPoint = Math.round(event.rawX - mBtnFloat.measuredWidth / 2)
                            var yPoint = Math.round(event.rawY - mBtnFloat.measuredHeight / 2) - statusBarHeight
                            if (xPoint < 0)
                                xPoint = 0
                            else if (xPoint > dm.widthPixels - mBtnFloat.measuredWidth)
                                xPoint = dm.widthPixels - mBtnFloat.measuredWidth
                            wmParams.x = xPoint
                            if (yPoint < 0)
                                yPoint = 0
                            else if (yPoint > dm.heightPixels - statusBarHeight - mBtnFloat.measuredHeight)
                                yPoint = dm.heightPixels - statusBarHeight - mBtnFloat.measuredHeight
                            wmParams.y = yPoint
                            // 刷新
                            mWindowManager.updateViewLayout(mFloatLayout, wmParams)
                        }
                    }
                }
                return false
            }

        })

    }

    private fun setWackLock() {
        mWakeLock = (applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, javaClass.name)
        mWakeLock?.acquire()
    }

    //Environment.getExternalStorageDirectory()
    private fun startScreenShot(x: Int, y: Int) {
        val command = "/system/bin/screencap -p "
        val fileName = "/sdcard" + Settings.SRC_PATH + Date().time + "screenshot.png"
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        this.mShellBase.execShellCmd(command + fileName)
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        Toast.makeText(applicationContext, "screenshot captured", Toast.LENGTH_SHORT).show()
        createFloatView(x, y)
    }

    private fun onShow() {
        this.mEndSelf = true
        val intent = Intent(this@FxService, FuncService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.putExtra("position_x", wmParams.x)
        intent.putExtra("position_y", wmParams.y)
        intent.putExtra("timecount", TimeCount.getInstance().hackCount)
        TimeCount.getInstance().hackCount = 0
        startService(intent)
        clean()
        stopSelf()
    }

    private fun clean(){
        TimeCount.getInstance().hackCount = 0
        stopForeground(true)
        if (mFloatLayout != null) {
            // 移除悬浮窗口
            mWindowManager.removeView(mFloatLayout)
        }
        mWakeLock?.release()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        if (!mEndSelf) {
            ActionReceiver.setFloatButton(this)
        }
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        if (!mEndSelf) {
            ActionReceiver.setFloatButton(this)
        }
    }

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    companion object {
        private val TAG = "FxService"
        private val NOTIFICATION_ID = 45148
        private val LongPressTime: Long = 500
    }


}



