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
    Cursor mCursor;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lecture);

        Intent launchIntent = getIntent();
        Integer year = launchIntent.getIntExtra(MESSAGE_YEAR, -1);
        Integer weeknumber = launchIntent.getIntExtra(MESSAGE_WEEK, -1);
        Week mWeek  = new Week(year,weeknumber);

        //TODO: restrict to lectures that happen in this week.
        mCursor = this.getContentResolver().query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), null, null, null, null);
        // TODO:Put a managed wrapper around the retrieved cursor so we don't have to worry about requerying or closing it as the activity changes state.
        startManagingCursor(mCursor);
        //TODO: Write separate class file for an adapter that implements the ListAdapter and inherits form MyBaseAdaper
        Log.d(TAG, "Full table dump:" + DatabaseUtils.dumpCursorToString(mCursor));
//        // Now create a new list adapter bound to the cursor.
//        // SimpleListAdapter is designed for binding to a Cursor.
        ListAdapter adapter = new SimpleCursorAdapter(
                this, // Context.
                android.R.layout.two_line_list_item,  // Specify the row template to use (here, two columns bound to the two retrieved cursor
                mCursor,                                   // Pass in the cursor to bind to.
                new String[] {SurveyContentProvider.DB_STRINGS_LECTURE.NAME, SurveyContentProvider.DB_STRINGS_LECTURE._ID},           // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2});  // Parallel array of which template objects to bind to those columns.

//         Bind to our new adapter.
        setListAdapter(adapter);
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
