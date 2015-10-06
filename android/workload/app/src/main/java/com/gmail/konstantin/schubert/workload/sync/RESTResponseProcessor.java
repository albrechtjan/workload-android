package com.gmail.konstantin.schubert.workload.sync;


import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

import java.util.List;

public class RESTResponseProcessor {

    public void updateAvailableLectures(List<Lecture> remoteLectures){

        localLectures = // all lectures (active and inactive) that are listed locally

        for (localLecture in localLectures) {
            if (not localLecture in remoteLectures){
                contentProvider.delete(localLecture.url, false) // real delete without sync
            }
        }
        for (remoteLecture in remoteLectures){
            if (not remoteLecture in localLectures){
                contentProvider.insert( ... active=false)
            }
        }

    }

}
