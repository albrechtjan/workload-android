package com.gmail.konstantin.schubert.workload;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.gmail.konstantin.schubert.workload.sync.SyncAdapter;

import java.util.ArrayList;
import java.util.List;


public class DBObjectBuilder {

    //TODO: It would be nice to eliminate this object builder and instead have the adapters connect to the database more closely (making the auto-updates work)
    //TODO: However, at this point I am not sure how the database would need to look like to this end.

    private ContentResolver mContentResolver;

    public DBObjectBuilder(ContentResolver contentResolver){
        this.mContentResolver = contentResolver;
    }

    public List<Lecture> getLectureList( boolean onlyActive, boolean noSync){
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"), null, null, null, null);
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
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/" + String.valueOf(lectureId)+ "/"), null, null, null, null);
        cursor.moveToFirst();
        Lecture lecture = buildLectureFromCursor(cursor);
        cursor.close();
        return lecture;
    }

    public void deleteLectureById(int lectureId){
        // you cannot delete a remote lecture
        // not even in the sense that it is marked as deleted
        Uri uri = Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/" + String.valueOf(lectureId)+ "/");
        mContentResolver.delete(uri, null, null);

    }

    public void addLecture(Lecture lecture, boolean performSync){
        String uriString = "content://" + SurveyContentProvider.AUTHORITY + "/lectures/";
        if (performSync){
            //TODO: set sync value to pending
        }

        ContentValues values = new ContentValues();
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE._ID,lecture._ID);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.NAME, lecture.name);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.SEMESTER, lecture.semester);
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.STARTYEAR, lecture.startWeek.year());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.STARTWEEK, lecture.startWeek.week());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ENDYEAR, lecture.endWeek.year());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ENDWEEK, lecture.endWeek.week());
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE, false);
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

        int _ID= cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE._ID));
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
        boolean isActive = ( cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE)) == 1); //TODO: Figure out if and why there is no boolean method.
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



    public WorkloadEntry getOrCreateWorkloadEntry(int lecture_id, Week week){
        String where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(lecture_id)+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(week.year())+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(week.week());
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, where, null, null);
        if (cursor.getCount()==0){
            ContentValues contentValues = new ContentValues(3);
            contentValues.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR, week.year());
            contentValues.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK, week.week());
            contentValues.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID, lecture_id);
            mContentResolver.insert(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), contentValues);
            cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, where, null, null);
        }
        cursor.moveToFirst();
        WorkloadEntry entry = buildWorkloadEntryFromCursor(cursor);
        cursor.close();
        return entry;
    }

    public List<WorkloadEntry> getWorkloadEntries(Lecture lecture){
        /* Gets the workload entries for a given lecture.
        * If passed null it will get the entries for all lectures.
        * */
        String where = null;
        if (lecture != null){
            where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(lecture._ID);
        }
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), null, where, null, null);
        List<WorkloadEntry> workloadEntries = new ArrayList<WorkloadEntry>();
        while (cursor.moveToNext()){

            WorkloadEntry newWorkloadEntry = buildWorkloadEntryFromCursor(cursor);
            workloadEntries.add(newWorkloadEntry);
        }
        cursor.close();
        return workloadEntries;
    }

    public void updateWorkloadEntryInDB(WorkloadEntry entry){
        ContentValues values = new ContentValues();
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_FOR_HOMEWORK, entry.getHoursForHomework());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_IN_LECTURE, entry.getHoursInLecture());
        values.put(SurveyContentProvider.DB_STRINGS_WORKENTRY.HOURS_STUDYING, entry.getHoursStudying());
        String where = SurveyContentProvider.DB_STRINGS_WORKENTRY.LECTURE_ID + "=" + String.valueOf(entry.lecture_id)+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.YEAR + "=" + String.valueOf(entry.week.year())+" AND ";
        where += SurveyContentProvider.DB_STRINGS_WORKENTRY.WEEK + "=" + String.valueOf(entry.week.week());
        mContentResolver.update(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/workentries/"), values, where, null);
    }


    public  void setLectureIsActive(int lecture_id, boolean isActive, boolean sync){
        ContentValues values = new ContentValues();
        values.put(SurveyContentProvider.DB_STRINGS_LECTURE.ISACTIVE, isActive);

        if (sync){
            values.put(SurveyContentProvider.DB_STRINGS_LECTURE.STATUS, SurveyContentProvider.SYNC_STATUS.PENDING);
            values.put(SurveyContentProvider.DB_STRINGS_LECTURE.OPERATION, SurveyContentProvider.SYNC_OPERATION.PATCH);
        }
        mContentResolver.update(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/lectures/"+String.valueOf(lecture_id)+ "/"), values, null, null);
        if (sync) {
         //   TODO:
         //   do I need to initiate a sync?
        }
    }

    public int getSyncStatus(String DBNAME, int id){
        Cursor cursor = mContentResolver.query(Uri.parse("content://" + SurveyContentProvider.AUTHORITY + "/" + DBNAME + "/" + String.valueOf(id)+ "/"), null, null, null, null);
        cursor.moveToFirst();
        int syncStatus = cursor.getInt(cursor.getColumnIndex(SurveyContentProvider.DB_STRINGS_LECTURE.STATUS));
        return  syncStatus;
    }




}
