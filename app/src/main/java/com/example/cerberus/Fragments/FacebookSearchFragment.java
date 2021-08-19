package com.example.cerberus.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cerberus.R;

public class FacebookSearchFragment extends Fragment {

    public FacebookSearchFragment() {}

    public static FacebookSearchFragment newInstance() {
        return new FacebookSearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_facebook_search, container, false);
    }
}