package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
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
    private final String sSemester;


    public CalendarAdapter(Context context, String semester) {
        super(context);
        mContext = context;
        sSemester = semester;
        updateMembers();
        Handler handler = new Handler(Looper.getMainLooper());
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/"),true, new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                this.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                updateMembers();
                notifyDataSetChanged();
            }
        }
        );

    }

    public void updateMembers(){

        this.mLectures = dbObjectBuilder.getLecturesOfSemester(sSemester, true);
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
        final TextView weekButton;
        if (convertView == null) {
            weekButton = (TextView) inflater.inflate(R.layout.week_button, null);
        } else {
            weekButton = (TextView) convertView;
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
