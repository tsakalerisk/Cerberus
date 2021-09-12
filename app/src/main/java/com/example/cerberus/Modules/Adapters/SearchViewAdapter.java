package com.example.cerberus.Modules.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.cerberus.R;

/*
Adapter used for the SearchView in FeedActivity.
 */
public class SearchViewAdapter extends SimpleCursorAdapter {
    public SearchViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    /*
    If there is no number of tweets available for the suggested trend
    it hides the tweetNum TextView in trend_layout.xml, so that it
    doesn't take up any space.
     */
    @Override
    public void setViewText(TextView v, String text) {
        if (v.getId() == R.id.tweetNum && text.length() == 0)
            v.setVisibility(View.GONE);
        else {
            super.setViewText(v, text);
            v.setVisibility(View.VISIBLE);
        }

    }
}
