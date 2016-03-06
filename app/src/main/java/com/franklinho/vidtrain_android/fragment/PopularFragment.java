package com.franklinho.vidtrain_android.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;

/**
 * Created by rahul on 3/5/16.
 */
public class PopularFragment extends Fragment {

    public PopularFragment() {
        // Required empty public constructor
    }

    public static PopularFragment newInstance() {
        PopularFragment fragment = new PopularFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_popular, container, false);
    }
}
