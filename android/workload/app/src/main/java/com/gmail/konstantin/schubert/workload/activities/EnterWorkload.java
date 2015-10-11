package com.gmail.konstantin.schubert.workload.activities;


import android.content.Intent;
import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.EnterWorkloadAdapter;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Week;

import java.util.Arrays;
import java.util.List;

public class EnterWorkload extends MyBaseListActivity {
    public final static String MESSAGE_YEAR = "com.gmail.konstantin.schubert.workload.YEAR";
    public final static String MESSAGE_WEEK = "com.gmail.konstantin.schubert.workload.WEEK";
    public final static String MESSAGE_LECTURE = "com.gmail.konstantin.schubert.workload.LECTURE";

    public final static int ROW_HOURS_ATTENDING = 0;
    public final static int ROW_HOURS_HOMEWORK = 1;
    public final static int ROW_HOURS_STUDYING = 2;
    public final static List<String> ROW_TITLES = Arrays.asList("attending", "homework", "studying");


    //TODO: Figure out if a ListActivity is really what we want here. Figure out what to do as we cannot inherit from MyBaseAcitivy

    private Week mWeek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lecture);
        Intent launchIntent = getIntent();
        Integer year = launchIntent.getIntExtra(MESSAGE_YEAR, -1);
        Integer weekNumber = launchIntent.getIntExtra(MESSAGE_WEEK, -1);
        Integer lectureId = launchIntent.getIntExtra(MESSAGE_LECTURE, -1);
        this.mWeek = new Week(year, weekNumber);
        setListAdapter(new EnterWorkloadAdapter(this, mWeek, lectureId));
        setTitle("Enter your hours");
    }



    @Override
    protected void onPause() {
        super.onPause();
        EnterWorkloadAdapter adaper = (EnterWorkloadAdapter) getListAdapter();
        adaper.saveWorkload();
    }

}
