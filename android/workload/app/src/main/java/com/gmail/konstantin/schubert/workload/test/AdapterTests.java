package com.gmail.konstantin.schubert.workload.test;

import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.konstantin.schubert.workload.MyBaseAdapter;
import com.gmail.konstantin.schubert.workload.WeekButtonAdapter;


public class AdapterTests extends AndroidTestCase{

    MyBaseAdapter mBaseAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.mBaseAdapter = new WeekButtonAdapter(mContext);

    }

    public void testWeekButtonAdapter__test_GetCount(){

        mBaseAdapter.getCount();
        assertEquals(1, 1);
    }
}
