package com.trainapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.trainapp.R;
import com.trainapp.adapters.FriendsAdapter.FriendViewHolder;
import com.trainapp.models.User;
import com.trainapp.utilities.Utility;
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

    private final Activity _activity;
    private Context _context;
    private boolean _showCheckbox;
    // All friends
    private List<User> _friends;
    // Selected friends
    private List<User> _collaborators;
    public static final int MAX_NUM_COLLABORATORS = 6;

    public FriendsAdapter(Activity activity, List<User> friends, boolean showCheckbox) {
        _context = activity.getApplicationContext();
        _activity = activity;
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

        private int _position;

        public FriendViewHolder(View view, boolean showCheckbox) {
            super(view);
            ButterKnife.bind(this, view);
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
            // dismiss the soft keyboard
            View currentFocus = _activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) _activity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            boolean isValid = true;
            User user = _friends.get(_position);
            if (_friendName.isChecked()) {
                // Remove it
                int userIndex = Utility.indexOf(_collaborators, user);
                _collaborators.remove(userIndex);
            } else {
                // Attempt to add it
                if (_collaborators.size() < MAX_NUM_COLLABORATORS) {
                    _collaborators.add(user);
                } else {
                    Toast.makeText(
                            _context,
                            _context.getString(R.string.group_size_error, MAX_NUM_COLLABORATORS),
                            Toast.LENGTH_SHORT)
                            .show();
                    isValid = false;
                }
            }
            if (isValid) {
                _friendName.toggle();
            }
        }
    }
}
