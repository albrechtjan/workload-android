package com.gmail.konstantin.schubert.workload.activities;

import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.ActiveLecturesAdapter;
import com.gmail.konstantin.schubert.workload.R;


public class ManageLectures extends MyBaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_lectures);


        setListAdapter(new ActiveLecturesAdapter(this));
        setTitle("Active Lectures");
    }
}
