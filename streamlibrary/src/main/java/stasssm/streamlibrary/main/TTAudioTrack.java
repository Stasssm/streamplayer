package stasssm.streamlibrary.main;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.tentracks.common.Mp3Decoder;
import stasssm.streamlibrary.utils.AudioPool;


@TargetApi(9)
public class TTAudioTrack {
    public enum AudioTrackState {
        Initial, Playing, Paused, Ended, Stopped, Error;

        public boolean isTerminated() {
            return this == Ended || this == Stopped || this == Error;
        }
    }

    public interface AudioTrackStateChangeListener {
        public void onAudioTrackStateChange(TTAudioTrack audioTrack);
    }

    public interface AudioTrackPreparedListener{
        public void onPrepared() ;
    }


    private String songId;
    private AudioTrack track;
    TTAudioFileStream audioFileStream;

    private int sdkVersion = 0;
    private int seconds;
    private long duration ;
    private AudioTrackState audioTrackState;
    private AudioTrackState requestedAudioTrackState;
    private AudioTrackState startingAudioTrackState;
    private int requestedSeek = -1;
    private boolean isBuffering = false;
    private Lock stateLock = new ReentrantLock();
    private Condition stateChangedCondition = stateLock.newCondition();
    private boolean deinitializedMp3Decoder = false;

    private boolean isPrepared = false ;
    // public static boolean isClosed = true ;

    short[] pcmBuffer;



    public String getSongId() {
        return songId;
    }

    AudioTrackStateChangeListener listener;
    AudioTrackPreparedListener onPreparedListener ;

    public void setListener(AudioTrackStateChangeListener listener) {
        this.listener = listener;
    }

