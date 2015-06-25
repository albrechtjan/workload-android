package com.gmail.konstantin.schubert.workload;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Simple Week representation aimed to monitor the most important functions of android's week.isoweek
 */
public class Week implements Comparable<Week>{

    Calendar mCalendar = Calendar.getInstance();

    public Week(int year,int weeknumber){
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.WEEK_OF_YEAR, weeknumber);
    }

    public Week getNextWeek(){
        Week newWeek = this.copy();

        newWeek.mCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        return newWeek;
    }

    public Calendar firstDay(){
        Calendar c = (Calendar) mCalendar.clone();
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        return c;
    }

    public Calendar lastDay(){
        Calendar c = this.firstDay();
        c.add(Calendar.DAY_OF_MONTH,6);
        return c;
    }

    public int year(){
        return mCalendar.get(Calendar.YEAR);
    }

    public int week(){
        return mCalendar.get(Calendar.WEEK_OF_YEAR);
    }

    public Week copy(){
        return new Week(mCalendar.get(mCalendar.YEAR), mCalendar.get(mCalendar.WEEK_OF_YEAR));
    }

    @Override
    public int compareTo(Week other) {
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.set(Calendar.YEAR, other.year());
        otherCalendar.set(Calendar.WEEK_OF_YEAR, other.week());
        if (mCalendar.before(otherCalendar)){
            return -1;
        }
        else if (mCalendar.after(otherCalendar)){
            return 1;
        } else {
            return 0;
        }
    }


}
