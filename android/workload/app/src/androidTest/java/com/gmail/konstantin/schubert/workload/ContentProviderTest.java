package com.gmail.konstantin.schubert.workload;

import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

/**
 * Created by kon on 01/05/15.
 */
public class ContentProviderTest extends ProviderTestCase2<SurveyContentProvider>{

    private static final String TAG = ContentProviderTest.class.getSimpleName();

    MockContentResolver mMockResolver;
    ContentValues mTestLectureEntry;

    ContentProviderTest(){
        super(SurveyContentProvider.class, SurveyContentProvider.CONTENT_PROVIDER_AUTHORITY );
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

    public void testLectureInsert__inserts_a_valid_lecture_record(){
        Uri uri = mMockResolver.insert(Uri.parse(SurveyContentProvider.CONTENT_PROVIDER_AUTHORITY+"/menu/lectures/active/"), getMockLectureEntry());
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


}
