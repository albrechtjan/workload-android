package com.gmail.konstantin.schubert.workload.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;


public class Calendar extends MyBaseActivity {

     String sSemester;

    I should probably introduce a semester class that supports to_string(), Semester(String), get_next(), get_previous()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Intent launchIntent = getIntent();
        String semester = launchIntent.getStringExtra("semester");
        if (semester==null){
            sSemester = get_current_semester();
        }
        else{
            sSemester = semester;
        }

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new CalendarAdapter(this,sSemester));
        View.OnClickListener semesterButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Calendar.this, SelectLecture.class);
                intent.putExtra(SelectLecture.MESSAGE_WEEK, week.week());
                if(v.getId()==R.id.next_semester_button) {
                    intent.putExtra("semester",get_next_semester(sSemester));
                }else if(v.getId()==R.id.previous_semester_button){
                    intent.putExtra("semester",get_previous_semester(sSemester));
                }
                Calendar.this.startActivity(intent);
            }
        };
        ImageView previousSemesterButton = (ImageView) findViewById(R.id.previous_semester_button);
        ImageView nextSemesterButton = (ImageView) findViewById(R.id.next_semester_button);
        previousSemesterButton.setOnClickListener(semesterButtonListener);
        nextSemesterButton.setOnClickListener(semesterButtonListener);

    }

    static private String get_current_semester(){
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar beginSummerSemester = (java.util.Calendar) now.clone();
        beginSummerSemester.set(java.util.Calendar.MONTH, 4);
        beginSummerSemester.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Calendar beginWinterSemester = (java.util.Calendar) now.clone();
        beginWinterSemester.set(java.util.Calendar.MONTH,10);
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


}

