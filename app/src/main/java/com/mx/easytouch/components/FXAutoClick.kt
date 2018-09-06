package com.mx.easytouch.components

import android.os.Handler
import android.os.Message
import com.mx.easytouch.utils.ShellBase
import com.mx.easytouch.utils.TimeCount
import java.util.Date

/**
 * Created by maoxin on 2018/8/13.
 */

class FXAutoClick(x: Int, y: Int, frequency: Int, timer: IntArray, private val mAutoClickHandle: FXAutoClick.AutoClickHandle) {

    private val mShellBase: ShellBase
    private var mAutoClickX: Int = 0
    private var mAutoClickY: Int = 0
    private var mAutoFrequency: Int = 0

    interface AutoClickHandle {
        fun floatText(input: String)
        fun end()
    }

    init {
        mShellBase = ShellBase()
        startAutoClick(x, y, frequency, timer)
        //Log.e("HGHGHGHGHGHHG", x + " : " + y);
    }

    private fun startAutoClick(x: Int, y: Int, frequency: Int, timer: IntArray?) {
        mAutoClickX = x
        mAutoClickY = y
        mAutoFrequency = if (frequency > 5) 5 else frequency

        if (timer == null) {
            startAutoClickRunnable((60 * 10 * mAutoFrequency).toInt())
        } else {

            val now = Date()
            val timerDate = Date(now.year, now.month, now.date, timer[0], timer[1], timer[2]).time
            val total = timerDate - now.time
            mAutoClickHandle.floatText(timer[0].toString() + ":" + timer[1] + ":" + timer[2])
            if (total > 0) {
                Handler().postDelayed({ startAutoClickRunnable((90 * mAutoFrequency).toInt()) }, total)
            }
        }

    }


    internal var mAutoClickRunnable: Runnable = Runnable {
        while (TimeCount.getInstance().subtractCount() > 0) {
            mShellBase.execShellCmd("input tap $mAutoClickX $mAutoClickY")
            if (TimeCount.getInstance().hackCount % mAutoFrequency === 0) {
                val msg = Message.obtain()
                msg.what = 1
                msg.arg1 = TimeCount.getInstance().hackCount
                mAutoclickHandler.sendMessage(msg)
            }
            try {
                Thread.sleep((1000 / mAutoFrequency).toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        mAutoClickHandle.end()
    }

    internal var mAutoclickHandler: Handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                mAutoClickHandle.floatText(msg.arg1.toString())
            }
        }
    }


    private fun startAutoClickRunnable(max: Int) {
        TimeCount.getInstance().hackCount = max
        Thread(mAutoClickRunnable).start()
    }
}
