package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.activities.SelectLecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CalendarAdapter extends MyBaseAdapter {
    private Context mContext;
    private List<Week> mWeeks;
    private List<Lecture> mLectures;



    public CalendarAdapter(Context context) {
        super(context);
        mContext = context;
        mContext.getContentResolver().registerContentObserver(..
        update the members and re-draw when the lectures or the entries (because of the color-coding of the buttons) change
        );
        this.updateMembers();
    }

    public void updateMembers(){

        this.mLectures = dbObjectBuilder.getLectureList(true);
        this.mWeeks = getWeeks(this.mLectures);
    }

    public int getCount() {
        return mWeeks.size();
    }

    public Object getItem(int position) {
        //TODO: Figure out if I can really return 0 here.
        return null;
    }

    public long getItemId(int position) {
        //TODO: figure out what this method is good for
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Button weekButton;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            //TODO: Use View.inflate instead of the LayoutInflater?
            weekButton = (Button) inflater.inflate(R.layout.week_button, null);
        } else {
            weekButton = (Button) convertView;
        }

        final Week week = mWeeks.get(position);

        String first_day = week.firstDay().toString("dd.MMM");
        String last_day = week.lastDay().toString("dd.MMM");

        weekButton.setText(first_day+"\n-\n"+last_day);
        weekButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SelectLecture.class);
                intent.putExtra(SelectLecture.MESSAGE_YEAR, week.year());
                intent.putExtra(SelectLecture.MESSAGE_WEEK, week.week());
                mContext.startActivity(intent);
            }
        });

        return weekButton;
    }

    private List<Week> getWeeks(List<Lecture> lectures){
        if(lectures.isEmpty()){
            // if there are not lectures, then the list of weeks is empty as well.
            return new LinkedList<>();
        }
        Week week = firstWeek(lectures).copy();
        List<Week> weeks = new LinkedList<Week>();
        final Week last = lastWeek(lectures);
        while(week.compareTo(last)<=0){
            weeks.add(week);
            week = week.getNextWeek();
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
        return Collections.max(endWeeks);

    }


}
