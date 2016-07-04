package com.gmail.konstantin.schubert.workload;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.Serializable;

/**
 * Class representing a lecture
 */
public class Lecture implements Serializable, Comparable<Lecture> {

    public final int _ID;
    public final String name;
    public final String semester;
    public final Week startWeek;
    public final Week endWeek;
    public boolean isActive;

    public Lecture(int _ID, String name, String semester, Week startWeek, Week endWeek, boolean isActive) {
        this._ID = _ID;
        this.name = name;
        this.semester = semester;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object otherObject) {
        Lecture other = (Lecture) otherObject;
        return (this._ID == other._ID && this.name.equals(other.name) && this.semester.equals(other.semester));
    }

    public boolean equals_exactly(Lecture other) {
        return (
                this.equals(other)
                        && (this.isActive == other.isActive)
                        && (this.startWeek.equals(other.startWeek))
                        && (this.endWeek.equals(other.endWeek))
        );
    }

    public boolean hasDataInWeek(ContentResolver resolver, Week week) {
        String query = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(this._ID) + " AND "
                + SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(week.year()) + " AND "
                + SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(week.week());
        Cursor cursor = resolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/sync/any/"), null, query, null, null);
        boolean hasData = cursor.getCount() != 0;
        cursor.close();
        return hasData;
    }

    @Override
    public int compareTo(Lecture another) {
        return this.name.compareTo(another.name);
    }
}
