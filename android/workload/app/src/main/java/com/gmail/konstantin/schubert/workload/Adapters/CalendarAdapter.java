package com.gmail.konstantin.schubert.workload.Adapters;


import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SquareTextView;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.activities.SelectLecture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CalendarAdapter extends MyBaseAdapter {
    private static final String TAG = CalendarAdapter.class.getSimpleName();
    private final String sSemester;
    private Context mContext;
    private List<Week> mWeeks;
    private List<Lecture> mLectures;

    public CalendarAdapter(Context context, String semester) {
        super(context);
        mContext = context;
        sSemester = semester;
        updateMembers(null);

        ContentResolver.requestSync(AccountManager.get(mContext).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, new Bundle());
    }

    public void updateMembers(Uri uri) {
        Log.d("CalendarAdapter", "calling updateMembers()");
        // We need to update when any of the tables changes

        this.mLectures = dbObjectBuilder.getLecturesOfSemester(sSemester, true);
        this.mWeeks = getWeeks(this.mLectures);
        // in any case, the calender view needs to be updated because additional workload entries
        // mean that some tiles need to be marked as visited
        notifyDataSetChanged();
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


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final SquareTextView weekButton;
        if (convertView == null) {
            weekButton = (SquareTextView) inflater.inflate(R.layout.week_button, null);
        } else {
            weekButton = (SquareTextView) convertView;
        }

        final Week week = mWeeks.get(position);

        String first_day = week.firstDay().toString("dd.MMM");
        String last_day = week.lastDay().toString("dd.MMM");

        weekButton.setText(first_day + "\n-\n" + last_day);
        weekButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SelectLecture.class);
                intent.putExtra(SelectLecture.MESSAGE_YEAR, week.year());
                intent.putExtra(SelectLecture.MESSAGE_WEEK, week.week());
                mContext.startActivity(intent);
            }
        });

        List<Lecture> lecturesThisWeek = this.dbObjectBuilder.getLecturesInWeek(week, true);
        if (allLecturesHaveDataInWeek(lecturesThisWeek, week)) {
            weekButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.visited));
        }


        return weekButton;
    }

    private static List<Week> getWeeks(List<Lecture> lectures) {
        if (lectures.isEmpty()) {
            // if there are not lectures, then the list of weeks is empty as well.
            return new LinkedList<>();
        }
        Week week = firstWeek(lectures).copy();
        List<Week> weeks = new LinkedList<Week>();
        final Week last = lastWeek(lectures);
        while (week.compareTo(last) <= 0) {
            weeks.add(week);
            week = week.getNextWeek();
        }

        return weeks;
    }


    private static Week firstWeek(List<Lecture> lectures) {
        List<Week> startWeeks = new ArrayList<>();
        for (Lecture lecture : lectures) {
            startWeeks.add(lecture.startWeek);
        }
        return Collections.min(startWeeks);
    }

    private static Week lastWeek(List<Lecture> lectures) {
        List<Week> endWeeks = new ArrayList<>();
        for (Lecture lecture : lectures) {
            endWeeks.add(lecture.endWeek);
        }
        return Collections.max(endWeeks);

    }

    private boolean allLecturesHaveDataInWeek(List<Lecture> lectures, Week week) {
        for (Lecture lecture : lectures) {
            if (!lecture.hasDataInWeek(mContext, week)) {
                return false;
            }
        }
        return true;
    }


}
