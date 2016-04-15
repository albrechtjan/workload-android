package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;

import com.gmail.konstantin.schubert.workload.SurveyContentProvider;

/**
 * Created by kon on 15/04/16.
 */
public class SyncSettingChangerService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SyncSettingChangerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        if (dataString == "turn_on_master_sync") {
            ContentResolver.setMasterSyncAutomatically(true);
        }
        if(dataString == "turn_on_app_sync"){
            ContentResolver.getSyncAutomatically(AccountManager.get(this).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY);
        }
    }
}