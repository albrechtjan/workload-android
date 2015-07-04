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

    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d("SyncAdapter","BAAAAAAA");
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();

    }

    //TODO:https://developer.android.com/training/sync-adapters/running-sync-adapter.html
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {


        // get items that need to be synched


        //TODO: do we also always perform a get of the full dataset? That seems excessive!
        // TODO ... yet at the same time, we have to make sure out database gets loaded when the app is first
        // TODO ... started and it must be up to date with remote changes

        // sync them


        // the call back shall be the processor

    }
}
