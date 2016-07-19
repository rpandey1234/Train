package com.franklinho.vidtrain_android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.FriendsAdapter;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.utilities.FacebookUtility;
import com.franklinho.vidtrain_android.utilities.FriendLoaderCallback;

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
        FacebookUtility.getFacebookFriendsUsingApp(new FriendLoaderCallback() {
            @Override
            public void setUsers(List<User> users) {
                friends.addAll(users);
                friendsAdapter.notifyDataSetChanged();
            }
        });
    }
}
