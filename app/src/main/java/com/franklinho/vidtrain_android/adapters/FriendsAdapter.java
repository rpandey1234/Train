package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.FriendViewHolder;
import com.franklinho.vidtrain_android.models.User;

import java.util.List;

/**
 * Adapter which accepts the user's list of facebook friends using the app.
 */
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