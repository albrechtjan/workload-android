package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.BaseAdapter;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

/**
 * Base adapter class
 *
 * The idea is that all inheriting adapters have java object fields which represent their data.
 * These fields are used to provide fill the views with data. In order to update the fields
 * when the data in the SurveyContentProvider changes, the base adapter uses a content observer
 * to listen to database changes. If necessary,the updateMembers() method is called.
 *
 * This is not very efficient and might harm the performance of the app, especially considering
 * that all of this happens in the main thread.
 *
 * \todo: I see are two ways to improve this:
 *   * perform the updateMemebers method in a separate thread, for example with an AsyncTask
 *   * do not build java objects from the database information, instead use the database
 *   directly to populate the views.
 */
abstract public class MyBaseAdapter extends BaseAdapter {
    private static final String TAG = MyBaseAdapter.class.getSimpleName();

    protected DBObjectBuilder dbObjectBuilder;
    protected ContentResolver mContentResolver;

    /**
     * Base adapter class
     *
     * Constructor. Registers a content observer which calls updateMembers() when the database is
     * updated.
     *
     * @param context Reference to the activity which owns the adapter
     *
     */
    public MyBaseAdapter(Context context) {

        mContentResolver = context.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(mContentResolver);
        Handler handler = new Handler(Looper.getMainLooper());
        mContentResolver.registerContentObserver(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/"), true, new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        this.onChange(selfChange, null);
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        Log.d(TAG, "content observer fired");
                        updateMembers(uri);
                    }
                }
        );
        // The contentobserver is never unregistered. This means that even if the activity is paused,
        // adapters and views are updated. Theoretically this should prevent the view from flickering
        // after the activity is resumed. On the other hand, this means that updateMembers() is called
        // for ALL adapters on every database update...


    }

    /** Updates the objects which mirror the database
     *
     * Should be called when the database udpates.
     * @param uri the uri that describes the affected part of the database.
     */
    public abstract void updateMembers(Uri uri);


}
