package com.gmail.konstantin.schubert.workload;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.auth.AuthenticationException;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static class SYNC_TASK {
        public static final int FULL_DOWNLOAD = 0;
        public static final int INCREMENTAL_DOWNLOAD = 1;
        public static final int PUSH_CHANGES = 2;
    }

    public final static String TAG = "WorkloadSyncAdapter";


    ContentResolver mContentResolver;
    static AccountManager sAccountManager;

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
        android.os.Debug.waitForDebugger();
        mContentResolver = context.getContentResolver();
        sAccountManager = AccountManager.get(getContext());



    }


    private void full_download(){
        Log.d(TAG,"Full download");
        // shares a lot of code with incremental_download
    }

    private void incremental_download(){
        Log.d(TAG,"Incremental download");
        // even with the incremental download of changes, it might be that the change has originated with us, so we have to be careful not to insert things twice

    }

    private void push_changes(){
        Log.d(TAG,"Pushing changes");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        //Many servers support some notion of an authentication token, which can be used to authenticate a request to the server without sending the user's actual password.
// (Auth tokens are normally created with a separate request which does include the user's credentials.)
// AccountManager can generate auth tokens for applications, so the application doesn't need to handle passwords directly.
// Auth tokens are normally reusable and cached by the account manager, but must be refreshed periodically.
// It's the responsibility of applications to INVALIDATE AUTH TOKENS WHEN THEY STOP WORKING !!!!!
//so the AccountManager knows it needs to regenerate them.
        Log.d(TAG,"start");
        if (sAccountManager == null) {
            Log.w(TAG, "maccoutmanager is definitely null");
        }else{
            Log.w(TAG, "accountmanager is not null");
        }
        Log.d(TAG,"test");
        Log.d(TAG, sAccountManager.getClass().getCanonicalName());
        Account[] accounts = sAccountManager.getAccountsByType("tu-dresden.de");
        // I would like to change ths account type identifier, but I need to do it at least also in the SurveyContentProvider and maybe also other places.
        // Better create a resource for it, but at a time when I am running and continously testing.
        sAccountManager.getAuthToken(accounts[0], "session_ID_token", Bundle.EMPTY, true, null, null);
        // For now let's just create a notification when login is needed.
        // TODO: In future force user to login directly via callback thread if no login ever happened before.
        //Assuming there is only one account of this type, maybe we should check this?
        // which activity should we pass?

        try {
            Log.d(TAG, "Performing sync");
            // TODO: what do we do if no extras are passed????
            int sync_task = extras.getInt("SYNC_MODUS");
            switch (sync_task) {
                case SYNC_TASK.FULL_DOWNLOAD: {
                    full_download();
                    break;
                }
                case SYNC_TASK.INCREMENTAL_DOWNLOAD: {
                    incremental_download();
                    break;
                }
                case SYNC_TASK.PUSH_CHANGES: {
                    push_changes();
                    break;
                }
                default: {
                    incremental_download();
                    //doing a gentle update
                }
            }
            throw new AuthenticationException(); // just for fun, always throw
        }
        catch (AuthenticationException e){
            sAccountManager.invalidateAuthToken("tu-dresden.de", "session_ID_token<-??????");
        }
    }
}
