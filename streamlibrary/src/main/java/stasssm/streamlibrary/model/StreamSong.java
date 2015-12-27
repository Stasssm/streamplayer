package stasssm.streamlibrary.model;

/**
 * Created by Стас on 12.12.2015.
 */
public interface StreamSong {

    public String getUniqueIdentifier() ;
    public boolean isOffline() ;
    public String isOfflineExists() ;
    public int getDuration() ;
    public String getStreamUrl() ;
    public String getTitle();
    public String getArtist();

}
