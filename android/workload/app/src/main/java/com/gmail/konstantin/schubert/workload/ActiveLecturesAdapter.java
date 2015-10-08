package com.gmail.konstantin.schubert.workload;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.activities.EnterWorkload;

import java.util.List;

public class ActiveLecturesAdapter  extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private Week mWeek;
    private List<Lecture> mActiveLectures;
    private Context mContext;

    public ActiveLecturesAdapter(Context context) {
        super(context);
        mContext = context;
        updateMembers();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LinearLayout lectureRow; //A button for the lecture wrapped in a LinearLayout
        if (convertView != null) {
            lectureRow = (LinearLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (LinearLayout) inflater.inflate(R.layout.lecture_row_modern, parent, false);
        }

        TextView label = (TextView) lectureRow.getChildAt(0);
        label.setText(mActiveLectures.get(position).name);

//
//        lectureButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Intent intent = new Intent(mContext, EnterWorkload.class);
//                intent.putExtra(EnterWorkload.MESSAGE_YEAR, mWeek.year());
//                intent.putExtra(EnterWorkload.MESSAGE_WEEK, mWeek.week());
//                intent.putExtra(EnterWorkload.MESSAGE_LECTURE, mLecturesThisWeek.get(position)._ID);
//                mContext.startActivity(intent);
//            }
//        });


        return lectureRow;
    }

    public int getCount() {
        return mActiveLectures.size();
    }

    public Object getItem(int position) {
        //stub
        return null;
    }

    public long getItemId(int position) {
        //stub
        return 0;
    }


    protected void updateMembers() {
        mActiveLectures = this.dbObjectBuilder.getLectureList(true);
    }

    ;


}
