package com.mx.easytouch.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class Providerdata {

    public static final String AUTHORITY="com.mx.easytouch.provider.csprovider";

    //数据库名称 
    public static final String DATABASE_NAME = "et.db";
    
    /**
     * 数据库的版本
     * 版本2 增加Remark
     */
    public static final int DATABASE_VERSION = 1;
    
    
    public static final class FavApp implements BaseColumns{

       //表名
       public static final String TABLE_NAME = "fav_app_list";

       //访问该ContentProvider的URI
       public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);
       
       //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
       public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.FavApp";
       public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.FavApp";

       //列名
       public static final String ID = "_id";
       public static final String COLUMN_NAME = "name";
       public static final String COLUMN_PACKAGE_NAME = "packageName";
        public static final String SORT_DESC = " DESC";
       public static final String SORT_ASC = " ASC";
    }

}
