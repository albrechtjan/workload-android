package com.gmail.konstantin.schubert.workload;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class WeekButtonAdapter extends BaseAdapter {
    private Context mContext;

    public WeekButtonAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 25;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        Button weekButton;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            // TODO: Use an XML layout for the button
            weekButton = new Button(mContext);
            weekButton.setLayoutParams(new GridView.LayoutParams(85, 85));
            weekButton.setPadding(2, 2, 2, 2);
        } else {
            weekButton = (Button) convertView;
        }

        return weekButton;
    }
}
