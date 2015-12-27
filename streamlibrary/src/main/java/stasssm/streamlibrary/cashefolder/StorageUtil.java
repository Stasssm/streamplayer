package stasssm.streamlibrary.cashefolder;

import android.os.Environment;

/**
 * Created by Стас on 25.08.2015.
 */
public class StorageUtil {

    private boolean useExternalIfPossible = true;
    private String externalStoragePath;
    private String internalStoragePath;
    private static StorageUtil storageUtil;

    protected StorageUtil() {}

    public static StorageUtil getStorage() {
        if (storageUtil == null) {
            storageUtil = new StorageUtil();
        }
        return storageUtil;
    }

    public String getCasheDir() {
        if (isExternalStorageWritable() && useExternalIfPossible) {
            return externalStoragePath;
        } else {
            return internalStoragePath;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public String getInternalStoragePath() {
        return internalStoragePath;
    }

    public StorageUtil setInternalStoragePath(String internalStoragePath) {
        this.internalStoragePath = internalStoragePath;
        return storageUtil;
    }

    public String getExternalStoragePath() {
        return externalStoragePath;
    }

    public StorageUtil setExternalStoragePath(String externalStoragePath) {
        this.externalStoragePath = externalStoragePath;
        return storageUtil;
    }

    public boolean isUseExternalIfPossible() {
        return useExternalIfPossible;
    }

    public StorageUtil setUseExternalIfPossible(boolean useExternalIfPossible) {
        this.useExternalIfPossible = useExternalIfPossible;
        return storageUtil;
    }

}


