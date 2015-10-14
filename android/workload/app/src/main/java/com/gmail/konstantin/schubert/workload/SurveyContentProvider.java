package com.gmail.konstantin.schubert.workload;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.sync.SyncAdapter;

public class SurveyContentProvider extends ContentProvider {

    private static final String TAG = SurveyContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "de.tu-dresden.zqa.survey";
    private MainDatabaseHelper mOpenHelper;

    private static final String DBNAME = "survey_database";
    private static int DATABASE_VERSION = 1;
    private static final int LECTURES = 2;
    private static final int ENTRIES = 4;
    private static final int NOSYNC = 6;
    private static final int STOPSYNC = 7;
    private static final int HAS_ID = 8;


    public static final String ACCOUNT_TYPE = "tu-dresden.de";
    public static final String ACCOUNT = "default_account";
    Account mAccount;


    // there is no point in trying to match the API in the database model
    // because the API is designed to work for certain perspectives on the same data
    // if we want to store data in the database we need to bring it back to a table format
    // Then, when accessing the Content Provider, we need to prepare it again for the
    // view/activity that requests it


    public static class SYNC_OPERATION {
        public static final int NONE = 0;
        public static final int GET = 1;  // == query
        public static final int PATCH = 2;  // == update
        public static final int POST = 3; // == insert
        public static final int DELETE = 4; // == delete
        // Honestly, I think the easiest way is to simply not delete anything, just mark rows as deleted.
        // That's unless something disappears remotely that can only deleted remotely, such as a lecture in the list of
        //  available lectures
    }

    public static class SYNC_STATUS {
        public static final int IDLE = 0;
        public static final int PENDING = 1;
        public static final int TRANSACTING = 2;
        public static final int RETRY = 3;


    }


    public static class DB_STRINGS_LECTURE {
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

    public static class DB_STRINGS_WORKENTRY {
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
            DB_STRINGS_LECTURE.STATUS + " INT DEFAULT 0, " +
            DB_STRINGS_LECTURE.OPERATION + "  INT DEFAULT 0" +
            ")";


    private static final String SQL_CREATE_WORKENTRIES = "CREATE TABLE " +
            "workentries " +                       // Table's name
            "(" +                           // The columns in the table
            DB_STRINGS_WORKENTRY._ID + " INTEGER PRIMARY KEY, " +
            DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.HOURS_STUDYING + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.YEAR + " INT, " +
            DB_STRINGS_WORKENTRY.WEEK + " INT, " +
            DB_STRINGS_WORKENTRY.LECTURE_ID + " INTEGER, " +
            DB_STRINGS_WORKENTRY.STATUS + " INT DEFAULT 0, " +
            DB_STRINGS_WORKENTRY.OPERATION + " INT DEFAULT 0, " +
            "FOREIGN KEY(" + DB_STRINGS_WORKENTRY.LECTURE_ID + ") REFERENCES lectures(" + DB_STRINGS_LECTURE._ID + ")" +
            ")";

    private static final UriMatcher sURITableTypeMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher sURIOptionMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher sURIHasIDMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURITableTypeMatcher.addURI(AUTHORITY, "/lectures/*", LECTURES);
        sURITableTypeMatcher.addURI(AUTHORITY, "/workentries/*", ENTRIES);
        sURIOptionMatcher.addURI(AUTHORITY, "*/nosync/", NOSYNC);
        sURIOptionMatcher.addURI(AUTHORITY, "*/stopsync/", STOPSYNC);
        sURIHasIDMatcher.addURI(AUTHORITY, "*/#/", HAS_ID);
    }

    private int table_sync_status_lectures;
    private int table_sync_status_workentries;
    private int table_sync_operation_lectures;
    private int table_sync_operation_workentries;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MainDatabaseHelper(getContext());
        Log.d(TAG, "Created Database Helper");
        mAccount = CreateSyncAccount(getContext());
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        return true;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case LECTURES:
                id = database.insert("lectures", null, values);
                break;
            case ENTRIES:
                id = database.insert("workentries", null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        maybeSync();

        return Uri.parse(uri + String.valueOf(id));

    }


    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int uriOption = sURIOptionMatcher.match(uri);

        switch (uriOption){
            case UriMatcher.NO_MATCH:
                break;
            case NOSYNC:
                break;
            case STOPSYNC:
                break;
            default:
                break;

        }

        int tableType = sURITableTypeMatcherMatcher.match(uri);
        int hasID = sURIHasIDMatcherMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        if(tableType==LECTURES){
            qBuilder.setTables(R.string.lecture_table_name);
        }

        if(tableType==ENTRIES){
            qBuilder.setTables(R.string.workentry_table_name);
        }

        if (hasID==HAS_ID){
            qBuilder.appendWhere("_ID=" + String.valueOf(ContentUris.parseId(uri)));
        }


