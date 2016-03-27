package com.gmail.konstantin.schubert.workload.Adapters;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;

public class ManageLecturesAdapter extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private List<Lecture> mActiveLectures;
    private Context mContext;

    public ManageLecturesAdapter(Context context) {
        super(context);
        mContext = context;
        updateMembers(null);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        RelativeLayout lectureRow;
        if (convertView != null) {
            lectureRow = (RelativeLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.lecture_row_modern_trashcan, parent, false);
        }

        final Lecture lecture = mActiveLectures.get(position);
        TextView label = (TextView) lectureRow.getChildAt(0);
        label.setText(lecture.name);

        ImageButton deleteButton = (ImageButton) lectureRow.getChildAt(1);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lecture.isActive = false;
                dbObjectBuilder.updateLecture(lecture, SurveyContentProvider.SYNC_STEER_COMMAND.SYNC);
                //TODO: Make this more efficient
                updateMembers(null);
                notifyDataSetChanged();
            }
        });


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


    public void updateMembers(Uri uri) {
        mActiveLectures = this.dbObjectBuilder.getLectureList(true);
        Collections.sort(mActiveLectures);
        notifyDataSetChanged();
    }


}
