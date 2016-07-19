package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.FriendsAdapter.FriendViewHolder;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Adapter which accepts the user's list of facebook friends using the app.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendViewHolder> {

    private Context _context;
    private boolean _showCheckbox;
    // All friends
    private List<User> _friends;
    // Selected friends
    private List<User> _collaborators;

    public FriendsAdapter(Context context, List<User> friends, boolean showCheckbox) {
        _context = context;
        _friends = friends;
        _showCheckbox = showCheckbox;
        _collaborators = new ArrayList<>();
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.item_user, parent, false);
        return new FriendViewHolder(view, _showCheckbox);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return _friends.size();
    }

    public List<User> getCollaborators() {
        return _collaborators;
    }

    /**
     * A view holder for a row in the list of friends
     */
    public class FriendViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.friendImage) RoundedImageView _friendImage;
        @Bind(R.id.friendName) CheckedTextView _friendName;

        private Context _context;
        private int _position;

        public FriendViewHolder(View view, boolean showCheckbox) {
            super(view);
            ButterKnife.bind(this, view);
            _context = view.getContext();
            if (!showCheckbox) {
                _friendName.setCheckMarkDrawable(null);
            }
        }

        public void bind(int position) {
            _position = position;
            User user = _friends.get(_position);
            _friendName.setText(user.getName());
            Glide.with(_context).load(user.getProfileImageUrl()).placeholder(
                    R.drawable.profile_icon).into(_friendImage);
        }

        @OnClick(R.id.friendName)
        public void checkboxClicked(View view) {
            _friendName.toggle();
            User user = _friends.get(_position);
            if (_friendName.isChecked()) {
                // Add it
                _collaborators.add(user);
            } else {
                // Remove it
                int userIndex = Utility.indexOf(_collaborators, user);
                _collaborators.remove(userIndex);
            }
        }
    }
}
