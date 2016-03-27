package com.gmail.konstantin.schubert.workload.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.Collections;
import java.util.List;


public class AddLectureAdapter extends MyBaseAdapter {

    private final String sSemester;
    private List<Lecture> mLectures;
    private Context mContext;

    public AddLectureAdapter(Context context, String semester) {
        super(context);
        mContext = context;
        sSemester = semester;
        updateMembers(null);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        RelativeLayout lectureRow; //A button for the lecture wrapped in a LinearLayout
        if (convertView != null) {
            lectureRow = (RelativeLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.add_lecture_row, parent, false);
        }
        final Lecture lecture = mLectures.get(position);

        TextView label = (TextView) lectureRow.findViewById(R.id.add_lecture_name_text_view);
        label.setText(lecture.name);

        final Switch activeSwitch = (Switch) lectureRow.findViewById(R.id.add_lecture_select_switch);
        activeSwitch.setChecked(lecture.isActive);

        activeSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lecture.isActive = activeSwitch.isChecked();
                dbObjectBuilder.updateLecture(lecture, SurveyContentProvider.SYNC_STEER_COMMAND.SYNC);
            }
        });

        return lectureRow;
    }

    public int getCount() {
        return mLectures.size();
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
        mLectures = this.dbObjectBuilder.getLecturesOfSemester(sSemester, false);
        Collections.sort(mLectures);
    }


}
