package com.example.cerberus.Modules.CustomViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.core.content.ContextCompat;

import com.example.cerberus.R;

/*
This class customises ToggleButtons and is used in FeedActivity
to select where to publish each post.
 */
public class PostToggleButton extends AppCompatToggleButton {
    private final int postToggleColor;

    @SuppressLint("ResourceType")
    public PostToggleButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int[] set = {
                R.attr.postToggleColor, //index 0
                android.R.attr.text     //index 1
        };
        final TypedArray a = context.obtainStyledAttributes(attrs, set);
        postToggleColor = a.getColor(0, 0);
        String text = a.getString(1);
        setTextOff(null);
        setTextOn(null);
        setText(text);
        a.recycle();

        setBackgroundTintMode(PorterDuff.Mode.ADD);
        setUncheckedProperties(context);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (checked)
            setCheckedProperties(getContext());
        else
            setUncheckedProperties(getContext());
    }

    private void setUncheckedProperties(Context context) {
        setBackground(ContextCompat.getDrawable(context, R.drawable.button_stroke));
        getBackground().setTint(postToggleColor);
        setTextColor(postToggleColor);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.uncheck, 0);
        getCompoundDrawables()[2].setTint(postToggleColor);
    }

    private void setCheckedProperties(Context context) {
        setBackground(ContextCompat.getDrawable(context, R.drawable.button_fill));
        getBackground().setTint(postToggleColor);
        setTextColor(ContextCompat.getColor(context, R.color.white));
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
        getCompoundDrawables()[2].setTint(ContextCompat.getColor(context, R.color.white));
    }
}
