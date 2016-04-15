package com.gmail.konstantin.schubert.workload;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
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


public class SurveyContentProvider extends ContentProvider implements OnAccountsUpdateListener {

    public static final String AUTHORITY = "de.tu-dresden.zqa.survey";
    private static final String TAG = SurveyContentProvider.class.getSimpleName();
    private static final String DBNAME = "survey_database";
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


    // there is no point in trying to match the API in the database model
    // because the API is designed to work for certain perspectives on the same data
    // if we want to store data in the database we need to bring it back to a table format
    // Then, when accessing the Content Provider, we need to prepare it again for the
    // view/activity that requests it
    private static final UriMatcher sURIOptionMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher sURIHasIDMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static int DATABASE_VERSION = 1;

    static {
        sURITableTypeMatcher.addURI(AUTHORITY, "lectures/*/*/", LECTURES);
        sURITableTypeMatcher.addURI(AUTHORITY, "workentries/*/*/", ENTRIES);
        sURIOptionMatcher.addURI(AUTHORITY, "*/sync/*/", SYNC);
        sURIOptionMatcher.addURI(AUTHORITY, "*/null/*/", DO_NOTHING);
        sURIOptionMatcher.addURI(AUTHORITY, "*/stopsync/*/", STOPSYNC);  // set to idle
        sURIOptionMatcher.addURI(AUTHORITY, "*/retrysync/*/", RETRY);  // set to retry
        sURIOptionMatcher.addURI(AUTHORITY, "*/" + SYNC_STEER_COMMAND.MARK_TRANSACTING + "/*/", MARK_TRANSACTING);
        sURIOptionMatcher.addURI(AUTHORITY, "*/get-overwrite/*/", GET_OVERWRITE);
        sURIHasIDMatcher.addURI(AUTHORITY, "*/*/#/", HAS_ID);
        sURIHasIDMatcher.addURI(AUTHORITY, "*/*/any/", HAS_NO_ID);
    }

    Account mAccount;
    private MainDatabaseHelper mOpenHelper;

    public static Account GetOrCreateSyncAccount(Context context) {
        // Create the account type and default account
        // Get an instance of the Android account manager
        AccountManager accountManager = AccountManager.get(context);

        Account[] accounts = accountManager.getAccountsByType("tu-dresden.de");
        if (accounts.length != 0) {
            Log.d(TAG, "Returning existing account");
            return accounts[0];
        } else {
            Account account = new Account("Uni-Account", "tu-dresden.de");
            boolean status = accountManager.addAccountExplicitly(account, null, null);
            if (!status) {
                throw new Error("Unable to find account");
            } else {
                Log.d(TAG, "Created new account");
            }
            return account;
        }
    }

    public static void syncWithUrgency(Context context) {
        Log.d(TAG, "requesting urgent sync");
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountManager.get(context).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, settingsBundle);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MainDatabaseHelper(getContext());
        Log.d(TAG, "Created Database Helper");
        mAccount = GetOrCreateSyncAccount(getContext());
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        // I am not sure what setsyncautomatically does. The documentation seems to be quite incomplete.
        // It seems to me that this function activates or de-activates the switch under
        // Settings->Accounts-> TU Dresden which allows or disallows syncs.
        // These syncs can then either be triggered by ContentResovler.requestSync()
        // (as I am currently doing it) or via the Google Cloud Messaging API which might be a better solution
        // -> TODO

        AccountManager.get(getContext()).addOnAccountsUpdatedListener(this, null, false);

        // Run on server data change
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
        if (tableType == UriMatcher.NO_MATCH || hasID == UriMatcher.NO_MATCH || uriOption == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(uri.toString());
        }


        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        if (tableType == LECTURES)
            qBuilder.setTables(getContext().getString(R.string.lectures_table_name));
        if (tableType == ENTRIES)
            qBuilder.setTables(getContext().getString(R.string.workentry_table_name));

