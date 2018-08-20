package com.mx.easytouch.utils;

/**
 * Created by maoxin on 2017/10/18.
 */

/**
 * 线程计数器
 */
public class TimeCount{

    private int mHackThreadCount;
    private int mHackThreadMaxCount;

    private static TimeCount tc = null;

    public static TimeCount getInstance(){
        if(tc == null)
            tc = new TimeCount();
        return tc;
    }

    private TimeCount(){
        mHackThreadCount = 0;
    }

    public int getHackCount() {
        return mHackThreadCount;
    }

    public int getHackMaxCount() {
        return mHackThreadMaxCount;
    }

    public int getStepCount(){
        return mHackThreadMaxCount - mHackThreadCount;
    }

    public void setHackCount(int mHackThreadCount) {
        this.mHackThreadMaxCount = mHackThreadCount;
        this.mHackThreadCount = mHackThreadCount;
    }

    public int subtractCount(){
        this.mHackThreadCount--;
        return this.mHackThreadCount;
    }

}
