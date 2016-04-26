package stasssm.streamlibrary.tagging;

import android.util.Pair;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import stasssm.streamlibrary.cashefolder.StorageUtil;
import stasssm.streamlibrary.model.StreamSong;

/**
 * Created by Stas on 28.03.2016.
 */
public class FileTagger {

    public static TagModel getAllTags(StreamSong song) {
        File file = new File(song.getStreamUrl()) ;
        if (!file.exists()) {
          file = new File(StorageUtil.getStorage().getCasheDir(),  StorageUtil.getStorage().getNameFile(song)) ;
        }
        if (!file.exists()) {
            return null ;
        }
        TagOptionSingleton.getInstance().setAndroid(true);
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            TagModel tagModel  =new TagModel();
            tagModel.setAlbum(tag.getFirst(FieldKey.ALBUM));
            tagModel.setAlbum_artist(tag.getFirst(FieldKey.ALBUM_ARTIST));
            tagModel.setAlbum_artist_sort(tag.getFirst(FieldKey.ALBUM_ARTIST_SORT));
            tagModel.setAlbum_sort(tag.getFirst(FieldKey.ALBUM_SORT));
            tagModel.setAmazon_id(tag.getFirst(FieldKey.AMAZON_ID));
            tagModel.setArranger(tag.getFirst(FieldKey.ARRANGER));
            tagModel.setArtist(tag.getFirst(FieldKey.ARTIST));
            tagModel.setArtist_sort(tag.getFirst(FieldKey.ARTIST_SORT));
            tagModel.setArtists(tag.getFirst(FieldKey.ARTISTS));
            tagModel.setBarcode(tag.getFirst(FieldKey.BARCODE));
            tagModel.setBpm(tag.getFirst(FieldKey.BPM));
            tagModel.setCatalog_no(tag.getFirst(FieldKey.CATALOG_NO));
            tagModel.setComment(tag.getFirst(FieldKey.COMMENT));
            tagModel.setComposer(tag.getFirst(FieldKey.COMPOSER));
            tagModel.setComposer_sort(tag.getFirst(FieldKey.COMPOSER_SORT));
            tagModel.setConductor(tag.getFirst(FieldKey.CONDUCTOR));
            tagModel.setCover_art(tag.getFirst(FieldKey.COVER_ART));
            tagModel.setCustom1(tag.getFirst(FieldKey.CUSTOM1));
            tagModel.setCustom2(tag.getFirst(FieldKey.CUSTOM2));
            tagModel.setCustom3(tag.getFirst(FieldKey.CUSTOM3));
            tagModel.setCustom4(tag.getFirst(FieldKey.CUSTOM4));
            tagModel.setCustom5(tag.getFirst(FieldKey.CUSTOM5));
            tagModel.setDisc_no(tag.getFirst(FieldKey.DISC_NO));
            tagModel.setDisc_total(tag.getFirst(FieldKey.DISC_TOTAL));
            tagModel.setDjmixer(tag.getFirst(FieldKey.DJMIXER));
            tagModel.setEncoder(tag.getFirst(FieldKey.ENCODER));
            tagModel.setEngineer(tag.getFirst(FieldKey.ENGINEER));
            tagModel.setFbpm(tag.getFirst(FieldKey.FBPM));
            tagModel.setGenre(tag.getFirst(FieldKey.GENRE));
            tagModel.setGrouping(tag.getFirst(FieldKey.GROUPING));
            tagModel.setIsrc(tag.getFirst(FieldKey.ISRC));
            tagModel.setIs_compilation(tag.getFirst(FieldKey.IS_COMPILATION));
            tagModel.setKey(tag.getFirst(FieldKey.KEY));
            tagModel.setLanguage(tag.getFirst(FieldKey.LANGUAGE));
            tagModel.setLyricist(tag.getFirst(FieldKey.LYRICIST));
            tagModel.setLyrics(tag.getFirst(FieldKey.LYRICS));
            tagModel.setMedia(tag.getFirst(FieldKey.MEDIA));
            tagModel.setMixer(tag.getFirst(FieldKey.MIXER));
            tagModel.setMood(tag.getFirst(FieldKey.MOOD));
            tagModel.setMusicbrainz_artistid(tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID));
            tagModel.setMusicbrainz_disc_id(tag.getFirst(FieldKey.MUSICBRAINZ_DISC_ID));
            tagModel.setMusicbrainz_original_releasE_ID(tag.getFirst(FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID));
            tagModel.setMusicbrainz_releaseartistid(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASEARTISTID));
            tagModel.setMusicbrainz_releaseid(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASEID));
            tagModel.setMusicbrainz_release_country(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY));
            tagModel.setMusicbrainz_release_group_iD(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID));
            tagModel.setMusicbrainz_release_status(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_STATUS));
            tagModel.setMusicbrainz_release_type(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_TYPE));
            tagModel.setMusicbrainz_track_id(tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID));
            tagModel.setMusicbrainz_work_id(tag.getFirst(FieldKey.MUSICBRAINZ_WORK_ID));
            tagModel.setMusicip_id(tag.getFirst(FieldKey.MUSICIP_ID));
            tagModel.setOccasion(tag.getFirst(FieldKey.OCCASION));
            tagModel.setOriginal_album(tag.getFirst(FieldKey.ORIGINAL_ALBUM));
            tagModel.setOriginal_artist(tag.getFirst(FieldKey.ORIGINAL_ARTIST));
            tagModel.setOriginal_lyricist(tag.getFirst(FieldKey.ORIGINAL_LYRICIST));
            tagModel.setOriginal_year(tag.getFirst(FieldKey.ORIGINAL_YEAR));
            tagModel.setQuality(tag.getFirst(FieldKey.QUALITY));
            tagModel.setProducer(tag.getFirst(FieldKey.PRODUCER));
            tagModel.setRating(tag.getFirst(FieldKey.RATING));
            tagModel.setRecord_label(tag.getFirst(FieldKey.RECORD_LABEL));
            tagModel.setRemixer(tag.getFirst(FieldKey.REMIXER));
            tagModel.setScript(tag.getFirst(FieldKey.SCRIPT));
            tagModel.setTags(tag.getFirst(FieldKey.TAGS));
            tagModel.setTempo(tag.getFirst(FieldKey.TEMPO));
            tagModel.setTitle(tag.getFirst(FieldKey.TITLE));
            tagModel.setTitle_sort(tag.getFirst(FieldKey.TITLE_SORT));
            tagModel.setTrack(tag.getFirst(FieldKey.TRACK));
            tagModel.setTrack_total(tag.getFirst(FieldKey.TRACK_TOTAL));
            tagModel.setUrl_discogs_artist_site(tag.getFirst(FieldKey.URL_DISCOGS_ARTIST_SITE));
            tagModel.setUrl_discogs_release_site(tag.getFirst(FieldKey.URL_DISCOGS_RELEASE_SITE));
            tagModel.setUrl_official_artist_site(tag.getFirst(FieldKey.URL_LYRICS_SITE));
            tagModel.setUrl_official_artist_site(tag.getFirst(FieldKey.URL_OFFICIAL_ARTIST_SITE));
            tagModel.setUrl_official_release_site(tag.getFirst(FieldKey.URL_OFFICIAL_RELEASE_SITE));
            tagModel.setUrl_wikipedia_artist_site(tag.getFirst(FieldKey.URL_WIKIPEDIA_ARTIST_SITE));
            tagModel.setUrl_official_release_site(tag.getFirst(FieldKey.URL_WIKIPEDIA_RELEASE_SITE));
            tagModel.setYear(tag.getFirst(FieldKey.YEAR));
            tagModel.setAcoustid_fingerprint(tag.getFirst(FieldKey.ACOUSTID_FINGERPRINT));
            tagModel.setAcoustid_id(tag.getFirst(FieldKey.ACOUSTID_ID));
            tagModel.setCountry(tag.getFirst(FieldKey.COUNTRY));
            return tagModel ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }

    public static ArrayList<Pair<FieldKey,String>> getTagArray(StreamSong song) {
        File file = new File(song.getStreamUrl()) ;
        if (!file.exists()) {
            file = new File(StorageUtil.getStorage().getCasheDir(),  StorageUtil.getStorage().getNameFile(song)) ;
        }
        if (!file.exists()) {
            return null ;
        }
        TagOptionSingleton.getInstance().setAndroid(true);
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            ArrayList<Pair<FieldKey, String>> arrayList = new ArrayList<>();
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ALBUM, tag.getFirst(FieldKey.ALBUM)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ALBUM_ARTIST, tag.getFirst(FieldKey.ALBUM_ARTIST)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ALBUM_ARTIST_SORT, tag.getFirst(FieldKey.ALBUM_ARTIST_SORT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ALBUM_SORT, tag.getFirst(FieldKey.ALBUM_SORT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.AMAZON_ID, tag.getFirst(FieldKey.AMAZON_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ARRANGER, tag.getFirst(FieldKey.ARRANGER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ARTIST, tag.getFirst(FieldKey.ARTIST)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ARTIST_SORT, tag.getFirst(FieldKey.ARTIST_SORT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ARTISTS, tag.getFirst(FieldKey.ARTISTS)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.BARCODE, tag.getFirst(FieldKey.BARCODE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.BPM, tag.getFirst(FieldKey.BPM)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CATALOG_NO, tag.getFirst(FieldKey.CATALOG_NO)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.COMMENT, tag.getFirst(FieldKey.COMMENT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.COMPOSER, tag.getFirst(FieldKey.COMPOSER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.COMPOSER_SORT, tag.getFirst(FieldKey.COMPOSER_SORT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CONDUCTOR, tag.getFirst(FieldKey.CONDUCTOR)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.COVER_ART, tag.getFirst(FieldKey.COVER_ART)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CUSTOM1, tag.getFirst(FieldKey.CUSTOM1)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CUSTOM2, tag.getFirst(FieldKey.CUSTOM2)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CUSTOM3, tag.getFirst(FieldKey.CUSTOM3)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CUSTOM4, tag.getFirst(FieldKey.CUSTOM4)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.CUSTOM5, tag.getFirst(FieldKey.CUSTOM5)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.DISC_NO, tag.getFirst(FieldKey.DISC_NO)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.DISC_TOTAL, tag.getFirst(FieldKey.DISC_TOTAL)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.DJMIXER, tag.getFirst(FieldKey.DJMIXER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ENCODER, tag.getFirst(FieldKey.ENCODER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ENGINEER, tag.getFirst(FieldKey.ENGINEER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.FBPM, tag.getFirst(FieldKey.FBPM)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.GENRE, tag.getFirst(FieldKey.GENRE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.GROUPING, tag.getFirst(FieldKey.GROUPING)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ISRC, tag.getFirst(FieldKey.ISRC)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.IS_COMPILATION, tag.getFirst(FieldKey.IS_COMPILATION)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.KEY, tag.getFirst(FieldKey.KEY)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.LANGUAGE, tag.getFirst(FieldKey.LANGUAGE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.LYRICIST, tag.getFirst(FieldKey.LYRICIST)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.LYRICS, tag.getFirst(FieldKey.LYRICS)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MEDIA, tag.getFirst(FieldKey.MEDIA)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MIXER, tag.getFirst(FieldKey.MIXER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MOOD, tag.getFirst(FieldKey.MOOD)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_ARTISTID, tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_DISC_ID, tag.getFirst(FieldKey.MUSICBRAINZ_DISC_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID, tag.getFirst(FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASEARTISTID, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASEARTISTID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASEID, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASEID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASE_STATUS, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_STATUS)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_RELEASE_TYPE, tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_TYPE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_TRACK_ID, tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICBRAINZ_WORK_ID, tag.getFirst(FieldKey.MUSICBRAINZ_WORK_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.MUSICIP_ID, tag.getFirst(FieldKey.MUSICIP_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.OCCASION, tag.getFirst(FieldKey.OCCASION)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ORIGINAL_ALBUM, tag.getFirst(FieldKey.ORIGINAL_ALBUM)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ORIGINAL_ARTIST, tag.getFirst(FieldKey.ORIGINAL_ARTIST)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ORIGINAL_LYRICIST, tag.getFirst(FieldKey.ORIGINAL_LYRICIST)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ORIGINAL_YEAR, tag.getFirst(FieldKey.ORIGINAL_YEAR)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.QUALITY, tag.getFirst(FieldKey.QUALITY)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.PRODUCER, tag.getFirst(FieldKey.PRODUCER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.RATING, tag.getFirst(FieldKey.RATING)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.RECORD_LABEL, tag.getFirst(FieldKey.RECORD_LABEL)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.REMIXER, tag.getFirst(FieldKey.REMIXER)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.SCRIPT, tag.getFirst(FieldKey.SCRIPT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TAGS, tag.getFirst(FieldKey.TAGS)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TEMPO, tag.getFirst(FieldKey.TEMPO)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TITLE, tag.getFirst(FieldKey.TITLE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TITLE_SORT, tag.getFirst(FieldKey.TITLE_SORT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TRACK, tag.getFirst(FieldKey.TRACK)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.TRACK_TOTAL, tag.getFirst(FieldKey.TRACK_TOTAL)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_DISCOGS_ARTIST_SITE, tag.getFirst(FieldKey.URL_DISCOGS_ARTIST_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_DISCOGS_RELEASE_SITE, tag.getFirst(FieldKey.URL_DISCOGS_RELEASE_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_LYRICS_SITE, tag.getFirst(FieldKey.URL_LYRICS_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_OFFICIAL_ARTIST_SITE, tag.getFirst(FieldKey.URL_OFFICIAL_ARTIST_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_OFFICIAL_RELEASE_SITE, tag.getFirst(FieldKey.URL_OFFICIAL_RELEASE_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_WIKIPEDIA_ARTIST_SITE, tag.getFirst(FieldKey.URL_WIKIPEDIA_ARTIST_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.URL_WIKIPEDIA_RELEASE_SITE, tag.getFirst(FieldKey.URL_WIKIPEDIA_RELEASE_SITE)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.YEAR, tag.getFirst(FieldKey.YEAR)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ACOUSTID_FINGERPRINT, tag.getFirst(FieldKey.ACOUSTID_FINGERPRINT)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.ACOUSTID_ID, tag.getFirst(FieldKey.ACOUSTID_ID)));
            arrayList.add(new Pair<FieldKey, String>(FieldKey.COUNTRY, tag.getFirst(FieldKey.COUNTRY)));
            return arrayList  ;
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        return null ;

    }


}
