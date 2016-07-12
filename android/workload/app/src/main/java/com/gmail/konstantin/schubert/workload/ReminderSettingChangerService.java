package com.gmail.konstantin.schubert.workload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Service which turns off the reminder notifications in the app settings.
 */
public class ReminderSettingChangerService extends IntentService {

    public ReminderSettingChangerService() {
        super("ReminderSettingChangerService");
    }


    @Override
    protected void onHandleIntent(Intent workIntent) {

        NotificationManagerCompat.from(this).cancel(003);
        // for now, we always turn off.
        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(getString(R.string.show_reminder_notification), false);
        editor.commit();
    }
}
