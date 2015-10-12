package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.MyBaseAdapter;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.WeekButton;
import com.gmail.konstantin.schubert.workload.activities.SelectLecture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class WeekButtonAdapter extends MyBaseAdapter {
    private Context mContext;
    private List<Week> mWeeks;
    private List<Lecture> mLectures;



    public WeekButtonAdapter(Context context) {
        super(context);
        mContext = context;
        this.updateMembers();
    }

    protected void updateMembers(){

        this.mLectures = dbObjectBuilder.getLectureList(true, false);
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
        final WeekButton weekButton;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            //TODO: Use View.inflate instead of the LayoutInflater
            weekButton = (WeekButton) inflater.inflate(R.layout.week_button, null);
        } else {
            weekButton = (WeekButton) convertView;
        }

        weekButton.setWeek(mWeeks.get(position));

        Calendar firstDay = weekButton.getWeek().firstDay();
        String firstDayString = firstDay.get(Calendar.DAY_OF_MONTH) + ". " + firstDay.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
        Calendar lastDay = weekButton.getWeek().lastDay();
        String lastDayString = lastDay.get(Calendar.DAY_OF_MONTH) + ". " + lastDay.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
        weekButton.setText(firstDayString + "\n-\n" +lastDayString);
        weekButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SelectLecture.class);
                intent.putExtra(SelectLecture.MESSAGE_YEAR, weekButton.getWeek().year());
                intent.putExtra(SelectLecture.MESSAGE_WEEK, weekButton.getWeek().week());
                mContext.startActivity(intent);
            }
        });

        return weekButton;
    }

    private List<Week> getWeeks(List<Lecture> lectures){
        if(lectures.isEmpty()){
            // if there are not lectures, then the list of weeks is empty as well.
            return new LinkedList<Week>();
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
