package com.example.cerberus.Modules;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.cerberus.R;

public class SearchViewCursorAdapter extends SimpleCursorAdapter {
    public SearchViewCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

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
