package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;

import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SyncAdapter extends AbstractThreadedSyncAdapter {


    public final static String TAG = "SyncAdapter";
    public final static String baseUrl = "https://survey.zqa.tu-dresden.de/app/workload/";
    static AccountManager sAccountManager;
    RESTResponseProcessor mRestResponseProcessor;
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


    private void get_table_entries_lectures(Account account) throws IOException, AuthenticatorException {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null, account);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl + "api/lectures/all/", headers, null);

            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            Log.d(TAG, response.substring(0, 50) + "...");
            List<Lecture> remoteLectures = RESTResponseProcessor.lectureListFromJson(response);
            mRestResponseProcessor.update_lectures_from_remote(remoteLectures);
        } catch (IOException e) {
            throw new AuthenticatorException(e);
        } catch (Exception e) {
            throw new IOException();
        }
    }

    private void get_table_entries_workentries(Account account) throws IOException, AuthenticatorException {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null, account);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl + "api/entries/active/", headers, null);

            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            Log.d(TAG, response.substring(0, 50) + "...");
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            mRestResponseProcessor.update_workloadentries_from_remote(remoteEntries);
        } catch (IOException e) {
            throw new AuthenticatorException(e);
        } catch (Exception e) {
            throw new IOException();
        }

    }


    private void post_workentry(WorkloadEntry workloadEntry, Account account) throws IOException, AuthenticatorException {
        try {
            String url = baseUrl;
            url += "api/entries/active/year/" + workloadEntry.week.year() + "/";
            url += workloadEntry.week.week() + "/";
            url += "lectures/" + workloadEntry.lecture_id + "/";
            ArrayList<NameValuePair> headers = buildAuthHeaders(url.toString(), account);
            ArrayList<NameValuePair> urlArgs = new ArrayList<>();
            urlArgs.add(new BasicNameValuePair("hoursInLecture", String.valueOf(workloadEntry.getHoursInLecture())));
            urlArgs.add(new BasicNameValuePair("hoursForHomework", String.valueOf(workloadEntry.getHoursForHomework())));
            urlArgs.add(new BasicNameValuePair("hoursStudying", String.valueOf(workloadEntry.getHoursStudying())));
            mRestClient.Execute(RestClient.RequestMethod.POST, url, headers, urlArgs);
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response == null) {
            // retry later
            //TODO: dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.RETRYSYNC);
            //TODO: First implement the stopsync in the content provider
            throw new IOException();
        } else {
            //TODO: Figure out why I am getting json back here! What's wrong with the remote?
            //TODO: Also handle 404s
            // we succeeded and now we stop the sync
            dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }
    }


    private void patch_lecture(Lecture lectureToPatch, Account account) throws IOException, AuthenticatorException {
        try {
            String url = baseUrl;
            url += "api/lectures/all/" + lectureToPatch._ID + "/";
            ArrayList<NameValuePair> headers = buildAuthHeaders(url.toString(), account);
            ArrayList<NameValuePair> urlArgs = new ArrayList<>();
            urlArgs.add(new BasicNameValuePair("isActive", String.valueOf(lectureToPatch.isActive)));
            mRestClient.Execute(RestClient.RequestMethod.POST, url, headers, urlArgs);
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response == null) {
            // retry later
            //TODO: dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.RETRYSYNC);
            //TODO: First implement the stopsync in the content provider
            throw new IOException();
        } else if (response.contains("DOCTYPE html")) {
            //TODO: When debug mode is off, we must handle 404s!
            // we got an error message most likely
            //TODO: Handle this!
        } else {
            // we succeeded and now we stop the sync
            dbObjectBuilder.updateLecture(lectureToPatch, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }
    }

    /*
    refererUrl can be null.
     */
    private ArrayList<NameValuePair> buildAuthHeaders(String refererUrl, Account account) throws android.accounts.OperationCanceledException, android.accounts.AuthenticatorException, java.io.IOException {

        AccountManagerFuture<Bundle> future = sAccountManager.getAuthToken(account, "session_ID_token", Bundle.EMPTY, true, null, null);
        // change true to false in the call parameters to have no notification
        // in any case the future.getResult() function returns immediately
        Bundle bundle = future.getResult();

        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        if (authToken != null) {
            ArrayList<NameValuePair> headers = new ArrayList<>();
            NameValuePair cookies = new BasicNameValuePair("Cookie", authToken);
            headers.add(cookies);
            if (refererUrl != null) {
                NameValuePair referer = new BasicNameValuePair("Referer", refererUrl);
                headers.add(referer);
            }
            return headers;
        } else {
            return null;
        }


    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        //TODO: Switch to ContentProviderClient (also in DBObjectbuilder) and use the one passed in the arguments.
        //TODO: However, I must make sure the release the provider on time ? No I think the sync framework does this for me.
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());


//        android.os.Debug.waitForDebugger();
        Log.d(TAG, "Starting sync!");


        try {

            get_table_entries_lectures(account);
            get_table_entries_workentries(account);


            Cursor cursor = dbObjectBuilder.getNotIdle(getContext().getString(R.string.lectures_table_name));
            while (cursor.moveToNext()) {
                int sync_operation = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS.OPERATION));
                int id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID));
                if (sync_operation == SurveyContentProvider.SYNC_OPERATION.POST) {
                    dbObjectBuilder.mark_as_transacting(id, getContext().getString(R.string.lectures_table_name));
                    patch_lecture(dbObjectBuilder.buildLectureFromCursor(cursor), account); // id is same as local-id since the ids are unique and identifying for lectures across local AND remote
                }
            }
            cursor.close();


            cursor = dbObjectBuilder.getNotIdle(getContext().getString(R.string.workentry_table_name));
            while (cursor.moveToNext()) {
                int sync_operation = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS.OPERATION));
                int local_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID));

                if (sync_operation == SurveyContentProvider.SYNC_OPERATION.POST) {
                    dbObjectBuilder.mark_as_transacting(local_id, getContext().getString(R.string.workentry_table_name));
                    post_workentry(dbObjectBuilder.getWorkloadEntryByLocalId(local_id), account);
                }
            }
            cursor.close();

            Log.d(TAG, "Sucessfully finalized sync");

        } catch (AuthenticatorException e) {
            Log.d(TAG, "Invalidating AuthToken");
            sAccountManager.invalidateAuthToken("tu-dresden.de", "session_ID_token"); // is the second parameter correct?
        } catch (IOException e) {
            Log.e(TAG, "IOException in onPerformSync.", e);
        }

    }

}
