package com.gmail.konstantin.schubert.workload.sync;


import android.content.ContentResolver;
import android.net.Uri;
import android.util.JsonReader;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RESTResponseProcessor {

    private DBObjectBuilder dbObjectBuilder;
    private ContentResolver mContentResolver;
    //this class kinda does the object building from json AND the merging logic with the local database. Not sure how
    // smart that combination of duties is.

    public RESTResponseProcessor(ContentResolver contentResolver){

        dbObjectBuilder = new DBObjectBuilder(contentResolver);
        this.mContentResolver = contentResolver;
    }

    static public List<Lecture> lectureListFromJson(String jsonList) throws IOException{
        JsonReader reader = new JsonReader(new StringReader(jsonList));
        List<Lecture> lectures = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            lectures.add(buildLecture(reader));
        }
        reader.endArray();
        reader.close();

        return lectures;
    }


    static public List<WorkloadEntry> entryListFromJson(String jsonList) throws IOException{
        JsonReader reader = new JsonReader(new StringReader(jsonList));
        List<WorkloadEntry> entries = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            entries.add(buildEntry(reader));
        }
        reader.endArray();
        reader.close();

        return entries;
    }


    static public Lecture buildLecture(JsonReader reader) throws IOException {

        int id = -1;
        String name = null;
        String semester = null;
        Week startWeek = null;
        Week endWeek = null;
        boolean isActive = false;

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
            } else if (key.equals("isActive")) {
                isActive = reader.nextBoolean();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Lecture(id, name, semester, startWeek, endWeek, isActive);
    }

    static public WorkloadEntry buildEntry(JsonReader reader) throws IOException {

        int lecture_id = -1;
        float hoursInLecture = -1;
        float hoursForHomework = -1;
        float hoursStudying = -1;
        Week week = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("lecture_id")) {
                lecture_id = reader.nextInt();
            } else if (key.equals("hoursInLecture")) {
                hoursInLecture = (float) reader.nextDouble();
            } else if (key.equals("hoursForHomework")) {
                hoursForHomework = (float) reader.nextDouble();
            } else if (key.equals("hoursStudying")) {
                hoursStudying = (float) reader.nextDouble();
            } else if (key.equals("week")) {
               week = Week.getWeekFromISOString(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WorkloadEntry(week, lecture_id, hoursInLecture, hoursForHomework, hoursStudying);

    }


    public void update_lectures_from_remote(List<Lecture> remoteLectures) {
        // updates the available lectures from the remote end to the local end.
        // Remote always supersedes local, even if syncing
        List<Lecture> localLectures = this.dbObjectBuilder.getLectureList(false);  // all lectures (active and inactive) that are listed locally
        // delete local lectures that are not in the remote list
        for (Lecture localLecture : localLectures) {
            if (!isInList(localLecture, remoteLectures)) {
                Uri uri = Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/" + String.valueOf(localLecture._ID) + "/STOPSYNC/");
                mContentResolver.delete(uri, null, null);
            }
        }
        // add remote lectures that are not in local lectures
        for (Lecture remoteLecture : remoteLectures){
            if(!isInList(remoteLecture, localLectures)){
               this.dbObjectBuilder.addLecture(remoteLecture,true);
            }else{
                this.dbObjectBuilder.updateLecture(remoteLecture, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);

            }
        }
    }




    public  void update_workloadentries_from_remote(List<WorkloadEntry> remoteWorkloadEntries){

        List<WorkloadEntry> localWorkloadEntries = this.dbObjectBuilder.getWorkloadEntries(null);
        for(WorkloadEntry localWorkloadEntry : localWorkloadEntries){
            if(!isInList(localWorkloadEntry, remoteWorkloadEntries)){
                dbObjectBuilder.deleteWorkloadEntry(localWorkloadEntry.lecture_id, localWorkloadEntry.week);
            }
        }
        for(WorkloadEntry remoteWorkloadEntry : remoteWorkloadEntries){
            if(!isInList(remoteWorkloadEntry, localWorkloadEntries)){
                this.dbObjectBuilder.addWorkloadEntry(remoteWorkloadEntry.lecture_id, remoteWorkloadEntry.week, SurveyContentProvider.SYNC_STEER_COMMAND.STOPSYNC);
            }else{
                dbObjectBuilder.updateWorkloadEntry(remoteWorkloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);
            }
        }
    }
    private boolean isInList(Object object, List list){
        boolean isInList = false;
        for (Object other : list){
            if (object.equals(other)){
                isInList = true;
                break;
            }
        }
        return  isInList;
    }

}
