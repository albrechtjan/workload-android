package com.gmail.konstantin.schubert.workload.sync;

import android.util.JsonReader;

import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.Week;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RESTResponseProcessor {
    //this class kinda does the json parsing AND the merging logic with the local database. Not sure how
    // smart that is.


    public List<Lecture> lectureListFromJson(String jsonList) {

        JsonReader reader = new JsonReader(new StringReader(jsonList));

        List lectures = new ArrayList();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                lectures.add(buildLecture(reader));
            }
            reader.endArray();
            reader.close();
        }
        catch(IOException e){
            //TODO: something
        }
        return lectures;
    }



    public Lecture buildLecture(JsonReader reader) throws IOException {

        int id = -1;
        String name = null;
        String semester = null;
        Week startWeek = null;
        Week endWeek = null;

        reader.beginObject();
        String key = reader.nextName();
        while (reader.hasNext()) {
            if (key.equals("id")) {
                id = reader.nextInt();
            } else if (key.equals("name")) {
                name = reader.nextString();
            } else if (key.equals("semester")) {
                semester = reader.nextString();
            } else if (key.equals("startWeek")) {
                startWeek =  Week.getWeekFromISOString(reader.nextString());
            } else if (key.equals("endWeek")) {
                endWeek = Week.getWeekFromISOString(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Lecture(id, name, semester, startWeek, endWeek, false);
    }


    public void updateAvailableLectures(List<Lecture> remoteLectures){

//        localLectures = // all lectures (active and inactive) that are listed locally
//
//        for (localLecture in localLectures) {
//            if (not localLecture in remoteLectures){
//                contentProvider.delete(localLecture.url, false) // real delete without sync
//            }
//        }
//        for (remoteLecture in remoteLectures){
//            if (not remoteLecture in localLectures){
//                contentProvider.insert( ... active=false)
//            }
//        }

    }

}
