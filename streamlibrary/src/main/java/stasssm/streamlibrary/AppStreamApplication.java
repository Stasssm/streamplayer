package stasssm.streamlibrary;

import android.app.Application;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import org.jaudiotagger.tag.TagOptionSingleton;

import stasssm.streamlibrary.main.PlayerService;

/**
 * Created by Стас on 12.12.2015.
 */
public class AppStreamApplication extends Application {

    private PhoneStateListener mPhoneStateListener;


    @Override
    public void onCreate() {
        super.onCreate();
        startPlayerService();
        setTagOptions();
    }

    private void setTagOptions() {
        TagOptionSingleton.getInstance().setAndroid(true);
    }


    public void startPlayerService() {
        PlayerService playerService = PlayerService.getSharedService();
        if (playerService == null) {
            startService(new Intent(getApplicationContext(), PlayerService.class));
        }
    }

    public void registerPhoneStateListener() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void unregisterPhoneStateListener() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mgr.listen(null, PhoneStateListener.LISTEN_CALL_STATE);
    }



}
