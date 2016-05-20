package stasssm.streamplayer.mediaStore;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.model.MediaStoreSong;
import stasssm.streamplayer.R;

/**
 * Created by Стас on 09.05.2016.
 */
public class StoreHolder extends RecyclerView.ViewHolder {


    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.subtitle)
    TextView subtitle;

    MediaStoreSong storeSong ;

    WeakReference<StoreClickListener> listener ;

    public StoreHolder(View itemView,StoreClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.listener = new WeakReference<>(listener) ;
    }

    public void attachData(MediaStoreSong song) {
        storeSong = song ;
        title.setText(song.getTitle());
        subtitle.setText(song.getArtist());
    }

    @OnClick(R.id.item_store)
    public void clickSong() {
        if (listener.get() != null) {
            listener.get().onClick(storeSong);
        }
    }


}
