package stasssm.streamplayer;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;

import stasssm.streamlibrary.AppStreamApplication;
import stasssm.streamlibrary.PlayerInitializer;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.playercore.PlaybackNotifier;
import stasssm.streamlibrary.playercore.PlayerController;

/**
 * Created by Стас on 13.12.2015.
 */
public class App extends AppStreamApplication {


    @Override
    public void onCreate() {
        super.onCreate();

        PlayerInitializer.getPlayerInitializer().init(this).
                setNotifIntentCreator(new PlayerInitializer.NotifIntentCreator() {
                    @Override
                    public Intent createIntent(StreamSong streamSong) {
                        Intent intent = new Intent(getApplicationContext() ,MainActivity.class) ;
                        return intent;
                    }

                    @Override
                    public Class getClassToOpen(StreamSong streamSong) {
                        return MainActivity.class;
                    }

                    @Override
                    public void loadImageWhenSongChanged(StreamSong streamSong, PlaybackNotifier playbackNotifier) {
                            playbackNotifier.onBitmapLoaded(BitmapFactory.decodeResource(getResources(),
                                    R.drawable.ic_launcher));
                    }
                }).
                getStorageUtils().setUseExternalIfPossible(true).
                setExternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setInternalStoragePath(Environment.getExternalStorageDirectory().getAbsolutePath()) ;

    }
}
