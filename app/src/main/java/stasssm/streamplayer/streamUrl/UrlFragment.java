package stasssm.streamplayer.streamUrl;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stasssm.streamlibrary.main.PlayerService;
import stasssm.streamlibrary.model.StreamSong;
import stasssm.streamplayer.R;
import stasssm.streamplayer.Song;

/**
 * Created by Стас on 10.05.2016.
 */
public class UrlFragment extends DialogFragment {

    public static String TAG = UrlFragment.class.getName();

    @Bind(R.id.url_edit)
    EditText urlEdit;
    @Bind(R.id.url_ok)
    Button urlOk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.dialog_url_strem, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @OnClick(R.id.url_ok)
    public void onClick() {
        String s = urlEdit.getText().toString();
        if (!TextUtils.isEmpty(s)) {
            Song song  = new Song();
            song.url = s  ;
            song.id = randomId() ;
            PlayerService playerService = PlayerService.getSharedService();
            ArrayList<StreamSong> streamSongs = new ArrayList<>();
            streamSongs.add(song);
            playerService.setCatalogSongs(streamSongs);
            playerService.selectSong(0);
            dismiss();
        }
    }

    private String randomId(){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output ;
    }


}
