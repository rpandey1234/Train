package com.trainapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.trainapp.R;
import com.trainapp.adapters.FriendsAdapter;
import com.trainapp.models.User;
import com.trainapp.utilities.FacebookUtility;
import com.trainapp.utilities.FriendLoaderCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the list of friends using the app.
 */
public class FriendListActivity extends AppCompatActivity {

    @Bind(R.id.friendsRecyclerView) RecyclerView _friendsRecyclerView;
    @Bind(R.id.toolbar) Toolbar _toolbar;
    @Bind(R.id.progressBar) ProgressBar _progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final List<User> friends = new ArrayList<>();
        final FriendsAdapter friendsAdapter = new FriendsAdapter(this, friends, false);
        _friendsRecyclerView.setAdapter(friendsAdapter);
        _friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        _progressBar.setVisibility(View.VISIBLE);
        FacebookUtility.getFacebookFriendsUsingApp(new FriendLoaderCallback() {
            @Override
            public void setUsers(List<User> users) {
                friends.addAll(users);
                friendsAdapter.notifyDataSetChanged();
                _progressBar.setVisibility(View.GONE);
            }
        });
    }
}
