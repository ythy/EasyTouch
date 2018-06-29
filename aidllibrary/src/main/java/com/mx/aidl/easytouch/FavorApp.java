package com.mx.aidl.easytouch;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by maoxin on 2018/6/28.
 */

public class FavorApp implements Parcelable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FavorApp(String name) {
        this.name = name;
    }


    protected FavorApp(Parcel in) {
        name = in.readString();
    }

    public static final Creator<FavorApp> CREATOR = new Creator<FavorApp>() {
        @Override
        public FavorApp createFromParcel(Parcel in) {
            return new FavorApp(in);
        }

        @Override
        public FavorApp[] newArray(int size) {
            return new FavorApp[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}
