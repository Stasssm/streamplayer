package stasssm.streamplayer;

import java.util.ArrayList;
import java.util.List;

import stasssm.streamlibrary.model.StreamSong;

/**
 * Created by Стас on 13.12.2015.
 */
public class Song implements StreamSong {

    public String id = "adnkcnrncrldhj" ;
    private String name = "stas" ;
    private String artistName = "stas" ;
    private boolean isOffline = false ;
    private int duration = 2319 ;
    public String url = "http://musix-lg-proxy.mboxltd.com/musicx/tracks/{ZmlsXzk0MTYxMDYubXAz}";



    @Override
    public String getUniqueIdentifier() {
        return id;
    }

    @Override
    public boolean isOffline() {
        return isOffline;
    }

    @Override
    public String isOfflineExists() {
        return null;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getStreamUrl() {
        return url;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getArtist() {
        return artistName;
    }

    public static ArrayList<StreamSong> generateSongs() {
        ArrayList<StreamSong> songs = new ArrayList<>();
        songs.add(new Song());
        songs.add(new Song());
        return songs ;
    }


}
