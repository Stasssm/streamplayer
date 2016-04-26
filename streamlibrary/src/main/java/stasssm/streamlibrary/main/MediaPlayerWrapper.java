package stasssm.streamlibrary.main;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import stasssm.streamlibrary.model.StreamSong;


/**
 * Created by Стас on 25.06.2015.
 */
public class MediaPlayerWrapper extends FFmpegMediaPlayer {

    public volatile static int playerId = 0 ;

    private String id = "MediaPlayerWrapper" ;

    private boolean isPlaying = false ;
    private boolean isPrepared = false ;
    private boolean isOffline = false ;
    private boolean isLastSong = false ;
    private boolean isCompleted = false ;
    private boolean isNextSong = false ;
    private boolean isStartedToPlay  = false ;

    private boolean stopFlag = false ;
    private int position = -100 ;

    private TTAudioFileStream audioFileStream ;
    private TTAudioTrack track ;
    private StreamSong catalogSongItem ;
    TTAudioFileStream.AudioFileStreamState gsdpAstate = new TTAudioFileStream.AudioFileStreamState();
    Handler bufHandler = new Handler();
    String uri  ;

    public String getId() {
        return id;
    }

    public MediaPlayerWrapper() {
        playerId++ ;
        id += playerId ;
        Log.d(id, "Created" ) ;
        PlayerService playerService = PlayerService.getSharedService() ;
        catalogSongItem = playerService.getSelectedItem();
    }

    @Override
    public void start() throws IllegalStateException {
        Log.d(id, "Started" ) ;
        isStartedToPlay = true ;
        if (isCompleted) {
            PlayerService  service = PlayerService.getSharedService();
            if( service != null){
                service.selectSong(service.getSelectedPosition());
            }
        }
        if (!stopFlag) {
            isPlaying = true;
            track.requestPlay();

        }
    }

    @Override
    public void stop() throws IllegalStateException {
        Log.d(id, "Stopped" ) ;
        if (isPlaying && isPrepared) {
            isPlaying = false ;
            track.requestStop();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        Log.d(id, "Paused" ) ;
        if (isPlaying && isPrepared) {
            track.requestPause();
            isPlaying = false ;
        }
    }

    @Override
    public void release() {
        Log.d(id, "Released") ;
        reset();
    }

    @Override
    public void reset() {
        isPlaying = false ;
        isPrepared = false ;
        isOffline = false ;
        stopFlag = false ;
        isLastSong =false ;
        isCompleted = false ;
        position = -100 ;
        //CHECK
        if (track !=null) {
            track.requestStop();
        }
        if (track != null && !isNextSong && isStartedToPlay) {
            track.closeAudioTrack();
        }
        if (audioFileStream != null && audioFileStream.getState() != TTAudioFileStream.DownloadState.Downloaded ) {
            audioFileStream.requestStop();
            final File file =  audioFileStream.getFile() ;
            if (file != null) {
                boolean b = file.delete();
                Log.d("DeleteFile", b+"") ;
            }
        }
        isNextSong = false;
        isStartedToPlay  = false ;
        audioFileStream = null ;
        track = null ;
        setOnPreparedListener(null);
        setOnBufferingUpdateListener(null);
    }

    private OnPreparedListener onPreparedListener ;
    private OnBufferingUpdateListener onBufferingUpdateListener ;
    private OnCompletionListener onCompletionListener ;

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        Log.d(id, "SetOnPrepareListener") ;
        onPreparedListener = listener ;
        if (track != null) {
            track.setListener(audioTrackPreparedListener);
        }
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        Log.d(id, "SetOnBufferingUpdateListener") ;
        onBufferingUpdateListener = listener ;
        if (listener != null) {
            bufHandler.removeCallbacks(mBufferTimeTask);
            bufHandler.postDelayed(mBufferTimeTask,300);
        } else {
            bufHandler.removeCallbacks(mBufferTimeTask);
        }
    }

    public void setOnCompletionListener(OnCompletionListener listener)
    {
       onCompletionListener = listener;
       track.setListener(audioTrackStateChangeListener);

    }
    private final Object stateSync = new Object();

