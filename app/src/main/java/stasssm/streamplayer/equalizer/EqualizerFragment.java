package stasssm.streamplayer.equalizer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import stasssm.streamlibrary.equalizer.StreamEqualizer;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 13.04.2016.
 */
public class EqualizerFragment extends Fragment {


    private static final String TAG = EqualizerFragment.class.getName();

    @Bind(R.id.seekBar)
    VerticalSeekBar seekBar;
    @Bind(R.id.seekBar_2)
    VerticalSeekBar seekBar2;
    @Bind(R.id.seekBar_3)
    VerticalSeekBar seekBar3;
    @Bind(R.id.seekBar_4)
    VerticalSeekBar seekBar4;
    @Bind(R.id.seekBar_5)
    VerticalSeekBar seekBar5;

    private StreamEqualizer equalizer;


    public static void start(PlayerActivity coreActivity) {
        EqualizerFragment tagsFragment = new EqualizerFragment();
        coreActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_fragment_continer, tagsFragment, TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        ButterKnife.bind(this, view);
        initSeekBars();
        return view;
    }


    private void initSeekBars() {

        equalizer = new StreamEqualizer();
        VerticalSeekBar[] verticalSeekBar = new VerticalSeekBar[5] ;
        verticalSeekBar[0] = seekBar;
        verticalSeekBar[1] = seekBar2;
        verticalSeekBar[2] = seekBar3;
        verticalSeekBar[3] = seekBar4;
        verticalSeekBar[4] = seekBar5;
        ArrayList<Integer> list =  equalizer.getLevels() ;
        for (int i = 0 ; i < list.size() ; i++) {
            if (verticalSeekBar.length < i) {
                verticalSeekBar[i].setProgress(list.get(i));
            }
            final int pos = i ;
            verticalSeekBar[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    equalizer.changeLevel(progress,pos);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


}
