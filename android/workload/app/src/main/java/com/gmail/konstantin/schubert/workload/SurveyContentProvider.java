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
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;

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

    private static class SYNC_STATUS {
        public static final int IDLE = 0;
        public static final int PENDING = 1;
        public static final int TRANSACTING = 2;
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
        sURITableTypeMatcher.addURI(AUTHORITY, "/lectures/", LECTURES);
        sURITableTypeMatcher.addURI(AUTHORITY, "/lectures/*", LECTURES);
        sURITableTypeMatcher.addURI(AUTHORITY, "/workentries/", ENTRIES);
        sURITableTypeMatcher.addURI(AUTHORITY, "/workentries/*", ENTRIES);
        sURIOptionMatcher.addURI(AUTHORITY, "*/nosync/", NOSYNC);
        sURIOptionMatcher.addURI(AUTHORITY, "*/stopsync/", STOPSYNC);  // set to idle
        sURIHasIDMatcher.addURI(AUTHORITY, "*/#/", HAS_ID);
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


        if (uriOption==STOPSYNC){
            throw new UnsupportedOperationException();
        }


        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        if (selection != null ) {
            qBuilder.appendWhere(selection);
        }

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
            database.update(qBuilder.getTables(), values, where, null);
            maybeSync(); //TODO: Does this create a deadlock where the calls in maybeSync are waiting for the cursor to be released?
        }
        int i = cursor.getCount();
        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // 1. An update will only work if
        //    - the affected entries are all IDLE, OR
        //    - for all affected entries, the operation is GET, the status is TRANSACTING and the STOPSYNC option is activated.
        //  Otherwise, the update fails.
        // 2. A PATCH to remote is initiated if
        //    - the update worked and neither the NOSYNC nor the STOPSYNC options are active.
        //    This implies that all entries are idle.

        // In case of no update, the return value is -1. No exception is thrown.
        // TODO: Maybe better return an exception?



        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);
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

        // checking if update can be done.
        String is_okay_selection = selection;
        if (is_okay_selection== null){
            is_okay_selection = "";
        }else {
            is_okay_selection+= " AND ";
        }
        is_okay_selection += DB_STRINGS.STATUS + "=" + SYNC_STATUS.IDLE;

        if(uriOption==STOPSYNC){
            is_okay_selection += " OR " + DB_STRINGS.OPERATION +"="+ SYNC_OPERATION.GET;
            is_okay_selection += " AND " + DB_STRINGS.STATUS +"="+ SYNC_STATUS.TRANSACTING;
        }

        String[] columns = {DB_STRINGS._ID};
        Cursor cursor_okay = database.query(table, columns, is_okay_selection, selectionArgs, null, null, null);
        Cursor cursor_all = database.query(table, columns, selection, selectionArgs, null, null, null);
        // Since is_okay_selection is a subset of selection, the sets must be equal
        // if the number of elements in the sets is the same
        if (cursor_okay.getCount() != cursor_all.getCount()){
            return -1;
        }


        if(uriOption==STOPSYNC){
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.IDLE);
        }
        if (uriOption == UriMatcher.NO_MATCH){
            // we do a patch
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.PATCH);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
        }
        int rows_updated = database.update(table, values, selection, null);
        maybeSync();
        return rows_updated;
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
            if (tableType == LECTURES) insert_from_remote_lectures_status = SYNC_STATUS.IDLE;
            if (tableType == ENTRIES) insert_from_remote_workentries_status = SYNC_STATUS.IDLE;
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
            if (tableType == LECTURES) delete_from_remote_lectures_status = SYNC_STATUS.IDLE;
            if (tableType == ENTRIES) delete_from_remote_workentries_status = SYNC_STATUS.IDLE;
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
            if (insert_from_remote_lectures_status == SYNC_STATUS.IDLE)
                insert_from_remote_lectures_status = SYNC_STATUS.PENDING;
            if (delete_from_remote_lectures_status == SYNC_STATUS.IDLE)
                delete_from_remote_lectures_status = SYNC_STATUS.PENDING;
        }
        if (tableType == ENTRIES) {
            if (insert_from_remote_workentries_status == SYNC_STATUS.IDLE)
                insert_from_remote_workentries_status = SYNC_STATUS.PENDING;
            if (delete_from_remote_workentries_status == SYNC_STATUS.IDLE)
                delete_from_remote_workentries_status = SYNC_STATUS.PENDING;
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
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

    /*
    This function figures out what should be synced.
     */
    private void maybeSync(){

        // obviously the decisions in this functions can be sped up by
        // restricting the things the function checks via parameters. For now, we check it all

        // there is a table_sync_status and table_sync_operation for each table
        // also each row has these defined.
        // These values are used to decide what is synced, and how. Finer-grained syncing methods
        // can anchored in this function.

        // VERY rough checks, and we will download everything
        if (insert_from_remote_lectures_status == SYNC_STATUS.PENDING || delete_from_remote_lectures_status == SYNC_STATUS.PENDING ) {
            insert_from_remote_lectures_status = SYNC_STATUS.TRANSACTING;
            delete_from_remote_lectures_status = SYNC_STATUS.TRANSACTING;
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.SYNC_TABLE_ENTRIES_LECTURES);
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }

        if(insert_from_remote_workentries_status == SYNC_STATUS.PENDING || delete_from_remote_workentries_status == SYNC_STATUS.PENDING ) {
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.SYNC_TABLE_ENTRIES_WORKENTRIES);
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
            insert_from_remote_workentries_status = SYNC_STATUS.TRANSACTING;
            delete_from_remote_workentries_status = SYNC_STATUS.TRANSACTING;
        }

        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        String table = getContext().getResources().getString(R.string.lectures_table_name);
        qBuilder.setTables(table);
        qBuilder.appendWhere(DB_STRINGS.STATUS+"="+SYNC_STATUS.PENDING);
        Cursor cursor = qBuilder.query(database, null, null, null, null, null, null);
        List<Integer> lectures_to_get = new ArrayList<>();
        List<Integer> lectures_to_patch_localid = new ArrayList<>();
        while (cursor.moveToNext()){
            int sync_operation = cursor.getInt(cursor.getColumnIndex(DB_STRINGS.OPERATION));
            int id = cursor.getInt(cursor.getColumnIndex(DB_STRINGS._ID));
            if(sync_operation == SYNC_OPERATION.GET){
                lectures_to_get.add(id);
                mark_as_transacting(id, table);
            }
            else if (sync_operation == SYNC_OPERATION.PATCH){
                lectures_to_patch_localid.add(id); // id is same as local-id since the ids are unique and identifiying for lectures across local AND remote
                mark_as_transacting(id, table);
            }
            // possibly add more
        }
        cursor.close();

        qBuilder = new SQLiteQueryBuilder();
        table = getContext().getResources().getString(R.string.workentry_table_name);
        qBuilder.setTables(table);
        qBuilder.appendWhere(DB_STRINGS.STATUS + "=" + SYNC_STATUS.PENDING);
        cursor = qBuilder.query(database, null, null, null, null, null, null);
        List<Integer> entries_to_get_lectureid = new ArrayList<>();
        List<Integer> entries_to_get_year = new ArrayList<>();
        List<Integer> entries_to_get_week = new ArrayList<>();
        List<Integer> entries_to_patch_localid = new ArrayList<>();
        List<Integer> entries_to_post_localid = new ArrayList<>();
        while (cursor.moveToNext()){
            int sync_operation = cursor.getInt(cursor.getColumnIndex(DB_STRINGS.OPERATION));
            int id = cursor.getInt(cursor.getColumnIndex(DB_STRINGS._ID));
            int local_id = cursor.getInt(cursor.getColumnIndex(DB_STRINGS._ID));
            int lecture_id = cursor.getInt(cursor.getColumnIndex(DB_STRINGS_WORKENTRY.LECTURE_ID));
            int year = cursor.getInt(cursor.getColumnIndex(DB_STRINGS_WORKENTRY.YEAR));
            int week = cursor.getInt(cursor.getColumnIndex(DB_STRINGS_WORKENTRY.WEEK));
            if(sync_operation == SYNC_OPERATION.GET){
                entries_to_get_lectureid.add(lecture_id);
                entries_to_get_year.add(year);
                entries_to_get_week.add(week);
                mark_as_transacting(id, table);
            }
            else if (sync_operation == SYNC_OPERATION.PATCH){
                entries_to_patch_localid.add(local_id);
                mark_as_transacting(id, table);
            }
            else if (sync_operation == SYNC_OPERATION.POST){
                entries_to_post_localid.add(local_id);
                mark_as_transacting(id, table);
            }
            // possibly add more
        }
        cursor.close();

        if (!lectures_to_get.isEmpty()){
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.INCREMENTAL_DOWNLOAD_LECTURES);
            syncBundle.putIntArray("IDS", Ints.toArray(lectures_to_get));
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }
        if(!lectures_to_patch_localid.isEmpty()){
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.INCREMENTAL_PATCH_LECTURES);
            syncBundle.putIntArray("LOCAL_IDs", Ints.toArray(lectures_to_patch_localid));
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }

        if (!entries_to_get_lectureid.isEmpty()){
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.INCREMENTAL_DOWNLOAD_ENTRIES);
            syncBundle.putIntArray("LECTURE_IDs", Ints.toArray(entries_to_get_lectureid));
            syncBundle.putIntArray ("YEARs", Ints.toArray(entries_to_get_year));
            syncBundle.putIntArray ("WEEKs", Ints.toArray(entries_to_get_week));
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }
        if(!entries_to_patch_localid.isEmpty()){
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.INCREMENTAL_PATCH_WORKENTRIES);
            syncBundle.putIntArray("LOCAL_IDs", Ints.toArray(entries_to_patch_localid));
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }
        if(!entries_to_post_localid.isEmpty()){
            Bundle syncBundle = new Bundle();
            syncBundle.putInt("SYNC_MODUS", SyncAdapter.SYNC_TASK.INCREMENTAL_POST_WORKENTRIES);
            syncBundle.putIntArray("LOCAL_IDs", Ints.toArray(entries_to_post_localid));
            ContentResolver.requestSync(mAccount, AUTHORITY, syncBundle);
        }
    }

    private void mark_as_transacting(int id, String table){
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(DB_STRINGS.STATUS, SYNC_STATUS.TRANSACTING);
        String selection = "_ID=" + String.valueOf(id);
        database.update(table, values, selection, null);
    }


}
