package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gmail.konstantin.schubert.workload.activities.EnterWorkload;

import java.util.List;


public class SelectLectureAdapter extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    //TODO: Check which of these members can move up into MyBaseAdapter
    private Context mContext;
    private Week mWeek;
    private List<Lecture> mLecturesThisWeek;

    public SelectLectureAdapter(Context context, Week week) {
        super(context);
        mContext = context;
        mWeek = week;
        //mWeek and mContext must be initialized before updateMembers is called!
        updateMembers();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        RelativeLayout lectureRow; //A button for the lecture wrapped in a LinearLayout
        if (convertView != null) {
            lectureRow = (RelativeLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.lecture_row_modern, parent, false);
        }

        TextView label = (TextView) lectureRow.getChildAt(0);
        label.setText(mLecturesThisWeek.get(position).name);


        label.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(mContext, EnterWorkload.class);
                intent.putExtra(EnterWorkload.MESSAGE_YEAR, mWeek.year());
                intent.putExtra(EnterWorkload.MESSAGE_WEEK, mWeek.week());
                intent.putExtra(EnterWorkload.MESSAGE_LECTURE, mLecturesThisWeek.get(position)._ID);
                mContext.startActivity(intent);
            }
        });


        return lectureRow;
    }

    public int getCount() {
        return mLecturesThisWeek.size();
    }

    public Object getItem(int position) {
        //TODO:Un-Stub
        return null;
    }

    public long getItemId(int position) {
        //TODO: figure out what this method is good for
        return 0;
    }


    protected void updateMembers() {
        //TODO: Limit this from all Lectures to the lectures in this mWeek
        mLecturesThisWeek = this.dbObjectBuilder.getLecturesInWeek(mWeek, true);
    }

    ;


}
