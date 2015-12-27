package stasssm.streamlibrary.playercore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import stasssm.streamlibrary.main.PlayerService;


public class MediaBtnsInterceptor extends BroadcastReceiver {

    public static final int KEYCODE_MEDIA_PLAY = 126;
    public static final int KEYCODE_MEDIA_PAUSE = 127;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null) {
                return;
            }

            int keycode = event.getKeyCode();
            int action = event.getAction();

            if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                if (action == KeyEvent.ACTION_DOWN) {

                    if (PlayerService.getSharedService() != null) {
                        try {
                            PlayerService.getSharedService().togglePlayPause();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Log.e("Player", "Player was called before initializing");
                        }
                    }

                    if (isOrderedBroadcast()) {
                        abortBroadcast();
                    }
                }
            }

            if (keycode == KEYCODE_MEDIA_PLAY) {
                if (action == KeyEvent.ACTION_DOWN) {

                    if (PlayerService.getSharedService() != null) {
                        PlayerService.getSharedService().requestPlay();
                    }

                    if (isOrderedBroadcast()) {
                        abortBroadcast();
                    }
                }
            }

            if (keycode == KEYCODE_MEDIA_PAUSE) {
                if (action == KeyEvent.ACTION_DOWN) {

                    if (PlayerService.getSharedService() != null) {
                        PlayerService.getSharedService().requestPause();
                    }

                    if (isOrderedBroadcast()) {
                        abortBroadcast();
                    }
                }
            }

            if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                if (action == KeyEvent.ACTION_DOWN) {

                    if (PlayerService.getSharedService() != null) {
                        PlayerService.getSharedService().playPrevious();
                    }

                    if (isOrderedBroadcast()) {
                        abortBroadcast();
                    }
                }
            }

            if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                if (action == KeyEvent.ACTION_DOWN) {

                    if (PlayerService.getSharedService() != null) {
                        PlayerService.getSharedService().playNext();
                    }

                    if (isOrderedBroadcast()) {
                        abortBroadcast();
                    }
                }
            }

        }

        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
            if (PlayerService.getSharedService() != null) {
                PlayerService.getSharedService().requestPause();
            }
        }

    }
}
