package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

/**
 * Service which changes Android's sync settings for the app's account.
 *
 * This is invoked from a notification which alerts the user that the settings are unsuitable.
 */
public class SyncSettingChangerService extends IntentService {

    /**
     * Zero-argument constructor
     *
     */
    public SyncSettingChangerService() {
        super("SyncSettingChangerService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {


        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        if (dataString.equals("turn_on_master_sync")) {
            ContentResolver.setMasterSyncAutomatically(true);
            NotificationManagerCompat.from(this).cancel(001);
        }
        if(dataString.equals("turn_on_app_sync")){
            ContentResolver.setSyncAutomatically(AccountManager.get(this).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, true);
            NotificationManagerCompat.from(this).cancel(002);
        }

    }
}