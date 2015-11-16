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
import java.util.Set;

public class SurveyContentProvider extends ContentProvider {

    private static final String TAG = SurveyContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "de.tu-dresden.zqa.survey";
    private MainDatabaseHelper mOpenHelper;

    private static final String DBNAME = "survey_database";
    private static int DATABASE_VERSION = 1;
    //TODO: Use enums.
    private static final int LECTURES = 2;
    private static final int ENTRIES = 4;
    private static final int GET_OVERWRITE = 4;
    private static final int STOPSYNC = 7;
    private static final int SYNC = 8;
    private static final int RETRY = 9;
    private static final int MARK_TRANSACTING = 12;
    private static final int DO_NOTHING = 13;
    private static final int HAS_ID = 10;
    private static final int HAS_NO_ID = 11;


    public static final String ACCOUNT_TYPE = "tu-dresden.de";
    public static final String ACCOUNT = "default_account";
    Account mAccount;


    // there is no point in trying to match the API in the database model
    // because the API is designed to work for certain perspectives on the same data
    // if we want to store data in the database we need to bring it back to a table format
    // Then, when accessing the Content Provider, we need to prepare it again for the
    // view/activity that requests it


    public static class SYNC_OPERATION {
        public static final int PATCH = 2;  // == update
        public static final int POST = 3; // == insert
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
        public static final String NAME = "NAME";
        public static final String STARTYEAR = "STARTYEAR";
        public static final String STARTWEEK = "STARTWEEK";
        public static final String ENDYEAR = "ENDYEAR";
        public static final String ENDWEEK = "ENDWEEK";
        public static final String SEMESTER = "SEMESTER";
        public static final String ISACTIVE = "ISACTIVE";
    }
    public static class DB_STRINGS {
        public static final String _ID = "_id";
        public static final String STATUS = "STATUS";
        public static final String OPERATION = "OPERATION";
    }


    public static class DB_STRINGS_WORKENTRY {
        public static final String HOURS_IN_LECTURE = "HOURS_IN_LECTURE";
        public static final String HOURS_FOR_HOMEWORK = "HOURS_FOR_HOMEWORK";
        public static final String HOURS_STUDYING = "HOURS_STUDYING";
        public static final String YEAR = "YEAR";
        public static final String WEEK = "WEEK";
        public static final String LECTURE_ID = "LECTURE_ID";
    }

    public static class SYNC_STEER_COMMAND{
        public final static String SYNC = "sync";
        public final static String MARK_TRANSACTING = "mark-transacting";
        public final static String STOPSYNC = "stopsync";
        public final static String RETRYSYNC = "retrysync";
        public final static String GET_OVERWRITE = "get-overwrite";
    }


    private static final String SQL_CREATE_LECTURES = "CREATE TABLE " +
            "lectures " +
            "(" +
            DB_STRINGS._ID + " INTEGER PRIMARY KEY, " +
            DB_STRINGS_LECTURE.NAME + " TEXT, " +
            DB_STRINGS_LECTURE.STARTYEAR + " INT, " +
            DB_STRINGS_LECTURE.STARTWEEK + " INT, " +
            DB_STRINGS_LECTURE.ENDYEAR + " INT, " +
            DB_STRINGS_LECTURE.ENDWEEK + " INT, " +
            DB_STRINGS_LECTURE.SEMESTER + " TEXT, " +
            DB_STRINGS_LECTURE.ISACTIVE + " BOOL, " +
            DB_STRINGS.STATUS + " INT DEFAULT 0, " +
            DB_STRINGS.OPERATION + "  INT DEFAULT 0" +
            ")";

