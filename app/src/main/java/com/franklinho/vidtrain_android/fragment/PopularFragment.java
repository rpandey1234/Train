package com.franklinho.vidtrain_android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VidTrainArrayAdapter;
import com.franklinho.vidtrain_android.models.VidTrain;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/5/16.
 */
public class PopularFragment extends Fragment {
    @Bind(R.id.rvVidTrains)
    public RecyclerView rvVidTrains;
    List<VidTrain> vidTrains;
    VidTrainArrayAdapter aVidTrains;

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
        //Create arraylist datasource
        vidTrains = new ArrayList<>();
        //Construct the adapter
        for (int i = 0;  i < 5; i++) {
            vidTrains.add(new VidTrain());
        }
        aVidTrains = new VidTrainArrayAdapter(vidTrains, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_popular, container, false);
        ButterKnife.bind(this, v);
        vidTrains = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvVidTrains.setAdapter(aVidTrains);
        rvVidTrains.setLayoutManager(linearLayoutManager);





        return v;
    }
}
