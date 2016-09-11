package com.gmail.konstantin.schubert.workload.Adapters;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;


/**
 *  Adapter for the ActiveLectures.java activity.
 *
 * This acts as a list adapter. It inherits from BaseAdapter which already implements the ListAdapter
 * interface.
 * Keeps a list of the active lectures of the user.
 * Directly uses a cursor instead of building intermediate java objects.
 */
public class ManageLecturesAdapter extends MyBaseAdapter {

    private Cursor mActiveLectures;
    private Context mContext;

    public ManageLecturesAdapter(Context context) {
        super(context);
        mContext = context;
        updateMembers(null);
    }

    /**
     *  Returns the view for one list item
     *  @param position index of the list item
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RelativeLayout lectureRow;
        if (convertView != null) {
            lectureRow = (RelativeLayout) convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lectureRow = (RelativeLayout) inflater.inflate(R.layout.lecture_row_modern_trashcan, parent, false);
        }

        mActiveLectures.moveToPosition(position);
        int nameIndex = mActiveLectures.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.NAME);
        final int idIndex = mActiveLectures.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID);

        TextView label = (TextView) lectureRow.getChildAt(0);
        label.setText(mActiveLectures.getString(nameIndex));

        ImageButton deleteButton = (ImageButton) lectureRow.getChildAt(1);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActiveLectures.moveToPosition(position);
                int lectureID = mActiveLectures.getInt(idIndex);
                ContentValues values = new ContentValues();
                values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE, 0);
                String uri = "content://" + SurveyContentProvider.AUTHORITY + "/lectures/";
                uri += SurveyContentProvider.SYNC_STEER_COMMAND.SYNC +"/";
                uri += String.valueOf(lectureID) + "/";
                int result = mContentResolver.update(Uri.parse(uri), values, null, null);
                if (result < 0) {
                    Log.d("ManageLecturesAdapter", "Could not update " + uri + ". Maybe SYNC_STATUS of row is not IDLE");
                }
                updateMembers(null);
            }
        });


        return lectureRow;
    }

    /**
     *  Returns the number of active lectures.
     *
     *  By definition this is also the number of list items.
     */
    @Override
    public int getCount() {
        return mActiveLectures.getCount();
    }
    @Override
    public Object getItem(int position) {
        //stub
        return null;
    }
    @Override
    public long getItemId(int position) {
        //stub
        return 0;
    }


    /* Is called when database changes.
     * Replaces the old cursor with a new one, then notifies the activity.
     */
    @Override
    public void updateMembers(Uri uri) {
        mActiveLectures = this.dbObjectBuilder.getLectureCursor(true);
        notifyDataSetChanged();
    }


}
