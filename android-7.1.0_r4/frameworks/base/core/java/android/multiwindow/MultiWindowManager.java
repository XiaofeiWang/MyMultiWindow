/*
* Add by wangxiaofei
*/
package android.multiwindow;


import android.os.SystemProperties;

/**
* @hide
*/
public class MultiWindowManager {
    private static final boolean FEATURE_SUPPORTED = SystemProperties.get("persist.sys.multiwindow_enable").equals("1");
    public static boolean isSupported() {
        return true;
    }
}
