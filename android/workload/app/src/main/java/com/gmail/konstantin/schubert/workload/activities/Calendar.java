package com.gmail.konstantin.schubert.workload.activities;


import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;


public class Calendar extends MyBaseActivity {

     Semester sSemester;
     DBObjectBuilder dbObjectBuilder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = this.getContentResolver();
        dbObjectBuilder = new DBObjectBuilder(resolver);


        if(dbObjectBuilder.getLectureList(false).isEmpty()){ //this should be false if we have synced ever before.
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(AccountManager.get(this).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, settingsBundle);
        }

        setContentView(R.layout.activity_calendar);
        sSemester = new Semester(get_best_semester());
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        if(dbObjectBuilder.getLectureList(false).isEmpty()) { //this should be false if we have synced ever before.
            gridview.setEmptyView(findViewById(R.id.initial_sync_view));
        } else {
            gridview.setEmptyView(findViewById(R.id.emptyView));
        }
        gridview.setAdapter(new CalendarAdapter(this,sSemester.to_string()));
        final TextView semesterText = (TextView) findViewById(R.id.calendar_semester);
        semesterText.setText(sSemester.to_string());
        View.OnClickListener semesterButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.next_semester_button) {
                    Calendar.this.sSemester = Calendar.this.sSemester.get_next();
                }else if(v.getId()==R.id.previous_semester_button){
                    Calendar.this.sSemester = Calendar.this.sSemester.get_previous();
                }
                semesterText.setText(sSemester.to_string());
                gridview.setAdapter(new CalendarAdapter(Calendar.this, sSemester.to_string()));
            }
        };
        ImageView previousSemesterButton = (ImageView) findViewById(R.id.previous_semester_button);
        ImageView nextSemesterButton = (ImageView) findViewById(R.id.next_semester_button);
        previousSemesterButton.setOnClickListener(semesterButtonListener);
        nextSemesterButton.setOnClickListener(semesterButtonListener);

    }

    @Override
    protected void onResume(){
        super.onResume();
        if (maybe_make_first_login()) return;
        if(assure_privacy_agreement()) return;
    }

    private String get_best_semester(){
        // If we are not sure which semester to display (because there is no saved instance)
        // the best semester is considered the newest semester that has a lecture
        // Otherwise it is the newest semester
        List<Semester> semesters= this.dbObjectBuilder.getSemesterList(true);
        Collections.sort(semesters);
        if (semesters.isEmpty()){
            return get_current_semester();
        }
        return semesters.get(semesters.size()-1).to_string();

    }

    static private String get_current_semester(){
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar beginSummerSemester = (java.util.Calendar) now.clone();
        beginSummerSemester.set(java.util.Calendar.MONTH, 4);
        beginSummerSemester.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Calendar beginWinterSemester = (java.util.Calendar) now.clone();
        beginWinterSemester.set(java.util.Calendar.MONTH, 10);
        beginWinterSemester.set(java.util.Calendar.DAY_OF_MONTH, 1);

        int thisYear = now.get(java.util.Calendar.YEAR);
        if (now.before(beginSummerSemester)){
            return "WS"+String.valueOf(thisYear-1)+"/"+String.valueOf(thisYear%100);
        }
        else if (now.before(beginWinterSemester)){
            return "SS"+String.valueOf(thisYear);
        }else{
            return "WS"+String.valueOf(thisYear)+"/"+String.valueOf(thisYear+1%100);
        }
    }

    public void onClickManageLectures(View view){

        this.startActivity(new Intent(this, AddLectureChooseSemester.class) );
    }

}

