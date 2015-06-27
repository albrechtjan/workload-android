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


    // http://android-restful-pattern.blogspot.de/
    //https://stackoverflow.com/questions/9112658/google-io-rest-design-pattern-finished-contentprovider-and-stuck-now
//
//    Database columns used to store transitional state, with valid values where indicated:
//    status: http method or new row
//      GET - query
//      POST - insert
//      PUT - update
//      DELETE - delete
//      NEW - a new row retrieve by a GET request
//    operation: the transactional state
//      0 - PENDING
//      1 - RETRY
//      2 - IN_PROGRESS
//      3 - COMPLETE
//    result: HTTP status code TODO:Add this!!!!

    public static class DB_STRINGS_LECTURE{
        public static final String _ID = "_id";
        public static final String NAME = "NAME";
        public static final String STARTYEAR = "STARTYEAR";
        public static final String STARTWEEK = "STARTWEEK";
        public static final String ENDYEAR = "ENDYEAR";
        public static final String ENDWEEK = "ENDWEEK";
        public static final String SEMESTER = "SEMESTER";
        public static final String ISACTIVE = "ISACTIVE";
        public static final String STATUS = "STATUS";
        public static final String OPERATION = "OPERATION";

    }

    public static class DB_STRINGS_WORKENTRY{
        public static final String _ID = "_id";
        public static final String HOURS_IN_LECTURE = "HOURS_IN_LECTURE";
        public static final String HOURS_FOR_HOMEWORK = "HOURS_FOR_HOMEWORK";
        public static final String HOURS_STUDYING = "HOURS_STUDYING";
        public static final String YEAR = "YEAR";
        public static final String WEEK = "WEEK";
        public static final String LECTURE_ID = "LECTURE_ID";
        public static final String STATUS = "STATUS";
        public static final String OPERATION = "OPERATION";

    }

    private static final String SQL_CREATE_LECTURES = "CREATE TABLE " +
            "lectures " +
            "(" +
            DB_STRINGS_LECTURE._ID + " INTEGER PRIMARY KEY, " +
            DB_STRINGS_LECTURE.NAME + " TEXT, " +
            DB_STRINGS_LECTURE.STARTYEAR + " INT, " +
            DB_STRINGS_LECTURE.STARTWEEK + " INT, " +
            DB_STRINGS_LECTURE.ENDYEAR + " INT, " +
            DB_STRINGS_LECTURE.ENDWEEK + " INT, " +
            DB_STRINGS_LECTURE.SEMESTER + " TEXT, " +
            DB_STRINGS_LECTURE.ISACTIVE + " BOOL, " +
            DB_STRINGS_LECTURE.STATUS + " TEXT, " +
            DB_STRINGS_LECTURE.OPERATION + " TEXT" +
            ")";



    private static final String SQL_CREATE_WORKENTRIES = "CREATE TABLE " +
            "workentries " +                       // Table's name
            "(" +                           // The columns in the table
            DB_STRINGS_WORKENTRY._ID + " INTEGER PRIMARY KEY, " +
            DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE + " REAL, " +
            DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK + " REAL," +
            DB_STRINGS_WORKENTRY.HOURS_STUDYING + " REAL," +
            DB_STRINGS_WORKENTRY.YEAR + " INT, " +
            DB_STRINGS_WORKENTRY.WEEK + " INT, " +
            DB_STRINGS_WORKENTRY.LECTURE_ID + " INTEGER, "+
            DB_STRINGS_WORKENTRY.STATUS + " TEXT, " +
            DB_STRINGS_WORKENTRY.OPERATION + " TEXT, " +
            "FOREIGN KEY("+DB_STRINGS_WORKENTRY.LECTURE_ID+") REFERENCES lectures("+DB_STRINGS_LECTURE._ID+")" +
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
                break;
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
