package io.awesome.gagtube.database.playlist.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static io.awesome.gagtube.database.playlist.model.PlaylistEntity.PLAYLIST_NAME;
import static io.awesome.gagtube.database.playlist.model.PlaylistEntity.PLAYLIST_TABLE;

@Entity(tableName = PLAYLIST_TABLE,
        indices = {@Index(value = {PLAYLIST_NAME})})
public class PlaylistEntity {
    final public static String PLAYLIST_TABLE = "playlists";
    final public static String PLAYLIST_ID = "uid";
    final public static String PLAYLIST_NAME = "name";
    final public static String PLAYLIST_THUMBNAIL_URL = "thumbnail_url";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PLAYLIST_ID)
    private long uid = 0;

    @ColumnInfo(name = PLAYLIST_NAME)
    private String name;

    @ColumnInfo(name = PLAYLIST_THUMBNAIL_URL)
    private String thumbnailUrl;

    public PlaylistEntity(String name, String thumbnailUrl) {
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
