package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by kon on 25.06.15.
 */
public class WeekButton extends Button {

    public WeekButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    private Week week;


    public Week getWeek() {
        return week;
    }

    public void setWeek(Week week) {
        this.week = week;
    }

}