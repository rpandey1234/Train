package com.franklinho.vidtrain_android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphResponse;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.LogInActivity;
import com.parse.ParseUser;

import org.json.JSONException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/5/16.
 */
public class UserInfoFragment extends Fragment {

    @Bind(R.id.ivProfileImage) ImageView ivProfileImage;
    @Bind(R.id.tvName) TextView tvName;
    @Bind(R.id.tvTagline) TextView tvTagline;
    @Bind(R.id.tvStories) TextView tvStories;
    @Bind(R.id.tvPoints) TextView tvPoints;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        ButterKnife.bind(this, view);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Log.d("Vidtrain", currentUser.toString());
            GraphRequest graphRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "me");
            graphRequest.setCallback(new Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    System.out.println(response);
                    try {
                        // TODO: get more data from fb, properly parse it
                        tvName.setText(response.getJSONObject().getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            graphRequest.executeAsync();
        } else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            startActivity(intent);
        }
        return view;
    }

    public static UserInfoFragment newInstance() {
        UserInfoFragment userInfoFragment = new UserInfoFragment();
        Bundle args = new Bundle();
        userInfoFragment.setArguments(args);
        return userInfoFragment;
    }
}
