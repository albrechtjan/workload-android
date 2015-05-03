package com.gmail.konstantin.schubert.workload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class SurveyContentProvider extends ContentProvider{


    public static final String AUTHORITY = "de.tu-dresden.zqa.survey";
    private MainDatabaseHelper mOpenHelper;
    private SQLiteDatabase db;

    private static final String DBNAME = "survey_database";
    private static int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_LECTURES = "CREATE TABLE " +
            "lectures " +
            "(" +
            " _ID INTEGER PRIMARY KEY, " +
            " NAME TEXT" +
            " SEMESTER TEXT" +
            " ISACTIVE BOOL" +
            " STATUS TEXT" +
            " OPERATION TEXT" +
            ")";
    private static final String SQL_CREATE_WORKENTRIES = "CREATE TABLE " +
            "workentries " +                       // Table's name
            "(" +                           // The columns in the table
            " _ID INTEGER PRIMARY KEY, " +
            " HOURS_IN_LECTURE TEXT" +
            " HOURS_FOR_HOMEWORK TEXT" +
            "HOURS_STUDYING TEXT" +
            " STATUS TEXT" +
            " OPERATION TEXT" +
            ")";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, "/get-table" + "/*", TABLE_REQUEST_CODE);
        sURIMatcher.addURI(AUTHORITY, "/get-row" + "/*", ROW_REQUEST_CODE);
        sURIMatcher.addURI(AUTHORITY, "/search" + "/*", TABLE_SEARCH_REQUEST_CODE);
    }



    protected static final class MainDatabaseHelper extends SQLiteOpenHelper {

        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }

        /*
             * Creates the data repository. This is called when the provider attempts to open the
             * repository and SQLite reports that it doesn't exist.
             */
        public void onCreate(SQLiteDatabase db) {
            // Creates the main table
            db.execSQL(SQL_CREATE_LECTURES);
            db.execSQL(SQL_CREATE_WORKENTRIES);
        }



        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        }


    }

    @Override
    public boolean onCreate(){

        mOpenHelper = new MainDatabaseHelper(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri){
        return new String();
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder){
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        db = mOpenHelper.getWritableDatabase();
        return null;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        return 0;
    }

    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs){
        return 0;
    }

}
