package com.gmail.konstantin.schubert.workload.sync;


import android.content.ContentResolver;
import android.net.Uri;
import android.util.JsonReader;
import android.util.MalformedJsonException;

import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.Week;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes the server response which is in JSON format and builds java objects from it. (A bit like
 * the DBObejctBuilder for the SurveyContentProvider.)
 * Then, where necessary, it takes care of updating the database of the SurveyContentProvider with
 * the information from the server response.
 *
 * \todo: The class does the object building from json AND the merging logic with the local database.
 * \todo: This violates the single responsibility principle for no good reason and should be changed.
 * \todo; https://en.wikipedia.org/wiki/Single_responsibility_principle
 * \todo: We should have a seperate class called RESTObjectBuilder.java
 */
public class RESTResponseProcessor {

    private DBObjectBuilder dbObjectBuilder;
    private ContentResolver mContentResolver;

    /**
     * Constructor. Initializes fields.
     * @param contentResolver A content resolver for the SurveyContentProvider
     */
    public RESTResponseProcessor(ContentResolver contentResolver) {

        dbObjectBuilder = new DBObjectBuilder(contentResolver);
        this.mContentResolver = contentResolver;
    }

    /**
     * Takes list of lectures in JSON format and turns it into java list of
     * java Lecture.java objects.
     */
    static public List<Lecture> lectureListFromJson(String jsonList) throws IOException {
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


    /**
     * Takes list of workload entries  in JSON format and turns it into java list of
     * java WorloadEntry.java objects.
     */
    static public List<WorkloadEntry> entryListFromJson(String jsonList) throws IOException {
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

    /**
     * Takes a json reader with a lecture in JSON format and turns it into java Lecture.java object.
     */
    static private Lecture buildLecture(JsonReader reader) throws IOException {
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

    /**
     * Takes a json reader with a workload entry in JSON format and
     * turns it into java WorkloadEntry.java object.
     */
    static private WorkloadEntry buildEntry(JsonReader reader) throws IOException {

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


    /**
     * Updates the available lectures from the remote end to the local end.
     * 
     * The remote end always supersedes local for inserts and deletes.
     * All rows are updated with the values from the remote end, unless the row in
     * the content provider table is marked as pending. In this case the remote end will
     * later be updated instead. We do not check for time stamps or anything. Data might get
     * lost if updates are made via the web interface and the app simultaneously, or even with
     * some time shift if the phone is offline.
     *
     * @param remoteLectures list of lecture objects as they are stored on the remote
     */
    public void update_lectures_from_remote(List<Lecture> remoteLectures) {
        List<Lecture> localLectures = this.dbObjectBuilder.getLectureList(false);
        // delete local lectures that are not in the remote list
        for (Lecture localLecture : localLectures) {
            if (!isInList(localLecture, remoteLectures)) {
                Uri uri = Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/" + String.valueOf(localLecture._ID) + "/STOPSYNC/");
                mContentResolver.delete(uri, null, null);
            }
        }

        for (Lecture remoteLecture : remoteLectures) {
            if (!isInList(remoteLecture, localLectures)) {
                // add remote lectures that are not in local lectures
                this.dbObjectBuilder.addLecture(remoteLecture, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);
            } else {
                // also update local lectures:
                // collect the local lecture that equals the remote lecture
                Lecture localLecture = (Lecture) getFromList(remoteLecture, localLectures);
                if (!localLecture.equals_exactly(remoteLecture)) {
                    // if they are not exactly the same, update the local lecture
                    // this will NOT update the local lecture if it is marked as pending.
                    this.dbObjectBuilder.updateLecture(remoteLecture, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);
                }

            }
        }
    }


    public void update_workloadentries_from_remote(List<WorkloadEntry> remoteWorkloadEntries) {

        List<WorkloadEntry> localWorkloadEntries = this.dbObjectBuilder.getWorkloadEntries(null);

        for (WorkloadEntry remoteWorkloadEntry : remoteWorkloadEntries) {
            if (!isInList(remoteWorkloadEntry, localWorkloadEntries)) {
                this.dbObjectBuilder.addWorkloadEntry(remoteWorkloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);
            } else {
                WorkloadEntry localEntry = (WorkloadEntry) getFromList(remoteWorkloadEntry, localWorkloadEntries);
                if (!localEntry.equals_exactly(remoteWorkloadEntry)) {
                    dbObjectBuilder.updateWorkloadEntry(remoteWorkloadEntry, SurveyContentProvider.SYNC_STEER_COMMAND.GET_OVERWRITE);
                }
            }
        }
    }

    private boolean isInList(Object object, List list) {
        boolean isInList = false;
        for (Object other : list) {
            if (object.equals(other)) {
                isInList = true;
                break;
            }
        }
        return isInList;
    }

    private Object getFromList(Object object, List list) {
        for (Object other : list) {
            if (object.equals(other)) {
                return other;
            }
        }
        // Better throw exception
        return null;
    }

}
