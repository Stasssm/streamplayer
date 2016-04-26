package stasssm.streamlibrary.model;

/**
 * Created by Стас on 12.12.2015.
 */
public interface StreamSong {

    String getUniqueIdentifier() ;
    boolean isOffline() ;
    String isOfflineExists() ;
    int getDuration() ;
    String getStreamUrl() ;
    String getTitle();
    String getArtist();

}