    private TTAudioTrack.AudioTrackStateChangeListener audioTrackStateChangeListener = new TTAudioTrack.AudioTrackStateChangeListener() {

        @Override
        public void onAudioTrackStateChange(TTAudioTrack audioTrack) {
            synchronized (stateSync) {
                //if (audioTrack.getAudioFileStream().getState() == TTAudioFileStream.DownloadState.Downloaded)


                if (audioTrack.getState() == TTAudioTrack.AudioTrackState.Error &&
                        (audioTrack.getAudioFileStream().getState() == TTAudioFileStream.DownloadState.TemporaryError ||
                                audioTrack.getAudioFileStream().getState() == TTAudioFileStream.DownloadState.Error)) {

                } else  if (audioTrack.getState() == TTAudioTrack.AudioTrackState.Ended ||
                        audioTrack.getState() == TTAudioTrack.AudioTrackState.Error) {

                    if (onCompletionListener != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                isCompleted = true ;
                                onCompletionListener.onCompletion(MediaPlayerWrapper.this);
                            }
                        });
                    }
                }
                stateSync.notifyAll();
            }
        }
    } ;

    TTAudioTrack.AudioTrackPreparedListener audioTrackPreparedListener = new TTAudioTrack.AudioTrackPreparedListener() {
        @Override
        public void onPrepared() {
            if (onPreparedListener != null) {
                Log.d("OnPrepListener", "  getOnPrepared") ;
                onPreparedListener.onPrepared(MediaPlayerWrapper.this);
            }
        }
    } ;


    private Runnable mBufferTimeTask = new Runnable() {
        @Override
        public void run() {
            if (onBufferingUpdateListener != null) {
                int percents = getSongDownloadPercent() ;
                Log.d("SendBufUpd", percents+ "%") ;
                onBufferingUpdateListener.onBufferingUpdate(MediaPlayerWrapper.this,percents);
                if (percents == 100) {
                    bufHandler.removeCallbacks(this);
                } else {
                    bufHandler.postDelayed(this, 300);
                }
            } else {
                bufHandler.removeCallbacks(this);
            }
        }
    } ;

    public int getSongDownloadPercent() {
        if (track == null) {
            return 0;
        } else {
            TTAudioFileStream stream = track.getAudioFileStream();
            if (stream.isCashedSongPlaying()) {
                return  100 ;
            }
            stream.fillAudioStreamState(gsdpAstate);
            if (gsdpAstate.totalSize == 0) {
                return 0;
            }
            long percent = (gsdpAstate.downloadedSize * 100L)
                    / gsdpAstate.totalSize;
            return (int) percent;
        }
    }


    @Override
    public boolean isPlaying() {
        Log.d(id, "isPlaying" ) ;
        if (track == null) {
            return  false ;
        } else {
            return track.getState() == TTAudioTrack.AudioTrackState.Playing;
        }
    }


    @Override
    public int getDuration() {
       if (catalogSongItem != null) {
           long calendar = catalogSongItem.getDuration() * 1000;
           if (track != null && track.getDuration() != 0) {
              return (int)track.getDuration() ;
           }
           if (calendar != 0 ) {
               return (int)calendar;
           } else {
               return 0 ;
           }
       } else {
           return 0 ;
       }



    }


    public TTAudioTrack getTrack() {
        return track;
    }

    @Override
    public int getCurrentPosition() {
        return track.getPlayPosition() ;
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if (isPrepared()) {
            track.requestSeek(msec / 1000);
        }
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        Log.d(id, "setAudioStreamType");
    }

    @Override
    public void setCatalogSongItem(StreamSong catalogSongItem) {
         this.catalogSongItem = catalogSongItem ;
         audioFileStream = new TTAudioFileStream(catalogSongItem);
         track =
                new TTAudioTrack(catalogSongItem.getUniqueIdentifier(), audioFileStream,
                        TTAudioTrack.AudioTrackState.Playing, Build.VERSION.SDK_INT);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Log.d(id,"setDataSource "+uri);
        audioFileStream.setDataSource(uri);
    }

    private String path ;

    @Override
    public void setDataSource(String path) {
        Log.d(id,"setDataSource "+path);
        this.path = path ;
        audioFileStream.setDataSource(path) ;
    }

    private static Handler handler = new Handler() ;

    @Override
    public void prepareAsync() throws IllegalStateException {
        Log.d(id,"prepareAsync");
        isLastSong = false ;
        audioFileStream.start();
        if ( onPreparedListener != null) {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    track.start();
                }
            },200) ;

        }
    }

    @Override
    public void prepareAsyncNext() throws IllegalStateException {
        Log.d(id,"prepareAsync");
        isLastSong = false ;
        isNextSong = true ;
        audioFileStream.start();
    }

    public void startNextTrack() {
        isNextSong = false ;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                track.start();
            }
        },200) ;

    }

    public boolean isDownloaded() {
        if (audioFileStream != null
                && audioFileStream.getDownloadedSize() != 0 && audioFileStream.getTotalSize() != 0
                && audioFileStream.getTotalSize() == audioFileStream.getDownloadedSize()  ) {
            return true ;
        } else {
            return false ;
        }
    }




    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isLastSong() {
        return isLastSong;
    }

    public void setLastSong(boolean isLastSong) {
        this.isLastSong = isLastSong;
    }
}
