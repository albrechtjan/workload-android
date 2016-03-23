package com.gmail.konstantin.schubert.workload.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.Semester;
import com.gmail.konstantin.schubert.workload.activities.AddLecture;

import java.util.List;


public class AddLectureChooseSemesterAdapter extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private List<Semester> mSemesters;
    private Context mContext;

    public AddLectureChooseSemesterAdapter(Context context) {
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
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.semester_row, parent, false);
        }

        TextView label = (TextView) lectureRow.findViewById(R.id.semester_name_text_view);
        label.setText(mSemesters.get(position).to_string());


        lectureRow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(mContext, AddLecture.class);
                intent.putExtra(AddLecture.SEMESTER, mSemesters.get(position).to_string());
                Activity activity = (Activity) mContext; // this is like cheating
                mContext.startActivity(intent);
                activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                //doing this after startActivity is strange. Does this mean the startActivity method does not
                // start the activity before the onClick method has finished.
            }
        });


        return lectureRow;
    }

    public int getCount() {
        return mSemesters.size();
    }

    public Object getItem(int position) {
        //stub
        return null;
    }

    public long getItemId(int position) {
        //stub
        return 0;
    }


    public void updateMembers() {
        mSemesters = this.dbObjectBuilder.getSemesterList(false);
    }


}
