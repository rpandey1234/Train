package com.trainapp.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.trainapp.R;
import com.trainapp.adapters.FriendsAdapter;
import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.utilities.FacebookUtility;
import com.trainapp.utilities.FriendLoaderCallback;
import com.trainapp.utilities.Utility;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreationDetailActivity extends AppCompatActivity {
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
        _videoPath = getIntent().getStringExtra(MainActivity.VIDEO_PATH);
    }

    @OnClick(R.id.btnSubmit)
    public void submitVidTrain(View view) {
        final ParseFile parseFile = Utility.createParseFile(_videoPath);
        if (parseFile == null) {
            Toast.makeText(this, R.string.video_file_fail, Toast.LENGTH_LONG).show();
            return;
        }
        final String titleText = _etTitle.getText().toString();
        if (titleText.isEmpty()) {
            Toast.makeText(this, R.string.add_title, Toast.LENGTH_LONG).show();
            return;
        }
        final User user = User.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.sign_in_to_create, Toast.LENGTH_LONG).show();
            return;
        }
        _progressDialog = ProgressDialog.show(this,
                getResources().getString(R.string.saving),
                getResources().getString(R.string.working_message),
                true);
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
                        ParseACL userAcl = new ParseACL();
                        for (User user : collaborators) {
                            userAcl.setReadAccess(user.getObjectId(), true);
                            userAcl.setWriteAccess(user.getObjectId(), true);
                        }
                        vidTrain.setACL(userAcl);
                        vidTrain.setVideos(videos);
                        vidTrain.setCollaborators(collaborators);
                        vidTrain.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                video.setVidTrain(vidTrain);
                                video.saveInBackground();
                                Unseen.addUnseen(vidTrain);
                                user.put(User.VIDTRAINS_KEY, user.maybeInitAndAdd(vidTrain));
                                user.put(User.VIDEOS_KEY, user.maybeInitAndAdd(video));
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Utility.sendNotification(vidTrain, getBaseContext());
                                        _progressDialog.dismiss();
                                        Toast.makeText(getBaseContext(),
                                                R.string.save_success, Toast.LENGTH_SHORT)
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

    @Override
    protected void onStop() {
        Utility.deleteFile(_videoPath);
        super.onStop();
    }
}
