package com.star15.moviesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBAdapt {
    public static final String KEY_ID = "movie_id";
    public static final String KEY_NAME = "title";
    public static final String KEY_ORIGINAL_TITLE = "original_title";
    public static final String KEY_POSTER = "poster";
    public static final String KEY_BACKDROP = "backdrop";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_RELESEDATE = "releasedate";
    public static final String KEY_VOTES = "votes";
    public static final String KEY_RUNTIME = "runtime";
    public static final String DB_NAME = "fav_movies";
    public static final int DB_VER = 1;
    private static final String FAV_CREATE = "create table fav_data(movie_id integer, overview text not null, title text not null, original_title text not null, poster text not null, backdrop text not null, votes integer, releasedate text not null, runtime integer);";
    private static DataBaseHelper DBHelper = null;

    public static void init(Context context) {
        if (DBHelper == null) {

            DBHelper = new DataBaseHelper(context);
        }
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {
        public DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(FAV_CREATE);
            } catch (Exception exception) {
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + "fav_data");
            onCreate(db);
        }
    }

    private static synchronized SQLiteDatabase open() throws SQLException {
        return DBHelper.getWritableDatabase();
    }

    public static void addData(String poster, String Name, String Title, String ReleaseDate, int votes, int movie_id, String Backdrop, String Overview, int runtime) {
        try {
            final SQLiteDatabase db = open();

            ContentValues cVal = new ContentValues();
            cVal.put(KEY_ID, movie_id);
            cVal.put(KEY_NAME, Name);
            cVal.put(KEY_ORIGINAL_TITLE, Title);
            cVal.put(KEY_OVERVIEW, Overview);
            cVal.put(KEY_VOTES, votes);
            cVal.put(KEY_RELESEDATE, ReleaseDate);
            cVal.put(KEY_BACKDROP, Backdrop);
            cVal.put(KEY_POSTER, poster);
            cVal.put(KEY_RUNTIME, runtime);


            db.insert("fav_data", null, cVal);
            db.close(); // Closing database connection
        } catch (Throwable t) {

        }
    }

    public static void delete(int id) {

        String Qur = "DELETE FROM fav_data WHERE movie_id=" + id;

        final SQLiteDatabase db = open();
        db.execSQL(Qur);
        db.close();
    }


    public static boolean getFavData(int id) {
        boolean found;
        String selectQuery = "SELECT * FROM fav_data WHERE movie_id=" + id;
        final SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        if (count > 0) {
            found = true;
        } else {
            found = false;
        }
        cursor.close();
        return found;
    }

    public static boolean datafound() {
        boolean found;
        String qur = "SELECT * FROM fav_data";
        final SQLiteDatabase db = open();
        Cursor c = db.rawQuery(qur, null);
        if (c.getCount() > 0) {
            found = true;
        } else {
            found = false;
        }
        return found;
    }

    public static List<Data> getFav() {
        List<Data> data = new ArrayList<>();
        String qur = "SELECT * FROM fav_data";
        final SQLiteDatabase db = open();
        Cursor c = db.rawQuery(qur, null);
        if (c.moveToFirst()) {
            do {
                Data current = new Data();
                current.movie_id = c.getInt(0);
                current.title = c.getString(2);
                current.original_title = c.getString(3);
                current.overview = c.getString(1);
                current.imgIconUrl = c.getString(4);
                current.backdropURL = c.getString(5);
                current.vote = c.getInt(6);
                current.releaseDate = c.getString(7);
                current.runtime = c.getInt(8);
                data.add(current);
            } while (c.moveToNext());
        }

        c.close();
        return data;
    }

}
