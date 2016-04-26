package stasssm.streamplayer.tags;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamlibrary.tagging.FileTagger;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 29.03.2016.
 */
public class TagsFragment extends Fragment {

    private static final String TAG = TagsFragment.class.getName() ;
    

    @Bind(R.id.fragment_tag_recycler_view)
    RecyclerView mRecyclerView;

    public static void start(PlayerActivity coreActivity) {
        TagsFragment tagsFragment = new TagsFragment();
        coreActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_fragment_continer, tagsFragment, TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tags, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);
        StreamSong streamSong =  PlayerService.getSharedService().getSelectedItem() ;
        if (streamSong != null) {
            TagAdapter tagAdapter = new TagAdapter(FileTagger.getTagArray(streamSong)) ;
            mRecyclerView.setAdapter(tagAdapter);
        }
        return view ;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
