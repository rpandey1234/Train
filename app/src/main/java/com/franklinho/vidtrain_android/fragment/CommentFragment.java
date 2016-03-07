package com.franklinho.vidtrain_android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.CommentArrayAdapter;
import com.franklinho.vidtrain_android.models.Comment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/5/16.
 */
public class CommentFragment extends Fragment {
    @Bind(R.id.rvComments)
    public RecyclerView rvComments;
    List<Comment> comments;
    CommentArrayAdapter aComments;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance() {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Create arraylist datasource
        comments = new ArrayList<>();
        //Construct the adapter
        for (int i = 0;  i < 5; i++) {
            comments.add(new Comment());
        }
        aComments = new CommentArrayAdapter(comments, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvComments.setAdapter(aComments);
        rvComments.setLayoutManager(linearLayoutManager);





        return v;
    }
}
