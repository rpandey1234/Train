package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UsersAdapter extends ArrayAdapter<User> {
    Context _context;
    private final LayoutInflater _layoutInflater;

    public UsersAdapter(Context context, List<User> users) {
        super(context, R.layout.item_user, users);
        _context = context;
        _layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = _layoutInflater.inflate(R.layout.item_user, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder._tvName.setText(user.getName());
        String profileImageUrl = user.getProfileImageUrl();
        Glide.with(_context).load(profileImageUrl).into(viewHolder._ivProfileImage);
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    static class ViewHolder {
        @Bind(R.id.friendName) TextView _tvName;
        @Bind(R.id.friendImage) RoundedImageView _ivProfileImage;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
