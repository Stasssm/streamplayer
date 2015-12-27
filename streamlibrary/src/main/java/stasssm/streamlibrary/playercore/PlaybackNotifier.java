package stasssm.streamlibrary.playercore;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

import stasssm.streamlibrary.PlayerInitializer;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;


/**
 * Created by Andrey Kulikov on 27.03.15.
 */
public class PlaybackNotifier  {

    public static final int NOTIFY_ID_PLAYING = 1;
    public static final String CURRENT_SONG_ID = "current_song_id";


    PlayerService mService;
    private Context mContext;
    StreamSong mShownSong;
    private Bitmap mBitmap;

    public PlaybackNotifier(PlayerService mService) {
        this.mService = mService;
        this.mContext = mService.getApplicationContext();
    }

    private boolean latestPauseState;

    public void showNotification(final StreamSong song, boolean isInPause) {
        if (song == mShownSong && isInPause == latestPauseState) {
            return;
        }
        latestPauseState = isInPause;
        if (song != mShownSong) {
            mShownSong = null;
            mBitmap = null;
            PlayerInitializer.NotifIntentCreator notifIntentCreator =
                    PlayerInitializer.getPlayerInitializer().getNotifIntentCreator();
            if (notifIntentCreator != null) {
                   notifIntentCreator.loadImageWhenSongChanged(song,this);
            }
        }

        showNotificationWithCover(song);
    }

    private void showNotificationWithCover(StreamSong song) {
        mShownSong = song;
        boolean inPause = latestPauseState;
        PendingIntent resultPendingIntent = null ;
        PlayerInitializer.NotifIntentCreator notifIntentCreator =
                PlayerInitializer.getPlayerInitializer().getNotifIntentCreator() ;
        if (notifIntentCreator != null) {
            Intent intent = notifIntentCreator.createIntent(song) ;
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mService)
//                    .addParentStack(notifIntentCreator.getClass())
                    .addNextIntent(intent);
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        final Notification notification = PlayerCore.getInstance().createNotification(mBitmap, song, inPause, !inPause);
        notification.contentIntent = resultPendingIntent;
        final NotificationManager manager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
        PlayerService.getSharedService().startForeground(NOTIFY_ID_PLAYING, notification);
    }


    private boolean isServiceRunning(){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i
                    .next();

                if (runningServiceInfo.service.getClassName().equals(PlayerService.class.getName())) {
                    Log.d("INFService", runningServiceInfo.foreground + "") ;
                    Log.d("INFService2", true + "") ;
                }

        }
        return serviceRunning;
    }

    public void hideNotification() {
        mBitmap = null;
        final PlayerService sharedService = PlayerService.getSharedService();
        if (PlayerService.getSharedService() != null) {
            PlayerService.getSharedService().stopForeground(true);
        }
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFY_ID_PLAYING);
    }

    public void onBitmapLoaded(Bitmap bitmap) {
        mBitmap = bitmap;
        if (mShownSong != null) {
            showNotificationWithCover(mShownSong);
        }
    }

}
