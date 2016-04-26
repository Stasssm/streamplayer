package stasssm.streamlibrary.helpers;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import stasssm.streamlibrary.main.FFmpegMediaPlayer;
import stasssm.streamlibrary.main.MediaPlayerWrapper;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.utils.PlayerUtilities;

/**
 * Created by Stas on 22.03.2016.
 */
public abstract class PlayerHelperActivity extends AppCompatActivity implements PlayerService.PlayerListener {


    protected PlayerUtilities utils =  new PlayerUtilities();;
    private Handler mHandler = new Handler();

    @Override
    protected void onResume() {
        super.onResume();
//        PlayerService.getSharedService().setPlayerListener(this);
        updateProgressBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  PlayerService.getSharedService().setPlayerListener(null);
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onSongChanged(StreamSong item, int position) {
        updateProgressBar();
    }

    @Override
    public void onPlayerPrepared() {
        updateProgressBar();
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        if (!isPlaying) {
            mHandler.removeCallbacks(mUpdateTimeTask);
        } else {
            updateProgressBar();
        }
    }

    @Override
    public void onSongPreparing() {

    }

    @Override
    public void onSongPrepared() {

    }

    @Override
    public void mediaPlayerChanged(FFmpegMediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingUpdate(FFmpegMediaPlayer mp, int percent) {

    }

    public abstract void onTotalDurationChanged(String duration);

    public abstract void onPlayingTimeChanged(String currentTime) ;

    protected abstract void updateSeekProgress(int progress) ;

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            MediaPlayerWrapper mp = PlayerService.getSharedService().getMp();
            if (mp != null && (mp.isLastSong() ||
                    PlayerService.getSharedService().getCurrentState() == PlayerService.STATE_PLAYING)) {
                mp.setLastSong(false);
                long currentDuration = mp.getCurrentPosition();
                long totalDuration = mp.getDuration();
                // Displaying Total Duration time
                onTotalDurationChanged(utils.milliSecondsToTimer(totalDuration)) ;
                // Displaying time completed playing
                //  Log.d("currentDuration" ,currentDuration + "" );
                if (currentDuration != -1000) {
                    onPlayingTimeChanged(utils.milliSecondsToTimer(currentDuration));
                }
                // Updating progress bar
                int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                updateSeekProgress(progress);
                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 400);
            } else {
                mHandler.removeCallbacks(this);
            }
        }

    };

    public void stopProgressBar() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }


    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


}
