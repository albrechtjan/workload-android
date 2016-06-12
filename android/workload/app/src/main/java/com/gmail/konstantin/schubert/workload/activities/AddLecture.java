package com.gmail.konstantin.schubert.workload.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.gmail.konstantin.schubert.workload.Adapters.AddLectureAdapter;
import com.gmail.konstantin.schubert.workload.R;


/*
* Activity where user can select the lectures for which he wants to
* monitor his time budget.This activity shows all lectures that exist for
* a certain semester, for example for the winter semester 2039.
* */
public class AddLecture extends MyBaseListActivity {

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
