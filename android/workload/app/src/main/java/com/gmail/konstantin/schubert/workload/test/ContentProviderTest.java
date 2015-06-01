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

import java.util.Random;
import java.util.UUID;

public class ContentProviderTest extends ProviderTestCase2<SurveyContentProvider>{


    private static final String TAG = ContentProviderTest.class.getSimpleName();

    MockContentResolver mMockResolver;

    public ContentProviderTest(){
        super(SurveyContentProvider.class, SurveyContentProvider.AUTHORITY );
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        Log.d(TAG, "setUp:");
        mMockResolver = getMockContentResolver();

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



    public static ContentValues getMockLectureEntry(){
        ContentValues v = new ContentValues(9);
        v.put("NAME", "LectureOne" );
        v.put("SEMESTER", "SS2015");
        v.put("STARTYEAR", 2015);
        v.put("STARTWEEK", 10);
        v.put("ENDYEAR", 2015);
        v.put("ENDWEEK", 40);
        v.put("ISACTIVE", true);
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;
    }


    public static ContentValues getMockWorkloadEntry(){
        ContentValues v = new ContentValues(8);
        v.put("HOURS_IN_LECTURE", 2.3);
        v.put("HOURS_FOR_HOMEWORK", 2.3);
        v.put("HOURS_STUDYING", 2.5);
        v.put("YEAR", 2015);
        v.put("WEEK", 12);
        v.put("LECTURE_ID", 1);
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;

    }


    public static ContentValues getRandomMockLectureEntry(){
        ContentValues v = new ContentValues(9);
        v.put("NAME", "Lecture" + UUID.randomUUID().toString());
        v.put("SEMESTER", "SS2015");
        v.put("STARTYEAR", 2015);
        v.put("STARTWEEK", 10);
        v.put("ENDYEAR", 2015);
        v.put("ENDWEEK", 40);
        v.put("ISACTIVE", true);
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;
    }

    private  int getIDOfRandomLecture(){
        //query the database for all lecture IDs
        String[] projection = {"_ID"};
        Cursor cursor = mMockResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), projection, null, null, null);
        //choose one randomly
        Random rand = new Random();
        int randomRowIndex = rand.nextInt(cursor.getCount());
        cursor.moveToPosition(randomRowIndex);
        return cursor.getInt(0);
    }


    private ContentValues getRandomMockWorkloadEntry(){
        ContentValues v = new ContentValues(8);
        Random rand = new Random();
        v.put("HOURS_IN_LECTURE", rand.nextInt(200)/10.);
        v.put("HOURS_FOR_HOMEWORK", rand.nextInt(200)/10.);
        v.put("HOURS_STUDYING", rand.nextInt(200)/10.);
        v.put("YEAR", 2015);
        v.put("WEEK", 12);
        v.put("LECTURE_ID", getIDOfRandomLecture());
        v.put("STATUS", "IDLE");
        v.put("OPERATION", "NONE");
        return v;

    }

}
