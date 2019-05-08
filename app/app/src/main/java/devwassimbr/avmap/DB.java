package devwassimbr.avmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class DB extends SQLiteOpenHelper {

    public static final String AUDIO_ID="audio_id";
    public static final String AUDIO_TITLE="audio_title";
    public static final String AUDIO_IMG="audio_img";
    public static final String AUDIO_ARTIST="audio_artist";
    public static final String AUDIO_DATA="audio_data";
    public static final String AUDIO_DATE="audio_date";
    public static final String AUDIO_TABLE ="AudioTable";
    public static final String CREATE_AUDIO_TABLE ="CREATE TABLE IF NOT EXISTS "+ AUDIO_TABLE +"("+
            AUDIO_ID+" INTEGER,"+
            AUDIO_TITLE+" TEXT," +
            AUDIO_ARTIST+" TEXT,"+
            AUDIO_DATA+" TEXT,"+
            AUDIO_DATE+" INTEGER,"+
            AUDIO_IMG+" TEXT)";

    public static final String PLAYLIST_ID="playlist_id";
    public static final String PLAYLIST_AUDIO_TABLE ="PlaylistAudio";
    public static final String CREATE_PLAYLIST_AUDIO_TABLE ="CREATE TABLE IF NOT EXISTS "+ PLAYLIST_AUDIO_TABLE +"("+
            PLAYLIST_ID+" INTEGER,"+
            AUDIO_ID+" INTEGER)";

    public static final String PLAYLIST_TABLE="Playlist";
    public static final String PLAYLIST_NAME="playlist_name";
    public static final String PLAYLIST_TYPE="playlist_type";
    public static final String CREATE_PLAYLIST_TABLE="CREATE TABLE IF NOT EXISTS "+ PLAYLIST_TABLE +"("+
            PLAYLIST_ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"+
            PLAYLIST_NAME+" TEXT," +
            PLAYLIST_TYPE+" INTEGER)";

    public DB(Context context, String name) {
        super(context, name, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PLAYLIST_TABLE);
        db.execSQL(CREATE_PLAYLIST_AUDIO_TABLE);
        db.execSQL(CREATE_AUDIO_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_AUDIO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + AUDIO_TABLE);
        onCreate(db);
    }

    public int createPlaylist(Playlist playlist){
        // add list of [ X , Y ] where x is playlist id , y is audio id
        for (Playlist temp:getPlaylistByType(playlist.getType())
             ) {
            if(temp.getName().equals(playlist.getName())) {
                updatePlaylist(temp.getID(), playlist.getList());
                return temp.getID();
            }
        }
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME,playlist.getName());
        values.put(PLAYLIST_TYPE,playlist.getType());
        int ID = (int) db.insert(PLAYLIST_TABLE, null, values);
        if(!playlist.getList().isEmpty()) {
            for (Audio track:playlist.getList()) {
                addPlaylistTrack(track.getID(),ID);
            }
        }
        return ID;
    }
    public void removePlaylist(int playlistID){
        Log.e(TAG, "removePlaylist: "+playlistID);
        SQLiteDatabase db = this.getWritableDatabase();
        for (Audio toRemove:getPlaylist(playlistID).getList()
             ) {
                removePlaylistTrack(playlistID,toRemove.getID());
        }
        db.delete(PLAYLIST_TABLE,PLAYLIST_ID+"= ?",new String[] { String.valueOf(playlistID)});
    }
    public void updatePlaylist(int playlistID,ArrayList<Audio> list){
        Log.e("Updated","playlist :"+playlistID);
        ArrayList<Audio> temp =getPlaylist(playlistID).getList();
        if(temp!= null)
        for (Audio track : temp
             ) {
                removePlaylistTrack(playlistID,track.getID());
        }

        for (Audio track :list
             ) {
                addPlaylistTrack(track.getID(),playlistID);
        }
    }
    public boolean getFavorite(int TrackID){
        for (Audio temp:getFavoritePlaylist().getList()
             ) {
                if(temp.getID()==TrackID)
                    return true;
        }
        return false;
    }
    public void setFavorite(int TrackID){
            addPlaylistTrack(TrackID,getFavoritePlaylist().getID());
    }
    public void removeFavorite(int TrackID){
        removePlaylistTrack(getFavoritePlaylist().getID(),TrackID);
    }

    public int getPlaylistCounter(int playlistID){
        return getPlaylist(playlistID).getList().size();
    }
    public ArrayList<Playlist> getPlaylistSearch(String keyword){

        ArrayList<Playlist> playlists= new ArrayList<>();
        String selectPlaylistQuery = "SELECT  * FROM "+ PLAYLIST_TABLE+" WHERE "+PLAYLIST_NAME+" LIKE '%"+keyword+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {

                playlists.add(getPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID))));
            }
            while (c.moveToNext());
        }c.close();

        return playlists;
    }
    public Playlist getAudioSearch(String keyword){
        Playlist playlist=new Playlist("TEMPO",-1,MainActivity.TEMPO_PLAYLIST_TYPE,new ArrayList<Audio>());
        String selectPlaylistQuery = "SELECT  * FROM "+ AUDIO_TABLE+" WHERE "+AUDIO_ARTIST+" LIKE '%"+keyword+"%' OR "+AUDIO_TITLE+" LIKE '%"+keyword+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist.getList().add(getAudio(c.getInt(c.getColumnIndex(AUDIO_ID))));
          }
            while (c.moveToNext());
        }c.close();
        Log.e("getAudioSearch","Found "+playlist.getList().size()+" song for keyword :"+keyword);
        return playlist;
    }
    public Playlist getTemporaryPlaylist(){
        Playlist playlist=null;
        String selectQuery = "SELECT * FROM "+PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.TEMPO_PLAYLIST_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist=getPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID)));
            }
            while (c.moveToNext());
        }c.close();
        if(playlist==null) {
            playlist=new Playlist("Temporary", -1, MainActivity.TEMPO_PLAYLIST_TYPE, new ArrayList<Audio>());
            playlist.setID(createPlaylist(playlist));

        }
        return playlist;
    }
    public Playlist getHistoryPlaylist(){
        Playlist playlist=null;
        String selectQuery = "SELECT * FROM "+PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.HISTORY_PLAYLIST_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist=getPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID)));
            }
            while (c.moveToNext());
        }c.close();
        if(playlist==null) {
            playlist=new Playlist("History", -1, MainActivity.HISTORY_PLAYLIST_TYPE, new ArrayList<Audio>());
            playlist.setID(createPlaylist(playlist));

        }
        return playlist;
    }
    public void addHistory(Audio toAdd){
        int id=getHistoryPlaylist().getID();
        for (Audio track: getTracksFromPlaylist(id)
             ) {
            if(track.getID()==toAdd.getID()){
                removePlaylistTrack(id,toAdd.getID());
                break;
            }
        }
        addPlaylistTrack(toAdd.getID(),id);
    }

    public void addTemporary(int TrackID){
        addPlaylistTrack(TrackID,getTemporaryPlaylist().getID());
    }
    public void removeTemporary(int TrackID){
        removePlaylistTrack(getTemporaryPlaylist().getID(),TrackID);
    }
    public void removeAllTemporary(){
        int Id=getTemporaryPlaylist().getID();
        for (Audio track:
        getTemporaryPlaylist().getList()) {
            removePlaylistTrack(Id,track.getID());
        }
    }
    public Playlist getFavoritePlaylist(){
        Playlist playlist=null;
        String selectQuery = "SELECT * FROM "+PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.FAVORITE_PLAYLIST_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist=getPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID)));
            }
            while (c.moveToNext());
        }c.close();
        if(playlist==null) {
            playlist=new Playlist("Favorite", -1, MainActivity.FAVORITE_PLAYLIST_TYPE, new ArrayList<Audio>());
            playlist.setID(createPlaylist(playlist));

        }
        return playlist;
    }
    public ArrayList<Playlist> buildArtistsPlaylists(){
        ArrayList<Playlist> playlists= new ArrayList<>();
        String selectPlaylistQuery = "SELECT DISTINCT "+AUDIO_ARTIST+" FROM "+ AUDIO_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                Playlist temp=new Playlist(c.getString(c.getColumnIndex(AUDIO_ARTIST)),0,MainActivity.ARTISTS_PLAYLIST_TYPE,null);
                temp.setList(getAudioByArtist(temp.getName()));
                temp.setID(createPlaylist(temp));
                playlists.add(temp);
            }
            while (c.moveToNext());
        }c.close();

        return playlists;
    }
    public ArrayList<Playlist> getArtistsPlaylists(){
        ArrayList<Playlist> playlists= new ArrayList<>();  // todo everything from PLAYLIST TYPE ARTISTS
        String selectPlaylistQuery = "SELECT * FROM "+PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.ARTISTS_PLAYLIST_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                Playlist temp =new Playlist(c.getString(c.getColumnIndex(PLAYLIST_NAME)),c.getInt(c.getColumnIndex(PLAYLIST_ID)),MainActivity.ARTISTS_PLAYLIST_TYPE,null);
                temp.setList(getTracksFromPlaylist(temp.getID()));
                playlists.add(temp);
            }
            while (c.moveToNext());
        }c.close();

        return playlists;
    }
    public ArrayList<Playlist> buildYearsPlaylists(){
        ArrayList<Playlist> playlists= new ArrayList<>();
        String selectPlaylistQuery = "SELECT DISTINCT "+AUDIO_DATE+" FROM "+ AUDIO_TABLE +" ORDER BY "+AUDIO_DATE+" DESC;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                Playlist temp=new Playlist(c.getString(c.getColumnIndex(AUDIO_DATE)),0,MainActivity.YEARS_PLAYLIST_TYPE,null);
                temp.setList(getAudioByYear(c.getInt(c.getColumnIndex(AUDIO_DATE))));
                temp.setID(createPlaylist(temp));
                playlists.add(temp);
            }
            while (c.moveToNext());
        }c.close();

        return playlists;
    }
    public ArrayList<Playlist> getYearsPlaylists(){
        ArrayList<Playlist> playlists= new ArrayList<>();  // todo everything from PLAYLIST TYPE ARTISTS
        String selectPlaylistQuery = "SELECT * FROM "+PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.YEARS_PLAYLIST_TYPE+" ORDER BY "+PLAYLIST_NAME+" DESC;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                Playlist temp =new Playlist(c.getString(c.getColumnIndex(PLAYLIST_NAME)),c.getInt(c.getColumnIndex(PLAYLIST_ID)),MainActivity.YEARS_PLAYLIST_TYPE,null);
                temp.setList(getTracksFromPlaylist(temp.getID()));
                playlists.add(temp);
            }
            while (c.moveToNext());
        }c.close();

        return playlists;
    }
    public ArrayList<Audio> getAudioByArtist(String artist){
        ArrayList<Audio> playlist= null;
        String selectPlaylistQuery = "SELECT * FROM "+ AUDIO_TABLE+" WHERE "+AUDIO_ARTIST+" = \""+artist+"\"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            playlist=new ArrayList<>();
            do {

                playlist.add(getAudio(c.getInt(c.getColumnIndex(AUDIO_ID))));
            }
            while (c.moveToNext()) ;
        }c.close();
        return playlist;
    }
    public ArrayList<Audio> getAudioByYear(int year){
        ArrayList<Audio> playlist= null;
        String selectPlaylistQuery = "SELECT * FROM "+ AUDIO_TABLE+" WHERE "+AUDIO_DATE+" = "+year;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            playlist=new ArrayList<>();
            do {

                playlist.add(getAudio(c.getInt(c.getColumnIndex(AUDIO_ID))));
            }
            while (c.moveToNext()) ;
        }c.close();
        return playlist;
    }
    public Playlist buildNewReleasesPlaylist(){
        Playlist playlist= new Playlist("New Releases",0,MainActivity.NEW_RELEASES_PLAYLIST_TYPE,getNewestAudio());
       playlist.setID(createPlaylist(playlist));
        return playlist;
    }
    public Playlist getNewReleasesPlaylist(){
        Playlist playlist=new Playlist();// todo : bad allocation
        String selectPlaylistQuery = "SELECT * FROM "+ PLAYLIST_TABLE+" WHERE "+PLAYLIST_TYPE+" = "+MainActivity.NEW_RELEASES_PLAYLIST_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist = new Playlist(c.getString(c.getColumnIndex(PLAYLIST_NAME)),c.getInt(c.getColumnIndex(PLAYLIST_ID)),MainActivity.NEW_RELEASES_PLAYLIST_TYPE,getTracksFromPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID))));
            }
            while (c.moveToNext()) ;
        }c.close();
        return  playlist;
    }
    public boolean AudioTracksExists(){
        String selectPlaylistQuery = "SELECT * FROM "+ AUDIO_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
            return true;
            }
            while (c.moveToNext()) ;
        }c.close();
        return false;
    }
    public ArrayList<Audio> getNewestAudio(){
        ArrayList<Audio> playlist= new ArrayList<>();
        String selectPlaylistQuery = "SELECT * FROM "+ AUDIO_TABLE+" ORDER BY "+AUDIO_DATE+" DESC;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            playlist=new ArrayList<>();
            do {

                playlist.add(getAudio(c.getInt(c.getColumnIndex(AUDIO_ID))));
            }
            while (c.moveToNext()) ;
        }c.close();
        return playlist;
    }
    public long addPlaylistTrack(int trackID,int playlistID){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AUDIO_ID,trackID);
        values.put(PLAYLIST_ID,playlistID);
        return db.insert(PLAYLIST_AUDIO_TABLE,null,values);
    }
    public void removePlaylistTrack(int playlistID,int trackID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PLAYLIST_AUDIO_TABLE,PLAYLIST_ID+"= ? AND "+AUDIO_ID+"= ?",new String[] { String.valueOf(playlistID),String.valueOf(trackID)});

    }

    public ArrayList<Audio> getTracksFromPlaylist(int playlistID){
        Log.e("DB","GETTING track from PLAYLIST id : "+playlistID);

        ArrayList<Audio> playlist= new ArrayList<>();
        String selectPlaylistQuery = "SELECT * FROM "+ PLAYLIST_AUDIO_TABLE+" WHERE "+ PLAYLIST_ID +" = "+playlistID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);


        if(c!=null && c.moveToFirst()){
            do {

                playlist.add(getAudio(c.getInt(c.getColumnIndex(AUDIO_ID))));
            }
            while (c.moveToNext()) ;
        }c.close();
        return playlist;
    }
    public Playlist getPlaylist(int playlistID){
        Log.e("DB","GETTING PLAYLIST id : "+playlistID);
        Playlist playlist =new Playlist("",0,0,null);
        String selectPlaylistQuery = "SELECT * FROM "+ PLAYLIST_TABLE+ " WHERE "+PLAYLIST_ID+" = "+playlistID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlist.setName(c.getString(c.getColumnIndex(PLAYLIST_NAME)));
                playlist.setType(c.getInt(c.getColumnIndex(PLAYLIST_TYPE)));
                playlist.setList(getTracksFromPlaylist(playlistID));
                playlist.setID(c.getInt(c.getColumnIndex(PLAYLIST_ID)));
            }
            while (c.moveToNext()) ;
        }c.close();
        return playlist;
    }
    public ArrayList<Playlist> getPlaylistByType(int TYPE){
        ArrayList<Playlist> playlists =new ArrayList<>();
        String selectPlaylistQuery = "SELECT * FROM "+ PLAYLIST_TABLE+ " WHERE "+PLAYLIST_TYPE+" = "+TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);
        if(c!=null && c.moveToFirst()){
            do {
                playlists.add(new Playlist(c.getString(c.getColumnIndex(PLAYLIST_NAME)),c.getInt(c.getColumnIndex(PLAYLIST_ID)),TYPE,getTracksFromPlaylist(c.getInt(c.getColumnIndex(PLAYLIST_ID)))));
            }
            while (c.moveToNext()) ;
        }c.close();
        Log.e("PLAYLIST BY TYPE","GOT "+playlists.size()+" playlists type : "+TYPE);
        return playlists;
    }
    Audio CACHE_AUDIO;
    public Audio getAudio(int trackID){
        Log.e("DB","GETTING AUDIO id : "+trackID);
        Audio track= null;
        String selectPlaylistQuery = "SELECT * FROM "+ AUDIO_TABLE+ " WHERE "+ AUDIO_ID +" = "+trackID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectPlaylistQuery, null);


        if(c!=null && c.moveToFirst()){
            do {

                track=new Audio(c.getString(c.getColumnIndex(AUDIO_DATA)),c.getString(c.getColumnIndex(AUDIO_TITLE)),c.getString(c.getColumnIndex(AUDIO_IMG)),c.getString(c.getColumnIndex(AUDIO_ARTIST)),c.getInt(c.getColumnIndex(AUDIO_ID)),c.getInt(c.getColumnIndex(AUDIO_DATE)));
            }
            while (c.moveToNext()) ;
        } c.close();

        return track;
    }
    public long addAudio(Audio toAdd){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AUDIO_TITLE,toAdd.getTitle());
        values.put(AUDIO_ARTIST,toAdd.getArtist());
        values.put(AUDIO_IMG,toAdd.getImage());
        values.put(AUDIO_ID,toAdd.getID());
        values.put(AUDIO_DATE,toAdd.getDate());
        values.put(AUDIO_DATA,toAdd.getData());
        return db.insert(AUDIO_TABLE,null,values);
    }

    public void removeAudio(int trackID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AUDIO_TABLE,AUDIO_ID+"= ? ",new String[] {String.valueOf(trackID)});
    }
    public void flushAUDIO(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ AUDIO_TABLE);
    }



}
