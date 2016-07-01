package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.OperationApplicationException;
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

/**
 * Implements the onPerformSync method which is called when a sync of the apps's data with the
 * remote server is initiated.
 *
 * The class also contains a number of related helper methods.
 *
 * For a description of the synchronization logic, please refer to the SyncLogic.md markdown
 * file in the 'documentation' folder in the root folder of the repository.
 *
 *
 * @inheritDoc
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {


    public final static String TAG = "SyncAdapter";
    // The base url of the web service.
    public final static String baseUrl = "https://survey.zqa.tu-dresden.de/app/workload/";
    static AccountManager sAccountManager;
    RESTResponseProcessor mRestResponseProcessor;
    RestClient mRestClient = new RestClient();
    DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(getContext().getContentResolver());

    /**
     * Constructor for backwards compatibility.
     *
     * @inheritDoc
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        //for backwards compatibility
        this(context, autoInitialize, false);
    }

    /**
     *
     * @inheritDoc
     */
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


    /**
     * Downloads all information about the available and user-active lectures from the remote and
     * updates the local tables accordingly.
     *
     * \todo: remove duplicate code with other methods in this class
     */
    private void get_table_entries_lectures(Account account) throws IOException, AuthenticatorException {
        // Delete anything local that is not in remote
        // Add anything to local that is in remote but not in local (as inactive)
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null, account);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl + "api/lectures/all/", headers, null);

            String response = mRestClient.response;
            //\todo: I do not like the usage of a public fields.
            //\todo The Exectute() function should return the response.
            List<Lecture> remoteLectures = RESTResponseProcessor.lectureListFromJson(response);
            mRestResponseProcessor.update_lectures_from_remote(remoteLectures);
        } catch (IOException e) {
            // We assume that the authentication witht the web service failed.
            // \todo: Make sure that this does not fire if the server is simply unavailable.
            throw new AuthenticatorException(e);
        } catch (Exception e) {
            throw new IOException();
        }
    }

    /**
     * Downloads all of the user's workload entries which are stored on the remote and updates
     * the local tables accordingly, if necessary.
     */
    private void get_table_entries_workentries(Account account) throws IOException, AuthenticatorException {
        // Delete any local entry that does not exist on the remote, unless it is pending for sync.
        // Add any entries which do not exist on local, but do exist on the remote,
        // to the local tables.
        try {
            ArrayList<NameValuePair> headers = buildAuthHeaders(null, account);
            mRestClient.Execute(RestClient.RequestMethod.GET, baseUrl + "api/entries/active/", headers, null);
            String response = mRestClient.response;
            //\todo: I do not like the usage of a public fields.
            //\todo The Exectute() function should return the response.
            List<WorkloadEntry> remoteEntries = RESTResponseProcessor.entryListFromJson(response);
            mRestResponseProcessor.update_workloadentries_from_remote(remoteEntries);
        } catch (IOException e) {
            // We assume that the authentication witht the web service failed.
            // \todo: Make sure that this does not fire if the server is simply unavailable.
            throw new AuthenticatorException(e);
        } catch (Exception e) {
            throw new IOException();
        }

    }


    /**
     * Uploads a new workload entry to the remote web service.
     *
     * @param workloadEntry The WorkloadEntry object that the remote server should be updated with.
     * @param account The app-specific account which holds the session token for authentication.
     *                (@See ../Authenticator)
     * @throws AuthenticatorException Thrown if session token has expired, possibly also in other
     *         cases
     * @throws IOException
     */
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

        //\todo: Catch an IOException and re-throw it as an AuthenticatorException, as we do it for
        //\todo the get_...() methods
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response == null) {
            // \todo mark the row in the content provider for retry.
            throw new IOException();
        } else {
            //TODO: Check for and handle server errors.
            // This tell marks the row in the content provider's table as successfully synched.
            dbObjectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }
    }


    /**
     * Tells the remove web service whether a user has selected a lecture for data entry
     *
     * @param lectureToPatch The lecture object who's changes should be propagated to the remote.
     * @param account The app-specific account which holds the session token for authentication.
     *                (@See ../Authenticator)
     * @throws AuthenticatorException Thrown if session token has expired, possibly also in other
     *         cases
     * @throws IOException
     */
    private void patch_lecture(Lecture lectureToPatch, Account account) throws IOException, AuthenticatorException {
        // \todo: remove duplicate code with post_workentry
        try {
            String url = baseUrl;
            url += "api/lectures/all/" + lectureToPatch._ID + "/";
            ArrayList<NameValuePair> headers = buildAuthHeaders(url.toString(), account);
            ArrayList<NameValuePair> urlArgs = new ArrayList<>();
            urlArgs.add(new BasicNameValuePair("isActive", String.valueOf(lectureToPatch.isActive)));
            mRestClient.Execute(RestClient.RequestMethod.POST, url, headers, urlArgs);
        //\todo: Catch an IOException and re-throw it as an AuthenticatorException, as we do it for
        //\todo the get_...() methods
        } catch (Exception e) {
            throw new IOException();
        }

        String response = mRestClient.response; //TODO: I do not like this. The function should return the response.
        // when calling updateWorkloadEntry, we are also doing an update of the time entries, but they should be correct, so it should be fine.
        if (response == null) {
            // retry later
            // \todo mark the row in the content provider for retry.
            throw new IOException();
        } else if (response.contains("DOCTYPE html")) {
            // The remote and is in debug mode and we got an error message most likely
            // \todo: Handle this?
            //TODO: Check for and handle server errors.
            // \todo mark the row in the content provider for retry.
        } else {
            // we succeeded and now we stop the sync
            dbObjectBuilder.updateLecture(lectureToPatch, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
        }
    }

    /**
     * Constructs a number of https headers needed for authentication in form of an ArrayList.
     *
     * @param account The app-specific account which holds the session token for authentication.
     *                (@See ../Authenticator)
     * @return ArrayList of the headers needed for authentication, or null if the authenticator holds
     * no valid session token for the account.
     * @throws android.accounts.OperationCanceledException
     * @throws android.accounts.AuthenticatorException

     * \todo: Remove refererUrl, we do not need that since we are not using CSRF protection for the
     * \todo  web api. (It is instead protected by requiring a non-browser user agent)
     */
    private ArrayList<NameValuePair> buildAuthHeaders(String refererUrl, Account account) throws android.accounts.OperationCanceledException, android.accounts.AuthenticatorException, java.io.IOException {

        // This issues a notification if no valid auth token is found
        AccountManagerFuture<Bundle> future = sAccountManager.getAuthToken(account, "session_ID_token", Bundle.EMPTY, true, null, null);
        // It seems like this returns immediately.
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

    /**
     * Entry method which is called when a sync starts.
     *
     * @inheritDoc
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.d(TAG, "Starting sync!");
        //TODO: Switch to ContentProviderClient (also in DBObjectbuilder) and use the one passed in the arguments.
        //TODO: However, I must make sure the release the provider on time ? No I think the sync framework does this for me.

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
            try {
                Log.d(TAG, "Invalidating AuthToken");
                AccountManagerFuture<Bundle> future = sAccountManager.getAuthToken(account, "session_ID_token", Bundle.EMPTY, true, null, null);
                String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                Log.d(TAG,"Authtoken is" + token);
                sAccountManager.invalidateAuthToken("tu-dresden.de", token);
            }
            catch (Exception ex ){
                Log.d(TAG, "Unable to invalidate Auth Token because we are not able to get it. Maybe it is already invalidated?");
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException in onPerformSync.", e);
        }

    }

}
