package com.example.cerberus.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cerberus.Modules.Adapters.TweetAdapter;
import com.example.cerberus.Modules.CustomLinearLayoutManager;
import com.example.cerberus.R;
import com.example.cerberus.SearchPostsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TwitterSearchFragment extends Fragment {
    public String query;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public SwipeRefreshLayout swipeLayout;
    private FloatingActionButton fab;

    public TwitterSearchFragment(String query) {
        this.query = query;
    }

    public static TwitterSearchFragment newInstance(String query) {
        return new TwitterSearchFragment(query);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_twitter_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeLayout = view.findViewById(R.id.swipeLayout);
        fab = view.findViewById(R.id.fab);

        TweetAdapter tweetAdapter = new TweetAdapter();
        recyclerView.setAdapter(tweetAdapter);
        tweetAdapter.getSearchResults(TwitterSearchFragment.this, query);
        SearchPostsActivity searchPostsActivity = (SearchPostsActivity) getActivity();
        recyclerView.setLayoutManager(new CustomLinearLayoutManager(searchPostsActivity));

        swipeLayout.setOnRefreshListener(() -> tweetAdapter.getSearchResults(TwitterSearchFragment.this, query));
        fab.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
    }
}