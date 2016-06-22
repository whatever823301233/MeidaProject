package com.qiang.meidaproject.biz;

import android.database.Cursor;
import android.media.MediaMetadata;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.qiang.meidaproject.MyApplication;
import com.qiang.meidaproject.entity.MutableMediaMetadata;
import com.qiang.meidaproject.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Qiang on 2016/3/24.
 *
 *  Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class MusicProvider {

    private static final String TAG = LogUtil.makeLogTag(MusicProvider.class);

    private static final String CATALOG_URL = "http://storage.googleapis.com/automotive-media/music.json";

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadata>> mMusicListByGenre;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;

    private final Set<String> mFavoriteTracks;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }


    /**
     * Get music tracks of the given genre
     *
     */
    public Iterable<MediaMetadata> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     *
     */
    public Iterable<MediaMetadata> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadata.METADATA_KEY_ARTIST, query);
    }

    Iterable<MediaMetadata> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadata> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US)
                    .contains(query)) {
                result.add(track.metadata);
            }
        }
        return result;
    }


    /**
     * Return the MediaMetadata for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadata getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public synchronized void updateMusic(String musicId, MediaMetadata metadata) {
        MutableMediaMetadata track = mMusicListById.get(musicId);
        if (track == null) {
            return;
        }

        String oldGenre = track.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
        String newGenre = metadata.getString(MediaMetadata.METADATA_KEY_GENRE);

        track.metadata = metadata;

        // if genre has changed, we need to rebuild the list by genre
        if (!oldGenre.equals(newGenre)) {
            buildListsByGenre();
        }
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogUtil.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadata>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
            List<MediaMetadata> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Cursor cursor = MyApplication.get().getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null,// 字段　没有字段　就是查询所有信息　相当于SQL语句中的　“ * ”
                        null, // 查询条件
                        null, // 条件的对应?的参数
                        MediaStore.Audio.AudioColumns.TITLE);// 排序方式
                if(cursor==null||cursor.getCount()==0){
                    return;
                }
                // 循环输出歌曲的信息
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToNext();
                    MediaMetadata item=buildFromCursor(cursor, null);
                    long duration=item.getLong(MediaMetadata.METADATA_KEY_DURATION);
                    if(duration>180000){
                        String musicId = item.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
                        mMusicListById.put(musicId,new MutableMediaMetadata(musicId, item));
                    }
                }
                cursor.close();
                buildListsByGenre();

                mCurrentState = State.INITIALIZED;
            }
        } catch (JSONException e) {
            LogUtil.e(TAG, e, "Could not retrieve music list");
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private MediaMetadata buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        LogUtil.d(TAG, "Found music track: ", json);

        // Media is stored relative to JSON file
        if (!source.startsWith("http")) {
            source = basePath + source;
        }
        if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        return new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadata.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }



    private MediaMetadata buildFromCursor(Cursor cursor, String basePath) throws JSONException {

        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
        String source = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//音频文件路径+文件名
        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//音频文件时长

        String id = String.valueOf(source.hashCode());
        int trackNumber = cursor.getPosition();
        int totalTrackCount = cursor.getCount();

        return new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadata.METADATA_KEY_GENRE, "Rock\\")
                //.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONObject fetchJSONFromUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}

/**
 *
 *  JSONObject jsonObj = null;//fetchJSONFromUrl(CATALOG_URL);
 String json="{\"music\" : [ \n" +
 "\t{ \"title\" : \"Jazz in Paris\",\n" +
 "\t  \"album\" : \"Jazz & Blues\",\n" +
 "\t  \"artist\" : \"Media Right Productions\",\n" +
 "\t  \"genre\" : \"Jazz & Blues\",\n" +
 "\t  \"source\" : \"Jazz_In_Paris.mp3\",\n" +
 "\t  \"image\" : \"album_art.jpg\",\n" +
 "\t  \"trackNumber\" : 1,\n" +
 "\t  \"totalTrackCount\" : 6,\n" +
 "\t  \"duration\" : 103,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"The Messenger\",\n" +
 "\t  \"album\" : \"Jazz & Blues\",\n" +
 "\t  \"artist\" : \"Silent Partner\",\n" +
 "\t  \"genre\" : \"Jazz & Blues\",\n" +
 "\t  \"source\" : \"The_Messenger.mp3\",\n" +
 "\t  \"image\" : \"album_art.jpg\",\n" +
 "\t  \"trackNumber\" : 2,\n" +
 "\t  \"totalTrackCount\" : 6,\n" +
 "\t  \"duration\" : 132,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Talkies\",\n" +
 "\t  \"album\" : \"Jazz & Blues\",\n" +
 "\t  \"artist\" : \"Huma-Huma\",\n" +
 "\t  \"genre\" : \"Jazz & Blues\",\n" +
 "\t  \"source\" : \"Talkies.mp3\",\n" +
 "\t  \"image\" : \"album_art.jpg\",\n" +
 "\t  \"trackNumber\" : 3,\n" +
 "\t  \"totalTrackCount\" : 6,\n" +
 "\t  \"duration\" : 162,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"On the Bach\",\n" +
 "\t  \"album\" : \"Cinematic\",\n" +
 "\t  \"artist\" : \"Jingle Punks\",\n" +
 "\t  \"genre\" : \"Cinematic\",\n" +
 "\t  \"source\" : \"On_the_Bach.mp3\",\n" +
 "\t  \"image\" : \"album_art.jpg\",\n" +
 "\t  \"trackNumber\" : 4,\n" +
 "\t  \"totalTrackCount\" : 6,\n" +
 "\t  \"duration\" : 66,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"The Story Unfolds\",\n" +
 "\t  \"album\" : \"Cinematic\",\n" +
 "\t  \"artist\" : \"Jingle Punks\",\n" +
 "\t  \"genre\" : \"Cinematic\",\n" +
 "\t  \"source\" : \"The_Story_Unfolds.mp3\",\n" +
 "\t  \"image\" : \"album_art.jpg\",\n" +
 "\t  \"trackNumber\" : 5,\n" +
 "\t  \"totalTrackCount\" : 6,\n" +
 "\t  \"duration\" : 91,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Drop and Roll\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Silent Partner\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Drop_and_Roll.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 1,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 121,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Motocross\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Topher Mohr and Alex Elena\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Motocross.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 2,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 182,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Wish You'd Come True\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"The 126ers\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Wish_You_d_Come_True.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 3,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 169,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Awakening\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Silent Partner\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Awakening.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 4,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 220,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Home\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Letter Box\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Home.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 5,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 213,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Tell The Angels\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Letter Box\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Tell_The_Angels.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 6,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 208,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Hey Sailor\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock\",\n" +
 "\t  \"artist\" : \"Letter Box\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Hey_Sailor.mp3\",\n" +
 "\t  \"image\" : \"album_art_2.jpg\",\n" +
 "\t  \"trackNumber\" : 7,\n" +
 "\t  \"totalTrackCount\" : 7,\n" +
 "\t  \"duration\" : 193,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"Keys To The Kingdom\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock 2\",\n" +
 "\t  \"artist\" : \"The 126ers\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"Keys_To_The_Kingdom.mp3\",\n" +
 "\t  \"image\" : \"album_art_3.jpg\",\n" +
 "\t  \"trackNumber\" : 1,\n" +
 "\t  \"totalTrackCount\" : 2,\n" +
 "\t  \"duration\" : 221,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t},\n" +
 "\t{ \"title\" : \"The Coldest Shoulder\",\n" +
 "\t  \"album\" : \"Youtube Audio Library Rock 2\",\n" +
 "\t  \"artist\" : \"The 126ers\",\n" +
 "\t  \"genre\" : \"Rock\",\n" +
 "\t  \"source\" : \"The_Coldest_Shoulder.mp3\",\n" +
 "\t  \"image\" : \"album_art_3.jpg\",\n" +
 "\t  \"trackNumber\" : 2,\n" +
 "\t  \"totalTrackCount\" : 2,\n" +
 "\t  \"duration\" : 160,\n" +
 "\t  \"site\" : \"https://www.youtube.com/audiolibrary/music\"\n" +
 "\t}\n" +
 "]}";
 jsonObj=new JSONObject(json);
 * */