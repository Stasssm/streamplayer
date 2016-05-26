package stasssm.streamlibrary.model;

import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by Stas on 09.05.2016.
 */
public class MediaStoreSong implements StreamSong {

    private String title  ;
    private String artist ;
    private String data ;
    private long songDuration ;

    public static ArrayList<MediaStoreSong> toSongsArray(Cursor c) {
        if (c == null) {
            return null ;
        }
        ArrayList<MediaStoreSong> songs = new ArrayList<>() ;
        if (c.moveToFirst()) {
            do {
                songs.add(MediaStoreSong.toSong(c));
            } while (c.moveToNext()) ;
        }
        return songs ;
    }

    public static MediaStoreSong toSong(Cursor c) {
        MediaStoreSong song  =new MediaStoreSong() ;
        song.title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
        song.artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        song.data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
        song.songDuration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        return song ;
    }


    @Override
    public String getUniqueIdentifier() {
        return data;
    }

    @Override
    public boolean isOffline() {
        return true;
    }

    @Override
    public String isOfflineExists() {
        return data;
    }

    @Override
    public int getDuration() {
        return (int)songDuration;
    }

    @Override
    public String getStreamUrl() {
        return "";
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(long songDuration) {
        this.songDuration = songDuration;
    }
}
