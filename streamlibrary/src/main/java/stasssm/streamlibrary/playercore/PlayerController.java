package stasssm.streamlibrary.playercore;

import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import stasssm.streamlibrary.main.FFmpegMediaPlayer;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.main.PlayerService.PlayerListener;
import stasssm.streamlibrary.model.StreamSong;

/**
 * Created by Стас on 26.05.2016.
 */
public class PlayerController implements PlayerListener {

    private static PlayerController playerController ;

    private ArrayList<WeakReference<PlayerListener>> listeners = new ArrayList<>();

    public static PlayerController getInstance() {
        if (playerController == null) {
            playerController = new PlayerController() ;
            PlayerService playerService = PlayerService.getSharedService();
            if (playerService != null) {
                playerService.setPlayerListener(playerController);
            }
        }
        return playerController ;
    }

    public synchronized void attachListener(PlayerListener listener) {
        listeners.add(new WeakReference<PlayerListener>(listener));
    }

    public synchronized boolean detachListener(PlayerListener listener) {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get();
            if (playerListener != null && listener.equals(playerListener)) {
                listeners.remove(weakReference);
                return true;
            }
        }
        return false ;
    }

    public synchronized void clearListeners() {
        listeners.clear();
    }

    @Override
    public void onSongChanged(StreamSong item, int position) {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onSongChanged(item,position);
            }
        }
    }

    @Override
    public void onPlayerPrepared() {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onPlayerPrepared();
            }
        }
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onStateChanged(isPlaying);
            }
        }
    }

    @Override
    public void onSongPreparing() {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onSongPreparing();
            }
        }
    }

    @Override
    public void onSongPrepared() {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onSongPrepared();
            }
        }
    }

    @Override
    public void mediaPlayerChanged(FFmpegMediaPlayer mediaPlayer) {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.mediaPlayerChanged(mediaPlayer);
            }
        }
    }

    @Override
    public void onBufferingUpdate(FFmpegMediaPlayer mp, int percent) {
        for (WeakReference<PlayerListener> weakReference : listeners) {
            PlayerListener playerListener = weakReference.get() ;
            if (playerListener != null) {
                playerListener.onBufferingUpdate(mp, percent);
            }
        }
    }

}
