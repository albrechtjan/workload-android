package com.gmail.konstantin.schubert.workload.activities;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;


/*  Activity showing a calendar holdings the weeks in a certain semester.
 * This is the usual entry point for the app. Here, the user can choose a
 * week and lecture and enter his data, switch to another semester or
 * go to the various settings menus.
 */
public class Calendar extends MyBaseActivity {

    Semester sSemester;
    DBObjectBuilder dbObjectBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ContentResolver resolver = this.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(resolver);


        setContentView(R.layout.activity_calendar);
        sSemester = new Semester(get_best_semester());
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new CalendarAdapter(this, sSemester.to_string()));
        final TextView semesterText = (TextView) findViewById(R.id.calendar_semester);
        semesterText.setText(sSemester.to_string());

        // Change the adapter if next semester is selected
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

    @Override
    protected void onResume() {
        super.onResume();
        if (maybe_make_first_login()) return;
        if (assure_privacy_agreement()) return;
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        chooseEmptyView(gridview);
    }

    private String get_best_semester() {
        // If we are not sure which semester to display (because there is no saved instance)
        // the best semester is considered the newest semester that has a lecture
        // Otherwise it is the newest semester
        List<Semester> semesters = this.dbObjectBuilder.getSemesterList(true);
        Collections.sort(semesters);
        if (semesters.isEmpty()) {
            return Semester.get_current_semester_string();
        }
        return semesters.get(semesters.size() - 1).to_string();

    }

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

    public void onClickManageLectures(View view) {

        this.startActivity(new Intent(this, AddLectureChooseSemester.class));
    }


}

