package com.gmail.konstantin.schubert.workload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;

import com.gmail.konstantin.schubert.workload.activities.SelectLecture;
import com.gmail.konstantin.schubert.workload.sync.SyncSettingChangerService;

/**
 * Created by Konstantin Schubert on 02/07/16.
 */
public class ReminderReceiver extends BroadcastReceiver {


    http://stackoverflow.com/a/20263092/1375015

    /**
     * Entry method which is called when the AlarmManager issues a fitting intent.
     *
     * Calling this method will issue a reminder notification to enter workload data if the
     * shouldReminderBeShown() method returns true.
     */
    @Override
    public void onReceive(Context context, Intent intent){

        Week weekToRemindFor = getWeekOfYesterday();

        if (shouldReminderBeShown(weekToRemindFor)){
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
        //

    }

    private boolean shouldReminderBeShown(Week week){
        // if reminders are turned off in the settings, do not show one


        // If all lectures have an entry for the week which was the current week yesterday,
        // do not show one

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

        Intent intent = new Intent(context, SelectLecture.class);
        intent.putExtra(SelectLecture.MESSAGE_YEAR, week.year());
        intent.putExtra(SelectLecture.MESSAGE_WEEK, week.week());

        // Wraps the intent in a PendingIntent
        PendingIntent contentPendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_sync_problem)
                        .setContentTitle(context.getResources().getString(R.string.reminder_title))
                        //// make notification disappear after it is used (Should this not be a
                        //// standard setting?
                        //.setAutoCancel(true) \todo: figure out if we need this.
                        .setContentText(context.getResources().getString(R.string.reminder_text))
                        .setContentIntent(contentPendingIntent)
                        ////\todo I think I could add an action that allows the user to turn off reminders
                        //.addAction(new NotificationCompat.Action(R.drawable.ic_settings_24dp, "Turn off reminders", actionPendingIntent));
        // Fire up the notification
        NotificationManagerCompat.from(context).notify(003, mBuilder.build());
    }


}
