package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.FriendsAdapter;
import com.franklinho.vidtrain_android.models.DynamicVideoView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.FacebookUtility;
import com.franklinho.vidtrain_android.utilities.FriendLoaderCallback;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreationDetailActivity extends AppCompatActivity {
    @Bind(R.id.vvPreview) DynamicVideoView _vvPreview;
    @Bind(R.id.vvThumbnail) ImageView _vvThumbnail;
    @Bind(R.id.btnSubmit) Button _btnSubmit;
    @Bind(R.id.etTitle) EditText _etTitle;
    @Bind(R.id.friendsRecyclerView) RecyclerView _friendsRecyclerView;

    private ProgressDialog _progressDialog;
    private String _videoPath;
    private FriendsAdapter _friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_detail);
        ButterKnife.bind(this);
        final List<User> friends = new ArrayList<>();
        _friendsAdapter = new FriendsAdapter(this, friends, true);
        _friendsRecyclerView.setAdapter(_friendsAdapter);
        _friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        _friendsRecyclerView.setHasFixedSize(true);
        FacebookUtility.getFacebookFriendsUsingApp(new FriendLoaderCallback() {
            @Override
            public void setUsers(List<User> users) {
                friends.addAll(users);
                _friendsAdapter.notifyDataSetChanged();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            _videoPath = null;
        } else {
            _videoPath = extras.getString("videoPath");
        }

        _vvPreview.setHeightRatio(1);
        if (_videoPath != null) {
            _vvThumbnail.setImageBitmap(Utility.getImageBitmap(_videoPath));
            _vvThumbnail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    _vvThumbnail.setVisibility(View.GONE);
                    _vvPreview.setVideoPath(_videoPath);
                    _vvPreview.start();
                }
            });
            _vvPreview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    _vvPreview.setVideoPath(_videoPath);
                    _vvPreview.start();
                }
            });
        }
    }

    @OnClick(R.id.btnSubmit)
    public void submitVidTrain(View view) {
        final ParseFile parseFile = Utility.createParseFile(_videoPath);
        if (parseFile == null) {
            Toast.makeText(this, "Unable to create a video file.", Toast.LENGTH_LONG).show();
            return;
        }
        final String titleText = _etTitle.getText().toString();
        if (titleText.isEmpty()) {
            Toast.makeText(this, "Please add a title.", Toast.LENGTH_LONG).show();
            return;
        }
        final User user = User.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sign in to create a Vidtrain.", Toast.LENGTH_LONG).show();
            return;
        }
        _progressDialog = ProgressDialog.show(this, "Saving", "Just a moment please!", true);
        final Video video = new Video();
        final VidTrain vidTrain = new VidTrain();

        final List<User> collaborators = new ArrayList<>();
        // collaborators is never empty since it always contains the current user
        collaborators.add(user);
        collaborators.addAll(_friendsAdapter.getCollaborators());

        Bitmap thumbnailBitmap = Utility.getImageBitmap(_videoPath);
        final ParseFile parseThumbnail = Utility.createParseFileFromBitmap(thumbnailBitmap);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                video.setUser(user);
                video.setVideoFile(parseFile);
                video.setThumbnail(parseThumbnail);
                video.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        vidTrain.setTitle(titleText);
                        vidTrain.setUser(user);
                        ArrayList<Video> videos = new ArrayList<>();
                        videos.add(video);
                        vidTrain.setVideos(videos);
                        vidTrain.setCollaborators(collaborators);
                        vidTrain.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                video.setVidTrain(vidTrain);
                                video.saveInBackground();
                                user.put("vidtrains", user.maybeInitAndAdd(vidTrain));
                                user.put("videos", user.maybeInitAndAdd(video));
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Utility.sendNotifications(vidTrain);
                                        _progressDialog.dismiss();
                                        Toast.makeText(getBaseContext(),
                                                "Successfully saved Vidtrain!", Toast.LENGTH_SHORT)
                                                .show();
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
