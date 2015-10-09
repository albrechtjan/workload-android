package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;





public class AddLectureAdapter  extends MyBaseAdapter { //BaseAdapter already implements Listadapter

    private List<Lecture> mLectures;
    private Context mContext;
    private final String sSemester;

    public AddLectureAdapter(Context context, String semester) {
        super(context);
        mContext = context;
        sSemester = semester;
        updateMembers();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        RelativeLayout lectureRow; //A button for the lecture wrapped in a LinearLayout
        if (convertView != null) {
            lectureRow = (RelativeLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.add_lecture_row, parent, false);

            //TODO: figure out why the following three lines set the width to zero
//            TextView label = (TextView) lectureRow.findViewById(R.id.semester_name_text_view);
//            ImageView imageView= (ImageView) lectureRow.findViewById(R.id.semester_select_button);
//            label.setMaxWidth(lectureRow.getWidth() - imageView.getWidth());
        }

        TextView label = (TextView) lectureRow.findViewById(R.id.add_lecture_name_text_view);
        label.setText(mLectures.get(position).name);




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


    protected void updateMembers() {
        mLectures = this.dbObjectBuilder.getLecturesOfSemester(sSemester, false);
    }




}
