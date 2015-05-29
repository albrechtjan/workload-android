package com.gmail.konstantin.schubert.workload;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class SurveyContentProvider extends ContentProvider{

    private static final String TAG = SurveyContentProvider.class.getSimpleName();


    public static final String AUTHORITY = "de.tu-dresden.zqa.survey";
    private MainDatabaseHelper mOpenHelper;
    private SQLiteDatabase db;

    private static final String DBNAME = "survey_database";
    private static int DATABASE_VERSION = 1;
    private static final int LECTURES = 300;
    private static final int LECTURES_ID = 301;
    private static final int ENTRIES = 400;
    private static final int ENTRIES_ID = 401;

    // there is no point in trying to match the API in the database model
    // because the API is designed to work for certain perspectives on the same data
    // if we want to store data in the database we need to bring it back to a table format
    // Then, when accessing the Content Provider, we need to prepare it again for the
    // view/activity that requests it

    private static final String SQL_CREATE_LECTURES = "CREATE TABLE " +
            "lectures " +
            "(" +
            "_ID INTEGER PRIMARY KEY, " +
            "NAME TEXT, " +
            "SEMESTER TEXT, " +
            "ISACTIVE BOOL, " +
            "STATUS TEXT, " +
            "OPERATION TEXT" +
            ")";
    private static final String SQL_CREATE_WORKENTRIES = "CREATE TABLE " +
            "workentries " +                       // Table's name
            "(" +                           // The columns in the table
            "_ID INTEGER PRIMARY KEY, " +
            "HOURS_IN_LECTURE REAL, " +
            "HOURS_FOR_HOMEWORK REAL," +
            "HOURS_STUDYING REAL," +
            "YEAR INT, " +
            "WEEK INT, " +
            "LECTURE_ID INTEGER, "+
            "STATUS TEXT, " +
            "OPERATION TEXT, " +
            "FOREIGN KEY(LECTURE_ID) REFERENCES lectures(_ID)" +
            ")";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, "/lectures/", LECTURES);
        sURIMatcher.addURI(AUTHORITY, "/lectures/#", LECTURES_ID);
        //I probably should not try to make the database give the lectures for a
        // given week, instead I should just grab all active lectures, and check them all
        // if they are in a given week. That should be sufficiently efficient.
        sURIMatcher.addURI(AUTHORITY,"/workentries/",ENTRIES);
        sURIMatcher.addURI(AUTHORITY,"/workentries/#",ENTRIES_ID);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case LECTURES:
                id = database.insert("lectures", null, values);
                break;
            case ENTRIES:
                id = database.insert("workentries",null,values);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri + String.valueOf(id));

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
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        long id;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        switch (uriType) {
            case LECTURES:
                qBuilder.setTables("lectures");
                break;
            case LECTURES_ID:
                qBuilder.setTables("lectures");
                qBuilder.appendWhere("_ID=" + String.valueOf(ContentUris.parseId(uri)));
                break;
            case ENTRIES:
                qBuilder.setTables("workentries");
                break;
            case ENTRIES_ID:
                qBuilder.setTables("workentries");
                qBuilder.appendWhere("_ID=" + String.valueOf(ContentUris.parseId(uri)));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Cursor cursor = qBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
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
            Log.d(TAG, "creating database: "+SQL_CREATE_LECTURES);
            db.execSQL(SQL_CREATE_LECTURES);
            Log.d(TAG, "created lecture database");
            Log.d(TAG, "creating database: "+SQL_CREATE_WORKENTRIES);
            db.execSQL(SQL_CREATE_WORKENTRIES);
            Log.d(TAG, "created workload entry database");
        }


        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        }

    }

}
