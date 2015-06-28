package com.gmail.konstantin.schubert.workload;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;


abstract public class MyBaseAdapter extends BaseAdapter{

    private final Context sContext;
    private ContentResolver mContentResolver;
    private static final String TAG = MyBaseAdapter.class.getSimpleName();
    private final LectureObserver sLectureObserver;


    public MyBaseAdapter(Context context){
        this.sContext = context;
        this.mContentResolver = context.getContentResolver();

        sLectureObserver = new LectureObserver(new Handler(), this);
        //TODO: Check if this works: The mLectures and the mWeeks attributes must both be updated when the content provider has a change. And the view must be notfied, so it can update!!!!
        sContext.getContentResolver().registerContentObserver(
                Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"),
                false,
                sLectureObserver);
    }

    protected List<Lecture> getLecturesInWeek(Week thatWeek, boolean onlyActive){
        // gets lectures that are active in 'thatWeek'
        List<Lecture> allLectures = getLectureList(onlyActive);
        List<Lecture> lecturesThatWeek = new ArrayList<Lecture>();
        for (Lecture lecture : allLectures){
            if (lecture.startWeek.compareTo(thatWeek)<=0 && thatWeek.compareTo(lecture.endWeek)<=0) {
                lecturesThatWeek.add(lecture);
            }
        }
        return lecturesThatWeek;
    }


    protected List<Lecture> getLectureList( boolean onlyActive){
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), null, null, null, null);
        String cursorDump = DatabaseUtils.dumpCursorToString(cursor);
        Log.d(TAG, "Full table dump:" + DatabaseUtils.dumpCursorToString(cursor));
        List<Lecture> lectures = new ArrayList<Lecture>();
        while (cursor.moveToNext()){
            int _ID= cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE._ID));
            String name = cursor.getString( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.NAME) );
            String semester = cursor.getString( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.SEMESTER) );
            Week startWeek = new Week(
                    cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.STARTYEAR)),
                    cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.STARTWEEK))
            );
            Week endWeek = new Week(
                    cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ENDYEAR)),
                    cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ENDWEEK))
            );
            boolean isActive = ( cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE)) == 1); //TODO: Figure out if and why there is no boolean method.
            Lecture newLecture = new Lecture(_ID, name, semester, startWeek, endWeek, isActive);
            if (!onlyActive || (onlyActive&&isActive)) {
                lectures.add(newLecture);
            }
        }
        return lectures;
    }

    protected List<WorkloadEntry> getWorkloadEntryList(Lecture lecture){
        /* Gets the workload entries for a given lecture.
        * If passed null it will get the entries for all lectures.
        * */
        String where = null;
        if (lecture != null){
            where = SurveyContentProvider.DB_STRINGS_WORKENTRY._ID + "=" + String.valueOf(lecture._ID);
        }
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, where, null, null);
        List<WorkloadEntry> workloadEntries = new ArrayList<WorkloadEntry>();
        while (cursor.moveToNext()){
            int _ID = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY._ID));
            int year = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR));
            int week = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK));
            int lecture_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID));
            float hoursInLecture = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE));
            float hoursForHomework = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK));
            float hoursStudying = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING));
            WorkloadEntry newWorkloadEntry = new WorkloadEntry(_ID, year, week, lecture_id, hoursInLecture, hoursForHomework, hoursStudying );
        }
        return workloadEntries;
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
