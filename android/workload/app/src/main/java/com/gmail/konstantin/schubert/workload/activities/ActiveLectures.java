package com.gmail.konstantin.schubert.workload.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.gmail.konstantin.schubert.workload.Adapters.ManageLecturesAdapter;
import com.gmail.konstantin.schubert.workload.R;

/* Activity where the user can see which lectures he has selected for data entry.
 * He can deactivate lectures by clicking on a trash can button or he can click on
 * a red Floating Action Button in the lower right corner to add a new lecture.
 */
public class ActiveLectures extends MyBaseListActivity {

    private Context mContext;

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
    public void onResume() {
        super.onResume();
        ManageLecturesAdapter manageLecturesAdapter = (ManageLecturesAdapter) getListAdapter();
        //TODO: Make this more efficient
        manageLecturesAdapter.updateMembers(null);
        manageLecturesAdapter.notifyDataSetChanged();
    }

}