    private static final String SQL_CREATE_WORKENTRIES = "CREATE TABLE " +
            "workentries " +                       // Table's name
            "(" +                           // The columns in the table
            DB_STRINGS._ID + " INTEGER PRIMARY KEY, " +
            DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.HOURS_STUDYING + " REAL DEFAULT 0," +
            DB_STRINGS_WORKENTRY.YEAR + " INT, " +
            DB_STRINGS_WORKENTRY.WEEK + " INT, " +
            DB_STRINGS_WORKENTRY.LECTURE_ID + " INTEGER, " +
            DB_STRINGS.STATUS + " INT DEFAULT 0, " +
            DB_STRINGS.OPERATION + " INT DEFAULT 0, " +
            "FOREIGN KEY(" + DB_STRINGS_WORKENTRY.LECTURE_ID + ") REFERENCES lectures(" + DB_STRINGS._ID + ")" +
            ")";

    private static final UriMatcher sURITableTypeMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher sURIOptionMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher sURIHasIDMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURITableTypeMatcher.addURI(AUTHORITY, "/lectures/*/*/", LECTURES);
        sURITableTypeMatcher.addURI(AUTHORITY, "/workentries/*/*/", ENTRIES);
        sURIOptionMatcher.addURI(AUTHORITY, "/*/sync/*/", SYNC);
        sURIOptionMatcher.addURI(AUTHORITY, "/*/null/*/", DO_NOTHING);
        sURIOptionMatcher.addURI(AUTHORITY, "/*/stopsync/*/", STOPSYNC);  // set to idle
        sURIOptionMatcher.addURI(AUTHORITY, "/*/retrysync/*/", RETRY);  // set to retry
        sURIOptionMatcher.addURI(AUTHORITY, "/*/"+SYNC_STEER_COMMAND.MARK_TRANSACTING+"/*/", MARK_TRANSACTING);
        sURIOptionMatcher.addURI(AUTHORITY, "/*/get-overwrite/*/", GET_OVERWRITE);
        sURIHasIDMatcher.addURI(AUTHORITY, "/*/*/#/", HAS_ID);
        sURIHasIDMatcher.addURI(AUTHORITY, "/*/*/any/", HAS_NO_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MainDatabaseHelper(getContext());
        Log.d(TAG, "Created Database Helper");
        mAccount = CreateSyncAccount(getContext());
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
//        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true  );
        return true;
    }



    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);
        if (tableType == UriMatcher.NO_MATCH || hasID == UriMatcher.NO_MATCH || uriOption == UriMatcher.NO_MATCH){
            throw new IllegalArgumentException(uri.toString());
        }


        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        if(tableType==LECTURES) qBuilder.setTables(getContext().getString(R.string.lectures_table_name));
        if(tableType==ENTRIES) qBuilder.setTables(getContext().getString(R.string.workentry_table_name));

        if (hasID==HAS_ID){
            qBuilder.appendWhere(DB_STRINGS._ID + "=" + String.valueOf(ContentUris.parseId(uri)));
        }


        Cursor cursor = qBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        maybeSync();
        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);
        if (tableType == UriMatcher.NO_MATCH || hasID == UriMatcher.NO_MATCH || uriOption == UriMatcher.NO_MATCH){
            throw new IllegalArgumentException();
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        if (hasID==HAS_ID){
            if (selection == null){
                selection = "";
            }else {
                selection += " AND ";
            }
            selection += DB_STRINGS._ID + "=" + String.valueOf(ContentUris.parseId(uri));
        }
        String table;
        if (tableType == LECTURES){
            table = getContext().getResources().getString(R.string.lectures_table_name);
        }else{
            table = getContext().getResources().getString(R.string.workentry_table_name);
        }

        //TODO: we restrict UPDATE to a single row. This is only for now, might want to change this.
        String[] columns = {DB_STRINGS._ID, DB_STRINGS.OPERATION, DB_STRINGS.STATUS};
        Cursor cursor_all = database.query(table, columns, selection, selectionArgs, null, null, null);
        if (cursor_all.getCount() > 1 ){
            throw new IllegalArgumentException("You cannot update more than one row at once." + uri.toString() + " " + selection + " " +cursor_all.getCount());
        }


        cursor_all.moveToFirst();
        int status =   cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.STATUS));
        int operation =   cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.OPERATION));

        if(uriOption==GET_OVERWRITE){
            if(status!=SYNC_STATUS.IDLE) {
                // do not overwrite
                return -1;
            }
        }
        else if(uriOption==SYNC){
            if(status==SYNC_STATUS.IDLE || status==SYNC_STATUS.RETRY){
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
                if(status==SYNC_STATUS.IDLE) {
                    // only in this case, the operation can be changed.
                    values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.PATCH);
                }
            }
        }
        else if (uriOption==STOPSYNC){
            if(status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.IDLE);
            }
        }else if(uriOption==RETRY) {
            if (status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.RETRY);
            }
        }else if (uriOption==MARK_TRANSACTING){
            if(status == SYNC_STATUS.PENDING){
                values.put(DB_STRINGS.STATUS,SYNC_STATUS.TRANSACTING);
            }
        }
        else{
            throw new IllegalArgumentException("Illegal uriOption. Maybe you wanted to use /get-overwrite/ ?");
        }


        int rows_updated = database.update(table, values, selection, null);
        maybeSync();
        return rows_updated;
    }

    private boolean valuesHaveData(ContentValues values){
        // figure out if we want to update actual data
        Set<String> keys = values.keySet();
        keys.remove(DB_STRINGS.OPERATION);
        keys.remove(DB_STRINGS.STATUS);
        return !keys.isEmpty();
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Operation succeeds if row does not yet exist locally.
        // Otherwise, -1 is returned.
        // If row already exists remotely, it will instead be updated.
        // If the STOPSYNC option is passed, it is assumed that the remote end
        // has been successfully checked for inserted entries, and the
        // insert_from_remote_*_status variables are set accordingly.
        //
        int tableType = sURITableTypeMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        String table;
        if (tableType == LECTURES){
            table = getContext().getResources().getString(R.string.lectures_table_name);
        }else{
            table = getContext().getResources().getString(R.string.workentry_table_name);
            if (workentry_exists(database,values)){
                throw new IllegalArgumentException("You are trying to insert an existing work entry");
            }
        }
        if (uriOption==SYNC){
            // we do a post
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.POST);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
        }
        long id = database.insert(table, null, values);
        maybeSync();
        return Uri.parse(uri + String.valueOf(id));

    }

    private boolean workentry_exists(SQLiteDatabase db, ContentValues values){
        String[] cols = {DB_STRINGS._ID};
        String where = DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + values.get(DB_STRINGS_WORKENTRY.LECTURE_ID);
        where += " AND " + DB_STRINGS_WORKENTRY.YEAR + "=" + values.get(DB_STRINGS_WORKENTRY.YEAR);
        where += " AND " + DB_STRINGS_WORKENTRY.WEEK + "=" + values.get(DB_STRINGS_WORKENTRY.WEEK);
        Cursor c = db.query(getContext().getResources().getString(R.string.workentry_table_name), cols, where, null, null, null, null);
        return c.getCount()>0;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Rows are only deleted locally only. No sync is performed.
        // If row still exists remote, it will likely be re-created
        // If the STOPSYNC option is passed, it is assumed that the remote end
        // has been successfully checked for deleted entries, and the
        // delete_from_remote_*_status variables are set accordingly.

        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);

        if (hasID==HAS_ID){
            if (selection == null) selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
            else selection += " AND _ID=" + String.valueOf(ContentUris.parseId(uri));
        }


        String table;
        if (tableType == LECTURES) table = getContext().getResources().getString(R.string.lectures_table_name);
        else table = getContext().getResources().getString(R.string.workentry_table_name);

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int rows_affected = database.delete(table, selection, null);
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rows_affected;
    }



    //TODO: replace comparisons of Integer objects with equals!
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
            Log.d(TAG, "tried to create an account, but the account either exists or some error occured.");
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

    private void maybeSync(){
        Log.d(TAG, "requesting sync");
        ContentResolver.requestSync(mAccount, AUTHORITY, new Bundle());
    }



}
