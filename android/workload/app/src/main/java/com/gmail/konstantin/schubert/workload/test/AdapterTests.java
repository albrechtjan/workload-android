package com.gmail.konstantin.schubert.workload.test;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.Adapters.MyBaseAdapter;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;


public class AdapterTests extends AndroidTestCase {

    private static final String TAG = AdapterTests.class.getSimpleName();
    MyBaseAdapter mBaseAdapter;
    ContentResolver mContentResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.mBaseAdapter = new CalendarAdapter(mContext, "SS2015");

        mContentResolver = mContext.getContentResolver(); // I am not inheriting from ProviderTestCase and thus cannot get a mock content resolver
        //TODO: put more entries
        mContentResolver.insert(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), ContentProviderTest.getRandomMockLectureEntry());
        //TODO: Make this one random and create a check that prevents entries for a certain week to be entered twice. Or maybe we should then in the content provider switch from insert to update?
        mContentResolver.insert(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), ContentProviderTest.getMockWorkloadEntry());

        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), null, null, null, null);
        String cursorDump = DatabaseUtils.dumpCursorToString(cursor);
        Log.d(TAG, "Full table dump:" + DatabaseUtils.dumpCursorToString(cursor));
        Log.d(TAG, "setUp done:");


        cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, null, null, null);
        cursorDump = DatabaseUtils.dumpCursorToString(cursor);
        Log.d(TAG, "Full table dump:" + DatabaseUtils.dumpCursorToString(cursor));
        Log.d(TAG, "setUp done:");
    }

    public void testWeekButtonAdapter__test_GetCount() {
        int numberOfWeeks = mBaseAdapter.getCount();
        assertTrue(numberOfWeeks > 0);
    }
}
