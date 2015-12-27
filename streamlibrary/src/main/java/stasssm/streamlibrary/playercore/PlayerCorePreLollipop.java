package stasssm.streamlibrary.playercore;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import stasssm.streamlibrary.R;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;


public class PlayerCorePreLollipop implements PlayerCore.BasePlayerCore, AudioManager.OnAudioFocusChangeListener {

    private PlayerService mService;
    private RemoteControlClientCompat mRemoteControlClientCompat;
    private ComponentName mRemoteControlResponder;
    private AudioManager mAudioManager;
    private NotificationCompat.Builder notifyBuilder;
    boolean mInTransientAudioFocusLoss;

    @Override
    public void initCore() {
        mService = PlayerService.getSharedService();

        mAudioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(mService.getPackageName(), MediaBtnsInterceptor.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);

        notifyBuilder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnlyAlertOnce(true);
    }

    @Override
    public void handleAction(String action) {
        if (action.equalsIgnoreCase(PlayerService.ACTION_PLAY)) {
            mService.onPlayerStart();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_PAUSE)) {
            mService.onPlayerPause();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_PREVIOUS)) {
            mService.playPrevious();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_NEXT)) {
            mService.playNext();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_STOP)) {
            mService.requestStop();
        } else if (action.equalsIgnoreCase(PlayerService.ACTION_CLOSE)) {
            mService.onNotificationClose();
        }
    }

    @Override
    public Notification createNotification(Bitmap cover, StreamSong song, boolean inPause, boolean onGoing) {
        notifyBuilder.setOngoing(onGoing);
        Notification notification;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            notifyBuilder.setContentTitle(song.getTitle())
                    .setContentText(song.getArtist())
                    .setLargeIcon(cover);
            notification = notifyBuilder.build();
        } else {
            notifyBuilder.setContent(createCoverRemoteView(cover, song, R.layout.notif_player, inPause));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
            }

            notification = notifyBuilder.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification.bigContentView = createCoverRemoteView(cover, song,
                            R.layout.notif_player_big, inPause);
            }
        }
        return notification;
    }

    @Override
    public void release() {
        RemoteControlHelper.unregisterRemoteControlClient(mAudioManager,
                mRemoteControlClientCompat);
        mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
    }

    @Override
    public void onSongChanged(StreamSong item, Bitmap cover) {

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mRemoteControlResponder);
            mRemoteControlClientCompat = new RemoteControlClientCompat(
                    PendingIntent.getBroadcast(mService /*context*/,
                            0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                    mRemoteControlClientCompat);
        }

        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);

        // Update the remote controls
        RemoteControlClientCompat.MetadataEditorCompat editor =
                mRemoteControlClientCompat.editMetadata(true);
        editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, item.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, item.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, item.getTitle());
        if (cover != null && !cover.isRecycled()) {
            try {
              //  Bitmap copy = cover.copy(cover.getConfig(), false);
                editor.putBitmap(cover);
            } catch (OutOfMemoryError e) {
                Log.e("PlayerCorePreLollipop", "OutOfMemoryError on cover copy");
            }
        }
        editor.apply();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Log.d("FocusChange",focusChange+"") ;
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ) {
            if (mService.getCurrentState() == PlayerService.STATE_PLAYING) {
                mInTransientAudioFocusLoss = true;
                mService.requestPause();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (mInTransientAudioFocusLoss) {
                mInTransientAudioFocusLoss = false;
                mService.requestPlay();
            } else {
                mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
            mAudioManager.abandonAudioFocus(this);
            mService.requestPause();
        }
    }

    @Override
    public void updatePlaybackState(int newState) {
        if (newState != PlayerService.STATE_NONE) {
            if (newState == PlayerService.STATE_PLAYING) {
                mAudioManager.requestAudioFocus(this,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);
            }
            if (mRemoteControlClientCompat != null) {
                mRemoteControlClientCompat.setPlaybackState(newState);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private RemoteViews createCoverRemoteView(Bitmap cover, StreamSong song, int layoutResId,
                                              boolean inPause) {
        RemoteViews rViews = new RemoteViews(mService.getPackageName(), layoutResId);
        if (cover == null) {
            rViews.setImageViewResource(R.id.iv_cover, R.drawable.ic_launcher);
        } else {
            rViews.setImageViewBitmap(R.id.iv_cover, cover);
        }
        rViews.setTextViewText(R.id.tv_artist, song.getArtist());
        rViews.setTextViewText(R.id.tv_song, song.getTitle());
        rViews.setOnClickPendingIntent(R.id.iv_play, getPlaybackIntent(PlayerService.ACTION_PLAY_PAUSE));
        rViews.setOnClickPendingIntent(R.id.iv_next, getPlaybackIntent(PlayerService.ACTION_NEXT));
        rViews.setImageViewResource(R.id.iv_play, inPause ?
                R.drawable.ic_action_play_button : R.drawable.ic_notif_pause_button);
        rViews.setOnClickPendingIntent(R.id.iv_prew, getPlaybackIntent(PlayerService.ACTION_PREVIOUS));
        rViews.setOnClickPendingIntent(R.id.iv_close_player_big, getPlaybackIntent(PlayerService.ACTION_CLOSE));
        return rViews;
    }

    private PendingIntent getPlaybackIntent(String action) {
        return PendingIntent.getService(mService, 0, new Intent(mService,
                PlayerService.class).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
