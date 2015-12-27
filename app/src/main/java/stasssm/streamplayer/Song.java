package stasssm.streamplayer;

import stasssm.streamlibrary.model.StreamSong;

/**
 * Created by Стас on 13.12.2015.
 */
public class Song implements StreamSong {

    private String id = "adnkcnrncrl" ;
    private String name = "stas" ;
    private String artistName = "stas" ;
    private boolean isOffline = false ;
    private int duration = 10000 ;
    private String url = "http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2005.mp3";



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
}