    public void setListener(AudioTrackPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    private void setState(AudioTrackState state, int seconds) {
        stateLock.lock();
        try {
            this.audioTrackState = state;
            this.seconds = seconds;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
        if (listener != null) {
            listener.onAudioTrackStateChange(this);
        }
    }

    private void setSeconds(int seconds) {
        stateLock.lock();
        try {
            this.seconds = seconds;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
        if (listener != null) {
            listener.onAudioTrackStateChange(this);
        }
    }

    private void setIsBuffering(boolean isBuffering) {
        stateLock.lock();
        try {
            this.isBuffering = isBuffering;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
        if (listener != null) {
            listener.onAudioTrackStateChange(this);
        }
    }

    public static class AudioTrackFullState {
        public AudioTrackState state;
        public int seconds;
        public boolean buffering;
    }

    public int getPlayPosition() {
        return seconds*1000;
    }

    public long getDuration() {
        return duration;
    }

    public AudioTrackState getState() {
        return audioTrackState;
    }

    public void getAudioTrackFullState(AudioTrackFullState state) {
        stateLock.lock();
        try {
            state.state = this.audioTrackState;
            state.seconds = this.seconds;
            state.buffering = this.isBuffering;
        } finally {
            stateLock.unlock();
        }
    }

    public TTAudioTrack(String songId, TTAudioFileStream audioFileStream, AudioTrackState startingState, int sdkVersion) {
        this.songId = songId;
        this.audioFileStream = audioFileStream;
        this.startingAudioTrackState = startingState;
        this.sdkVersion = sdkVersion;

        int bufferSize = 512 * 1024;
        int maxMem = (int) (Runtime.getRuntime().maxMemory() / 4);
        if (maxMem > 0) {
            bufferSize = Math.min(bufferSize, maxMem);
        }
        boolean everythingOk = false;
        while (!everythingOk) {
            try {
                pcmBuffer = new short[bufferSize];
                everythingOk = true;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                bufferSize *= 0.75;
            }
        }

        setState(AudioTrackState.Initial, -1);
    }

    public TTAudioFileStream getAudioFileStream() {
        return audioFileStream;
    }

    public interface TTAudioTrackStatePredicate {
        boolean satisfies(TTAudioTrack stream);
    }

    public void waitForState(TTAudioTrackStatePredicate predicate) {
        stateLock.lock();
        try {
            while (!predicate.satisfies(this)) {
                try {
                    stateChangedCondition.wait();
                } catch (InterruptedException e) {
                    //Utils.reportError(e, false);
                    e.printStackTrace();
                }
            }
        } finally {
            stateLock.unlock();
        }
    }

    public void requestStop() {
        stateLock.lock();
        try {
            this.requestedAudioTrackState = AudioTrackState.Stopped;
            //  closeAudioTrack() ;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    public void waitForStop() {
        stateLock.lock();
        try {
            while (audioTrackState != AudioTrackState.Ended
                    && audioTrackState != AudioTrackState.Error
                    && audioTrackState != AudioTrackState.Stopped) {
                try {
                    stateChangedCondition.wait();
                } catch (InterruptedException e) {
                    //Utils.reportError(e, false);
                    e.printStackTrace();
                }
            }
        } finally {
            stateLock.unlock();
        }
    }

    public void requestPause() {
        stateLock.lock();
        try {
            this.requestedAudioTrackState = AudioTrackState.Paused;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    public void requestPlay() {
        stateLock.lock();
        try {
            this.requestedAudioTrackState = AudioTrackState.Playing;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    public void requestSeek(int toSecond) {
        stateLock.lock();
        try {
            this.requestedSeek = toSecond;
            stateChangedCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    Runnable audioRunnable = new Runnable() {
        @Override
        public void run() {
            runAudioTrack();
        }
    };
    TTAudioFileStream.AudioFileStreamState fileStreamState = new TTAudioFileStream.AudioFileStreamState();

    private void runAudioTrack() {
        setState(startingAudioTrackState, 0);
//        while (!isClosed) {
        //   try {
        //       Thread.sleep(100);
        //    } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        //   }

        try {
            waitForDownloading();
            if (this.requestedAudioTrackState != AudioTrackState.Stopped) {
                initializePlayback();
                //  isClosed = false ;
                Log.d("OnPrepListener", "  preOnPrepared");
                isPrepared = true;
                if (onPreparedListener != null) {
                    Log.d("OnPrepListener", "  notNullOnPrepared");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OnPrepListener", "  SENDOnPrepared");
                            onPreparedListener.onPrepared();
                        }
                    });
                }
                while (audioTrackState == AudioTrackState.Playing
                        || audioTrackState == AudioTrackState.Paused) {

                    maybeWaitWhilePaused();

                    if (requestedAudioTrackState == AudioTrackState.Stopped) {
                        break;
                    }

                    playIteration();
                }
                //closeAudioTrack();
                setState(AudioTrackState.Stopped, seconds);
            }
        } catch (Exception e) {
            //Utils.reportError(e, false);
            e.printStackTrace();
            //	closeAudioTrack();
            Log.d("TotalError","Exception audiotrack");
            setState(AudioTrackState.Error, -1);
        }
    }

    private void maybeWaitWhilePaused() {
        stateLock.lock();
        try {
            if (requestedAudioTrackState != AudioTrackState.Paused) {
                return;
            }
            if (requestedAudioTrackState == AudioTrackState.Paused) {
                if (track != null) {
                    track.pause();
                }
                stateLock.unlock();
                try {
                    setState(AudioTrackState.Paused, seconds);
                } finally {
                    stateLock.lock();
                }
                while (requestedAudioTrackState == AudioTrackState.Paused) {
                    try {
                        stateChangedCondition.await();
                    } catch (InterruptedException e) {
                        //Utils.reportError(e, false);
                        e.printStackTrace();
                    }
                }
                if (track != null) {
                    track.play();
                }
            }
        } finally {
            stateLock.unlock();
        }
    }

    private void waitForDownloading() {
        audioFileStream.waitForState(new TTAudioFileStream.AudioFileStreamStatePredicate() {
            TTAudioFileStream.AudioFileStreamState state = new TTAudioFileStream.AudioFileStreamState();

            @Override
            public boolean satisfies(TTAudioFileStream stream) {
                stream.fillAudioStreamState(state);
                if (state.downloadState == TTAudioFileStream.DownloadState.Aborted
                        || state.downloadState == TTAudioFileStream.DownloadState.Downloaded
                        || state.downloadState == TTAudioFileStream.DownloadState.Error
                        || state.downloadState == TTAudioFileStream.DownloadState.TemporaryError) {
                    return true;
                }
                return state.downloadState != TTAudioFileStream.DownloadState.Initial && state.downloadedSize >= 256 * 1024;
            }
        });
    }

    private void initializePlayback() {
//		Log.i(logTag, "Begin initializing ffmpeg");
        int err = Mp3Decoder.initializeDecoder();
        Log.i("DecoderMusix" , this.toString());
        Log.i("DecoderMusix", "init decoder");
        if (err != 0) {
            setState(AudioTrackState.Error, -1);
            return;
        }
        err = audioFileStream.getAudioFileName(new TTAudioFileStream.AudioFileStreamFilenameAcceptor<Integer>() {

            @Override
            public Integer process(File filename) {
                Log.i("DecoderMusix", "configure decoder");
                if (filename != null) {
                    Log.i("DecoderMusix", filename.toString());
                    Log.i("DecoderMusix", filename.exists()+"");
                } else {
                    Log.i("DecoderMusix", "null");
                }
                return Mp3Decoder.configureDecoder(filename == null ? "" : filename.toString());
            }
        });
        if (err != 0) {
            deinitializedMp3Decoder = true;
            Log.d("TotalError","Exception ffmpeg problem");
            setState(AudioTrackState.Error, -1);
            return;
        }
//		Log.i(logTag, "Done initializing ffmpeg");
        duration =  convertDurationSong() ;


        audioFileStream.waitForState(new TTAudioFileStream.AudioFileStreamStatePredicate() {

            @Override
            public boolean satisfies(TTAudioFileStream stream) {
                TTAudioFileStream.AudioFileStreamState fileStreamState = new TTAudioFileStream.AudioFileStreamState();
                stream.fillAudioStreamState(fileStreamState);
                return fileStreamState.downloadState != TTAudioFileStream.DownloadState.Initial;
            }
        });
    }

    private long convertDurationSong() {
        try {
            if (audioFileStream.isOfflinePlaying()) {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(audioFileStream.getUrl());
                String duration =
                        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long dur = Long.parseLong(duration);
                return dur;
            }
        } catch (Exception e) {
            return  0 ;
        }
        return  0 ;
    }


    private void playIteration() throws Exception {


        maybeRefillBuffer();

        //Log.i(logTag, String.format("TTAudioTrack: %d bytes in buffer", mpegBufferSize));

        if (track != null && track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            track.play();
        }

        //Log.i(TTAudioTrack.class.getName(), "cached: " + fileStreamState.downloadedSize + " decoded: " + Mp3Decoder.getDecoderFrameSize());

        int bytesRead = 0;
        for (int i = 0; i < 3; i++) {
            bytesRead = Mp3Decoder.decodeFrame(pcmBuffer);
            //Log.i("DecoderMusix", "decode frame");
            if (bytesRead > 0) {
                break;
            }
        }
        if (bytesRead <= 0 && seconds > 0) {
            //  closeAudioTrack();
            if (listener !=null) {
                listener.onAudioTrackStateChange(this);
            }
            setState(AudioTrackState.Ended, seconds);

            return;
        }

        if (track == null) {
            initializeTrack();
        }

        track.write(pcmBuffer, 0, bytesRead / 2);

        if (requestedSeek != -1) {
            int seekTo;
            stateLock.lock();
            try {
                seekTo = requestedSeek;
                requestedSeek = -1;
            } finally {
                stateLock.unlock();
            }
            if (seekTo != -1) {
                int result = Mp3Decoder.seek(seekTo);
                Log.i("DecoderMusix", "seek to " + seekTo);
                if (result >= 0) {
                    track.pause();
                    track.flush();
                    track.play();
                    track.setPositionNotificationPeriod(Mp3Decoder.getDecoderSampleRate());
                    Log.i("DecoderMusix", "get decoder sample rate");
                    track.setPlaybackPositionUpdateListener(playbackPositionUpdateListener);
                    setSeconds(seekTo);
                }
            }
        }
    }

    int minBufferSize = 0;
    int lastDecoderFrameSize = 0;

    private boolean bufferIsFilled(boolean isAlreadyBuffering) {
        lastDecoderFrameSize = Math.max(lastDecoderFrameSize, Mp3Decoder.getDecoderFrameSize());
        if (fileStreamState.downloadState.isTerminated())
            return true;

        if (fileStreamState.downloadedSize < minBufferSize)
            return false;

        int k = isAlreadyBuffering ? 128 : 16;

        return !(fileStreamState.downloadedSize <
                lastDecoderFrameSize + Math.max(k * 1024, 2 * minBufferSize));
    }

    private TTAudioFileStream.AudioFileStreamStatePredicate audioFileStreamPredicate =
            new TTAudioFileStream.AudioFileStreamStatePredicate() {

                @Override
                public boolean satisfies(TTAudioFileStream stream) {
                    audioFileStream.fillAudioStreamState(fileStreamState);
                    lastDecoderFrameSize = Math.max(lastDecoderFrameSize, Mp3Decoder.getDecoderFrameSize());
                    return bufferIsFilled(true);
                }
            };

    private void maybeRefillBuffer() throws Exception {
        audioFileStream.fillAudioStreamState(fileStreamState);
        if (!bufferIsFilled(false)) {

            if (track != null && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause();
            }

            setIsBuffering(true);

            audioFileStream.waitForState(audioFileStreamPredicate);

            setIsBuffering(false);

            if (track != null && track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                track.pause();
            }
        }
    }

    public void closeAudioTrack() {
        Log.d("audioTrackState","Ended") ;
        if (!deinitializedMp3Decoder) {
            deinitializedMp3Decoder = true;
            Log.i("DecoderMusix", "Deinitializing ffmpeg");

            // if (!isClosed) {
            int close = Mp3Decoder.closeDecoder();
        }
        //    isClosed = true;
        // }
        //Log.i("DecoderMusix", "Deinitialized ffmpeg"+close);

        if (track != null) {
            track.flush();
            track.stop();
            track.release();
            track = null;
        }

        //disableEqualizer();
        //cleanEqualizer();
    }

    OnPlaybackPositionUpdateListener playbackPositionUpdateListener = new OnPlaybackPositionUpdateListener() {
        @Override
        public void onPeriodicNotification(AudioTrack track) {
            setSeconds(seconds + 1);
        }
        @Override
        public void onMarkerReached(AudioTrack track) {
        }
    };

    private void initializeTrack() throws Exception {
        int sampleRate = Mp3Decoder.getDecoderSampleRate();
        int channelConfig = Mp3Decoder.getDecoderChannels() == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);

        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2, AudioTrack.MODE_STREAM);

        track.setPositionNotificationPeriod(Mp3Decoder.getDecoderSampleRate());
        track.setPlaybackPositionUpdateListener(playbackPositionUpdateListener);

        if (track.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new Exception(String.format("Error initializing audioTrack: %d", track.getState()));
        }
        Log.d("TrackConfig", "channelConfig = " + channelConfig + " , sample rate = " +
                sampleRate + " , minbuffer = " + minBufferSize) ;
        track.play();

    }


    public void start() {
        AudioPool.getSharedInstance().submit(audioRunnable);
        //	requestPlay();
    }

}
