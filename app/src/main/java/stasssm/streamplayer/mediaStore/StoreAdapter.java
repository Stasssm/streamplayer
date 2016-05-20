package stasssm.streamplayer.mediaStore;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import stasssm.streamlibrary.model.MediaStoreSong;
import stasssm.streamplayer.R;
import stasssm.streamplayer.tags.TagHolder;

/**
 * Created by Stas on 09.05.2016.
 */
public class StoreAdapter extends RecyclerView.Adapter<StoreHolder> {

    ArrayList<MediaStoreSong> storeSongs = new ArrayList<>() ;

    WeakReference<StoreClickListener>  listener  ;

    public StoreAdapter(ArrayList<MediaStoreSong> storeSongs,StoreClickListener listener) {
        this.storeSongs = storeSongs;
        this.listener = new WeakReference<>(listener) ;
    }

    @Override
    public StoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_store_song, parent, false);
        return new StoreHolder(v,listener.get());
    }

    @Override
    public void onBindViewHolder(StoreHolder holder, int position) {
            holder.attachData(storeSongs.get(position));
    }

    @Override
    public int getItemCount() {
        return storeSongs.size();
    }


}
