package com.gmail.konstantin.schubert.workload;


public class Semester implements Comparable<Semester> {

//    to_string(), Semester(String), get_next(), get_previous()


    private final SemesterType sSemesterType;
    private int year; //by definition the year in which the semester starts
    public Semester(String s) {
        if (s.contains("WS")) {
            this.sSemesterType = SemesterType.WS;
        } else {
            this.sSemesterType = SemesterType.SS;
        }
        this.year = Integer.valueOf(s.substring(2, 6));
    }


    public Semester(int year, SemesterType semesterType) {
        this.sSemesterType = semesterType;
        this.year = year;
    }

    public static String get_current_semester_string() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar beginSummerSemester = (java.util.Calendar) now.clone();
        beginSummerSemester.set(java.util.Calendar.MONTH, 4);
        beginSummerSemester.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.util.Calendar beginWinterSemester = (java.util.Calendar) now.clone();
        beginWinterSemester.set(java.util.Calendar.MONTH, 10);
        beginWinterSemester.set(java.util.Calendar.DAY_OF_MONTH, 1);

        int thisYear = now.get(java.util.Calendar.YEAR);
        if (now.before(beginSummerSemester)) {
            return "WS" + String.valueOf(thisYear - 1) + "/" + String.valueOf(thisYear % 100);
        } else if (now.before(beginWinterSemester)) {
            return "SS" + String.valueOf(thisYear);
        } else {
            return "WS" + String.valueOf(thisYear) + "/" + String.valueOf(thisYear + 1 % 100);
        }
    }

    public Semester get_next() {
        if (this.sSemesterType.equals(SemesterType.SS)) {
            return new Semester(this.year, SemesterType.WS);
        } else {
            return new Semester(this.year + 1, SemesterType.SS);
        }
    }

    public Semester get_previous() {
        if (this.sSemesterType.equals(SemesterType.SS)) {
            return new Semester(this.year - 1, SemesterType.WS);
        } else {
            return new Semester(this.year, SemesterType.SS);
        }
    }

    public String to_string() {
        if (this.sSemesterType.equals(SemesterType.SS)) {
            return "SS" + String.valueOf(year);
        } else {
            return "WS" + String.valueOf(year) + "/" + String.valueOf((year + 1) % 100);
        }
    }

    public int compareTo(Semester other) {
        if (this.year < other.year) {
            return -1;
        } else if (this.year > other.year) {
            return 1;
        } else {
            if (this.sSemesterType.equals(other.sSemesterType)) {
                return 0;
            } else {
                if (this.sSemesterType.equals(SemesterType.SS)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    private enum SemesterType {
        WS,
        SS
    }
}
