package com.gmail.konstantin.schubert.workload.activities;


import android.accounts.AccountManager;
import android.content.ContentResolver;
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
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.WorkloadEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Activity that displays some nice graphs with information about the data which was entered.
 *
 * This uses the open source charting library MPChartlib.
 * https://github.com/PhilJay/MPAndroidChart
 *
 * I had trouble configuring Gradle to properly pull in the source of this library for building.
 * Therefore, I opted to simply copy the library code into the MPChartLib folder which is located
 * in the project directory.
 *
 * \todo: Configure gradle to import the proper version of the MPChartLib
 * \todo: show more, and more interesting, statistics
 */
public class Statistics extends MyBaseActivity {

    DBObjectBuilder dbObjectBuilder;
    List<Lecture> mActiveLectures;
    ArrayList<Integer> mColors;

    // fields which contain interesting information about the
    // data which was entered by the user
    Map<Lecture, Map<Integer,Double>> hoursSpentMap;

    // \todo: the EnterWorkload activity already has these public final static int fields.
    // \todo: Just use those.
    public final static int HOURS_ATTENDING = 0;
    public final static int HOURS_HOMEWORK = 1;
    public final static int HOURS_STUDYING = 2;

    /**
     * @inheritDoc
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ContentResolver.requestSync(AccountManager.get(this).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, new Bundle());

        // initialize the fields
        dbObjectBuilder = new DBObjectBuilder(getContentResolver());
        hoursSpentMap = new HashMap<>();
        collectData();

        // The MPAndroidChart library will cycle through the list of colors when
        // displaying charts. To avoid showing the same color twice, we need a lot of colors.
        mColors = new ArrayList<>();
        for (int c : ColorTemplate.JOYFUL_COLORS)
            mColors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            mColors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            mColors.add(c);


        // Every chart is a view.

        // Build chart #1
        PieChart pieChartTimePerLecture = (PieChart) findViewById(R.id.pie_chart);
        buildPieChartTimePerLecture(pieChartTimePerLecture);

        // Build chart #2
        PieChart pieChartTimePerActivity = (PieChart) findViewById(R.id.pie_chart_time_per_activity);
        buildPieChartTimePerActivity(pieChartTimePerActivity);

    }


    /**
     * Initializes the classes fields with interesting information about the data which was
     * entered by the user.
     */
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


    /**
     * This method defines and initializes the pie chart which displays
     * how the user's time was distributed onto the various lectures.
     *
     * Please refer to the documentation of the MPAndroidChart library for more detailed documentation.
     *
     * @param pieChart A PieChart object is a view which holds a pie chart.
     */
    private void buildPieChartTimePerLecture(PieChart pieChart) {

        pieChart.setCenterText("Distribution of time onto lectures.");
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setTouchEnabled(false);
        pieChart.setDrawSliceText(false);
        pieChart.setDescription("");


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

        dataSet.setColors(mColors);


        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);

        pieChart.highlightValues(null);

        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        Legend l = pieChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
    }

    /**
     * This method defines and initializes the pie chart which displays
     * how the user's time was distributed onto the three different kinds of activities.
     *
     * Please refer to the documentation of the MPAndroidChart library for more detailed documentation.
     *
     * @param pieChart A PieChart object is a view which holds a pie chart.
     */
    private void buildPieChartTimePerActivity(PieChart pieChart) {
        pieChart.setCenterText("Distribution of time onto activities.");
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setTouchEnabled(false);
        pieChart.setDrawSliceText(false);
        pieChart.setDescription("");


        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.

        double totalHoursHomework  = 0;
        double totalHoursStudying  = 0;
        double totalHoursAttending = 0;

        for (Lecture lecture : mActiveLectures){
            totalHoursHomework  += hoursSpentMap.get(lecture).get(HOURS_HOMEWORK);
            totalHoursStudying  += hoursSpentMap.get(lecture).get(HOURS_STUDYING);
            totalHoursAttending += hoursSpentMap.get(lecture).get(HOURS_ATTENDING);
        }
        xVals.add("Homework");
        xVals.add("Studying");
        xVals.add("Attending");
        yVals1.add(new Entry((float) totalHoursHomework, 0));
        yVals1.add(new Entry((float) totalHoursStudying, 1));
        yVals1.add(new Entry((float) totalHoursAttending, 2));

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);


        dataSet.setColors(mColors);


        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);

        pieChart.highlightValues(null);

        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        Legend l = pieChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
    }


}
