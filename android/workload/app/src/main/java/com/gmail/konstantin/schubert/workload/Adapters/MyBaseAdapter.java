package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.widget.BaseAdapter;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;


abstract public class MyBaseAdapter extends BaseAdapter{

    private final MyContentObserver mLectureObserver;
    protected DBObjectBuilder dbObjectBuilder;


    public MyBaseAdapter(Context context){

        mLectureObserver = new MyContentObserver(new Handler(), this);
        //TODO: Figure out which role the handler plays.
        ContentResolver resolver = context.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(resolver);
        resolver.registerContentObserver(
                Uri.parse("content://" + SurveyContentProvider.AUTHORITY),
                //for now we are watching the whole data set with one observer.
                //TODO: Think about using finer-tuned observers
                true,
                mLectureObserver);
    }

    // TODO: unregister ContentObserver when activity is resumed and/or paused (?)
    //TODO: Then also call updateMemebers every time the activity is resumed? How to do that?



    public abstract void updateMembers();

    class MyContentObserver extends ContentObserver {
        final MyBaseAdapter myBaseAdapter;

        public MyContentObserver(Handler handler, MyBaseAdapter myBaseAdapter) {
            super(handler);
            this.myBaseAdapter = myBaseAdapter;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            myBaseAdapter.updateMembers();
            myBaseAdapter.notifyDataSetChanged();
        }
    }


}
