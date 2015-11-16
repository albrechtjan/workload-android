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

import java.util.List;
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
    private static final int NOSYNC = 6;
    private static final int STOPSYNC = 7;
    private static final int SYNC = 8;
    private static final int RETRY = 9;
    private static final int HAS_ID = 10;
    private static final int HAS_NO_ID = 11;


    public static final String ACCOUNT_TYPE = "tu-dresden.de";
    public static final String ACCOUNT = "default_account";
    Account mAccount;

    public int getInsert_from_remote_lectures_status() {
        return insert_from_remote_lectures_status;
    }

    public void setInsert_from_remote_lectures_status(int insert_from_remote_lectures_status) {
        this.insert_from_remote_lectures_status = insert_from_remote_lectures_status;
    }

    public int getDelete_from_remote_lectures_status() {
        return delete_from_remote_lectures_status;
    }

    public void setDelete_from_remote_lectures_status(int delete_from_remote_lectures_status) {
        this.delete_from_remote_lectures_status = delete_from_remote_lectures_status;
    }

    public int getInsert_from_remote_workentries_status() {
        return insert_from_remote_workentries_status;
    }

    public void setInsert_from_remote_workentries_status(int insert_from_remote_workentries_status) {
        this.insert_from_remote_workentries_status = insert_from_remote_workentries_status;
    }

    public int getDelete_from_remote_workentries_status() {
        return delete_from_remote_workentries_status;
    }

    public void setDelete_from_remote_workentries_status(int delete_from_remote_workentries_status) {
        this.delete_from_remote_workentries_status = delete_from_remote_workentries_status;
    }


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
        public final static String NOSYNC = "nosync";
        public final static String STOPSYNC = "stopsync";
        public final static String RETRYSYNC = "retrysync";
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
        sURIOptionMatcher.addURI(AUTHORITY, "/*/nosync/*/", NOSYNC);
        sURIOptionMatcher.addURI(AUTHORITY, "/*/stopsync/*/", STOPSYNC);  // set to idle
        sURIOptionMatcher.addURI(AUTHORITY, "/*/retrysync/*/", RETRY);  // set to idle
        sURIHasIDMatcher.addURI(AUTHORITY, "/*/*/#/", HAS_ID);
        sURIHasIDMatcher.addURI(AUTHORITY, "/*/*/any/", HAS_NO_ID);
    }

    // These flags indicate if the table is being checked for possible inserts or deletes.
    private int insert_from_remote_lectures_status;
    private int delete_from_remote_lectures_status;
    private int insert_from_remote_workentries_status;
    private int delete_from_remote_workentries_status;

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

        // 1. A query will never fail.
        // 2. Any queried entry causes a remote GET on this entry IF the entry is idle.
        // 3. Querying all rows causes a check for inserts/deletes that occurred remotely.
        // 4. Querying with a selection where nothing is found causes a check for remote inserts.
        // 4. If a remote get or a check for remote inserts fails, it goes back to IDLE.
        // 4. the /nosync/ parameter prevents any remote operations.

        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);
        if (tableType == UriMatcher.NO_MATCH || hasID == UriMatcher.NO_MATCH || uriOption == UriMatcher.NO_MATCH){
            throw new IllegalArgumentException(uri.toString());
        }


        if (uriOption==STOPSYNC){
            throw new UnsupportedOperationException();
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

        if (uriOption != NOSYNC) {
            // we sync
            // but only idle entries can be GET-ed
            String where = DB_STRINGS.STATUS + "=" + SYNC_STATUS.IDLE;
            if(hasID == HAS_ID){
                where += " AND " + DB_STRINGS._ID + "=" + String.valueOf(ContentUris.parseId(uri));
            }
            if (selection != null){
                where += " AND " + selection;
            }

            if (hasID!=HAS_ID && selection==null) {
                // querying all rows causes a check for inserts and deletes
                setFlagsForRowChecks(tableType);
            }else{
                if(cursor.getCount()==0){
                    // querying with a selection where nothing is found causes a check for inserts
                    // (and deletes, the implementation is a bit more conservative than the pattern
                    // requires it.
                    setFlagsForRowChecks(tableType);
                }
            }



            ContentValues values = new ContentValues(2);
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.GET);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
            int rows = database.update(qBuilder.getTables(), values, where, null);
            maybeSync();
        }

        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //    The STOPSYNC option can change a TRANSACTING row to IDLE, otherwise it fails and -1 is returned.
        //    The NOSYNC option always fails for update (is this a good idea?)

        //    The SYNC option can change an IDLE or RETRY row to TRANSACTING, otherwise it is ignored.
        //    The RETRY option can change a TRANSACTING row to RETRY, otherwise it is ignored.
        //


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

        // we restrict UPDATE to a single row
        String[] columns = {DB_STRINGS._ID, DB_STRINGS.OPERATION, DB_STRINGS.STATUS};
        Cursor cursor_all = database.query(table, columns, selection, selectionArgs, null, null, null);
        if (cursor_all.getCount() > 1 ){
            throw new IllegalArgumentException("You cannot update more than one row at once.");
        }
        cursor_all.moveToFirst();
        int status =   cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.STATUS));
        int operation =   cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.OPERATION));

        if(uriOption==STOPSYNC && valuesHaveData(values)){
            // we seem to be returning from a get to the remote
            if(status==SYNC_STATUS.TRANSACTING && operation==SYNC_OPERATION.GET){
                // this is the expected case,
            }
            else if(status==SYNC_STATUS.IDLE) {
                //we can also write to the row
            }else{
                // do not overwrite
                return -1;
            }
        }

        if(uriOption!=SYNC){
            if (values.keySet().contains(DB_STRINGS.OPERATION)){
                throw new UnsupportedOperationException("Unless you are initiating a new sync, " +
                        "you must pass sync operation type in the values.");
            }
            if(values.getAsInteger(DB_STRINGS.OPERATION)!=operation){
                // the operation we are trying to stop is not the one currently ongoing.
            }
            if(status!=SYNC_STATUS.TRANSACTING) {
//                throw new UnsupportedOperationException("You cannot do a stopsync if the line is not transacting");
                return -1;
            }
            else{
                values.put(DB_STRINGS.STATUS,SYNC_STATUS.IDLE);
            }
        }
        else if(uriOption==NOSYNC){

        }
        else if(uriOption==SYNC){
            if(status == SYNC_STATUS.RETRY || status==SYNC_STATUS.IDLE){
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
            }
        }else if(uriOption==RETRY){
            if(status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.RETRY);
            }
        }


        if(uriOption==STOPSYNC){
            if(status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.IDLE);
            }
        }
        if (uriOption == SYNC){
            //TODO: Do this only if current status is IDLE or RETRY??!?!?
            // we do a patch
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.PATCH);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
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

        if (uriOption == STOPSYNC){
            //TODO: Do this only if variables are TRANSACTING
            if (tableType == LECTURES) setInsert_from_remote_lectures_status(SYNC_STATUS.IDLE);
            if (tableType == ENTRIES) setInsert_from_remote_workentries_status(SYNC_STATUS.IDLE);
        }

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        String table;
        if (tableType == LECTURES){
            table = getContext().getResources().getString(R.string.lectures_table_name);
        }else{
            table = getContext().getResources().getString(R.string.workentry_table_name);
        }
        if (uriOption != NOSYNC && uriOption!=STOPSYNC){
            // we do a post
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.POST);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
        }
        long id = database.insert(table, null, values);
        maybeSync();
        return Uri.parse(uri + String.valueOf(id));

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

        if (uriOption == STOPSYNC){
            //TODO: Do this only if variables are TRANSACTING
            if (tableType == LECTURES) setDelete_from_remote_lectures_status(SYNC_STATUS.IDLE);
            if (tableType == ENTRIES) setDelete_from_remote_workentries_status(SYNC_STATUS.IDLE);
        }

        String table;
        if (tableType == LECTURES) table = getContext().getResources().getString(R.string.lectures_table_name);
        else table = getContext().getResources().getString(R.string.workentry_table_name);

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int rows_affected = database.delete(table, selection, null);
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rows_affected;
    }

    private void setFlagsForRowChecks(int tableType){
        if (tableType == LECTURES) {
            if (getInsert_from_remote_lectures_status() == SYNC_STATUS.IDLE)
                setInsert_from_remote_lectures_status(SYNC_STATUS.PENDING);
            if (getDelete_from_remote_lectures_status() == SYNC_STATUS.IDLE)
                setDelete_from_remote_lectures_status(SYNC_STATUS.PENDING);
        }
        if (tableType == ENTRIES) {
            if (getInsert_from_remote_workentries_status() == SYNC_STATUS.IDLE)
                setInsert_from_remote_workentries_status(SYNC_STATUS.PENDING);
            if (getDelete_from_remote_workentries_status() == SYNC_STATUS.IDLE)
                setDelete_from_remote_workentries_status(SYNC_STATUS.PENDING);
        }
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
