package com.gmail.konstantin.schubert.workload.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.gmail.konstantin.schubert.workload.Adapters.ManageLecturesAdapter;
import com.gmail.konstantin.schubert.workload.R;

/**
 * Activity where the user can see which lectures he has selected for data entry.
 * He can deactivate lectures by clicking on a trash can button or he can click on
 * a red Floating Action Button in the lower right corner to add a new lecture.
 */
public class ActiveLectures extends MyBaseListActivity {

    private Context mContext;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_active_lectures);
        setListAdapter(new ManageLecturesAdapter(this));
        setTitle("Active Lectures");

        ImageButton addLectureButton = (ImageButton) findViewById(R.id.add_lecture_button);
        addLectureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddLectureChooseSemester.class);
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    /**
     * @inheritDoc
     */
    public void onResume() {
        super.onResume();
        // other than for the other Activities, we call updateMembers on the adapter when
        // the activity resumes.
        // \todo: Figure out if this is really necessary, since the BaseAdapter *should* already take
        // \todo care of calling updateMembers when the data in the content provider changes
        ManageLecturesAdapter manageLecturesAdapter = (ManageLecturesAdapter) getListAdapter();
        manageLecturesAdapter.updateMembers(null);
    }

}
