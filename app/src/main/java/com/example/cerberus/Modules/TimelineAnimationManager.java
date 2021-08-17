package com.example.cerberus.Modules;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TimelineAnimationManager {
    private static boolean isAtTop = true;
    private static boolean movedThisScroll = false;
    private static boolean isExpanded = false;
    private static AnimatorSet animatorSet;
    private static FloatingActionButton scrollUpButton = null;

    public static void init(FeedActivity feedActivity, RecyclerView timelineRecyclerView) {
        ConstraintLayout layout = feedActivity.findViewById(R.id.timelineLayout);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
        LinearLayoutManager layoutManager = (LinearLayoutManager) timelineRecyclerView.getLayoutManager();

        animatorSet = new AnimatorSet();
        ValueAnimator leftMarginAnimation = ValueAnimator.ofInt(layoutParams.leftMargin, 0);
        leftMarginAnimation.addUpdateListener(animation -> {
            layoutParams.leftMargin = (Integer) animation.getAnimatedValue();
            layout.setLayoutParams(layoutParams);
        });
        ValueAnimator rightMarginAnimation = ValueAnimator.ofInt(layoutParams.rightMargin, 0);
        rightMarginAnimation.addUpdateListener(animation -> {
            layoutParams.rightMargin = (Integer) animation.getAnimatedValue();
            layout.setLayoutParams(layoutParams);
        });

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ValueAnimator topMarginAnimation = ValueAnimator.ofInt(layoutParams.topMargin, (int) -layout.getY());
                topMarginAnimation.addUpdateListener(animation -> {
                    layoutParams.topMargin = (Integer) animation.getAnimatedValue();
                    layout.setLayoutParams(layoutParams);
                });
                animatorSet.play(topMarginAnimation).with(leftMarginAnimation).with(rightMarginAnimation);
                topMarginAnimation.setDuration(500);
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        timelineRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                        isAtTop = true;
                        if (!movedThisScroll) {
                            tryCollapse();
                        }
                    }
                    movedThisScroll = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAtTop && dy > 0 && !movedThisScroll) {
                    isAtTop = false;
                    movedThisScroll = true;
                    if (!animatorSet.isRunning() && !isExpanded) {
                        tryExpand();
                    }
                }
                else if (!isAtTop && dy < 0 && !movedThisScroll) {
                    if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                        isAtTop = true;
                        movedThisScroll = true;
                        tryCollapse();
                    }
                }
            }
        });

        scrollUpButton = feedActivity.findViewById(R.id.scrollUpButton);
        scrollUpButton.setOnClickListener(v -> {
            timelineRecyclerView.scrollToPosition(0);
            isAtTop = true;
            tryCollapse();
        });
    }

    private static void tryExpand() {
        if (!animatorSet.isRunning() && !isExpanded) {
            animatorSet.start();
            isExpanded = true;
            scrollUpButton.setVisibility(View.VISIBLE);
        }
    }

    private static void tryCollapse() {
        if (!animatorSet.isRunning() && isExpanded) {
            animatorSet.reverse();
            isExpanded = false;
            scrollUpButton.setVisibility(View.GONE);
        }
    }
}
