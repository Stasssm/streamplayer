package stasssm.streamplayer.tags;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jaudiotagger.tag.FieldKey;

import butterknife.Bind;
import butterknife.ButterKnife;
import stasssm.streamplayer.R;

/**
 * Created by Stas on 29.03.2016.
 */
public class TagHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tag_holder_name)
    TextView tagHolderName;
    @Bind(R.id.tag_holder_edit_text)
    EditText tagHolderEditText;

    Pair<FieldKey,String> pair ;

    public TagHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void attachData(Pair<FieldKey,String> pair) {
        this.pair = pair ;
        tagHolderName.setText(pair.first.toString());
        tagHolderEditText.setText(pair.second);
    }


}