        Cursor cursor = qBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        if (uriOption!=NOSYNC){
            // add sync entries
            //TODO: next three lines
//            if(hasID=HAS_ID){
//             // add sync values for the specific entries
//            }
//            else{
                if(tableType==LECTURES){
                    table_sync_operation_lectures = SYNC_OPERATION.GET;
                    table_sync_status_lectures = SYNC_STATUS.PENDING;
                }
                else if (tableType==ENTRIES){
                    table_sync_operation_workentries = SYNC_OPERATION.GET;
                    table_sync_status_workentries = SYNC_STATUS.PENDING;
                }

//            }
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //TODO: figure out if I need this.
        //TODO: Understand if setNotificationUri does also trigger a server sync

        maybeSync(); //TODO: Does this create a deadlock where the calls in maybeSync are waiting for the cursor to be released?

        return cursor;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //delete is always local.
        // If you want to 'delete remote', do an update with SYNC_OPERATION=DELETE

        if (selection != null) {
            throw new IllegalArgumentException("Do not pass selection to delete query. Not supported");
        }

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        String table;

        switch (uriType) {
            case LECTURES_ID:
                table = "lectures";
                selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
                break;
            case ENTRIES_ID:
                table = "workentries";
                selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown or invalid uri." + uri);
        }
        database.delete(table, selection, null);
        getContext().getContentResolver().notifyChange(uri, null, false);

        maybeSync();
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        String table;

        switch (uriType) {
            case LECTURES_ID:
                table = "lectures";
                if (selection != null)  throw new IllegalArgumentException("Do not pass selection when using ID.");
                selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
                break;
            case ENTRIES_ID:
                table = "workentries";
                if (selection != null)  throw new IllegalArgumentException("Do not pass selection when using ID.");
                selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
                break;
            case LECTURES:
                table = "lectures";
                break;
            case ENTRIES:
                table = "workentries";
                break;
            default:
                throw new IllegalArgumentException("Unknown or invalid uri.");
        }

        if (uriOption==NOSYNC){

        }
        else if(uriOption==STOPSYNC){
            // remove

        }


        database.update(table, values, selection, null);

        maybeSync();

        return 1;
    }
//
//    7. Be aware of ContentResolver.notifyChange()
//
//    One tricky thing. ContentResolver.notifyChange() is a function used by ContentProviders to notify Android that the local database has been changed. This serves two functions, first,
//   it will cause cursors following that contenturi to update, and in turn requery and invalidate and redraw a ListView, etc...
    //   TODO: are my view adapters written in a way so the view updates automatically?
//   It's very magical, the database changes and your ListView just updates automatically.
// Awesome. Also, when the database changes, Android will request Sync for you,
// even outside your normal schedule, so that those changes get taken off the device and synced to the server as rapidly as possible. Also awesome.
//
//    There's one edge case though. If you pull from the server, and push an update into the ContentProvider, it will dutifully call notifyChange() and android will go, "Oh, database changes, better put them on the server!" (Doh!) Well written ContentProviders will have some tests to see if the changes came from the network or from the user, and will set the boolean syncToNetwork flag false if so, to prevent this wasteful double-sync. If you're feeding data into a ContentProvider, it behooves you to figure out how to get this working -- Otherwise you'll end up always performing two syncs when only one is needed


    protected static final class MainDatabaseHelper extends SQLiteOpenHelper {

        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }


        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_LECTURES);
            db.execSQL(SQL_CREATE_WORKENTRIES);
        }


        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }


    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {

        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

    private void maybeSync(){

        // obviously the deicisions in this functions can be sped up by
        // restricting the things the function checks via parameters. For now, we check it all

        // there is a table_sync_status and table_sync_operation for each table
        // also each row has these defined.
        // These values are used to decide what is synced, and how. Finer-grained syncing methods
        // can anchored in this function.

        // VERY rough checks, and we will download everything
        if (table_sync_status_lectures == SYNC_STATUS.PENDING ||
                table_sync_operation_workentries==SYNC_STATUS.PENDING){
            if(table_sync_operation_lectures==SYNC_OPERATION.GET ||
                    table_sync_operation_workentries==SYNC_OPERATION.GET) {
                Bundle syncBundle = new Bundle();
                syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.FULL_DOWNLOAD_USERDATA);
                ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
            }
        }


        //TODO: Check if single rows are marked as pending for get


        //TODO: Check if single are marked for update, and then push remote

            boolean performSync = false;
            switch (uriType) {
                case LECTURES:
                    if ( values.get(DB_STRINGS_LECTURE.STATUS)==SYNC_STATUS.PENDING ){
                        performSync = true;
                    }
                    break;
                case ENTRIES:
                    if (values.get(DB_STRINGS_WORKENTRY.STATUS)==SYNC_STATUS.PENDING){
                        performSync = true;
                    }
                    break;
            }

        }


    }


}
