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
import android.widget.Toast;

import com.gmail.konstantin.schubert.workload.R;
import com.gmail.konstantin.schubert.workload.SurveyContentProvider;
import com.gmail.konstantin.schubert.workload.sync.SyncSettingChangerService;

/**
 * Base class for the Activities in this app
 */
abstract public class MyBaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * @inheritDoc
     */
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

        // check if account synchronization is enabled globally. Otherwise issue a notification.
        if (!SurveyContentProvider.isMasterSyncSettingTrue()){
            buildSyncSettingChangerIntent("Turn on Auto-Sync", "Time Monitor needs auto-sync.", "Turn on now", "turn_on_master_sync",001);
        }
        // check if account synchronization is enabled for the account. Otherwise issue a notification.
        if (!SurveyContentProvider.isAccountSyncSettingTrue(this)){
            buildSyncSettingChangerIntent("Turn on App-Sync", "Sync must be activated for Time Monitor.", "Turn on now", "turn_on_app_sync",002);
        }

        // re-draw the options menu to keep the reminder checkbox updated
        supportInvalidateOptionsMenu();
        // register a listener to invalid the options menu when the reminder settings change
        this.getSharedPreferences("workload", Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.getSharedPreferences("workload", Context.MODE_PRIVATE).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Builds a notification that prompts the user to enable certain the synchronization settings.
     *
     * The notification contains a shortcut for the user to change the setting.
     *
     * @param contentTitle Title text of the notification
     * @param contentText Content text of the notification
     * @param actionText Text describing the effect of the shortcut button to the user
     * @param specifier String that specifies the action of the shortcut,
     *                  check the SyncSettingChangerService.java for reference
     * @param id Android notification id, see
     */
    private void buildSyncSettingChangerIntent(String contentTitle, String contentText, String actionText, String specifier, int id){

        // Construct an intent that starts the SyncSettingChangerService when the action button of the
        // notification is pressed. The Service will take care of applying the setting which is specified
        // in the specifier string.
        Intent intent = new Intent(this, SyncSettingChangerService.class);
        intent.setData(Uri.parse(specifier));
        // Wraps the intent in a PendingIntent
        PendingIntent actionPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_sync_problem)
                        .setContentTitle(contentTitle)
                        // make notification disappear after it is used (Should this not be a
                        // standard setting?
                        .setAutoCancel(true)
                        .setContentText(contentText)
                        // This adds the action button with the above intent which will start a service when pressed.
                        // the service will then change the settings.
                        .addAction(new NotificationCompat.Action(R.drawable.ic_settings_24dp, actionText, actionPendingIntent));
        // Fire up the notification
        NotificationManagerCompat.from(this).notify(id,mBuilder.build());
    }


    /**
     * Takes care of setting up the options menu
     * which is defined in xml.
     *
     * @inheritDoc
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        boolean reminders_on = settings.getBoolean(getString(R.string.show_reminder_notification), true);
        menu.findItem(R.id.action_reminder_toggle).setChecked(reminders_on);
        return true;
    }



    /**
     * Defines the actions when a menu item on the options menu is pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_manage_lectures:
                intent = new Intent(this, ActiveLectures.class);
                break;
            case R.id.action_privacy_agreement:
                intent = new Intent(this, PrivacyAgreement.class);
                break;
            case R.id.action_statistics:
                intent = new Intent(this, Statistics.class);
                break;
            case R.id.action_reminder_toggle:
                SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                if (item.isChecked()){
                    editor.putBoolean(getString(R.string.show_reminder_notification), false);
                    item.setChecked(false);
                    Toast.makeText(this, "Notifications off", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putBoolean(getString(R.string.show_reminder_notification), true);
                    item.setChecked(true);
                    Toast.makeText(this, "Notifications on", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
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


    /**
     * Returns false if user has agreed to privacy agreement Otherwise launches intent for
     * activity which prompts the user to agree and returns true.
     */
    protected boolean assure_privacy_agreement() {
        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        if (!settings.getBoolean("privacy_agreed", false)) {
            Intent intent = new Intent(this, PrivacyAgreement.class);
            this.startActivity(intent);
            return true;
        }
        return false;
    }



    /**
     * Returns false if user has logged in before. Otherwise launches intent for
     * login activity and returns true.
     */
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key)
    {
        if (key.equals(getString(R.string.show_reminder_notification))){
            supportInvalidateOptionsMenu();
        }
    }

}
