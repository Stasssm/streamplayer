package stasssm.streamlibrary.playercore;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.util.Log;

import stasssm.streamlibrary.R;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;


/**
 * Created by Andrey Kulikov on 26.03.15.
 */
@TargetApi(21)
public class PlayerCoreLollipop implements PlayerCore.BasePlayerCore {

    private MediaSession mSession;
    private MediaController mController;
    private PlayerService mService;

    @Override
    public void initCore() {
        mService = PlayerService.getSharedService();
        mSession = new MediaSession(mService, "simple player session");
        mController = new MediaController(mService, mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e("MediaPlayerService", "onPlay");
                mService.onPlayerStart();
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e("MediaPlayerService", "onPause");
                mService.onPlayerPause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.e("MediaPlayerService", "onSkipToNext");
                mService.playNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e("MediaPlayerService", "onSkipToPrevious");
                mService.playPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.e("MediaPlayerService", "onStop");
                mService.requestStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

        });


    }

    @Override
    public void updatePlaybackState(int newState) {
        if (newState != PlayerService.STATE_NONE) {
            if (newState == PlayerService.STATE_STOPPED) {
                if (mSession.isActive()) {
                    mSession.setActive(false);
                }
            } else {
                if (!mSession.isActive()) {
                    mSession.setActive(true);
                }
            }
            mSession.setPlaybackState(new PlaybackState.Builder()
                    .setState(mService.getCurrentState(),
                            (long) mService.getMp().getCurrentPosition(), 1f)
                    .build());
        }
    }

    @Override
    public void handleAction(String action) {
        if (action.equalsIgnoreCase(PlayerService.ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    @Override
    public Notification createNotification(Bitmap cover, StreamSong song, boolean inPause, boolean onGoing) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(mService, PlayerService.class);
        intent.setAction(PlayerService.ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(mService, 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(mService)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setDeleteIntent(pendingIntent)
                .setStyle(style)
                .setOngoing(onGoing)
                .setShowWhen(false)
                .setLargeIcon(cover)
                .setColor(mService.getResources().getColor(android.R.color.white));

        Notification.Action mainAction;
        if (inPause) {
            mainAction = generateAction(R.drawable.next, "Play", PlayerService.ACTION_PLAY);
        } else {
            mainAction = generateAction(R.drawable.next, "Pause", PlayerService.ACTION_PAUSE);
        }
        builder.addAction(generateAction(R.drawable.prev, "Previous", PlayerService.ACTION_PREVIOUS));
        builder.addAction(mainAction);
        builder.addAction(generateAction(R.drawable.next, "Next", PlayerService.ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        return builder.build();
    }

    @Override
    public void onSongChanged(StreamSong item, Bitmap bitmap) {
        MediaMetadata.Builder builder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, item.getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, item.getArtist())
                .putString(MediaMetadata.METADATA_KEY_TITLE, item.getTitle());
        if (bitmap != null) {
            builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap);
        }
        mSession.setMetadata(builder.build());
    }

    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(mService, PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(PlayerService.getSharedService(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    public void release() {
        mSession.release();
    }

}
