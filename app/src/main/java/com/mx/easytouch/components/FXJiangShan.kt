package com.mx.easytouch.components

import android.graphics.Point
import android.os.Handler
import android.os.Message
import com.mx.easytouch.utils.ShellBase
import com.mx.easytouch.utils.TimeCount
import java.util.ArrayList

class FXJiangShan(private val mJSHandle: FXJiangShan.JSHandle) {

    private val mShellBase: ShellBase
    private val mPosition: MutableList<Point>

    interface JSHandle {
        fun floatText(input: String)
    }

    init {
        mShellBase = ShellBase()
        mPosition = ArrayList<Point>()
        mPosition.add(Point(209, 1130))
        mPosition.add(Point(827, 956))
        mPosition.add(Point(868, 1466))
        startJS()
    }

    internal var mJSHandler: Handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1)
                mJSHandle.floatText(TimeCount.getInstance().stepCount.toString())
        }
    }

    private fun startJS() {
        TimeCount.getInstance().hackCount = 60 * 24
        Thread(mRunJS).start()
    }

    private val mRunJS = Runnable {
        while (TimeCount.getInstance().hackCount > 0) {
            try {
                mShellBase.execShellCmd("input tap " + (mPosition[0].x.toString() + " " + mPosition[0].y))
                Thread.sleep((1000 * 2).toLong())
                mShellBase.execShellCmd("input tap " + (mPosition[0].x.toString() + " " + mPosition[0].y))
                Thread.sleep((1000 * 2).toLong())
                mShellBase.execShellCmd("input tap " + (mPosition[0].x.toString() + " " + mPosition[0].y))
                mJSHandler.sendEmptyMessage(1)
                Thread.sleep((1000 * 56).toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}
