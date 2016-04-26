package stasssm.streamplayer.drawer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamplayer.PlayerActivity;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 13.04.2016.
 */
public class NawAdapter extends RecyclerView.Adapter<NawAdapter.NawHolder> {

    private String[] items = {"Player Page","Track MetaData","Storage Settings","Equalizer", "Visualizer"
            , "Other Settings"};

    PlayerActivity playerActivity ;

    public NawAdapter(PlayerActivity playerActivity) {
        this.playerActivity = playerActivity ;
    }

    @Override
    public NawHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.holder_naw, parent, false);
        return new NawHolder(view);
    }

    @Override
    public void onBindViewHolder(NawHolder holder, int position) {
        holder.attachData(items[position],position);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    protected class NawHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.naw_text)
        TextView nawText;

        int position ;

        public NawHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void attachData(String data,int position) {
            this.position = position ;
            nawText.setText(data);
        }

        @OnClick(R.id.naw_layout)
        public void clickElement() {
            playerActivity.clickMenu(position);
        }

    }


}
