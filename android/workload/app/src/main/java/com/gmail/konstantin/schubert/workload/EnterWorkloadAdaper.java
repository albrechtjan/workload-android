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
    private final int sLectureId;

    public EnterWorkloadAdaper(Context context, Week week, int lectureId) {
        super(context);
        mContext = context;
        sWeek = week;
        sLectureId = lectureId;
        updateMembers();
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        //With three rows only, we are ignoring the convertView

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lectureRow = (LinearLayout) inflater.inflate(R.layout.workload_row, parent, false);

        TextView textView = (TextView) lectureRow.getChildAt(0);
        textView.setText(EnterWorkload.ROW_TITLES.get(position));
        EditText editText = (EditText) lectureRow.getChildAt(1);
        //TODO:Load and set default value

        editText.addTextChangedListener(new WorkloadTextWatcher(position, this));


        class WorkloadTextWatcher implements TextWatcher {
            public final int fWatchedPosition;
//            public final MyBaseAdapter;

            WorkloadTextWatcher(int position, int adapter){
                this.fWatchedPosition = position;
            }

            public void afterTextChanged(Editable s) {

//                mAdapter.updateMembers();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        }




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


    protected void updateMembers(){

    };


}
