package com.gmail.konstantin.schubert.workload.activities;


import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.sync.SyncSettingChangerService;


abstract public class MyBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    protected void onResume(){
        super.onResume();
        // We aggressively request a sync to poll the server every time an activity is opened.
        //TODO: It will be nice to replace this by a push system such as google's cloud messaging API
        ContentResolver.requestSync(AccountManager.get(this).getAccountsByType("tu-dresden.de")[0], SurveyContentProvider.AUTHORITY, new Bundle());

        if (!SurveyContentProvider.isMasterSyncSettingTrue()){
            buildSyncSettingChangerIntent("Turn on Auto-Sync", "Time Monitor needs auto-sync.", "Turn on now", "turn_on_master_sync",001);
        }
        if (!SurveyContentProvider.isAccountSyncSettingTrue(this)){
            buildSyncSettingChangerIntent("Turn on App-Sync", "Sync must be activated for Time Monitor.", "Turn on now", "turn_on_app_sync",002);
        }
    }

    private void buildSyncSettingChangerIntent(String contentTitle, String contentText, String actionText, String specifier, int id){
        Intent intent = new Intent(this, SyncSettingChangerService.class);
        intent.setData(Uri.parse(specifier));
        PendingIntent actionPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_sync_problem)
          //              .setLargeIcon(largeIcon)
                        .setContentTitle(contentTitle)
                        .setAutoCancel(true)
                        // shouldn't this be a standard setting that the notification
                        // disappears after it is used? Am I doing something wong?
                        .setContentText(contentText)
                        .addAction(new NotificationCompat.Action(R.drawable.ic_settings_24dp, actionText, actionPendingIntent));

        NotificationManagerCompat.from(this).notify(id,mBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_manage_lectures:
                intent = new Intent(this, ManageLectures.class);
                break;
            case R.id.action_privacy_agreement:
                intent = new Intent(this, PrivacyAgreement.class);
                break;
            case R.id.action_statistics:
                intent = new Intent(this, Statistics.class);
                break;
            case R.id.about:
                intent = new Intent(this, About.class);
                break;
        }
        if (intent == null) {
            return false;
        } else {
            startActivity(intent);
            return true;
        }
    }

    protected boolean assure_privacy_agreement() {
        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        if (!settings.getBoolean("privacy_agreed", false)) {
            Intent intent = new Intent(this, PrivacyAgreement.class);
            this.startActivity(intent);
            return true;
        }
        return false;
    }



    protected boolean maybe_make_first_login() {

        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        if (settings.getBoolean("user_has_never_logged_in", true)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("user_has_never_logged_in", false);
            editor.commit();
            Intent intent = new Intent(this, WebLoginActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
