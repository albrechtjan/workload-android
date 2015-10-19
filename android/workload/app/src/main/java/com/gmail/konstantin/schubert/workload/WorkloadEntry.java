package com.gmail.konstantin.schubert.workload;


import android.database.Cursor;

import java.io.Serializable;

public class WorkloadEntry{


    // since all variables are final, make them public
    public final Week week;
    public final int lecture_id;


    /* I decided against passing an actual lecture object here since these might have multiple instances for a certain index
     * Maybe one day I will use a pattern that enforces only one instance per ID and then I change this. */
    public WorkloadEntry(Week week, int lecture_id, float hoursInLecture, float hoursForHomework, float hoursStudying) { //use a dict here to make method signatures more future-proof?
        this.week = week;
        this.lecture_id = lecture_id;
        this.hoursInLecture = hoursInLecture;
        this.hoursForHomework = hoursForHomework;
        this.hoursStudying = hoursStudying;
    }

    public WorkloadEntry(Cursor cursor){
        cursor.moveToFirst();
        this.week = new Week(
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR)),
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK))
        );
        this.lecture_id = cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID));
        this.hoursInLecture = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE));
        this.hoursForHomework = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK));
        this.hoursStudying = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING));
        cursor.close();
    }

    @Override
    public boolean equals(Object otherObject){
        WorkloadEntry other = (WorkloadEntry) otherObject;
        return (other.lecture_id == this.lecture_id) && other.week.equals(this.week);
    }



    public float getHoursInLecture() {
        return hoursInLecture;
    }

    public void setHoursInLecture(float hoursInLecture) {
        this.hoursInLecture = hoursInLecture;
    }

    public float getHoursForHomework() {
        return hoursForHomework;
    }

    public void setHoursForHomework(float hoursForHomework) {
        this.hoursForHomework = hoursForHomework;
    }

    public float getHoursStudying() {
        return hoursStudying;
    }

    public void setHoursStudying(float hoursStudying) {
        this.hoursStudying = hoursStudying;
    }


    private float hoursInLecture;
    private float hoursForHomework;
    private float hoursStudying;

}
