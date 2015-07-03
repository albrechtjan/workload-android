package com.gmail.konstantin.schubert.workload;


import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class EnterWorkloadAdaper extends MyBaseAdapter  {


    private Context mContext;
    private final Week sWeek;
    private final Lecture sLecture;
    private WorkloadEntry mWorkloadEntry;

    public EnterWorkloadAdaper(Context context, Week week, int lectureId) {
        super(context);
        mContext = context;
        sWeek = week;
        sLecture = getLectureById(lectureId);
        mWorkloadEntry = getOrCreateWorkloadEntry(sLecture,sWeek);
        updateMembers();
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        //With three rows only, we are ignoring the convertView

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lectureRow = (LinearLayout) inflater.inflate(R.layout.workload_row, parent, false);

        TextView textView = (TextView) lectureRow.getChildAt(0);
        textView.setText(EnterWorkload.ROW_TITLES.get(position));
        EditText editText = (EditText) lectureRow.getChildAt(1);
        switch (position){
            case EnterWorkload.ROW_HOURS_ATTENDING:
                editText.setText(String.valueOf(mWorkloadEntry.getHoursInLecture()));
                break;
            case EnterWorkload.ROW_HOURS_HOMEWORK:
                editText.setText(String.valueOf(mWorkloadEntry.getHoursForHomework()));
                break;
            case EnterWorkload.ROW_HOURS_STUDYING:
                editText.setText(String.valueOf(mWorkloadEntry.getHoursStudying()));
                break;
        }


        class WorkloadTextWatcher implements TextWatcher {
            public final int fWatchedPosition;
            public final EnterWorkloadAdaper fAdapter;

            WorkloadTextWatcher(int position, EnterWorkloadAdaper adapter){
                this.fWatchedPosition = position;
                this.fAdapter = adapter;
            }
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    Float hours = Float.valueOf(s.toString());
                    switch (position) {
                        case EnterWorkload.ROW_HOURS_ATTENDING:
                            fAdapter.mWorkloadEntry.setHoursInLecture(hours);
                            break;
                        case EnterWorkload.ROW_HOURS_STUDYING:
                            fAdapter.mWorkloadEntry.setHoursStudying(hours);
                            break;
                        case EnterWorkload.ROW_HOURS_HOMEWORK:
                            fAdapter.mWorkloadEntry.setHoursForHomework(hours);
                            break;
                    }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        }

        editText.addTextChangedListener(new WorkloadTextWatcher(position, this));

        return lectureRow;
    }

    public int getCount(){
        return 3;
    }

    public Object getItem(int position){
        //TODO:Un-Stub
        return null;
    }

    public long getItemId(int position){
        //TODO: figure out what this method is good for
        return 0;
    }

    public void saveWorkload() {
        updateWorkloadEntryInDB(mWorkloadEntry);
    }

    protected void updateMembers(){
        mWorkloadEntry = getOrCreateWorkloadEntry(sLecture,sWeek);
    };

}
