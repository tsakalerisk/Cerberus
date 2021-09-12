package com.example.cerberus.Modules;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

/*
LinearLayoutManager with the ability to pause scrolling (to type a reply)
 */
public class CustomLinearLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
