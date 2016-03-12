package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by rahul on 3/11/16.
 */
public class UsersAdapter extends ArrayAdapter<ParseUser> {
    Context mContext;

    public UsersAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.item_user, users);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ParseUser user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        ImageView ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);

        // Populate the data into the template view using the data object
        tvName.setText(User.getName(user));
        String profileImageUrl = User.getProfileImageUrl(user);
        Glide.with(mContext).load(profileImageUrl).into(ivProfileImage);
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name;
    }
}
