package com.gmail.konstantin.schubert.workload.sync;

import android.content.ContentResolver;
import android.util.JsonReader;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.Week;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RESTResponseProcessor {

    private DBObjectBuilder dbObjectBuilder;
    //this class kinda does the object building from json AND the merging logic with the local database. Not sure how
    // smart that combination of duties is.

    public RESTResponseProcessor(ContentResolver resolver){
        dbObjectBuilder = new DBObjectBuilder(resolver);
    }

    static public List<Lecture> lectureListFromJson(String jsonList) {

        JsonReader reader = new JsonReader(new StringReader(jsonList));

        List lectures = new ArrayList();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                lectures.add(buildLecture(reader));
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            //TODO: something
        }
        return lectures;
    }


    static public Lecture buildLecture(JsonReader reader) throws IOException {

        int id = -1;
        String name = null;
        String semester = null;
        Week startWeek = null;
        Week endWeek = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("id")) {
                id = reader.nextInt();
            } else if (key.equals("name")) {
                name = reader.nextString();
            } else if (key.equals("semester")) {
                semester = reader.nextString();
            } else if (key.equals("startDay")) {
                startWeek = Week.getWeekFromISOString(reader.nextString());
            } else if (key.equals("endDay")) {
                endWeek = Week.getWeekFromISOString(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Lecture(id, name, semester, startWeek, endWeek, false);
    }


    public void updateAvailableLectures(List<Lecture> remoteLectures) {
        // updates the available lectures from the remote end to the local end.
        // Remote always supersedes local, even if syncing

        List<Lecture> localLectures = this.dbObjectBuilder.getLectureList(false);  // all lectures (active and inactive) that are listed locally

        // delete local lectures that are not in the remote list
        for (Lecture localLecture : localLectures) {
            boolean found = false;
            for (Lecture remoteLecture : remoteLectures) {
                if (localLecture.equals(remoteLecture)) {
                    found = true;
                }
            }
            if (!found) {
                this.dbObjectBuilder.deleteLectureById(localLecture._ID);
            }
        }

        // add remote lectures that are not in local lectures
        for (Lecture remoteLecture : remoteLectures){
            boolean found = false;
            for (Lecture localLecture : localLectures) {
                if (localLecture.equals(remoteLecture)) {
                    found = true;
                }
            }
            if(!found){
                this.dbObjectBuilder.addLecture(remoteLecture,false);
            }
        }

    }
    public void updateActiveLectures(List<Lecture> remoteActiveLectures){
        // Update which lectures are active
        // If local is syncing its status is not overwritten.
    }

}
