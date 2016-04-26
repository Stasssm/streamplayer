package stasssm.streamlibrary.cashefolder;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Created by Стас on 25.08.2015.
 */
public class FolderManipulator {
    static long MAXSIZE = 357913941L ;

    // return false if directory can not be cleared
    // or not enough space on the device
    public static boolean maybeClearCashe() {
        try {
            long availableBytes = availableExternalSpaceBytes();
            long dirSize = dirSize(new File(StorageUtil.getStorage().getCasheDir()));
            long sumSize = dirSize;
            if (MAXSIZE > sumSize && sumSize < availableBytes) {
                return true;
            } else {
                File encDir = new File(StorageUtil.getStorage().getCasheDir());
                if (encDir.exists()) {
                    File[] files = encDir.listFiles();
                    if (files != null && files.length > 2) {
                        File fileDelete = files[0];
                        for (File fileBidder : files) {
                            if (fileDelete.lastModified() > fileBidder.lastModified()) {
                                fileDelete = fileBidder;
                            }
                        }
                        // TODO is now playing
                        fileDelete.delete();
                        return maybeClearCashe();
                    } else {
                        return false;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false ;
        }
    }


    /**
     * Return the size of a directory in bytes
     */
    private static long dirSize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    // Recursive call if it's a directory
                    if (fileList[i].isDirectory()) {
                        result += dirSize(fileList[i]);
                    } else {
                        // Sum the file size in bytes
                        result += fileList[i].length();
                    }
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    private static long availableExternalSpaceBytes() {
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        } else {
            availableSpace = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        return availableSpace;
    }


}
