package com.gmail.konstantin.schubert.workload;

/**
 * Class representing a lecture
 */
public class Lecture {

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


    public WorkloadEntry[] getWorkloadEntrie() {
        //Stub
        return null;
    }


}
