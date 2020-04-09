package com.mx.easytouch.utils;


import java.util.ArrayList;
import java.util.List;
import com.mx.easytouch.db.Providerdata.FavApp;
import com.mx.easytouch.vo.InstallPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {   
    
    public DBHelper(Context context, String name,    
            CursorFactory factory, int version) {   
        super(context, name, factory, version);
        this.getWritableDatabase(); 
    }
    
    /**
     * should be invoke when you never use DBhelper
     * To release the database and etc.
     */
    public void Close() {
    	this.getWritableDatabase().close();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {   
        db.execSQL("CREATE TABLE IF NOT EXISTS "    
                + FavApp.TABLE_NAME + " ("
                + FavApp.ID + " INTEGER PRIMARY KEY,"
                + FavApp.COLUMN_NAME + " VARCHAR,"
                + FavApp.COLUMN_PACKAGE_NAME + " VARCHAR"
                + " )");
    }   
    
    @Override
    public void onUpgrade(SQLiteDatabase db,    
            int oldVersion, int newVersion) {   
//    	if(newVersion == 1) {
//
//		}
    }
    
    
    public ArrayList<InstallPackage> queryFavApp() {
    	ArrayList<InstallPackage> infos = new ArrayList<InstallPackage>();
    	String[] selectionArg = null;
    	String sql = "SELECT * FROM " + FavApp.TABLE_NAME;
    	String sqlWhere = "";
    	String selectionValue = "";

    	Cursor cusor = this.getWritableDatabase().rawQuery(sql + (selectionArg == null ? 
    			"" : " WHERE " + sqlWhere), selectionArg);
		if (cusor != null) {
			while (cusor.moveToNext()) {
				InstallPackage fav = new InstallPackage();
				fav.setId(cusor.getInt(cusor.getColumnIndex(FavApp.ID)));
				fav.setAppName(cusor.getString(cusor.getColumnIndex(FavApp.COLUMN_NAME)));
				fav.setPackageName(cusor.getString(cusor.getColumnIndex(FavApp.COLUMN_PACKAGE_NAME)));
				infos.add(fav);
			}
			cusor.close();
		}
		return infos;
    }

	public long addFavApp(InstallPackage info)
	{
		if(isDupAppInfo(info.getPackageName()))
			return -1;
		ContentValues values = new ContentValues();
		values.put(FavApp.COLUMN_NAME, info.getAppName());
		values.put(FavApp.COLUMN_PACKAGE_NAME, info.getPackageName());
		return this.getWritableDatabase().insert(FavApp.TABLE_NAME, null, values);
	}

	public long delFavApp(InstallPackage cardinfo)
	{
		String selection = FavApp.ID + "=?";
		String[] selectionArg = new String[] {String.valueOf(cardinfo.getId())};
		return this.getWritableDatabase().delete(FavApp.TABLE_NAME, selection, selectionArg);
	}

	private boolean isDupAppInfo(String currentPackagName){
		List<InstallPackage> infos = queryFavApp();
		for( InstallPackage installPackage: infos ){
			if(installPackage.getPackageName().equals(currentPackagName))
				return true;
		}
		return false;
	}
} 