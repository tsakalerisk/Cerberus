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

import com.example.cerberus.Modules.Adapters.InstagramPostAdapter;
import com.example.cerberus.Modules.CustomLinearLayoutManager;
import com.example.cerberus.R;
import com.example.cerberus.SearchPostsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InstagramSearchFragment extends Fragment {
    public String query;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public SwipeRefreshLayout swipeLayout;
    private FloatingActionButton fab;

    public InstagramSearchFragment(String query) {
        this.query = query;
    }

    public static InstagramSearchFragment newInstance(String query) {
        return new InstagramSearchFragment(query);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instagram_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeLayout = view.findViewById(R.id.swipeLayout);
        fab = view.findViewById(R.id.fab);

        InstagramPostAdapter instagramPostAdapter = new InstagramPostAdapter(getActivity());
        recyclerView.setAdapter(instagramPostAdapter);
        instagramPostAdapter.getSearchResults(InstagramSearchFragment.this, query);
        SearchPostsActivity searchPostsActivity = (SearchPostsActivity) getActivity();
        recyclerView.setLayoutManager(new CustomLinearLayoutManager(searchPostsActivity));

        swipeLayout.setOnRefreshListener(() -> instagramPostAdapter.getSearchResults(InstagramSearchFragment.this, query));
        fab.setOnClickListener(v -> recyclerView.scrollToPosition(0));

    }
}