package com.gmail.konstantin.schubert.workload.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.gmail.konstantin.schubert.workload.R;


public class PrivacyAgreement extends MyBaseActivity {

    //It should look like this: http://img.wonderhowto.com/img/77/04/63580517782596/0/combine-whatsapp-facebook-other-messengers-into-one-app.w654.jpg
    //If it has been agreed to before, it will not display the overlay on the bottom
    //If the user clicks on the green arrow (let's make this a swoosh) then the privacy setting will
    // be updated in the setting and on the website

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_privacy_agreement);
        if (settings.getBoolean("privacy_agreed", false)) {
            View footer = findViewById(R.id.privacy_agree_footer);
            ((ViewGroup) footer.getParent()).removeView(footer);
        } else {

            Button agreeButton = (Button) findViewById(R.id.privacy_agree_button);
            agreeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) findViewById(R.id.privacy_checkBox);
                    if (checkBox.isChecked()) {
                        SharedPreferences settings = PrivacyAgreement.this.getSharedPreferences("workload", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("privacy_agreed", true);
                        editor.commit();
                        PrivacyAgreement.this.finish();
                    } else {
                        checkBox.setBackgroundColor(Color.RED); //TODO: This is ugly
                    }
                }
            });
        }
    }
}

