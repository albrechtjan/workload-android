package com.gmail.konstantin.schubert.workload;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.sync.SyncAdapter;

import java.io.IOError;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DBObjectBuilder {

    //TODO: It would be nice to eliminate this object builder and instead have the adapters connect to the database more closely (making the auto-updates work)
    //TODO: However, at this point I am not sure how the database would need to look like to this end.

    private ContentResolver mContentResolver;

    public DBObjectBuilder(ContentResolver contentResolver){
        this.mContentResolver = contentResolver;
    }


    public List<Lecture> getLectureList( boolean onlyActive, boolean noSync){
        Cursor cursor;
        if (noSync)
            cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/nosync/any/"), null, null, null, null);
        else
            cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/sync/any/"), null, null, null, null);
        List<Lecture> lectures = new ArrayList<Lecture>();
        while (cursor.moveToNext()){
            Lecture newLecture = buildLectureFromCursor(cursor);
            if (!onlyActive || newLecture.isActive) {
                lectures.add(newLecture);
            }
        }
        cursor.close();
        return lectures;
    }


    public Lecture getLectureById(Integer lectureId, boolean nosync){
        Cursor cursor;
        if (nosync) {
            cursor= mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/nosync/" + String.valueOf(lectureId)), null, null, null, null);
        }else{
            cursor= mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/sync/" + String.valueOf(lectureId) ), null, null, null, null);
        }
        cursor.moveToFirst();
        Lecture lecture = buildLectureFromCursor(cursor);
        cursor.close();
        return lecture;
    }

    public Cursor getPending(String table){
        String uri = "content://" + SurveyContentProvider.AUTHORITY+ "/";
        uri += table + "/";
        uri +="nosync/any/";
        String where = SurveyContentProvider.DB_STRINGS.STATUS + "=" + SurveyContentProvider.SYNC_STATUS.PENDING;
        return  mContentResolver.query(Uri.parse(uri), null, where, null, null);
    }

    public void mark_as_transacting(int id, String table){
        String uri = "content://" + SurveyContentProvider.AUTHORITY+ "/";
        uri += table + "/";
        uri +="nosync/";
        uri += String.valueOf(id)+"/";
        ContentValues values = new ContentValues(1);
        values.put(SurveyContentProvider.DB_STRINGS.STATUS, SurveyContentProvider.SYNC_STATUS.TRANSACTING);
        int rows_affected = mContentResolver.update(Uri.parse(uri), new ContentValues(), null, null);
        if (rows_affected!=1) throw new IOError(new Throwable());
    }


    public WorkloadEntry getWorkloadEntryByLocalId(int local_id, boolean nosync){

        String uri = "content://" + SurveyContentProvider.AUTHORITY + "/workentries/";
        uri += nosync ? "nosync/" : "sync/";
        uri += String.valueOf(local_id) + "/";
        Cursor cursor = mContentResolver.query(Uri.parse(uri), null, null, null, null);
        cursor.moveToFirst();
        WorkloadEntry entry =  buildWorkloadEntryFromCursor(cursor);
        cursor.close();
        return entry;
    }



    public void addLecture(Lecture lecture, boolean stopsync){
        String uriString = "content://" + SurveyContentProvider.AUTHORITY;
        uriString += stopsync ?  "/lectures/stopsync/" : "/lectures/sync/";
        uriString += "/any";
        ContentValues values = getValues(lecture);
        mContentResolver.insert(Uri.parse(uriString), values);
    }

    private WorkloadEntry buildWorkloadEntryFromCursor(Cursor cursor){
        Week week = new Week(
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR)),
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK))
        );
        int lecture_id = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID));
        float hoursInLecture = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE));
        float hoursForHomework = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK));
        float hoursStudying = cursor.getFloat(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING));
        return new WorkloadEntry(week, lecture_id, hoursInLecture, hoursForHomework, hoursStudying );

    }

    private Lecture buildLectureFromCursor(Cursor cursor){

        int _ID= cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS._ID));
        String name = cursor.getString( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.NAME) );
        String semester = cursor.getString(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.SEMESTER));
        Week startWeek = new Week(
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.STARTYEAR)),
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.STARTWEEK))
        );
        Week endWeek = new Week(
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ENDYEAR)),
                cursor.getInt( cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ENDWEEK))
        );
        boolean isActive = ( cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE)) == 1);
        return new Lecture(_ID, name, semester, startWeek, endWeek, isActive);
    }


    public List<Lecture> getLecturesInWeek(Week thatWeek, boolean onlyActive){
        // gets lectures that are active in 'thatWeek'
        List<Lecture> allLectures = getLectureList(onlyActive, false);
        List<Lecture> lecturesThatWeek = new ArrayList<Lecture>();
        for (Lecture lecture : allLectures){
            if (lecture.startWeek.compareTo(thatWeek)<=0 && thatWeek.compareTo(lecture.endWeek)<=0) {
                lecturesThatWeek.add(lecture);
            }
        }
        return lecturesThatWeek;
    }

    public List<Lecture> getLecturesOfSemester(String semester, boolean onlyActive, boolean nosync){
        List<Lecture> lectures = getLectureList(onlyActive, nosync);
        List<Lecture> lecturesInSemester = new ArrayList<>();
        for (Lecture lecture : lectures){
            if (lecture.semester.equals(semester)){
                lecturesInSemester.add(lecture);
            }
        }
        return  lecturesInSemester;
    }



    public List<String> getSemesterList(boolean onlyActive, boolean nosync){
        //TODO: Make this more efficient
        List<String> semesters = new ArrayList<>();
        List<Lecture> lectures = getLectureList(onlyActive, nosync);
        //Wow, this would be two lines in python. Am I doing this wrong?
        for (Lecture lecture : lectures ){
            boolean found =false;
            for (String semester : semesters){
                if(semester.equals(lecture.semester)){
                    found = true;
                    break;
                }
            }
            if (!found){
                semesters.add(lecture.semester);
            }
        }
        return semesters;
    }

    public Cursor getWorkloadEntry(int lecture_id, Week week){
        String where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(lecture_id)+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(week.year())+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(week.week());
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/sync/any/"), null, where, null, null);
        return cursor;
    }

    public void addWorkloadEntry(int lecture_id, Week week, String syncSteerCommand){
        ContentValues values = new ContentValues(3);
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR, week.year());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK, week.week());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID, lecture_id);
        String uri = "content://" + SurveyContentProvider.AUTHORITY + "/workentries/";
        uri += SurveyContentProvider.SYNC_STEER_COMMAND.SYNC+"/";
        uri += "any/";
        mContentResolver.insert(Uri.parse(uri), values);

    }

    public void deleteWorkloadEntry(int lecture_id, Week week, boolean stopsync){
        String where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(lecture_id)+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(week.year())+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(week.week());
        Uri uri = Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/stopsync/any/");
        mContentResolver.delete(uri, where, null);

    }

    public List<WorkloadEntry> getWorkloadEntries(Lecture lecture, boolean nosync){
        /* Gets the workload entries for a given lecture.
        * If passed null it will get the entries for all lectures.
        * */
        String where = null;
        if (lecture != null){
            where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(lecture._ID);
        }

        String uri = "content://" + SurveyContentProvider.AUTHORITY + "/workentries/";
        uri += nosync ? "nosync/" : "sync/";
        uri += "any/";

        Cursor cursor = mContentResolver.query(Uri.parse(uri), null, where, null, null);

        List<WorkloadEntry> workloadEntries = new ArrayList<WorkloadEntry>();
        while (cursor.moveToNext()){

            WorkloadEntry newWorkloadEntry = buildWorkloadEntryFromCursor(cursor);
            workloadEntries.add(newWorkloadEntry);
        }
        cursor.close();
        return workloadEntries;
    }

    public void updateWorkloadEntry(WorkloadEntry entry, String syncSteerCommand){
        ContentValues values = new ContentValues();
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK, entry.getHoursForHomework());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE, entry.getHoursInLecture());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING, entry.getHoursStudying());
        String where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(entry.lecture_id)+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(entry.week.year())+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(entry.week.week());

        String uri = "content://" + SurveyContentProvider.AUTHORITY + "/workentries/";
        uri += syncSteerCommand + "/";
        uri += "any/";
        int result = mContentResolver.update(Uri.parse(uri), values, where, null);
        if (result<0){
//            throw new UnsupportedOperationException();
        }
    }

    public void updateLecture(Lecture lecture, boolean stopsync){

        ContentValues values = getValues(lecture);
        values.remove(SurveyContentProvider.DB_STRINGS._ID); // We add the ID in the url.

        String uri = "content://" + SurveyContentProvider.AUTHORITY + "/lectures/";
        uri += stopsync ? "stopsync/" : "sync/";
        uri += String.valueOf(lecture._ID) + "/";
        int result = mContentResolver.update(Uri.parse(uri), values, null, null);
        if (result<0){
            throw new UnsupportedOperationException();
        }
    }

    private ContentValues getValues(Lecture lecture){
        ContentValues values = new ContentValues();
        values.put(SurveyContentProvider.DB_STRINGS._ID, lecture._ID);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.NAME, lecture.name);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.SEMESTER, lecture.semester);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ENDWEEK, lecture.endWeek.week());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ENDYEAR, lecture.endWeek.year());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.STARTWEEK, lecture.startWeek.week());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.STARTYEAR, lecture.startWeek.year());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE, lecture.isActive ? 1 : 0);
        return values;
    }


}
