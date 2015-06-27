package com.gmail.konstantin.schubert.workload;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;


public class SelectLecture extends ListActivity {
    private static final String TAG = SelectLecture.class.getSimpleName();
    public final static String MESSAGE_YEAR = "com.gmail.konstantin.schubert.workload.YEAR";
    public final static String MESSAGE_WEEK = "com.gmail.konstantin.schubert.workload.WEEK";
    private Week mWeek;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lecture);

        Intent launchIntent = getIntent();
        Integer year = launchIntent.getIntExtra(MESSAGE_YEAR, -1);
        Integer weekNumber = launchIntent.getIntExtra(MESSAGE_WEEK, -1);
        this.mWeek  = new Week(year,weekNumber);
        setListAdapter(new SelectLectureAdapter(this, mWeek));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
