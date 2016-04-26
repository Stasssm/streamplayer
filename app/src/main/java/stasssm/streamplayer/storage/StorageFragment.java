package stasssm.streamplayer.storage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.tagging.FileTagger;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;
import stasssm.streamplayer.tags.TagAdapter;

/**
 * Created by Stas on 13.04.2016.
 */
public class StorageFragment extends Fragment {



    private static final String TAG = StorageFragment.class.getName() ;



    public static void start(PlayerActivity coreActivity) {
        StorageFragment tagsFragment = new StorageFragment();
        coreActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_fragment_continer, tagsFragment, TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        ButterKnife.bind(this, view);
        return view ;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


}
