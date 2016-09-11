package com.gmail.konstantin.schubert.workload.activities;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.ReminderReceiver;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


/**
 *  Activity showing a calendar holdings the weeks in a certain semester.
 * This is the usual entry point for the app. Here, the user can choose a
 * week and lecture and enter his data, switch to another semester or
 * go to the various menus.
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

        startAlarmManager();



    }

    /**
     * Executes whenever the calendar activity comes into view.
     *
     * @inheritDoc
     *
     * We check if the user needs to log in for the first time and if he has
     * agreed to the privacy agreement. This should create a streamlined experience when
     * first starting the app. The privacy agreement is also checked for in the activity where the
     * user enters data.
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

    /**
     * Set up the AlarmManager for the weekly reminder
     *
     * If it has been set up already with an intent with the same request code,
     * this will simply overwrite the previous alarm manager
     * //\todo: make sure this is the case
     */
    private void startAlarmManager(){

        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 004, intent, 0);
        //\todo Try FLAG_UPDATE_CURRENT in the last argument
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        java.util.Calendar calendar = java.util.Calendar.getInstance(); // instantiated with current time
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
        calendar.set(java.util.Calendar.HOUR_OF_DAY,19);
        calendar.set(java.util.Calendar.MINUTE, 12);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        long interval = AlarmManager.INTERVAL_DAY * 7;

        // DEBUGGING:
        //calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        //calendar.set(java.util.Calendar.HOUR_OF_DAY,1);
        //calendar.set(java.util.Calendar.MINUTE, 42);
        //long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;


        if(calendar.before(java.util.Calendar.getInstance())) {
            // if reminder moment is in the past, set the alarm for next week
            calendar.setTimeInMillis(calendar.getTimeInMillis() + interval);
            // if this condition is executed between the moment that the weekly alarm is fired
            // and the moment that the broadcast receiver is started, this might cancel the
            // PendingIntent and cause the notification not to be shown for the given week.
            // This seems like a very minos issue though.
        }
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent); //  for testing
        Log.d("Calendar", "setting up alarm manager");
    }

}

