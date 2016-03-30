package com.gmail.konstantin.schubert.workload.activities;


import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gmail.konstantin.schubert.workload.DBObjectBuilder;
import com.gmail.konstantin.schubert.workload.Lecture;
import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics extends MyBaseActivity {

    DBObjectBuilder dbObjectBuilder;
    List<Lecture> mActiveLectures;
    Map<Lecture, Map<Integer,Double>> hoursSpentMap;

    public final static int HOURS_ATTENDING = 0;
    public final static int HOURS_HOMEWORK = 1;
    public final static int HOURS_STUDYING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        dbObjectBuilder = new DBObjectBuilder(getContentResolver());

        hoursSpentMap = new HashMap<>();

        collectData();

        PieChart pieChartTimePerLecture = (PieChart) findViewById(R.id.pie_chart);
        buildPieChartTimePerLecture(pieChartTimePerLecture);
//
//        PieChart pieChartTimePerActivity = (PieChart) findViewById(R.id.pie_chart_time_per_activity);
//        buildPieChartTimePerActivity(pieChartTimePerActivity);



    }



    private void collectData(){

        mActiveLectures = dbObjectBuilder.getLectureList(true);

        for (Lecture lecture : mActiveLectures){
            // query all workload entries for the lecture
             List<WorkloadEntry> workloadEntries = dbObjectBuilder.getWorkloadEntries(lecture);
            double homeworkHours = 0;
            double lectureHours = 0;
            double studyingHours = 0;
            for(WorkloadEntry workloadEntry : workloadEntries){
                homeworkHours += workloadEntry.getHoursForHomework();
                lectureHours  += workloadEntry.getHoursInLecture();
                studyingHours += workloadEntry.getHoursStudying();
            }

            Map<Integer, Double> hours = new HashMap<>();
            hours.put(HOURS_ATTENDING, lectureHours);
            hours.put(HOURS_STUDYING, studyingHours);
            hours.put(HOURS_HOMEWORK, homeworkHours);
            hoursSpentMap.put(lecture,hours);
        }

    }


    private void buildPieChartTimePerLecture(PieChart pieChartTimePerLecture) {

        pieChartTimePerLecture.setExtraOffsets(5, 10, 5, 5);
        pieChartTimePerLecture.setDragDecelerationFrictionCoef(0.95f);
        pieChartTimePerLecture.setCenterText("Distribution of time onto lectures.");
        pieChartTimePerLecture.setDrawHoleEnabled(true);
        pieChartTimePerLecture.setHoleColor(Color.WHITE);
        pieChartTimePerLecture.setTransparentCircleColor(Color.WHITE);
        pieChartTimePerLecture.setTransparentCircleAlpha(110);
        pieChartTimePerLecture.setHoleRadius(58f);
        pieChartTimePerLecture.setTransparentCircleRadius(61f);
        pieChartTimePerLecture.setDrawCenterText(true);
        pieChartTimePerLecture.setRotationAngle(0);
        pieChartTimePerLecture.setRotationEnabled(true);
        pieChartTimePerLecture.setHighlightPerTapEnabled(true);


        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.

        int count = 0;
        for (Lecture lecture : mActiveLectures){
            xVals.add(lecture.name);
            double totalHours = hoursSpentMap.get(lecture).get(HOURS_HOMEWORK) +
                                hoursSpentMap.get(lecture).get(HOURS_STUDYING) +
                                hoursSpentMap.get(lecture).get(HOURS_ATTENDING);
            yVals1.add(new Entry((float) totalHours, count));
        }

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);


        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        pieChartTimePerLecture.setData(data);

        pieChartTimePerLecture.highlightValues(null);

        pieChartTimePerLecture.invalidate();

        pieChartTimePerLecture.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        Legend l = pieChartTimePerLecture.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

//
//    private void buildPieChartTimePerActivity(PieChart pieChart) {
//
//        pieChart.setExtraOffsets(5, 10, 5, 5);
//        pieChart.setDragDecelerationFrictionCoef(0.95f);
//        pieChart.setCenterText("Distribution of time onto lectures.");
//        pieChart.setDrawHoleEnabled(true);
//        pieChart.setHoleColor(Color.WHITE);
//        pieChart.setTransparentCircleColor(Color.WHITE);
//        pieChart.setTransparentCircleAlpha(110);
//        pieChart.setHoleRadius(58f);
//        pieChart.setTransparentCircleRadius(61f);
//        pieChart.setDrawCenterText(true);
//        pieChart.setRotationAngle(0);
//        pieChart.setRotationEnabled(true);
//        pieChart.setHighlightPerTapEnabled(true);
//
//
//        ArrayList<Entry> yVals1 = new ArrayList<>();
//        ArrayList<String> xVals = new ArrayList<>();
//
//        // IMPORTANT: In a PieChart, no values (Entry) should have the same
//        // xIndex (even if from different DataSets), since no values can be
//        // drawn above each other.
//
//        int count = 0;
//        for (Lecture lecture : mActiveLectures){
//            xVals.add(lecture.name);
//            double totalHours = hoursSpentMap.get(lecture).get(HOURS_HOMEWORK) +
//                    hoursSpentMap.get(lecture).get(HOURS_STUDYING) +
//                    hoursSpentMap.get(lecture).get(HOURS_ATTENDING);
//            yVals1.add(new Entry((float) totalHours, count));
//        }
//
//        PieDataSet dataSet = new PieDataSet(yVals1, "");
//        dataSet.setSliceSpace(3f);
//        dataSet.setSelectionShift(5f);
//
//        ArrayList<Integer> colors = new ArrayList<Integer>();
//
//        for (int c : ColorTemplate.JOYFUL_COLORS)
//            colors.add(c);
//
//        colors.add(ColorTemplate.getHoloBlue());
//
//        dataSet.setColors(colors);
//
//
//        PieData data = new PieData(xVals, dataSet);
//        data.setValueTextSize(11f);
//        data.setValueTextColor(Color.BLACK);
//        pieChart.setData(data);
//
//        pieChart.highlightValues(null);
//
//        pieChart.invalidate();
//
//        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
//        Legend l = pieChart.getLegend();
//        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
//        l.setXEntrySpace(7f);
//        l.setYEntrySpace(0f);
//        l.setYOffset(0f);
//    }


}
