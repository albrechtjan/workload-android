package com.gmail.konstantin.schubert.workload.activities;


import android.content.Intent;
import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.Adapters.EnterWorkloadAdapter;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Week;

import java.util.Arrays;
import java.util.List;

/**
 * Activity where the workload data can be entered.
 *
 */
public class EnterWorkload extends MyBaseListActivity {
    public final static String MESSAGE_YEAR = "com.gmail.konstantin.schubert.workload.YEAR";
    public final static String MESSAGE_WEEK = "com.gmail.konstantin.schubert.workload.WEEK";
    public final static String MESSAGE_LECTURE = "com.gmail.konstantin.schubert.workload.LECTURE";

    public final static int ROW_HOURS_ATTENDING = 0;
    public final static int ROW_HOURS_HOMEWORK = 1;
    public final static int ROW_HOURS_STUDYING = 2;
    public final static List<String> ROW_TITLES = Arrays.asList("Attending", "Homework", "Studying");


    private Week mWeek;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_workload);
        Intent launchIntent = getIntent();
        Integer year = launchIntent.getIntExtra(MESSAGE_YEAR, -1);
        Integer weekNumber = launchIntent.getIntExtra(MESSAGE_WEEK, -1);
        Integer lectureId = launchIntent.getIntExtra(MESSAGE_LECTURE, -1);
        this.mWeek = new Week(year, weekNumber);
        setListAdapter(new EnterWorkloadAdapter(this, mWeek, lectureId));
        setTitle("Enter your hours");
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Because this is the activity where the user enters his data, the privacy agreement
        // is checked for every time the activity is loaded.
        assure_privacy_agreement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        EnterWorkloadAdapter adapter = (EnterWorkloadAdapter) getListAdapter();
        adapter.saveEditsIfUserHasEdited();

    }

}
