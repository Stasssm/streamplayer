package stasssm.streamplayer.mediaStore;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.MediaStoreSong;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamplayer.R;

/**
 * Created by Стас on 09.05.2016.
 */
public class StoreFragment extends DialogFragment implements StoreClickListener {

    public static String TAG = StoreFragment.class.getName();

    @Bind(R.id.fragment_store_recycler_view)
    RecyclerView mRecyclerView;

    ArrayList<MediaStoreSong>  songs ;

    @TargetApi(16)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.dialog_media_store, null);
        ButterKnife.bind(this, v);
        loadData();
        return v;
    }

    public void loadData() {
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selectionArgsMp3 = new String[]{mimeType};
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, selectionMimeType, selectionArgsMp3, null);
        songs = MediaStoreSong.toSongsArray(cursor);
        if(songs != null) {
            LinearLayoutManager manager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(manager);
            StoreAdapter storeAdapter = new StoreAdapter(songs,this);
            mRecyclerView.setAdapter(storeAdapter);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(MediaStoreSong song) {
        PlayerService playerService = PlayerService.getSharedService();
        if (playerService != null) {
            playerService.setCatalogSongs(new ArrayList<StreamSong>(songs));
            playerService.selectSong(songs.indexOf(song));
        }
        dismiss();
    }


}
