package com.gmail.konstantin.schubert.workload;


import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gmail.konstantin.schubert.workload.activities.SelectLecture;
import com.gmail.konstantin.schubert.workload.sync.SyncSettingChangerService;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.List;

/**
 *
 * Broadcast Receiver which checks if user should be alerted to enter his data.
 *
 * If it decides that the user should be alerted, it creates a notification to alert the user.
 *
 *
 * http://stackoverflow.com/a/20263092/1375015
 */
public class ReminderReceiver extends BroadcastReceiver {




    /**
     * Entry method which is called when the AlarmManager issues a fitting intent.
     *
     * Calling this method will issue a reminder notification to enter workload data if the
     * shouldReminderBeShown() method returns true.
     */
    @Override
    public void onReceive(Context context, Intent intent){

        Log.d("ReminderReceiver", "onReceive called");

        Week weekToRemindFor = getWeekOfYesterday();

        if (shouldReminderBeShown(context, weekToRemindFor)){
            showReminderNotification(context, weekToRemindFor);
        }

    }

    /**
     * Get the week which was the current week yesterday
     *
     * This should give the week which we want to remind the user about.
     * Taking the week of yesterday is more robust against off-by-one errors, for example
     * if the new week unexpectedly starts on Sunday...
     * @return  The week which was the current week yesterday
     */
    Week getWeekOfYesterday(){
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(Period.days(1));
        return new Week(yesterday.getYear(), yesterday.getWeekOfWeekyear());
    }

    /**
     * Decides if a reminder to enter workload data should be displayed
     *
     * If reminders are turned off in the settings, do not show one
     * If all lectures have an entry for the week which was the current week yesterday,
     * do not show one
     * @param context An Android context
     * @param week The week for which a reminder is being considered
     * @return
     */
    private boolean shouldReminderBeShown(Context context, Week week){

        SharedPreferences settings = context.getSharedPreferences("workload", Context.MODE_PRIVATE);
        boolean reminders_on = settings.getBoolean("show_reminder_notification", true);

        DBObjectBuilder builder = new DBObjectBuilder(context.getContentResolver());
        List<Lecture> lectures = builder.getLecturesInWeek(week,true);
        boolean data_missing = ! builder.allLecturesHaveDataInWeek(lectures,week);
        return reminders_on && data_missing;

    }

    /**
     * Constructs and shows the reminder notification
     *
     * The notification reminds the user to enter data for a given week.
     * Clicking on the notification opens the SelectLecture activity for the given week.
     *
     * \todo: Offer additional action button where notifications can be turned off
     *
     * @param context An Android context
     * @param week The week that the notification is reminding the user about
     */
    private void showReminderNotification(Context context, Week week){

        // Building the content intent
        Intent intent = new Intent(context, SelectLecture.class);
        intent.putExtra(SelectLecture.MESSAGE_YEAR, week.year());
        intent.putExtra(SelectLecture.MESSAGE_WEEK, week.week());

        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 701, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Building the action intent. The service always turns the reminders off
        Intent actionIntent = new Intent(context, ReminderSettingChangerService.class);
        PendingIntent actionPendingIntent = PendingIntent.getService(context, 702, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Building the notification.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_show_chart_black_24px)
                        .setContentTitle(context.getResources().getString(R.string.reminder_title))
                        .setContentText(context.getResources().getString(R.string.reminder_text))
                        .setContentIntent(contentPendingIntent)
                        .addAction(new NotificationCompat.Action(R.drawable.ic_settings_24dp, "Turn off reminders", actionPendingIntent))
                        .setAutoCancel(true);
        // Fire up the notification
        NotificationManagerCompat.from(context).notify(003, mBuilder.build());
    }


}
