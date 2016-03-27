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


abstract public class MyBaseAdapter extends BaseAdapter {
    private static final String TAG = MyBaseAdapter.class.getSimpleName();

    protected DBObjectBuilder dbObjectBuilder;
    protected ContentResolver mContentResolver;

    public MyBaseAdapter(Context context) {

        //TODO: Figure out which role the handler plays.
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

    }

    // TODO: unregister ContentObserver when activity is resumed and/or paused (?)
    //TODO: Then also call updateMemebers every time the activity is resumed? How to do that?


    public abstract void updateMembers(Uri uri);
    // uri is the uri that describes the affected part of the database.


}
