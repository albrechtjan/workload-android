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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static class SYNC_TASK {
        public static final int SYNC_TABLE_ENTRIES_LECTURES = 0;
        public static final int SYNC_TABLE_ENTRIES_WORKENTRIES = 1;
        public static final int INCREMENTAL_DOWNLOAD_LECTURES = 2;
        public static final int INCREMENTAL_DOWNLOAD_ENTRIES = 3;
        public static final int INCREMENTAL_PATCH_LECTURES = 4;
        public static final int INCREMENTAL_PATCH_ENTRIES = 5;
        public static final int INCREMENTAL_POST_ENTRIES = 6;

    }

    public final static String TAG = "WorkloadSyncAdapter";
    public final static String baseUrl = "https://survey.zqa.tu-dresden.de/app/workload/";


    RESTResponseProcessor mRestResponseProcessor;
    static AccountManager sAccountManager;
    RestClient mRestClient = new RestClient();



    public SyncAdapter(Context context, boolean autoInitialize) {
        //for backwards compatibility
        this(context, autoInitialize, false);
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.d(TAG, "Initialized");
        mRestResponseProcessor = new RESTResponseProcessor(context.getContentResolver());
        sAccountManager = AccountManager.get(getContext());
        Log.d(TAG, "got account manager" + sAccountManager.getClass().getCanonicalName());

    }



    // There is a lot of duplicate code and even duplicate downloads in this function.
    // Until I have the pattern fully figured out, I will favor clarity over efficiency.

    private void get_workload_entries(int[] IDs){
        // Update workload entries
        // If local entry is syncing it is not overwritten.
        try {
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/entries/active/", buildAuthHeaders(), null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            reduce list to those that were requested
            TODO            mRestResponseProcessor.updateEntries(remoteEntries);

        }
        catch (Exception e ){
            //TODO
        }
    }

    private void get_lecture_entries(int[] IDs){
        // Update workload entries
        // If local entry is syncing it is not overwritten.
        try {
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/entries/active/", buildAuthHeaders(), null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            reduce list to those that were requested
TODO            mRestResponseProcessor.updateEntries(remoteEntries);
        }
        catch (Exception e ){
            //TODO
        }
    }




    private void sync_table_entries_lectures() {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders();
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/lectures/all/", headers, null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<Lecture> remoteLectures = RESTResponseProcessor.lectureListFromJson(response);
            mRestResponseProcessor.insert_delete_lectures_from_remote(remoteLectures);
        }
        catch (Exception e ){
            //TODO
        }
    }

    private void sync_table_entries_workentries() {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders();
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/entries/active/", headers, null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            mRestResponseProcessor.insert_delete_workloadentries_from_remote(remoteEntries);
        }
        catch (Exception e ){
            //TODO
        }

    }


    private void push_changes(){
    //TODO        send_synching_lectures();
    //TODO        send_synching_workload_entries();
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
//            getContext().startActivity(intent);
            return null;
        }else{
            return null;
        }


    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        int sync_task = extras.getInt("SYNC_MODUS");
        switch (sync_task) {
            case SYNC_TASK.SYNC_TABLE_ENTRIES_WORKENTRIES: {
                sync_table_entries_workentries();
                break;
            }
            case SYNC_TASK.INCREMENTAL_DOWNLOAD_ENTRIES: {
                get_workload_entries(extras.getIntArray("IDS"));
                break;
            }
            case SYNC_TASK.SYNC_TABLE_ENTRIES_LECTURES: {
                sync_table_entries_lectures();
                break;
            }
            case SYNC_TASK.INCREMENTAL_DOWNLOAD_LECTURES: {
                get_lecture_entries(extras.getIntArray("IDS"));
                break;
            }
            case SYNC_TASK.INCREMENTAL_PATCH_ENTRIES: {
                push_changes();
                break;
            }
            default: {
                throw new IllegalArgumentException("specified sync task invalid");
            }
        }


    }

}
