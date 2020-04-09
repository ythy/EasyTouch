package com.mx.easytouch.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.mx.easytouch.R
import com.mx.easytouch.receiver.ActionReceiver
import com.mx.easytouch.service.FuncService
import com.mx.easytouch.service.FxService

class ServiceUtil {

    companion object{

        fun startFxService(context:Context, x:Int, y:Int){
            /*val fxIntent = Intent(context, FxService::class.java)
            val extras = Bundle()
            extras.putInt("position_x", x)
            extras.putInt("position_y", y)
            fxIntent.putExtras(extras)
            context.stopService(Intent(context, FuncService::class.java)) //防止两个service同时存在，先关闭再开启
            context.startService(fxIntent)*/
            startFxServiceByBroadcast(context, x, y)
        }

        @SuppressLint("WrongConstant")
        fun startFxServiceByBroadcast(context:Context, x:Int, y:Int){
            val intent = Intent(ActionReceiver.ACTION_ALARM)
            intent.addFlags(0x01000000) //强制API26 接收隐式Broadcast
            intent.action = ActionReceiver.ACTION_ALARM
            val extras = Bundle()
            extras.putInt("x", x)
            extras.putInt("y", y)
            intent.putExtras(extras)
            context.sendBroadcast(intent)
        }

        fun startFxService(context:Context){
            if(!CommonUtils.isMyServiceRunning(context, FxService::class.java))
                startFxService(context, 0, 0)
        }

    }

}