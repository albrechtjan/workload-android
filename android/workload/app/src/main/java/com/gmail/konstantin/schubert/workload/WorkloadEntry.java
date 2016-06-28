package com.gmail.konstantin.schubert.workload;


import android.database.Cursor;
import java.io.Serializable;

/**
 * Models a single workload entry.
 *
 * That is, the entered data for one lecture in a certain week
 */
public class WorkloadEntry implements Serializable {


    public final int lecture_id;
    public final Week week;

    /* \todo: Use a 'decimal' type instead of float
     * \todo: Check if these can be made public final
     */
    private float hoursInLecture;
    private float hoursForHomework;
    private float hoursStudying;

    /**
     * Constructs an instance
     *
     * @param week week that the entry is for
     * @param lecture_id id of the lecture that the entry is for
     *
     * I decided against passing an actual lecture object here since these might have multiple instances for a certain index
     * Maybe one day I will use a pattern that enforces only one instance per ID and then I change this.
     *
     * */
    public WorkloadEntry(Week week, int lecture_id, float hoursInLecture, float hoursForHomework, float hoursStudying) {
        this.week = week;
        this.lecture_id = lecture_id;
        this.hoursInLecture = hoursInLecture;
        this.hoursForHomework = hoursForHomework;
        this.hoursStudying = hoursStudying;
    }


    /**
     * Constructs an instance from a cursor.
     *
     * It constructs the instance from the *FIRST* entry in the cursor
     * \todo: It should just construct from the entry of the cursor that the cursor is currently
     * \todo pointing to. The caller should be responsible for setting the cursor to the right row.
     *
     */
    public WorkloadEntry(Cursor cursor) {
        cursor.moveToFirst();
        this.week = new Week(
                cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR)),
                cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK))
        );
        this.lecture_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID));
        this.hoursInLecture = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE));
        this.hoursForHomework = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK));
        this.hoursStudying = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING));
        cursor.close();
    }

    /**
     * Defines equality between two workload entries.
     *
     * The entries are defined as equal iff they belong to the same week and the same lecture.
     * This does NOT require that the entered values are equal.
     */
    @Override
    public boolean equals(Object otherObject) {
        WorkloadEntry other = (WorkloadEntry) otherObject;
        return (other.lecture_id == this.lecture_id) && (other.week.compareTo(this.week) == 0);
    }

    /**
     * Defines *exact* equality between two workload entries
     *
     * Additionally to the equality defined in @equals, this also requires that the entered data is
     * identical.
     * \todo: We are comparing floats here, right? Does this even work? The  fields should be of
     * \todo  type "decimal"
     */
    public boolean equals_exactly(WorkloadEntry otherEntry) {
        return this.equals(otherEntry)
                && this.hoursForHomework == otherEntry.hoursForHomework
                && this.hoursInLecture == otherEntry.hoursInLecture
                && this.hoursStudying == otherEntry.hoursStudying;
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



}
