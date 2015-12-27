package stasssm.streamlibrary.playercore;

import android.app.Notification;
import android.graphics.Bitmap;

import stasssm.streamlibrary.model.StreamSong;


public class PlayerCore {

    private static BasePlayerCore sCore;
    private static StreamSong sCurrentSongItem;
    private static Bitmap sBitmap;
    public static PlayerCoreSongChangedWorker changedWorker = null;


    static {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sCore = new PlayerCoreLollipop();
        } else {
            sCore = new PlayerCorePreLollipop();
        }*/
        sCore = new PlayerCorePreLollipop();
    }

    public interface BasePlayerCore {
        void initCore();

        void handleAction(String action);

        void onSongChanged(StreamSong item, Bitmap bitmap);

        void updatePlaybackState(int newState);

        Notification createNotification(Bitmap cover, StreamSong song, boolean inPause, boolean onGoing);

        void release();
    }

    public static BasePlayerCore getInstance() {
        return sCore;
    }

    public static void onSongChanged(StreamSong item) {
        sCurrentSongItem = null;
        if (changedWorker != null) {
            changedWorker.onSongChanged(item);
        }
        sCurrentSongItem = item;
        getInstance().onSongChanged(item, sBitmap);
        sBitmap = null;
    }


    public interface PlayerCoreSongChangedWorker {
        void onSongChanged(StreamSong item);
    }
}
