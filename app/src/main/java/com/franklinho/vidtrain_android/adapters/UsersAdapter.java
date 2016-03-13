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
import java.util.zip.Inflater;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/11/16.
 */
public class UsersAdapter extends ArrayAdapter<ParseUser> {
    Context mContext;
    private final LayoutInflater inflater;

    public UsersAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.item_user, users);
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ParseUser user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_user, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.tvName.setText(User.getName(user));
        String profileImageUrl = User.getProfileImageUrl(user);
        Glide.with(mContext).load(profileImageUrl).into(viewHolder.ivProfileImage);
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    static class ViewHolder {
        @Bind(R.id.tvName) TextView tvName;
        @Bind(R.id.ivProfileImage) ImageView ivProfileImage;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
