package com.mx.easytouch.components

import android.os.Handler
import android.os.Message

import com.mx.easytouch.utils.ShellBase
import com.mx.easytouch.utils.TimeCount

/**
 * Created by maoxin on 2018/8/13.
 */

class FXHaiyang(type: String, internal var mHaiyangHandle: FXHaiyang.HaiyangHandle) {
    internal var mShellBase: ShellBase

    interface HaiyangHandle {
        fun floatText(text: String)
    }

    init {
        mShellBase = ShellBase()
        startHYAuto(type)
    }

    private fun startHYAuto(type: String) {
        if ("moyao" == type)
            startMoyao()
        else if ("duilian" == type)
            startDuilian()
        else if ("yigong" == type)
            startYigong()
    }

    private fun startMoyao() {
        TimeCount.getInstance().hackCount = Integer.MAX_VALUE
        val duration = 3
        Thread(Runnable {
            var turn = duration
            mShellBase.sendKeyEventCode("chen gc")
            mShellBase.sendKeyEventCode("e")
            mShellBase.sendKeyEventCode("e")
            mShellBase.sendKeyEventCode("n")
            while (TimeCount.getInstance().subtractCount() > 0) {
                mShellBase.sendKeyEventCode("ask ping about job")
                mShellBase.sendKeyEventCode("moyao", 8000)
                if (--turn == 0) {
                    turn = duration
                    mShellBase.sendKeyEventCode("xiuxi beg", 3000)
                }
            }
        }
        ).start()
    }

    private fun startDuilian() {
        TimeCount.getInstance().hackCount = Integer.MAX_VALUE
        Thread(Runnable {
            mShellBase.sendKeyEventCode("remove all")
            mShellBase.sendKeyEventCode("wear all")
            while (TimeCount.getInstance().subtractCount() > 0) {
                setFloatText()
                mShellBase.sendKeyEventCode("chen dl")
                mShellBase.sendKeyEventCode("n")
                mShellBase.sendKeyEventCode("n")
                mShellBase.sendKeyEventCode("e")
                mShellBase.sendKeyEventCode("n")
                excuteDuilian(DuilianJia())
                excuteDuilian(DuilianZhu())
                excuteDuilian(DuilianFu())
                excuteDuilian(DuilianWei())
                mShellBase.sendKeyEventCode("xiuxi beg", 3000)
            }
        }
        ).start()
    }

    private fun setFloatText() {
        val msg = Message.obtain()
        msg.arg1 = Integer.MAX_VALUE - TimeCount.getInstance().hackCount
        msg.what = 1
        hyHandler.sendMessage(msg)
    }

    internal var hyHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                mHaiyangHandle.floatText(msg.arg1.toString())
            }
        }
    }


    private fun excuteDuilian(duilian: IDuilian) {
        var dlTurn = 7
        mShellBase.sendKeyEventCode("job")
        duilian.go()
        while (dlTurn-- > 0) {
            duilian.duilian()
        }
        duilian.back()
    }

    private fun startYigong() {
        TimeCount.getInstance().hackCount = Integer.MAX_VALUE
        val duration = 25
        Thread(Runnable {
            mShellBase.sendKeyEventCode("remove all")
            mShellBase.sendKeyEventCode("wear all")
            mShellBase.sendKeyEventCode("chen dl")
            mShellBase.sendKeyEventCode("n")
            mShellBase.sendKeyEventCode("n")
            mShellBase.sendKeyEventCode("e")
            mShellBase.sendKeyEventCode("n")
            mShellBase.sendKeyEventCode("enter")
            mShellBase.sendKeyEventCode("s")
            mShellBase.sendKeyEventCode("enter")
            mShellBase.sendKeyEventCode("ne")
            mShellBase.sendKeyEventCode("get huachu")
            mShellBase.sendKeyEventCode("wield huachu")
            var xiuxiDuration = duration
            while (TimeCount.getInstance().subtractCount() > 0) {
                setFloatText()
                mShellBase.sendKeyEventCode(1000, "ask huajiang about job")
                var dur = 6
                while (dur-- > 0) {
                    if (xiuxiDuration-- < 0) {
                        xiuxiDuration = duration
                        mShellBase.sendKeyEventCode("xiuxi beg", 3000)
                    }
                    mShellBase.sendKeyEventCode("work", 1000)
                }
            }
        }
        ).start()
    }

    /// 以下 内部class

    internal interface IDuilian {
        fun go()
        fun duilian()
        fun back()
    }

    internal inner class DuilianZhu : ShellBase(), IDuilian {

        override fun go() {}

        override fun duilian() {
            sendKeyEventCode("duilian zhu wanli", 1500)
        }

        override fun back() {}
    }

    internal inner class DuilianWei : ShellBase(), IDuilian {

        override fun go() {}

        override fun duilian() {
            sendKeyEventCode("duilian wei shi", 1500)
        }

        override fun back() {}
    }

    internal inner class DuilianJia : ShellBase(), IDuilian {

        override fun go() {
            sendKeyEventCode("enter")
        }

        override fun duilian() {
            sendKeyEventCode("duilian jia ding", 1500)
        }

        override fun back() {
            sendKeyEventCode("out")
        }
    }

    internal inner class DuilianFu : ShellBase(), IDuilian {

        override fun go() {
            sendKeyEventCode("s")
        }

        override fun duilian() {
            sendKeyEventCode("duilian fu sigui", 1500)
        }

        override fun back() {
            sendKeyEventCode("n")
        }
    }


}
