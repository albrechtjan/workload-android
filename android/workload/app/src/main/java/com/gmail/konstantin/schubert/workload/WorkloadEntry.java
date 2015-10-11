package com.gmail.konstantin.schubert.workload;


public class WorkloadEntry {


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
