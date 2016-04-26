package stasssm.streamplayer.tags;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;

import stasssm.streamplayer.App;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 29.03.2016.
 */
public class TagAdapter extends RecyclerView.Adapter<TagHolder> {

    private ArrayList<Pair<FieldKey,String>> tagList = new ArrayList<>() ;

    public TagAdapter(ArrayList<Pair<FieldKey, String>> tagList ) {
        this.tagList = tagList;
    }

    @Override
    public TagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_tag, parent, false);
        return new TagHolder(v);
    }

    @Override
    public void onBindViewHolder(TagHolder holder, int position) {
        Pair<FieldKey,String> pair = tagList.get(position) ;
        holder.attachData(pair);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }
}
