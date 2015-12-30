package com.gmail.konstantin.schubert.workload.activities;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.konstantin.schubert.workload.R;

public class MyBaseListActivity extends ListActivity{


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO: Remove duplicate code with MyBaseActivity

        Intent intent = null;
        switch(item.getItemId()){
            case R.id.action_manage_lectures:
                intent = new Intent(this, ManageLectures.class);
                break;
            case R.id.action_statistics:
                intent = new Intent(this, Statistics.class);
                break;
            case R.id.action_privacy_agreement:
                intent = new Intent(this, PrivacyAgreement.class);
                break;
            case R.id.action_settings:
                intent = new Intent(this, Settings.class);
                break;
            //TODO: Implement logout. This doesn't need an extra activity, right?
        }
        if (intent==null){
            return false;
        }else {
            startActivity(intent);
            return true;
        }
    }

    //TODO: Remove duplicate code with MyBaseActivity
    protected void assure_privacy_agreement() {
        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        if (settings.getBoolean("privacy_agreed", false)) {
            Intent intent = new Intent(this, PrivacyAgreement.class);
            this.startActivity(intent);
        }
    }
}
