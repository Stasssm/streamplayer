package stasssm.streamlibrary.main;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import stasssm.streamlibrary.PlayerInitializer;
import stasssm.streamlibrary.cashefolder.StorageUtil;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.playercore.PlaybackNotifier;
import stasssm.streamlibrary.playercore.PlayerCore;
import stasssm.streamlibrary.utils.L;


public class PlayerService extends Service implements
        FFmpegMediaPlayer.OnPreparedListener ,FFmpegMediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_CLOSE = "action_close";

    public final static int STATE_NONE = 0;
    public final static int STATE_STOPPED = 1;
    public final static int STATE_PAUSED = 2;
    public final static int STATE_PLAYING = 3;

    public final static int PREPARING  = 4 ;
    public final static int PREPARED = 5 ;

    private int prepareState = 0 ;

    private static PlayerService sharedService;
    public static MediaPlayerWrapper mp;
    public static MediaPlayerWrapper nextPlayer ;
    private PlayerListener mPlayerListener;
    private NoisyAudioStreamReceiver mNoisyAudioStreamReceiver;

    private boolean isPreparing;
    private boolean isNextPrepared = false ;
    private String currentSongUrl;
    private int selectedPosition = -1;
    private boolean isShuffle = false;
    private boolean isRepeat = true;
    private int mCurrentState = STATE_NONE;


    private ArrayList<StreamSong> catalogSongs = new ArrayList<>();

    private PlaybackNotifier mNotifier;

    public static final int DELAY_BEFORE_POST_SONG_PLAYED = 10000;


    public static PlayerService getSharedService() {
        if (sharedService != null) {
            return sharedService;
        } else {
            return null;
        }
    }



    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        L.d("PlayerServiceLife", "OnStartCommand") ;
        if (sharedService == null) {
            sharedService = this;
            mNotifier = new PlaybackNotifier(this);
            PlayerCore.getInstance().initCore();
        }

        if (intent != null && intent.getAction() != null) {
            performAction(intent.getAction());
        }
        return START_STICKY;
    }

    private void performAction(String action) {
      //  Toast.makeText(getApplicationContext(),action,Toast.LENGTH_LONG) ;
        if (!isPreparing()) {
            if (action.equalsIgnoreCase(PlayerService.ACTION_PLAY_PAUSE)) {
                togglePlayPause();
            } else {
                PlayerCore.getInstance().handleAction(action);
            }
        }
    }

    public void onPlayerStart() {
        if (mp!= null) {
            mp.start();
            mp.setOnCompletionListener(this);
            mCurrentState = STATE_PLAYING;
            PlayerCore.getInstance().updatePlaybackState(mCurrentState);
            updateNotification();
            if (mPlayerListener != null) {
                mPlayerListener.onStateChanged(true);
            }
            PlayerInitializer.getContext().registerPhoneStateListener();
        }
    }

    public void onPlayerPause() {
        if (mp != null) {
            mp.pause();
        }
        mCurrentState = STATE_PAUSED;
        PlayerCore.getInstance().updatePlaybackState(mCurrentState);
        updateNotification();
        if (mPlayerListener != null) {
            mPlayerListener.onStateChanged(false);
        }
        PlayerInitializer.getContext().unregisterPhoneStateListener();
    }

    public void onNotificationClose() {
        if (mp != null) {
            mp.pause();
            mCurrentState = STATE_PAUSED;
            PlayerCore.getInstance().updatePlaybackState(mCurrentState);
            if (mPlayerListener != null) {
                mPlayerListener.onStateChanged(false);
            }
            mNotifier.hideNotification();
        }
    }

    public void removeItem(String id) {
        StreamSong musicFeed  = null ;
        for (StreamSong song : this.catalogSongs) {
            if (song.getUniqueIdentifier().equals(id)) {
                musicFeed = song ;
                break;
            }
        }
        if (musicFeed != null) {
            int pos = this.catalogSongs.indexOf(musicFeed);
            catalogSongs.remove(musicFeed);
            if (isNextSong(pos)) {
                isNextPrepared = false;
                if (nextPlayer != null) {
                    nextPlayer.setPrepared(false);
                }
                generateNextSongMp();
            }
        }
    }

    public void removeItem(int from, int to) {
        StreamSong item = (StreamSong)catalogSongs.get(from);
        catalogSongs.remove(item);
        catalogSongs.add(to, item);
        if (isNextSong(to) || isNextSong(from)) {
            isNextPrepared = false ;
            if (nextPlayer != null) {
                nextPlayer.setPrepared(false);
            }
            generateNextSongMp();
        }
    }

    public void remove(StreamSong song) {
        int position = -1  ;
        boolean isFound = false ;
        for (StreamSong musicFeed : catalogSongs) {
            position++ ;
            if (musicFeed != null && song != null
                    && musicFeed.getUniqueIdentifier() != null) {
                if (musicFeed.getUniqueIdentifier().equals(song.getUniqueIdentifier())) {
                    isFound = true;
                    break;
                }
            }
        }
        if (isFound) {
            catalogSongs.remove(position);
            isNextPrepared = false;
            selectSong(position);
        } else {
            selectSong(selectedPosition);
        }


    }

    public ArrayList<StreamSong> getCatalogSongs() {
        return catalogSongs;
    }

    public void addToQueue(ArrayList<StreamSong> songItems) {
       if (catalogSongs != null && songItems!=null) {
           catalogSongs.addAll(songItems);
       }
    }

    public void addToQueue(StreamSong songItem) {
        catalogSongs.add(songItem);
    }


    public MediaPlayerWrapper getMp() {
        return mp;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public void requestPlay() {
        updateNotification();
        performAction(ACTION_PLAY);
    }

    public void requestPause() {
        performAction(ACTION_PAUSE);
    }

    public void requestStop() {
        mp.stop();
        mCurrentState = STATE_STOPPED;
        PlayerCore.getInstance().updatePlaybackState(mCurrentState);
        selectedPosition = -1;
        if (mPlayerListener != null) {
            mPlayerListener.onSongChanged(null, selectedPosition);
        }
//        mNotifier.hideNotification();
    }

    public void setPlayerListener(PlayerListener playerListener) {
        mPlayerListener = playerListener;
        if (mp != null) {
            mp.setOnBufferingUpdateListener(playerListener);
        }
    }

    @Override
    public void onPrepared(FFmpegMediaPlayer mp) {
        L.e("STATS", "Player's on prepared");
        isPreparing = false;
        if (getMp() != null) {
            getMp().setPrepared(true);
            if (getMp().isStopFlag()) {
                getMp().setStopFlag(false);
                return;
            }
        }
        //prepareState = PREPARED ;
        if (mPlayerListener != null) {
            mp.setOnBufferingUpdateListener(mPlayerListener);
            mPlayerListener.onPlayerPrepared();
        }
        if (mPlayerListener != null) {
            prepareState = PREPARED ;
            mPlayerListener.onSongPrepared();
        }
        requestPlay();
        generateNextSongMp();
    }

    /**
     * detect isNext Song will be played or not
     *
     * @param position
     */
    public boolean isNextSong(int position) {
       return selectedPosition != -1 && selectedPosition + 1 == position;
    }


    /**
     * triggers MediaPlayer to play from url or file
     *
     * @param position
     */
    public void selectSong(final int position) {
       // if (PermissionHelper.showMessageIfNeeded()) {
       //     return;
       // }
        if (mp != null) {
            mp.setStopFlag(true);
            if (isPlaying()) {
                mp.stop();
            }
            mp.setPrepared(false);
            mp.setOnBufferingUpdateListener(null);
        }
        if (position < getCatalogSongs().size()){
            boolean isNext = isNextSong(position) ;
            selectedPosition = position;
            final StreamSong item;
            setSelectedPosition(position);
            item = getCatalogSongs().get(position);
            if (mPlayerListener != null) {
                mPlayerListener.onSongChanged(item, position);
            }
            if (item != null) {
                if (isNext && isNextPrepared() && nextPlayer != null && nextPlayer.getPosition() == position) {
                    isNextPrepared = false;
                    playGapless();
                } else {
                    isNextPrepared = false;
                    File fileCashe = new File(StorageUtil.getStorage().getCasheDir(),"filen" +  item.getUniqueIdentifier() + ".mp3");
                    if (fileCashe.exists()) {
                        playSong("filen"+item.getUniqueIdentifier()+".mp3", item);
                    } else {
                        playSong(item.getStreamUrl(),item);
                    }
                }
                PlayerCore.onSongChanged(item);
            }
        } else {
            if (mPlayerListener != null) {
                mPlayerListener.onPlayerPrepared();
            }
        }
    }



    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public StreamSong getSelectedItem() {
        if (selectedPosition >= 0 && selectedPosition < getCatalogSongs().size()) {
            return getCatalogSongs().get(selectedPosition);
        } else {
            return null;
        }
    }


    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }


    private boolean isNextPrepared() {
        return isNextPrepared ;
    }

    private void generateNextSongMp() {
        int nextPos =  selectedPosition + 1 ;
        L.d("NextPos","next = " + nextPos) ;
        if (nextPos < getCatalogSongs().size()) {
            isNextPrepared = false ;
            final StreamSong songItem = getCatalogSongs().get(nextPos) ;
            final int position = nextPos ;
            initNextPlayer(songItem.getStreamUrl(), position, songItem);
        }
    }

    private void initNextPlayer(String url,int nextPos,StreamSong songToPlay) {
        if (nextPlayer == null) {
            nextPlayer = new MediaPlayerWrapper();
        }
        nextPlayer.reset();
        nextPlayer.setPosition(nextPos);
        nextPlayer.setOnBufferingUpdateListener(null);
        if (mPlayerListener != null) {
            mPlayerListener.mediaPlayerChanged(mp);
        }
        nextPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        nextPlayer.setCatalogSongItem(songToPlay);
        nextPlayer.setDataSource(url);
        nextPlayer.setPrepared(true);
        isNextPrepared = true ;
        nextPlayer.prepareAsyncNext();
    }

    public void seekTo(int msk) {
        if (mp != null) {
            mp.seekTo(msk) ;
        }
    }


    public void playNext() {
        if (isRepeat && selectedPosition + 1 == getCatalogSongs().size()) {
            //means repeat is on and current song is last in list
            L.d("Position selected", selectedPosition+"") ;
            selectSong(0);
        } else if (selectedPosition + 1 < getCatalogSongs().size()) {
            L.d("Position selected", selectedPosition+"") ;
            selectSong(selectedPosition + 1);
        } else {
            L.d( "Position selected", "lastPlayed") ;
            // last song in list played
            if (getMp() != null) {
                getMp().setLastSong(true);
            }
            requestPause();
        }
        updateNotification();
    }

    public void playPrevious() {
        if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            int i = rand.nextInt(getCatalogSongs().size() + 1); ///rand.nextInt(Integer.MAX_VALUE)%getCatalogSongs().size();
            selectSong(i);
        } else if (selectedPosition - 1 >= 0 && selectedPosition - 1 < getCatalogSongs().size()) {
            selectSong(selectedPosition - 1);
        }
        updateNotification();
    }

    public boolean togglePlayPause() {
        if (isPlaying()) {
            requestPause();
            return false ;
        } else {
            requestPlay();
            return true ;
        }
    }

    public void updateNotification() {
        StreamSong song = getSelectedItem();
        if (song == null) {
            mNotifier.hideNotification();
        } else {
            mNotifier.showNotification(song, !isPlaying());
        }
    }

    public void closeNotification() {
        if (mNotifier != null) {
            mNotifier.hideNotification();
        }
    }

    public void playSong(String url,StreamSong songToPlay) {
        if (mp != null) {
            mp.stop();
        } else {
            mp = new MediaPlayerWrapper();
        }
        mp.reset();
        if (mPlayerListener != null) {
            mPlayerListener.mediaPlayerChanged(mp);
        }
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setCatalogSongItem(songToPlay);
        L.d("NextPlayerInitOnline", url);
        mp.setDataSource(url);
        mp.setOnPreparedListener(this);
        setPlayerListener(mPlayerListener);
        isPreparing = true;
        if (mPlayerListener != null) {
            mPlayerListener.onSongPreparing();
        }
        mp.prepareAsync();
    }

    private void playGapless() {
        // Play song
        if (mp != null) {
            mp.stop();
            mp.reset();
        }
        MediaPlayerWrapper mediaPlayer = mp;
        mp = nextPlayer;
        nextPlayer = mediaPlayer;
        mediaPlayer = null;
        if (mPlayerListener != null) {
            mPlayerListener.mediaPlayerChanged(mp);
        }
        setPlayerListener(mPlayerListener);
        isPreparing = true;
        mp.setOnPreparedListener(this);
        mp.startNextTrack();
    }




    public void setCatalogSongs(List<StreamSong> catalogSongs) {
        isNextPrepared = false ;
        this.catalogSongs = new ArrayList<>(catalogSongs);
        selectedPosition = -1;
    }

    public void updateCatalogSongs(List<StreamSong> catalogSongs, StreamSong song) {
        this.catalogSongs = new ArrayList<>(catalogSongs);
        int pos = 0 ;
        if (song != null) {
            for (StreamSong musicFeed : catalogSongs) {
                if (musicFeed != null && musicFeed.getUniqueIdentifier() != null) {
                    if (musicFeed.getUniqueIdentifier().equals(song.getUniqueIdentifier())) {
                        break;
                    }
                }
                pos++;
            }
        }
        setSelectedPosition(pos);
        isNextPrepared = false ;
    }

    public void addCatalogSongs(List<StreamSong> catalogSongs) {
        this.catalogSongs.addAll(catalogSongs) ;
    }

    public boolean isCatalogEqualToCurrent(List<StreamSong> newCatalog) {
        if(newCatalog == null || this.catalogSongs == null) {
            return false;
        }
        if(newCatalog.size() != this.catalogSongs.size()) {
            return false;
        }
        for(int i = 0; i < newCatalog.size(); i++) {
            if(!newCatalog.get(i).equals(this.catalogSongs.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isSongListUpdated(List<StreamSong> newCatalog) {
        if(newCatalog == null || this.catalogSongs == null || this.catalogSongs.isEmpty()) {
            return false;
        }
        if(newCatalog.size() < this.catalogSongs.size()) {
            return false;
        }
        for(int i = 0; i < this.catalogSongs.size(); i++) {
            if(!newCatalog.get(i).getUniqueIdentifier().equals(this.catalogSongs.get(i).getUniqueIdentifier())) {
                return false;
            }
        }
        return true;
    }


    public String getCurrentSongUrl() {
        return currentSongUrl;
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("PlayerServiceLife", "onCreate") ;
        if (sharedService == null) {
            sharedService = this;
            mNotifier = new PlaybackNotifier(this);
            PlayerCore.getInstance().initCore();
        }

        mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
        registerReceiver(mNoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }


    @Override
    public void onDestroy() {
        L.d("PlayerServiceLife", "OnDestroy") ;
        //clearTimer();
        PlayerCore.BasePlayerCore playerCore = PlayerCore.getInstance();
        if (playerCore != null) {
            playerCore.release();
        }
        try {
            unregisterReceiver(mNoisyAudioStreamReceiver);
        } catch (IllegalArgumentException e) {
            L.d("oops","710");
            e.printStackTrace();
        }
        closeNotification();
        L.d("MediaPlayer", "GC DESTROYED");
        if (mp != null) {
            mp.release();
            mp = null ;
        }
        if (nextPlayer != null) {
            nextPlayer.release();
            nextPlayer = null ;
        }
        super.onDestroy();
    }


    @TargetApi(14)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        L.d("TASKREMOVED", "remove");
        stopForeground(true);
        if (getApplicationContext() != null) {
            ((NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(PlaybackNotifier.NOTIFY_ID_PLAYING);
        }
        if (isPlaying()) {
            requestStop();
        }

       // Intent intent = new Intent();
       // intent.setAction("il.co.pelephone.musix.UI.KILL");
       // intent.putExtra("Status", true);
       // sendBroadcast(intent);

        stopSelf();
        catalogSongs = new ArrayList<>() ;
        setSelectedPosition(-1);
    }

    public void removeItems(ArrayList<StreamSong> toDelete) {
        catalogSongs.removeAll(toDelete);
    }

    public AudioTrack getCurrentAudioTrack() {
        MediaPlayerWrapper mediaPlayer = getMp();
        if (mediaPlayer != null ) {
            TTAudioTrack ttAudioTrack = mediaPlayer.getTrack();
            return ttAudioTrack.getTrack() ;
        }
        return  null ;
    }



    @Override
    public void onCompletion(FFmpegMediaPlayer mp) {
       //if (mp != null) {
           if (mp instanceof MediaPlayerWrapper) {
               L.d("MediaPlayer", "onCompleteion happened " + ((MediaPlayerWrapper) mp).getId());
           } else {
               L.d("MediaPlayer", "onCompleteion happened");
           }
       //}
        playNext();
    }


    public static void release() {
        final PlayerService inst = getSharedService();
        if (inst != null) {
            inst.catalogSongs.clear();
            inst.selectedPosition = -1;
            inst.currentSongUrl = null;
            inst.mCurrentState = STATE_STOPPED;
        }
        //sharedService = null;
    }

    public int getPrepareState() {
        return prepareState;
    }

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface PlayerListener extends FFmpegMediaPlayer.OnBufferingUpdateListener {
        void onSongChanged(StreamSong item, int position);

        void onPlayerPrepared();

        void onStateChanged(boolean isPlaying);

        void onSongPreparing();

        void onSongPrepared();

        void mediaPlayerChanged(FFmpegMediaPlayer mediaPlayer) ;
    }


    /**
     *  receives headphone jack unplugged event
     */
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                requestPause();
            }
        }
    }
}
