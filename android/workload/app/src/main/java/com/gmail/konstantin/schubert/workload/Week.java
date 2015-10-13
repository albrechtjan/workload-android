package com.gmail.konstantin.schubert.workload;


import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Partial;



/**
 * Simple Week representation aimed to mirror the most important functions of android's week.isoweek
 */
public class Week implements Comparable<Week> {

    Partial yearWeek;

    public Week(int year, int weeknumber) {
        yearWeek =  new Partial(
                new DateTimeFieldType[] {DateTimeFieldType.weekyear(),
                        DateTimeFieldType.weekOfWeekyear()},
                new int[] {year, weeknumber});
    }


    public static Week getWeekFromISOString(String s) {


        LocalDate date = LocalDate.parse(s);
        Week week = new Week(date.getYear(), date.getWeekOfWeekyear());
        return week;

    }

    public Week getNextWeek() {

        Partial nextWeekPartial = yearWeek.property(DateTimeFieldType.weekOfWeekyear()).addToCopy(1);
        Week newWeek = new Week(nextWeekPartial.get(DateTimeFieldType.weekyear()), nextWeekPartial.get(DateTimeFieldType.weekOfWeekyear()));
        return newWeek;
    }

    public LocalDate firstDay() {
        LocalDate date = new LocalDate();
        date = date.withYear(yearWeek.get(DateTimeFieldType.weekyear()));
        date = date.withWeekOfWeekyear(yearWeek.get(DateTimeFieldType.weekOfWeekyear()));
        date = date.withDayOfWeek(1);
        return date;
    }

    public LocalDate lastDay() {
        LocalDate date = new LocalDate();
        date = date.withYear(yearWeek.get(DateTimeFieldType.weekyear()));
        date = date.withWeekOfWeekyear(yearWeek.get(DateTimeFieldType.weekOfWeekyear()));
        date = date.withDayOfWeek(7);
        return date;
    }

    public int year() {
        return this.yearWeek.get(DateTimeFieldType.weekyear());
    }

    public int week() {
        return this.yearWeek.get(DateTimeFieldType.weekOfWeekyear());
    }

    public Week copy(){
        int year = yearWeek.get(DateTimeFieldType.weekyear());
        int weekOfYear = yearWeek.get(DateTimeFieldType.weekOfWeekyear());
        return new Week(year, weekOfYear);
    }


    @Override
    public int compareTo(Week other) {
       return this.yearWeek.compareTo(other.yearWeek);
    }


}
