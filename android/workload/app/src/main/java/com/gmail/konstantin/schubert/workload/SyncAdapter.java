package com.gmail.konstantin.schubert.workload;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;



public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static class SYNC_TASK {
        public static final int FULL_DOWNLOAD = 0;
        public static final int INCREMENTAL_DOWNLOAD = 1;
        public static final int PUSH_CHANGES = 2;
    }

    public final static String TAG = "WorkloadSyncAdapter";


    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(TAG,"Initialized");
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();

    }


    private void full_download(){
        Log.d(TAG,"Full download");
        // shares a lot of code with incremental_download
    }

    private void incremental_download(){
        Log.d(TAG,"Increlental download");

    }

    private void push_changes(){
        Log.d(TAG,"Pushing changes");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG,"Performing sync");
        // TODO: what do we do if no extras are passed????
        int sync_task = extras.getInt("SYNC_MODUS");
        switch (sync_task){
            case SYNC_TASK.FULL_DOWNLOAD:
                full_download();
                break;
            case SYNC_TASK.INCREMENTAL_DOWNLOAD:
                incremental_download();
                break;
            case SYNC_TASK.PUSH_CHANGES:
                push_changes();
                break;
        }

    }
}
