package com.gmail.konstantin.schubert.workload;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

/**
 * Simple Week representation aimed to mirror the most important functions of android's week.isoweek
 */
public class Week implements Comparable<Week> {

    Calendar mCalendar = Calendar.getInstance();

    public Week(int year, int weeknumber) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.WEEK_OF_YEAR, weeknumber);
    }


    public static Week getWeekFromISOString(String s) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(sdf.parse(s));
            Week week = new Week(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
            return week;
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public Week getNextWeek() {
        Week newWeek = this.copy();

        newWeek.mCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        return newWeek;
    }

    public Calendar firstDay() {
        Calendar c = (Calendar) mCalendar.clone();
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        return c;
    }

    public Calendar lastDay() {
        Calendar c = this.firstDay();
        c.add(Calendar.DAY_OF_MONTH, 6);
        return c;
    }

    public int year() {
        return mCalendar.get(Calendar.YEAR);
    }

    public int week() {
        return mCalendar.get(Calendar.WEEK_OF_YEAR);
    }

    public Week copy() {
        return new Week(mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.WEEK_OF_YEAR));
    }

    @Override
    public int compareTo(Week other) {
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.set(Calendar.YEAR, other.year());
        otherCalendar.set(Calendar.WEEK_OF_YEAR, other.week());
        if (mCalendar.before(otherCalendar)) {
            return -1;
        } else if (mCalendar.after(otherCalendar)) {
            return 1;
        } else {
            return 0;
        }
    }


}
