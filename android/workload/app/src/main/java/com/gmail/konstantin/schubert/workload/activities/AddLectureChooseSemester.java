package com.gmail.konstantin.schubert.workload.activities;

import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.Adapters.AddLectureChooseSemesterAdapter;
import com.gmail.konstantin.schubert.workload.R;


/* Activity to select a semester such as summer semester 2057
* The user goes via this activity before accessing the AddLecture activity
* where he can managing his lectures for the selected semester.
*/
public class AddLectureChooseSemester extends MyBaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_add_lecture_semester);

        setListAdapter(new AddLectureChooseSemesterAdapter(this));
        setTitle("Select semester");
    }
}
