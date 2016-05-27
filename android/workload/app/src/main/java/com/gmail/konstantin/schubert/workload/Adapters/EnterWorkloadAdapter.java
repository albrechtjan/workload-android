package com.gmail.konstantin.schubert.workload.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;
import com.gmail.konstantin.schubert.workload.activities.EnterWorkload;

import java.util.ArrayList;
import java.util.List;


public class EnterWorkloadAdapter extends BaseAdapter {

    private final static String TAG = "EnterWorkloadAdapter";
    private Context mContext;
    private Cursor mCursor;
    private boolean userHasEdited;
    private List<EditText> editTexts = new ArrayList<>();

    public EnterWorkloadAdapter(Context context, Week week, int lectureId) {
        super();
        mContext = context;
        mCursor = getCursor(lectureId, week);
        userHasEdited = false;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //With three rows only, we are ignoring the convertView

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lectureRow = (LinearLayout) inflater.inflate(R.layout.workload_row, parent, false);

        TextView textView = (TextView) lectureRow.getChildAt(0);
        textView.setText(EnterWorkload.ROW_TITLES.get(position));
        EditText editText = (EditText) lectureRow.getChildAt(1);
        switch (position) {
            case EnterWorkload.ROW_HOURS_ATTENDING:
                editText.setText(String.valueOf(mCursor.getFloat(mCursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE))));
                break;
            case EnterWorkload.ROW_HOURS_HOMEWORK:
                editText.setText(String.valueOf(mCursor.getFloat(mCursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK))));
                break;
            case EnterWorkload.ROW_HOURS_STUDYING:
                editText.setText(String.valueOf(mCursor.getFloat(mCursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING))));
                break;

        }
        editTexts.add(position, editText);

        class WorkloadTextWatcher implements TextWatcher {
            public final int fWatchedPosition;
            public final EnterWorkloadAdapter fAdapter;

            WorkloadTextWatcher(int position, EnterWorkloadAdapter adapter) {
                this.fWatchedPosition = position;
                this.fAdapter = adapter;
            }

            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    fAdapter.userHasEdited = true;
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

    public int getCount() {
        return 3;
    }

    public Object getItem(int position) {
        EditText editText = editTexts.get(position);
        return Float.parseFloat(editText.getText().toString());
    }

    public long getItemId(int position) {
        //TODO: figure out what this method is good for
        return position;
    }

    public void saveEditsIfUserHasEdited() {
        if (userHasEdited) {
            WorkloadEntry workloadEntry = new WorkloadEntry(mCursor);
            try {
                workloadEntry.setHoursInLecture((float) getItem(EnterWorkload.ROW_HOURS_ATTENDING));
            } catch (NumberFormatException e){};
            try {
                workloadEntry.setHoursStudying((float) getItem(EnterWorkload.ROW_HOURS_STUDYING));
            } catch (NumberFormatException e){};
            try {
                workloadEntry.setHoursForHomework((float) getItem(EnterWorkload.ROW_HOURS_HOMEWORK));
            } catch (NumberFormatException e){};
            // I damn sure love my python ... can write it in any language
            Toast.makeText(mContext, "saved", Toast.LENGTH_SHORT).show();
            DBObjectBuilder objectBuilder = new DBObjectBuilder(mContext.getContentResolver());
            objectBuilder.updateWorkloadEntry(workloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.SYNC);
        }
    }


    private Cursor getCursor(int lecture_id, Week sWeek) {
        DBObjectBuilder dbObjectBuilder = new DBObjectBuilder(mContext.getContentResolver());
        Cursor cursor = dbObjectBuilder.getWorkloadEntry(lecture_id, sWeek);
        if (cursor.getCount() == 0) {
            dbObjectBuilder.addWorkloadEntry(new WorkloadEntry(sWeek, lecture_id, 0, 0, 0), SurveyContentProvider.SYNC_STEER_COMMAND.SYNC);
            cursor = dbObjectBuilder.getWorkloadEntry(lecture_id, sWeek);
        }
        cursor.moveToFirst();
        return cursor;
    }

}
