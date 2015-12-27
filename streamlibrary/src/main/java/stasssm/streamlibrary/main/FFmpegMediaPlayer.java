package stasssm.streamlibrary.main;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;

import stasssm.streamlibrary.model.StreamSong;

/**
 * Created by Стас on 20.08.2015.
 */
public abstract class FFmpegMediaPlayer {

    public interface OnPreparedListener
    {
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        void onPrepared(FFmpegMediaPlayer mp);
    }

    public interface OnBufferingUpdateListener
    {
        void onBufferingUpdate(FFmpegMediaPlayer mp, int percent);
    }

    public interface OnCompletionListener
    {
        void onCompletion(FFmpegMediaPlayer mp);
    }


    public abstract void setOnCompletionListener(OnCompletionListener listener) ;

    public abstract void start() throws IllegalStateException ;

    public abstract void stop() throws IllegalStateException ;


    public abstract void pause() throws IllegalStateException ;


    public abstract void release() ;


    public abstract void reset() ;

    public abstract void setOnPreparedListener(OnPreparedListener listener) ;

    public abstract void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) ;

    public abstract boolean isPlaying();


    public abstract int getDuration()  ;

    public abstract int getCurrentPosition() ;

    public abstract void seekTo(int msec) throws IllegalStateException ;

    public abstract void setAudioStreamType(int streamtype) ;


    public abstract void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException ;

    public abstract void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException ;


    public abstract void prepareAsync() throws IllegalStateException ;

    public abstract void prepareAsyncNext() ;

    public abstract void startNextTrack() ;

    public abstract void setCatalogSongItem(StreamSong catalogSongItem) ;


}
