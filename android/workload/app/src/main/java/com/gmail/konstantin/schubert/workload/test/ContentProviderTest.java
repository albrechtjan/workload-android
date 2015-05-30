package com.gmail.konstantin.schubert.workload.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

public class ContentProviderTest extends ProviderTestCase2<SurveyContentProvider>{


    private static final String TAG = ContentProviderTest.class.getSimpleName();

    MockContentResolver mMockResolver;
    ContentValues mTestLectureEntry;

    public ContentProviderTest(){
        super(SurveyContentProvider.class, SurveyContentProvider.AUTHORITY );
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        Log.d(TAG, "setUp:");
        mMockResolver = getMockContentResolver();
        mTestLectureEntry = getMockLectureEntry();

    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        Log.d(TAG, "tearDown:");
    }

    public void testDatabase__inserts_records(){
        Uri uri = mMockResolver.insert(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), getMockLectureEntry());
        Log.d(TAG,"inserted lecture values with mock-resolver. Resulting uri is "+ uri.toString());

        //get cursor for full DB
        Cursor cursor = mMockResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), null, null, null, null);
        Log.d(TAG, "Full table dump:"+ DatabaseUtils.dumpCursorToString(cursor));

        //get cursor for entry that was just added
        cursor = mMockResolver.query(uri, null, null, null, null);
        Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
        assertEquals(1, cursor.getCount()); // make sure we retrieved exactly one row with the call


        uri = mMockResolver.insert(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), getMockWorkloadEntry());
        Log.d(TAG,"inserted workload values with mock-resolver. Resulting uri is "+ uri.toString());


        //get cursor for full DB
        cursor = mMockResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, null, null, null);
        Log.d(TAG, "Full table dump:"+ DatabaseUtils.dumpCursorToString(cursor));

        //get cursor for entry that was just added
        cursor = mMockResolver.query(uri, null, null, null, null);
        Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
        assertEquals(1, cursor.getCount()); // make sure we retrieved exactly one row with the call

    }

    private static ContentValues getMockLectureEntry(){
        ContentValues v = new ContentValues(5);
        v.put("NAME", "Testlecture");
        v.put("SEMESTER", "SS2015");
        v.put("ISACTIVE", true);
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;
    }

    private static ContentValues getMockWorkloadEntry(){
        ContentValues v = new ContentValues(7);
        v.put("HOURS_IN_LECTURE", 2.3);
        v.put("HOURS_FOR_HOMEWORK", 2.3);
        v.put("HOURS_STUDYING", 2.5);
        v.put("YEAR", 2015);
        v.put("WEEK", 12);
        v.put("LECTURE_ID",1);
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;

    }



}
