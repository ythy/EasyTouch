package com.mx.easytouch.utils;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by maoxin on 2017/10/18.
 */

public class ShellBase{

    public void sendKeyEventCode(int timeout, String input, int delay){
        sendKeyEventCodeAct(input, delay, timeout);
    }
    public void sendKeyEventCode(int timeout, String input){
        sendKeyEventCodeAct(input, 500, timeout);
    }
    public void sendKeyEventCode(String input, int delay){
        sendKeyEventCodeAct(input, delay, 0);
    }

    public void sendKeyEventCode(String input){
        sendKeyEventCodeAct(input, 500, 0);
    }

    public void sendKeyEventCodeAct(String input, int delay, int timeout){
        if(TimeCount.getInstance().getHackCount() <= 0)
            return;
        input = input.replaceAll("\\s", "%s");
        try {
            if(timeout > 0)
                Thread.sleep(timeout);
            execShellCmd("input text " + input);
            int dur = input.length() * 150;
            Thread.sleep( dur > 2000 ? 2000 :  ( dur < 1000 ? 1000 : dur));
            execShellCmd("input keyevent 66" );
            if(delay > 0)
                Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public void execShellCmd(String cmd){
        Log.d("FxService", cmd);
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        Process process = null;
        try {
            process =  Runtime.getRuntime().exec("su");
            // 获取输出流
            outputStream = process.getOutputStream();
            dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
            TimeCount.getInstance().setHackCount(0);
        }finally {
            try {
                process.getOutputStream().close();
                process.getErrorStream().close();
                process.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
