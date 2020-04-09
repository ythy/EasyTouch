package com.mx.easytouch.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by maoxin on 2017/11/13.
 */

class InstallPackage constructor(var appName: String?, var packageName: String?):Parcelable {

    var id: Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
        id = parcel.readInt()
    }

    constructor() : this(
           null,
            null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeString(packageName)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InstallPackage> {
        override fun createFromParcel(parcel: Parcel): InstallPackage {
            return InstallPackage(parcel)
        }

        override fun newArray(size: Int): Array<InstallPackage?> {
            return arrayOfNulls(size)
        }
    }

}
