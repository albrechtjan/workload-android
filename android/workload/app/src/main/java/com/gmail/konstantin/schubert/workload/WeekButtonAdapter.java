package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WeekButtonAdapter extends MyBaseAdapter {
    private Context mContext;

    public WeekButtonAdapter(Context context) {
        super(context);
        mContext = context;
    }

    public int getCount() {
        List<Lecture> lectures = getLectureList(true);
        List<Week> weeks = getWeeks(lectures);
        return weeks.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        Button weekButton;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            // TODO: Use an XML layout for the button
            weekButton = new Button(mContext);
            weekButton.setLayoutParams(new GridView.LayoutParams(85, 85));
            weekButton.setPadding(2, 2, 2, 2);
        } else {
            weekButton = (Button) convertView;
        }

        return weekButton;
    }

    private List<Week> getWeeks(List<Lecture> lectures){
        Week week = firstWeek(lectures);
        List<Week> weeks = new LinkedList<Week>();
        while(week.compareTo(lastWeek(lectures))<=0){
            weeks.add(week.copy());
            week.addWeeks(1);
        }

        return weeks;
    }



    private Week firstWeek(List<Lecture> lectures){
        List<Week> startWeeks = new ArrayList<>();
        for(Lecture lecture : lectures){
            startWeeks.add(lecture.startWeek);
        }
        return Collections.min(startWeeks);
    }

    private Week lastWeek(List<Lecture> lectures){
        List<Week> endWeeks = new ArrayList<>();
        for(Lecture lecture : lectures){
            endWeeks.add(lecture.endWeek);
        }
        return Collections.min(endWeeks);

    }
}
