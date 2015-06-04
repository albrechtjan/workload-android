package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.database.ContentObserver;
import android.util.Log;
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
    private ContentObserver mContentObserver;
//    private List<Week> mWeeks; TODO: Check if this variable could give any performance improvements
    private List<Lecture> mLectures;


    TODD: http://www.grokkingandroid.com/use-contentobserver-to-listen-to-changes/

    public WeekButtonAdapter(Context context) {
        super(context);
        mContext = context;
        mLectures = getLectureList(true);
    }

    public int getCount() {
        List<Week> weeks = getWeeks(mLectures);
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
        List<Week> weeks = getWeeks(mLectures);
        weekButton.setText(String.valueOf(weeks.get(position).week()));
        return weekButton;
    }

    private List<Week> getWeeks(List<Lecture> lectures){
        Week week = firstWeek(lectures).copy();
        List<Week> weeks = new LinkedList<Week>();
        while(week.compareTo(lastWeek(lectures))<=0){
            weeks.add(week);
            week = week.getNextWeek();
            int test = week.week();
            int test2 = week.year();
        }

        return weeks;
    }



    private Week firstWeek(List<Lecture> lectures){
        List<Week> startWeeks = new ArrayList<>();
        for(Lecture lecture : lectures){
            startWeeks.add(lecture.startWeek);
            Log.d("BBBSS", "added startweek of" + lecture.startWeek.week());
        }
        return Collections.min(startWeeks);
    }

    private Week lastWeek(List<Lecture> lectures){
        List<Week> endWeeks = new ArrayList<>();
        for(Lecture lecture : lectures){
            endWeeks.add(lecture.endWeek);
            Log.d("BBBSS", "added endweek of" + lecture.startWeek.week());
        }
        return Collections.max(endWeeks);

    }
}
