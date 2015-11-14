package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static class SYNC_TASK {
        public static final int SYNC_TABLE_ENTRIES_LECTURES = 0;
        public static final int SYNC_TABLE_ENTRIES_WORKENTRIES = 1;
        public static final int INCREMENTAL_DOWNLOAD_LECTURES = 2;
        public static final int INCREMENTAL_DOWNLOAD_ENTRIES = 3;
        public static final int INCREMENTAL_PATCH_LECTURES = 4;
        public static final int INCREMENTAL_PATCH_WORKENTRIES = 5;
        public static final int INCREMENTAL_POST_WORKENTRIES = 6;

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

    //TODO: Do we really want the remote end to only give the entries of active lectures?
    // That means it will look as if remote deletes/adds a lot of entries when a lecture is activated or
    // de-activated. Is that what we want?

    // There is a lot of duplicate code and even duplicate downloads in this function.
    // Until I have the pattern fully figured out, I will favor clarity over efficiency.

    private void get_workload_entry(int lecture_id, int year, int week){
        // Update workload entries
        // If local entry is syncing it is not overwritten.
        try {
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/entries/active/", buildAuthHeaders(null), null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
//            reduce list to those that were requested
//            This is all extremely inefficient obviously, but right now I am favoring clarity over performance
            List<WorkloadEntry> requestedRemoteEntries = new ArrayList<>();
            for (WorkloadEntry remoteEntry : remoteEntries){
                if (remoteEntry.week.week() == week
                        && remoteEntry.week.year() == year
                        && remoteEntry.lecture_id == lecture_id){
                    requestedRemoteEntries.add(remoteEntry);
                }
            }
            mRestResponseProcessor.updateWorkloadRows(requestedRemoteEntries);
        }
        catch (Exception e ){
            //TODO
            throw new Error(e);
        }
    }

    private void get_lecture(int id){
        // Update workload entries
        // If local entry is syncing it is not overwritten.
        try {
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/lectures/all/", buildAuthHeaders(null), null);
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<Lecture> remoteLectures = RESTResponseProcessor.lectureListFromJson(response);
            List<Lecture> requestedRemoteLectures = new ArrayList<>();
            for (Lecture remoteLecture : remoteLectures){
                if(remoteLecture._ID == id){
                    requestedRemoteLectures.add(remoteLecture);
                    break;
                }
            }
            mRestResponseProcessor.updateLectureRows(requestedRemoteLectures);
        }
        catch (Exception e ){
            //TODO
        }
    }




    private void sync_table_entries_lectures() throws IOException, AuthenticatorException{
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl+"api/lectures/all/", headers, null);
        } catch (IOException e){
            throw new AuthenticatorException();
        }
        catch (Exception e) {
            throw new IOException();
        }
        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.

        List<Lecture> remoteLectures = RESTResponseProcessor.lectureListFromJson(response);
        mRestResponseProcessor.insert_delete_lectures_from_remote(remoteLectures);
    }

    private void sync_table_entries_workentries() throws IOException, AuthenticatorException {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl + "api/entries/active/", headers, null);
        } catch (IOException e) {
            throw new AuthenticatorException();
        }catch (Exception e){
            throw  new IOException();
        }
        try{
            String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            mRestResponseProcessor.insert_delete_workloadentries_from_remote(remoteEntries);
        }
        catch (IOException e){
            throw new AuthenticatorException(e);
        }

    }


    private void patch_workentry( WorkloadEntry workloadEntry) throws IOException, AuthenticatorException{
        //TODO: Remove duplicate code with post_workentries

        try {
            String url = baseUrl;
            url += "api/entries/active/year/"+ workloadEntry.week.year() +"/";
            url +=  workloadEntry.week.week() + "/";
            url += "lectures/" + workloadEntry.lecture_id + "/";
            ArrayList<NameValuePair> headers = buildAuthHeaders(url.toString());
            ArrayList<NameValuePair> urlArgs = new ArrayList<>();
            urlArgs.add(new BasicNameValuePair("hoursInLecture",String.valueOf(workloadEntry.getHoursInLecture())));
            urlArgs.add(new BasicNameValuePair("hoursForHomework", String.valueOf(workloadEntry.getHoursForHomework())));
            urlArgs.add(new BasicNameValuePair("hoursInStudying", String.valueOf(workloadEntry.getHoursStudying())));
            mRestClient.Execute(RestClient.RequestMethod.PUT, url, headers, urlArgs);
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response==null){
            // retry later
            //TODO: dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.RETRYSYNC);
            //TODO: First implement the stopsync in the content provider
            throw new IOException();
        } else {
            // we succeeded and stop the sync
            dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }
    }


    private void post_workentry( WorkloadEntry workloadEntry) throws IOException, AuthenticatorException{
        try {
            String url = baseUrl;
            url += "api/entries/active/year/"+ workloadEntry.week.year() +"/";
            url +=  workloadEntry.week.week() + "/";
            url += "lectures/" + workloadEntry.lecture_id + "/";
            ArrayList<NameValuePair> headers = buildAuthHeaders(url.toString());
            ArrayList<NameValuePair> urlArgs = new ArrayList<>();
            urlArgs.add(new BasicNameValuePair("hoursInLecture",String.valueOf(workloadEntry.getHoursInLecture())));
            urlArgs.add(new BasicNameValuePair("hoursForHomework", String.valueOf(workloadEntry.getHoursForHomework())));
            urlArgs.add(new BasicNameValuePair("hoursInStudying", String.valueOf(workloadEntry.getHoursStudying())));
            mRestClient.Execute(RestClient.RequestMethod.POST, url, headers, urlArgs);
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response==null){
            // retry later
            //TODO: dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.RETRYSYNC);
            //TODO: First implement the stopsync in the content provider
            throw new IOException();
        } else {
            // we succeeded and stop the sync
            dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }

    }


    private void patch_lecture(Lecture lectureToPatch){
        //TODO: Implement
        //TODO: Actually we are only changing the active/nonactive status
    }

    /*
    refererUrl can be null.
     */
    private ArrayList<NameValuePair> buildAuthHeaders(String refererUrl) throws android.accounts.OperationCanceledException, android.accounts.AuthenticatorException, java.io.IOException{

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
            ArrayList<NameValuePair> headers = new ArrayList<>();
            NameValuePair cookies = new BasicNameValuePair("Cookie",authToken);
            headers.add(cookies);
            if (refererUrl!=null) {
                NameValuePair referer = new BasicNameValuePair("Referer", refererUrl);
                headers.add(referer);
            }
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

        //TODO: Switch to ContentProviderClient (also in DBObjectbuilder) and use the one passed in the arguments.
        //TODO: However, I must make sure the release the provider on time (!?!)
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());


        android.os.Debug.waitForDebugger();


            // obviously the decisions in this functions can be sped up by
            // restricting the things the function checks via parameters. For now, we check it all

            // there is a table_sync_status and table_sync_operation for each table
            // also each row has these defined.
            // These values are used to decide what is synced, and how. Finer-grained syncing methods
            // can anchored in this function.

        try {
            // VERY rough checks, and we will download everything
                //TODO: un-comment the checks
//            if (insert_from_remote_lectures_status == SYNC_STATUS.PENDING || delete_from_remote_lectures_status == SYNC_STATUS.PENDING ) {
//                insert_from_remote_lectures_status = SYNC_STATUS.TRANSACTING;
//                delete_from_remote_lectures_status = SYNC_STATUS.TRANSACTING;
                sync_table_entries_lectures();
//            }

//            if(insert_from_remote_workentries_status == SYNC_STATUS.PENDING || delete_from_remote_workentries_status == SYNC_STATUS.PENDING ) {
//                insert_from_remote_workentries_status = SYNC_STATUS.TRANSACTING;
//                delete_from_remote_workentries_status = SYNC_STATUS.TRANSACTING;
                sync_table_entries_workentries();
//            }


            Cursor cursor = dbObjectBuilder.getPending(getContext().getString(R.string.lectures_table_name));
            while (cursor.moveToNext()){
                int sync_operation = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS.OPERATION));
                int id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID));
                if(sync_operation == SurveyContentProvider.SYNC_OPERATION.GET){
                    get_lecture(id);
                    dbObjectBuilder.mark_as_transacting(id, getContext().getString(R.string.lectures_table_name));
                }
                else if (sync_operation == SurveyContentProvider.SYNC_OPERATION.PATCH){
                    patch_lecture(dbObjectBuilder.getLectureById(id, true)); // id is same as local-id since the ids are unique and identifiying for lectures across local AND remote
                    dbObjectBuilder.mark_as_transacting(id, getContext().getString(R.string.lectures_table_name));
                }
                // possibly add more
            }
            cursor.close();


            cursor = dbObjectBuilder.getPending(getContext().getString(R.string.workentry_table_name));
            while (cursor.moveToNext()){
                int sync_operation = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS.OPERATION));
                int local_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID));
                int lecture_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID));
                int year = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR));
                int week = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK));
                if(sync_operation == SurveyContentProvider.SYNC_OPERATION.GET){
                    get_workload_entry(lecture_id,year,week);
                    dbObjectBuilder.mark_as_transacting(local_id, getContext().getString(R.string.workentry_table_name));
                }
                else if (sync_operation == SurveyContentProvider.SYNC_OPERATION.PATCH){
                    patch_workentry(dbObjectBuilder.getWorkloadEntryByLocalId(local_id, true));
                    dbObjectBuilder.mark_as_transacting(local_id, getContext().getString(R.string.workentry_table_name));
                }
                else if (sync_operation == SurveyContentProvider.SYNC_OPERATION.POST){
                    post_workentry(dbObjectBuilder.getWorkloadEntryByLocalId(local_id, true));
                    dbObjectBuilder.mark_as_transacting(local_id, getContext().getString(R.string.workentry_table_name));
                }
                // possibly add more
            }
            cursor.close();

        }
        catch (AuthenticatorException e){
            sAccountManager.invalidateAuthToken("tu-dresden.de", "session_ID_token"); // is the second parameter correct?
        }
        catch (IOException e){
            //TODO: Handle this better.
            throw new IOError(e);
        }

    }

}
