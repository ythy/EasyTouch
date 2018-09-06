package com.mx.easytouch.db

import android.provider.BaseColumns

object Providerdata {

    val AUTHORITY = "com.mx.easytouch.provider.csprovider"

    //数据库名称
    const val DATABASE_NAME = "et.db"

    /**
     * 数据库的版本
     * 版本2 增加Remark
     */
    const val DATABASE_VERSION = 1


    class FavApp : BaseColumns {

        companion object {
            const val TABLE_NAME = "fav_app_list"
            //列名
            const val ID = "_id"
            const val COLUMN_NAME = "name"
            const val COLUMN_PACKAGE_NAME = "packageName"
        }
    }

}