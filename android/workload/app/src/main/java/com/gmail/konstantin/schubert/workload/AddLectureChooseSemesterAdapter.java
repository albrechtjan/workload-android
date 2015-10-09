package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;





public class AddLectureChooseSemesterAdapter  extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private List<String> mSemesters;
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

            //TODO: figure out why the following three lines set the width to zero
//            TextView label = (TextView) lectureRow.findViewById(R.id.semester_name_text_view);
//            ImageView imageView= (ImageView) lectureRow.findViewById(R.id.semester_select_button);
//            label.setMaxWidth(lectureRow.getWidth() - imageView.getWidth());
        }

        TextView label = (TextView) lectureRow.findViewById(R.id.semester_name_text_view);
        label.setText(mSemesters.get(position));

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


    protected void updateMembers() {
        mSemesters = this.dbObjectBuilder.getSemesterList(true);
    }




}
