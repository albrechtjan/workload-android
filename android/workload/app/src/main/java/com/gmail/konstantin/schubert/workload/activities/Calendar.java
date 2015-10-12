package com.gmail.konstantin.schubert.workload.activities;

import android.os.Bundle;
import android.widget.GridView;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Adapters.WeekButtonAdapter;


public class Calendar extends MyBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new WeekButtonAdapter(this));

    }


}

