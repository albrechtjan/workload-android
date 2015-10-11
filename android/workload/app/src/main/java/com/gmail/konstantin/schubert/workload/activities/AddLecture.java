package com.gmail.konstantin.schubert.workload.activities;

import android.content.Intent;
import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.AddLectureAdapter;
import com.gmail.konstantin.schubert.workload.AddLectureChooseSemesterAdapter;
import com.gmail.konstantin.schubert.workload.R;

public class AddLecture  extends MyBaseListActivity {

    public final static String SEMESTER = "com.gmail.konstantin.schubert.workload.SEMESTER";

//    http://stackoverflow.com/a/2377946/1375015

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lecture);
        Intent launchIntent = getIntent();
        String mSemester = launchIntent.getStringExtra(SEMESTER);

        setListAdapter(new AddLectureAdapter(this, mSemester));
        setTitle("Activate Lectures");
    }
}
