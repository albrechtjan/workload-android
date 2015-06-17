package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.widget.GridView.*;

public class WeekButtonAdapter extends MyBaseAdapter {
    private Context mContext;
    private ContentObserver mContentObserver;
    private List<Week> mWeeks;
    private List<Lecture> mLectures;
    private final WeekObserver sWeekObserver;


    public WeekButtonAdapter(Context context) {
        super(context);
        mContext = context;
        mLectures = getLectureList(true);
        mWeeks = getWeeks(mLectures);
        sWeekObserver = new WeekObserver(new Handler(), this);
        //TODO: Check if this works
        mContext.getContentResolver().registerContentObserver(
                Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"),
                false,
                sWeekObserver);
    }

    public void updateLectureList(){
        this.mLectures = getLectureList(true);
    }

    public int getCount() {
        return mWeeks.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Button weekButton;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            weekButton = (Button) inflater.inflate(R.layout.week_button, null);
        } else {
            weekButton = (Button) convertView;
        }

        weekButton.setText(String.valueOf(mWeeks.get(position).week()));
        return weekButton;
    }

    private List<Week> getWeeks(List<Lecture> lectures){
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

    class WeekObserver extends ContentObserver {
        final WeekButtonAdapter weekButtonAdapter;

        public WeekObserver(Handler handler, WeekButtonAdapter weekButtonAdapter) {
            super(handler);
            this.weekButtonAdapter = weekButtonAdapter;
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            weekButtonAdapter.updateLectureList();
            weekButtonAdapter.notifyDataSetChanged();
        }
    }

}
