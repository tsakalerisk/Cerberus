package com.example.cerberus;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cerberus.Fragments.FacebookSearchFragment;
import com.example.cerberus.Modules.NetworkCallback;
import com.example.cerberus.Modules.SearchManager;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class SearchPostsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TabItem facebookTab;
    private TabItem twitterTab;
    private TabItem instagramTab;
    private ViewPager viewPager;

    private ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;

    private static final String TAG = "TAG";

    public String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_posts);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findsAllViews();

        query = getIntent().getStringExtra(SearchManager.QUERY_LITERAL);
        setUpToolbar();
        setUpTabs();

        checkForNetwork();
    }

    private class SearchPagerAdapter extends FragmentPagerAdapter {
        public SearchPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = FacebookSearchFragment.newInstance();
                    break;
                case 1:
                    fragment = TwitterSearchFragment.newInstance(query);
                    break;
                case 2:
                    fragment = FacebookSearchFragment.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }


    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(query);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setUpTabs() {
        setIndicatorColor(tabLayout.getSelectedTabPosition());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setIndicatorColor(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.setAdapter(new SearchPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        View facebookTab = LayoutInflater.from(SearchPostsActivity.this).inflate(R.layout.facebook_tab, null, false);
        tabLayout.getTabAt(0).setCustomView(facebookTab);

        View twitterTab = LayoutInflater.from(SearchPostsActivity.this).inflate(R.layout.twitter_tab, null, false);
        tabLayout.getTabAt(1).setCustomView(twitterTab);

        View instagramTab = LayoutInflater.from(SearchPostsActivity.this).inflate(R.layout.instagram_tab, null, false);
        tabLayout.getTabAt(2).setCustomView(instagramTab);

        tabLayout.getTabAt(1).select();
    }

    private void findsAllViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabs);
        facebookTab = findViewById(R.id.fbTab);
        twitterTab = findViewById(R.id.twTab);
        instagramTab = findViewById(R.id.instaTab);
        viewPager = findViewById(R.id.container);
    }

    private void setIndicatorColor(int position) {
        switch (position) {
            case 0:
                tabLayout.setSelectedTabIndicatorColor(
                        getResources().getColor(R.color.com_facebook_blue, getTheme()));
                break;
            case 1:
                tabLayout.setSelectedTabIndicatorColor(
                        getResources().getColor(R.color.tw__blue_default, getTheme()));
                break;
            case 2:
                tabLayout.setSelectedTabIndicatorColor(
                        getResources().getColor(R.color.instagram_pink, getTheme()));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkCallback = new NetworkCallback(this);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void checkForNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra(NetworkCallback.ACTIVITY_FROM, getClass());
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        }
    }
}