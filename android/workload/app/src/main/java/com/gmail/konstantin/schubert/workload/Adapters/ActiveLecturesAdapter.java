package com.gmail.konstantin.schubert.workload.Adapters;


import android.content.Context;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.MyBaseAdapter;
import com.gmail.konstantin.schubert.workload.R;

import java.util.List;

public class ActiveLecturesAdapter  extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private List<Lecture> mActiveLectures;
    private Context mContext;

    public ActiveLecturesAdapter(Context context) {
        super(context);
        mContext = context;
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
        label.setText(mActiveLectures.get(position).name);


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
        mActiveLectures = this.dbObjectBuilder.getLectureList(true, false);
    }

    ;


}
