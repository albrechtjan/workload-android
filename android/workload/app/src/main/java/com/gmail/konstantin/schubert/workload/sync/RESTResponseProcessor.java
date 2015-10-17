package com.gmail.konstantin.schubert.workload.sync;


import android.util.JsonReader;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RESTResponseProcessor {

    private DBObjectBuilder dbObjectBuilder;
    //this class kinda does the object building from json AND the merging logic with the local database. Not sure how
    // smart that combination of duties is.

    public RESTResponseProcessor(DBObjectBuilder builder){
        dbObjectBuilder = builder;
    }

    static public List<Lecture> lectureListFromJson(String jsonList) {
        JsonReader reader = new JsonReader(new StringReader(jsonList));
        List<Lecture> lectures = new ArrayList();
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


    static public List<WorkloadEntry> entryListFromJson(String jsonList) {
        JsonReader reader = new JsonReader(new StringReader(jsonList));
        List<WorkloadEntry> entries = new ArrayList();
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                entries.add(buildEntry(reader));
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            //TODO: something
        }
        return entries;
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

    static public WorkloadEntry buildEntry(JsonReader reader) throws IOException {

        int lecture_id = -1;
        Long hoursInLecture = null;
        Long hoursForHomework = null;
        Long hoursStudying = null;
        Week week = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("lecture_id")) {
                lecture_id = reader.nextInt();
            } else if (key.equals("hoursInLecture")) {
                hoursInLecture = reader.nextLong();
            } else if (key.equals("hoursForHomework")) {
                hoursForHomework = reader.nextLong();
            } else if (key.equals("hoursStudying")) {
                hoursStudying = reader.nextLong();
            } else if (key.equals("week")) {
               week = Week.getWeekFromISOString(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WorkloadEntry(week, lecture_id, hoursInLecture, hoursForHomework, hoursStudying);

    }


    public void updateAvailableLectures(List<Lecture> remoteLectures) {
        // updates the available lectures from the remote end to the local end.
        // Remote always supersedes local, even if syncing

        List<Lecture> localLectures = this.dbObjectBuilder.getLectureList(false, true);  // all lectures (active and inactive) that are listed locally

        // delete local lectures that are not in the remote list
        for (Lecture localLecture : localLectures) {

            if (!lectureIsInList(localLecture, remoteLectures)) {
                this.dbObjectBuilder.deleteLectureById(localLecture._ID);
            }
        }

        // add remote lectures that are not in local lectures
        for (Lecture remoteLecture : remoteLectures){
            if(!lectureIsInList(remoteLecture, localLectures)){
                this.dbObjectBuilder.addLecture(remoteLecture,false);
            }
        }
    }
    public void updateActiveLectures(List<Lecture> remoteActiveLectures){
        List<Lecture> localLectures = dbObjectBuilder.getLectureList(false, true);
        for (Lecture localLecture : localLectures){

            if (lectureIsInList(localLecture, remoteActiveLectures) && !localLecture.isActive){
                dbObjectBuilder.setLectureIsActive(localLecture._ID, true, false);
            }
            else if (!lectureIsInList(localLecture, remoteActiveLectures) && localLecture.isActive){
                dbObjectBuilder.setLectureIsActive(localLecture._ID, false, false);

            }
        }
    }

    private boolean lectureIsInList(Lecture lecture, List<Lecture> lectureList){
        boolean isInList = false;
        for (Lecture other : lectureList){
            if (lecture.equals(other)){
                isInList = true;
                break;
            }
        }
        return  isInList;
    }

}
