package stasssm.streamplayer.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.cashefolder.StorageUtil;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 13.04.2016.
 */
public class SettingsFragment extends Fragment {


    private static final String TAG = SettingsFragment.class.getName();

    @Bind(R.id.player_switch_shuffle)
    Switch mSwitchShuffle;
    @Bind(R.id.player_switch_repeat)
    Switch mSwitchRepeat;
    @Bind(R.id.player_switch_external_storage)
    Switch mSwitchStorage;
    @Bind(R.id.player_internal_path)
    EditText mInternalPath;
    @Bind(R.id.player_save_internal)
    Button playerSaveInternal;
    @Bind(R.id.player_external_path)
    EditText mExternalPath;
    @Bind(R.id.player_save_external)
    Button playerSaveExternal;
    @Bind(R.id.player_storage_size)
    EditText mStorageEditText;
    @Bind(R.id.player_save_storage_size)
    Button playerSaveStorageSize;
    @Bind(R.id.player_buffer_size)
    EditText mBufferSize;
    @Bind(R.id.player_save_buffe_size)
    Button playerSaveBuffeSize;
    @Bind(R.id.player_switch_layout)
    LinearLayout playerSwitchLayout;
    @Bind(R.id.player_state)
    TextView playerState;
    @Bind(R.id.player_current_time)
    TextView playerCurrentTime;
    @Bind(R.id.player_total_time)
    TextView playerTotalTime;
    @Bind(R.id.player_buff_percent)
    TextView playerBuffPercent;

    PlayerService playerService;

    public static void start(PlayerActivity coreActivity) {
        SettingsFragment tagsFragment = new SettingsFragment();
        coreActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_fragment_continer, tagsFragment, TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_other_settings, container, false);
        ButterKnife.bind(this, view);
        playerService = PlayerService.getSharedService() ;
        initStorageSize();
        initPath();
        initSwithes();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void initPath() {
        mExternalPath.setText(StorageUtil.getStorage().getExternalStoragePath());
        mInternalPath.setText(StorageUtil.getStorage().getInternalStoragePath());
    }

    private void initStorageSize() {
        mStorageEditText.setText(StorageUtil.getStorage().getMaxStorage() + "");
         mBufferSize.setText(StorageUtil.getStorage().getBufferStartSize() + "");
    }


    private void initSwithes() {
        mSwitchRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playerService.setRepeat(isChecked);
            }
        });
        mSwitchShuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // playerService.
            }
        });
        mSwitchStorage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageUtil.getStorage().setUseExternalIfPossible(isChecked);
            }
        });

    }

    @OnClick(R.id.player_save_external)
    public void clickExternal() {
        StorageUtil.getStorage().setExternalStoragePath(mExternalPath.getText().toString());
    }

    @OnClick(R.id.player_save_internal)
    public void clickInternal() {
        StorageUtil.getStorage().setExternalStoragePath(mInternalPath.getText().toString());
    }

    @OnClick(R.id.player_save_storage_size)
    public void clickSize() {
        try {
            long l = Long.parseLong(mStorageEditText.getText().toString(), 10);
            StorageUtil.getStorage().changeMaxStorageSize(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.player_save_buffe_size)
    public void clickBufferSize() {
        try {
            long l = Long.parseLong(mBufferSize.getText().toString(), 10);
            StorageUtil.getStorage().changeMaxStorageSize(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
