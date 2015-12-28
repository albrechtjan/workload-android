package com.gmail.konstantin.schubert.workload.activities;


import android.os.Bundle;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Adapters.CalendarAdapter;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.OnSwipeTouchListener;


public class Calendar extends MyBaseActivity {

     Semester sSemester;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        sSemester = new Semester(get_current_semester());
//        this.findViewById(R.id.calendar_semester_selector).setOnTouchListener(new OnSwipeTouchListener(Calendar.this) {
//            public void onSwipeTop() {
//                Toast.makeText(Calendar.this, "top", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeRight() {
//                Toast.makeText(Calendar.this, "right", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeLeft() {
//                Toast.makeText(Calendar.this, "left", Toast.LENGTH_SHORT).show();
//            }
//            public void onSwipeBottom() {
//                Toast.makeText(Calendar.this, "bottom", Toast.LENGTH_SHORT).show();
//            }
//
//        } );

        final GridView gridview = (GridView) findViewById(R.id.gridview);
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
                //TODO: Maybe this instead:
                // adapter.changeSemester();
                // adapter.notifyDatasetChanged


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


}

