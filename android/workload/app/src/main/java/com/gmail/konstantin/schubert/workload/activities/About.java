package com.gmail.konstantin.schubert.workload.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.gmail.konstantin.schubert.workload.R;

public class About extends MyBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        TextView feedback = (TextView) findViewById(R.id.text_email);
        feedback.setText(Html.fromHtml(getString(R.string.about_email)));
        feedback.setMovementMethod(LinkMovementMethod.getInstance());

        TextView about = (TextView) findViewById(R.id.text_about);
        about.setText(Html.fromHtml(getString(R.string.about)));
        about.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
