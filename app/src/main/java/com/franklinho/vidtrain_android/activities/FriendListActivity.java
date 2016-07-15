package com.franklinho.vidtrain_android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the list of friends using the app.
 */
public class FriendListActivity extends AppCompatActivity {

    @Bind(R.id.friendListTitle) TextView _friendsTitle;
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
        final FriendsAdapter friendsAdapter = new FriendsAdapter(this, friends);
        _friendsRecyclerView.setAdapter(friendsAdapter);
        _friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        List<String> facebookFriends = Utility.getFacebookFriends(response, "id");
                        ParseQuery<User> userQuery = ParseQuery.getQuery("_User");
                        userQuery.whereContainedIn("fbid", facebookFriends)
                                .findInBackground(new FindCallback<User>() {
                                    public void done(List<User> objects, ParseException e) {
                                        if (e == null) {
                                            friends.addAll(objects);
                                            friendsAdapter.notifyDataSetChanged();
                                        } else {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        friendsAdapter.notifyDataSetChanged();
                    }
                }
        ).executeAsync();
        friendsAdapter.notifyDataSetChanged();
    }

    public class FriendsAdapter extends RecyclerView.Adapter<FriendViewHolder> {

        private final List<User> _friends;
        private Context _context;

        public FriendsAdapter(Context context, List<User> friends) {
            _context = context;
            _friends = friends;
        }

        @Override
        public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(_context).inflate(R.layout.item_user, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FriendViewHolder holder, int position) {
            User user = _friends.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return _friends.size();
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.ivProfileImage) RoundedImageView _friendImage;
        @Bind(R.id.tvName) TextView _friendName;

        public FriendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(User user) {
            _friendName.setText(user.getName());
            Glide.with(getApplicationContext()).load(user.getProfileImageUrl()).placeholder(
                    R.drawable.profile_icon).into(_friendImage);
        }
    }
}