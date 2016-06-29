package com.gmail.konstantin.schubert.workload.activities;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;


/**
 *  Activity showing a calendar holdings the weeks in a certain semester.
 * This is the usual entry point for the app. Here, the user can choose a
 * week and lecture and enter his data, switch to another semester or
 * go to the various settings menus.
 */
public class Calendar extends MyBaseActivity {

    Semester sSemester;
    DBObjectBuilder dbObjectBuilder;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize class fields
        ContentResolver resolver = this.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(resolver);
        // sSemester is the semester for which the calendar is displayed
        sSemester = new Semester(get_best_semester());


        setContentView(R.layout.activity_calendar);
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new CalendarAdapter(this, sSemester.to_string()));
        final TextView semesterText = (TextView) findViewById(R.id.calendar_semester);
        semesterText.setText(sSemester.to_string());

        // Change the adapter if a button to change the semester is clicked
        View.OnClickListener semesterButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.next_semester_button) {
                    Calendar.this.sSemester = Calendar.this.sSemester.get_next();
                } else { // if (v.getId() == R.id.previous_semester_button)
                    Calendar.this.sSemester = Calendar.this.sSemester.get_previous();
                }
                semesterText.setText(sSemester.to_string());
                gridview.setAdapter(new CalendarAdapter(Calendar.this, sSemester.to_string()));
            }
        };

        findViewById(R.id.previous_semester_button).setOnClickListener(semesterButtonListener);
        findViewById(R.id.next_semester_button).setOnClickListener(semesterButtonListener);

        // Update the empty state when lectures change.
        // This makes sure the empty state is updated as soon as the available lectures
        // have been downloaded.
        Handler handler = new Handler(Looper.getMainLooper());
        resolver.registerContentObserver(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), true, new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        this.onChange(selfChange, null);
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        chooseEmptyView(gridview);
                    }
                }
        );

    }

    /**
     * Executes whenever the calendar activity comes into view.
     *
     * @inheritDoc
     *
     * Only here we check if the user needs to log in for the first time and if he has
     * agreed to the privacy agreement. This assumes that the Calendar activity is the only entry
     * point to the app, which it currently is. If there are other entry points, we would have to check
     * there as well,
     * \todo: Think if we can/should better move the maybe_make_first_login() and assure_privacy_agreement
     * \todo checks to the onResume method of the MyBaseActivity class.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (maybe_make_first_login()) return;
        if (assure_privacy_agreement()) return;
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        chooseEmptyView(gridview);
    }

    /**
     * Selects heuristically the most relevant semester for the calendar to display.
     */
    private String get_best_semester() {
        List<Semester> semesters = this.dbObjectBuilder.getSemesterList(true);
        if (semesters.isEmpty()) {
            // If there are no semesters (because no lectures are active), show the current semester
            return Semester.get_current_semester_string();
        }
        // Else show the latest semester with an active lecture.
        Collections.sort(semesters);
        return semesters.get(semesters.size() - 1).to_string();

    }

    /**
     * Sets the empty view of the Activity's grid view
     *
     * If syncing is activated and there are no lectures (active or inactive) in the content provider,
     * the empty view tells the user that the phone is syncing.
     * \todo: Only say we are syncing if we actually are, do not claim it if the phone is offline
     * If sync is deactivated, the empty view tells the user about it.
     * Else, the empty view just says that there are no lectures in a given semester.
     *
     * @param gridView the view for which the empty view is being set
     */
    private void chooseEmptyView(GridView gridView) {
        if (dbObjectBuilder.getLectureList(false).isEmpty()) { //this should be false if we have synced ever before.
            gridView.setEmptyView(findViewById(R.id.initial_sync_view));
            SurveyContentProvider.syncWithUrgency(this);
            if (! (SurveyContentProvider.isMasterSyncSettingTrue() && SurveyContentProvider.isAccountSyncSettingTrue(this)) ){
                //somehow, sync got deactivated. We will not be able to sync. we must inform the user about this.
            }
        } else {
            gridView.setEmptyView(findViewById(R.id.emptyView));
        }
    }

    /**
     * Starts the AddLectureChooseSemester activity.
     *
     * This is an onclick listener.
     * @param view
     */
    public void onClickManageLectures(View view) {
        this.startActivity(new Intent(this, AddLectureChooseSemester.class));
    }


}

