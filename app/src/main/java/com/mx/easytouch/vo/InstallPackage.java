package com.mx.easytouch.vo;

/**
 * Created by maoxin on 2017/11/13.
 */

public  class InstallPackage{

    private String appName;
    private String packageName;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InstallPackage(){
    }

    public InstallPackage(String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
