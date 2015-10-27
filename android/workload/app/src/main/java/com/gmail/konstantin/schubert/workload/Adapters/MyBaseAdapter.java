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

    private final LectureObserver sLectureObserver;
    protected DBObjectBuilder dbObjectBuilder;


    public MyBaseAdapter(Context context){

        sLectureObserver = new LectureObserver(new Handler(), this);
        //TODO: Check if this works: The mLectures and the mWeeks attributes must both be updated when the content provider has a change. And the view must be notfied, so it can update!!!!
        ContentResolver resolver = context.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(resolver);
        resolver.registerContentObserver(
                Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"),
                false,
                sLectureObserver);
    }


    protected abstract void updateMembers();

    class LectureObserver extends ContentObserver {
        final MyBaseAdapter myBaseAdapter;

        public LectureObserver(Handler handler, MyBaseAdapter myBaseAdapter) {
            super(handler);
            this.myBaseAdapter = myBaseAdapter;
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            myBaseAdapter.updateMembers();
            myBaseAdapter.notifyDataSetChanged();
        }
    }


}
