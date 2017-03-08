package com.example.lenovo.movies.data;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.lenovo.movies.Movie;

public class MovieDbHelper extends SQLiteOpenHelper{
    public final String LOG_TAG = MovieDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movies.db";

    public static final String TABLE_NAME = "movies";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRAILER_KEY = "key";
    public static final String COLUMN_TRAILER_NAME = "name";
    public static final String COLUMN_POSTER = "poster";
    public static final String COLUMN_MOVIE_ID = "movie_id";//movie id
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_RELEASE_YEAR = "release_year";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_REVIEWS = "reviews";

    public MovieDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        Log.e(LOG_TAG, "Database created or opened... ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                COLUMN_POSTER + " TEXT NOT NULL, " +
                COLUMN_REVIEWS + " TEXT, " +
                COLUMN_TRAILER_KEY + " TEXT, " +
                COLUMN_TRAILER_NAME + " TEXT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                COLUMN_RATE + " REAL NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        Log.e(LOG_TAG, "Table created... ");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void addMovies(Movie movie){

        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVIE_ID, movie.id);
        values.put(COLUMN_OVERVIEW, movie.OVERVIEW);
        values.put(COLUMN_POSTER, movie.poster);
        values.put(COLUMN_RATE, movie.VOTEAVERAGE);
        values.put(COLUMN_RELEASE_YEAR, movie.RELEASEYEAR);
        values.put(COLUMN_TITLE, movie.TITLE);
        values.put(COLUMN_REVIEWS, movie.review);
        values.put(COLUMN_TRAILER_KEY, movie.Trailer_key);
        values.put(COLUMN_TRAILER_NAME, movie.Trailer_name);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
        Log.e(LOG_TAG, movie.TITLE + " movie inserted... ");

    }


    public void deleteMovies(String title){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_TITLE + "=\"" + title + "\";");
        Log.e(LOG_TAG, title + " movie deleted... ");

    }
    public Cursor readMovies(SQLiteDatabase db){
        Cursor cursor;
        String[] data = {
                COLUMN_POSTER,
                COLUMN_TITLE,
                COLUMN_OVERVIEW,
                COLUMN_RELEASE_YEAR,
                COLUMN_RATE,
                COLUMN_MOVIE_ID,
                COLUMN_TRAILER_NAME,
                COLUMN_TRAILER_KEY,
                COLUMN_REVIEWS
        };
        cursor = db.query(TABLE_NAME,data,null,null,null,null,null);
        return cursor;
    }
    public boolean isFaveorite(SQLiteDatabase db,int id){
        String Query = "Select * from " + TABLE_NAME + " where " + COLUMN_MOVIE_ID + " = " + id;
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            return false;
        }
        return true;
    }
}
