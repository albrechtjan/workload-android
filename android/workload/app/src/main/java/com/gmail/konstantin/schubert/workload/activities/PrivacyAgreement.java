package com.gmail.konstantin.schubert.workload.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.gmail.konstantin.schubert.workload.R;


public class PrivacyAgreement extends MyBaseActivity {

    //It should look like this: http://img.wonderhowto.com/img/77/04/63580517782596/0/combine-whatsapp-facebook-other-messengers-into-one-app.w654.jpg
    //If it has been agreed to before, it will not display the overlay on the bottom
    //If the user clicks on the green arrow (let's make this a swoosh) then the privacy setting will
    // be updated in the setting and on the website

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_agreement);


    }

    private void agree_privacy(){

        // tell the website via api/privacyAgree that the privacy has been agreed on


        SharedPreferences settings = this.getSharedPreferences("workload", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("privacy_agreed", true); //<- do this last to make an overwrite less likely.
    }

}

