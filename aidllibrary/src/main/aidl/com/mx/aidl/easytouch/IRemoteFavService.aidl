// IRemoteFavService.aidl
package com.mx.aidl.easytouch;

// Declare any non-default types here with import statements
import com.mx.aidl.easytouch.FavorApp;

interface IRemoteFavService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void addFavor(String name);

    List<FavorApp> getFavor();

}
