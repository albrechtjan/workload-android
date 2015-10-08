package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import com.gmail.konstantin.schubert.workload.Lecture;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static class SYNC_TASK {
        public static final int FULL_DOWNLOAD_USERDATA = 0;
        public static final int INCREMENTAL_DOWNLOAD_USERDATA = 1;
        public static final int PUSH_CHANGES = 2;
        public static final int GET_AVAILABLE_LECTURES = 3;
    }

    public final static String TAG = "WorkloadSyncAdapter";
    public final static String baseUrl = "https://survey.zqa.tu-dresden.de/app/workload/";


    ContentResolver mContentResolver;
    RESTResponseProcessor mRestResponseProcessor;
    static AccountManager sAccountManager;
    RestClient mRestClient = new RestClient();



    public SyncAdapter(Context context, boolean autoInitialize) {
        //for backwards compatibility
        this(context,autoInitialize,false);
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.d(TAG, "Initialized");
        mContentResolver = context.getContentResolver();
        mRestResponseProcessor = new RESTResponseProcessor(mContentResolver);
        sAccountManager = AccountManager.get(getContext());
        Log.d(TAG, "got account manager" + sAccountManager.getClass().getCanonicalName());

    }

    private void get_available_lectures(){
        // Update list of lectures with list of available lectures
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders();
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/menu/lectures/all/", headers, null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<Lecture> remoteLectures = mRestResponseProcessor.lectureListFromJson(response);
            mRestResponseProcessor.updateAvailableLectures(remoteLectures);

        }
        catch (Exception e ){
            //TODO
        }


    }


    private void full_download(){
        Log.d(TAG,"Full download");
        get_available_lectures();
        // shares a lot of code with incremental_download
    }

    private void incremental_download(){
        Log.d(TAG, "Incremental download");
        // even with the incremental download of changes, it might be that the change has originated with us, so we have to be careful not to insert things twice

    }

    private void push_changes(){
        Log.d(TAG,"Pushing changes");
    }


    private ArrayList<NameValuePair> buildAuthHeaders() throws android.accounts.OperationCanceledException, android.accounts.AuthenticatorException, java.io.IOException{
        // It's the responsibility of applications to INVALIDATE AUTH TOKENS WHEN THEY STOP WORKING !!!!!
        //so the AccountManager knows it needs to regenerate them.
        Account[] accounts = sAccountManager.getAccountsByType("tu-dresden.de");//TODO: make the string a resource
        AccountManagerFuture<Bundle> future =  sAccountManager.getAuthToken(accounts[0], "session_ID_token", Bundle.EMPTY, true, null, null);
        // I have been over engineering this. For now it is absolutely fine to launch the notification every time.
        // (Maybe we can make it a bit nicer, but that's not priority.)
        // If one day I want to run the sync continuously in background, I need to think of some logic about when I want
        // to notify the user-and when not.
        Bundle bundle = future.getResult();
        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
        if (authToken != null) {
            NameValuePair cookies = new BasicNameValuePair("Cookie",authToken);
            ArrayList<NameValuePair> headers = new ArrayList<>();
            headers.add(cookies);
            return headers;
        }
        else if (intent != null) {
            //TODO: actually handle this case
            return null;
        }else{
            return null;
        }


    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {




        android.os.Debug.waitForDebugger();
        int sync_task = extras.getInt("SYNC_MODUS");
        switch (sync_task) {
            case SYNC_TASK.FULL_DOWNLOAD_USERDATA: {
                full_download();
                break;
            }
            case SYNC_TASK.INCREMENTAL_DOWNLOAD_USERDATA: {
                incremental_download();
                break;
            }
            case SYNC_TASK.PUSH_CHANGES: {
                push_changes();
                break;
            }
            case SYNC_TASK.GET_AVAILABLE_LECTURES: {
                get_available_lectures();
                break;
            }
            default: {
                throw new IllegalArgumentException("specified sync task invalid");
            }
        }


    }

}
