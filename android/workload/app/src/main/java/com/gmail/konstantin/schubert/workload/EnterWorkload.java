package com.gmail.konstantin.schubert.workload;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnterWorkload extends ListActivity {
    public final static String MESSAGE_YEAR = "com.gmail.konstantin.schubert.workload.YEAR";
    public final static String MESSAGE_WEEK = "com.gmail.konstantin.schubert.workload.WEEK";
    public final static String MESSAGE_LECTURE = "com.gmail.konstantin.schubert.workload.LECTURE";

    public final static int ROW_HOURS_ATTENDING = 0;
    public final static int ROW_HOURS_HOMEWORK = 1;
    public final static int ROW_HOURS_STUDYING = 2;
    public final static List<String> ROW_TITLES = Arrays.asList("attending", "homework", "studying");


    //TODO: Figure out if a ListActivity is really what we want here.

    private Week mWeek;
    private int lectureId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lecture);
        Intent launchIntent = getIntent();
        Integer year = launchIntent.getIntExtra(MESSAGE_YEAR, -1);
        Integer weekNumber = launchIntent.getIntExtra(MESSAGE_WEEK, -1);
        Integer lectureId = launchIntent.getIntExtra(MESSAGE_LECTURE, -1);
        this.mWeek = new Week(year, weekNumber);
        setListAdapter(new EnterWorkloadAdaper(this, mWeek, lectureId));
        setTitle("Enter your hours");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        EnterWorkloadAdaper adaper = (EnterWorkloadAdaper) getListAdapter();
        adaper.saveWorkload();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
