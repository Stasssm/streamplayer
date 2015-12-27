package stasssm.streamlibrary.utils;

import android.util.Log;

/**
 * Created by Стас on 12.12.2015.
 */
public class L {

    public static final boolean DEBUG = true;

    public static void d(String TAG, String msg) {
        if(DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if(DEBUG) {
            Log.e(TAG, msg);
        }
    }


}
