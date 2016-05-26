package stasssm.streamlibrary;

import android.content.Intent;

import stasssm.streamlibrary.cashefolder.StorageUtil;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.playercore.PlaybackNotifier;
import stasssm.streamlibrary.playercore.PlayerController;

/**
 * Created by Стас on 12.12.2015.
 */
public class PlayerInitializer {

    private static PlayerInitializer playerInitializer  ;
    private AppStreamApplication context ;
    private NotifIntentCreator notifIntentCreator ;


    protected PlayerInitializer(){}

    public static PlayerInitializer getPlayerInitializer() {
        if (playerInitializer == null) {
            playerInitializer = new PlayerInitializer() ;
        }
        return playerInitializer;
    }

    public PlayerInitializer init(AppStreamApplication context) {
        this.context = context ;
        return playerInitializer ;
    }

    public PlayerInitializer setNotifIntentCreator(NotifIntentCreator notifIntentCreator) {
            this.notifIntentCreator = notifIntentCreator ;
        return playerInitializer ;
    }

    public NotifIntentCreator getNotifIntentCreator() {
        return notifIntentCreator;
    }

    public StorageUtil getStorageUtils() {
        return StorageUtil.getStorage() ;
    }

    public static AppStreamApplication getContext() {
        return PlayerInitializer.getPlayerInitializer().context;
    }

    public static void clearContext() {
        PlayerInitializer.getPlayerInitializer().context = null ;
    }

    public interface NotifIntentCreator {
        Intent createIntent(StreamSong streamSong) ;
        Class getClassToOpen(StreamSong streamSong) ;
        void loadImageWhenSongChanged(StreamSong streamSong, PlaybackNotifier playbackNotifier) ;
    }


}
