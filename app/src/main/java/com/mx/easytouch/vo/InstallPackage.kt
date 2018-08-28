package com.mx.easytouch.vo

/**
 * Created by maoxin on 2017/11/13.
 */

class InstallPackage constructor(var appName: String?, var packageName: String?) {

    var id: Int = 0

    constructor():this(null, null) {
    }

}