        if (hasID == HAS_ID) {
            qBuilder.appendWhere(DB_STRINGS._ID + "=" + String.valueOf(ContentUris.parseId(uri)));
        }


        Cursor cursor = qBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int tableType = sURITableTypeMatcher.match(uri);
        int hasID = sURIHasIDMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);
        if (tableType == UriMatcher.NO_MATCH || hasID == UriMatcher.NO_MATCH || uriOption == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException();
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        if (hasID == HAS_ID) {
            if (selection == null) {
                selection = "";
            } else {
                selection += " AND ";
            }
            selection += DB_STRINGS._ID + "=" + String.valueOf(ContentUris.parseId(uri));
        }
        String table;
        if (tableType == LECTURES) {
            table = getContext().getResources().getString(R.string.lectures_table_name);
        } else {
            table = getContext().getResources().getString(R.string.workentry_table_name);
        }

        //TODO: we restrict UPDATE to a single row. This is only for now, might want to change this.
        String[] columns = {DB_STRINGS._ID, DB_STRINGS.OPERATION, DB_STRINGS.STATUS};
        Cursor cursor_all = database.query(table, columns, selection, selectionArgs, null, null, null);
        if (cursor_all.getCount() > 1) {
            throw new IllegalArgumentException("You cannot update more than one row at once." + uri.toString() + " " + selection + " " + cursor_all.getCount());
        }


        cursor_all.moveToFirst();
        int status = cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.STATUS));
        int operation = cursor_all.getInt(cursor_all.getColumnIndex(DB_STRINGS.OPERATION));

        if (uriOption == GET_OVERWRITE) {
            if (status != SYNC_STATUS.IDLE) {
                // do not overwrite
                return -1;
            }
        } else if (uriOption == SYNC) {
            if (status == SYNC_STATUS.IDLE || status == SYNC_STATUS.RETRY) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
                values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.POST);
            } else if ((status == SYNC_STATUS.TRANSACTING || status == SYNC_STATUS.PENDING) && operation == SYNC_OPERATION.POST) {
                // I hope this works
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
                values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.POST);
            } else {
                Log.w(TAG, "Could not mark row as pending sync");
            }
        } else if (uriOption == STOPSYNC) {
            if (status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.IDLE);
            } else {
                Log.d(TAG, "Cannot mark row as idle. Current status is not TRANSACTING, but " + status);
            }
        } else if (uriOption == RETRY) {
            if (status == SYNC_STATUS.TRANSACTING) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.RETRY);
            } else {
                Log.d(TAG, "Cannot mark row as RETRY. Current status is not TRANSACTING, but " + status);
            }
        } else if (uriOption == MARK_TRANSACTING) {
            if (status == SYNC_STATUS.PENDING || status == SYNC_STATUS.RETRY) {
                values.put(DB_STRINGS.STATUS, SYNC_STATUS.TRANSACTING);
            } else {
                Log.d(TAG, "Cannot mark row as transacting. Current status is not PENDING or RETRY, but " + status);
            }
        } else {
            throw new IllegalArgumentException("Illegal uriOption.");
        }

        int rows_updated = 1;
        try {
            rows_updated = database.update(table, values, selection, null);
        } catch (java.lang.IllegalArgumentException e) {
            Log.d(TAG, "Logging exception: " + e.toString());
        }
        if (uriOption == SYNC) {
            ContentResolver.requestSync(mAccount, AUTHORITY, new Bundle());
        }
        Log.d(TAG, "Updated " + rows_updated + " rows.");
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rows_updated;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Operation succeeds if row does not yet exist locally.
        // Otherwise, -1 is returned.
        // If the STOPSYNC option is passed, it is assumed that the remote end
        // has been successfully checked for inserted entries, and the
        // insert_from_remote_*_status variables are set accordingly.
        //
        int tableType = sURITableTypeMatcher.match(uri);
        int uriOption = sURIOptionMatcher.match(uri);

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        String table;
        if (tableType == LECTURES) {
            table = getContext().getResources().getString(R.string.lectures_table_name);
        } else {
            table = getContext().getResources().getString(R.string.workentry_table_name);
            if (workentry_exists(database, values)) {
                throw new IllegalArgumentException("You are trying to insert an existing work entry");
            }
        }
        if (uriOption == SYNC) {
            // we do a post
            values.put(DB_STRINGS.OPERATION, SYNC_OPERATION.POST);
            values.put(DB_STRINGS.STATUS, SYNC_STATUS.PENDING);
        }
        long id = database.insert(table, null, values);
        ContentResolver.requestSync(mAccount, AUTHORITY, new Bundle());
        getContext().getContentResolver().notifyChange(uri, null, false);
        return Uri.parse(uri + String.valueOf(id));

    }

    private boolean workentry_exists(SQLiteDatabase db, ContentValues values) {
        String[] cols = {DB_STRINGS._ID};
        String where = DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + values.get(DB_STRINGS_WORKENTRY.LECTURE_ID);
        where += " AND " + DB_STRINGS_WORKENTRY.YEAR + "=" + values.get(DB_STRINGS_WORKENTRY.YEAR);
        where += " AND " + DB_STRINGS_WORKENTRY.WEEK + "=" + values.get(DB_STRINGS_WORKENTRY.WEEK);
        Cursor c = db.query(getContext().getResources().getString(R.string.workentry_table_name), cols, where, null, null, null, null);
        return c.getCount() > 0;

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

        if (hasID == HAS_ID) {
            if (selection == null) selection = "_ID=" + String.valueOf(ContentUris.parseId(uri));
            else selection += " AND _ID=" + String.valueOf(ContentUris.parseId(uri));
        }


        String table;
        if (tableType == LECTURES)
            table = getContext().getResources().getString(R.string.lectures_table_name);
        else table = getContext().getResources().getString(R.string.workentry_table_name);

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int rows_affected = database.delete(table, selection, null);
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rows_affected;
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // this doesn't seem to get called when it should get called...
        Log.d(TAG, "onAccountsUpdated called");
        this.syncWithUrgency(getContext());

    }

    public static class SYNC_OPERATION {
        //        public static final int PATCH = 2;  // == update
        // we do not distinguish between patch and post any more, we always post
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

    public static class SYNC_STEER_COMMAND {
        public final static String SYNC = "sync";
        public final static String MARK_TRANSACTING = "mark-transacting";
        public final static String STOPSYNC = "stopsync";
        public final static String RETRYSYNC = "retrysync";
        public final static String GET_OVERWRITE = "get-overwrite";
    }

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

    public static boolean isMasterSyncSettingTrue(){
        // this function checks if the master sync setting is set

        // In SurveyContentProvider.onCreate(), the ContentResolver.setSyncAutomatically(True)
        // method is called. This activates the setting that syncs for the specific account are allowed
        // and can be triggered by ContentResolver.requestSync(). This can be manually changed by the user
        // under Settings->Accounts->TU Dresden. We do not check for this currently.
        // However, this setting is overrriden by a global ("Master") sync setting, which can be found in the
        // action bar menu under Settings->Accounts.
        return ContentResolver.getMasterSyncAutomatically();
    }

    public static boolean isAccountSyncSettingTrue(Context context){
        // checks if the account-specific sync setting under Settings->Account->TU Dresden is set.

        // In SurveyContentProvider.onCreate(), the ContentResolver.setSyncAutomatically(True)
        // method is called. This activates the setting that syncs for the specific account are allowed
        // and can be triggered by ContentResolver.requestSync(). This can be manually changed by the user
        // under Settings->Accounts->TU Dresden. We do not check for this currently.
        // However, this setting is overrriden by a global ("Master") sync setting, which can be found in the
        // action bar menu under Settings->Accounts.
        return ContentResolver.getSyncAutomatically(AccountManager.get(context).getAccountsByType("tu-dresden.de")[0], AUTHORITY);
    }

}
