package com.trainapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.trainapp.R;
import com.trainapp.adapters.FriendsAdapter;
import com.trainapp.adapters.VidtrainAdapter;
import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.utilities.FacebookUtility;
import com.trainapp.utilities.FriendLoaderCallback;
import com.trainapp.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreationDetailActivity extends AppCompatActivity {

    @Bind(R.id.friendsRecyclerView) RecyclerView _friendsRecyclerView;
    @Bind(R.id.tvExistingGroupInstructions) TextView _existingGroupsInstructions;
    @Bind(R.id.groupsRecyclerView) RecyclerView _vidtrainsRecyclerView;
    @Bind(R.id.noFriendsTextView) TextView _noFriendsTextView;
    @Bind(R.id.progressBar) ProgressBar _progressBar;
    @Bind(R.id.btnSubmit) Button _submitButton;
    @Bind(R.id.parentContainer) LinearLayout _parentContainer;

    private ProgressDialog _progressDialog;
    private String _videoPath;
    private FriendsAdapter _friendsAdapter;
    private Context _context;
    private String _message;
    private VidtrainAdapter _vidtrainsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_detail);
        ButterKnife.bind(this);
        if (!Utility.isOnline()) {
            Utility.showNoInternetSnackbar(_parentContainer);
        }
        final List<User> friends = new ArrayList<>();
        _friendsAdapter = new FriendsAdapter(this, friends, true);
        _friendsRecyclerView.setAdapter(_friendsAdapter);
        _friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        _friendsRecyclerView.setHasFixedSize(true);
        FacebookUtility.getFacebookFriendsUsingApp(new FriendLoaderCallback() {
            @Override
            public void setUsers(List<User> users) {
                friends.addAll(users);
                if (friends.isEmpty()) {
                    _noFriendsTextView.setVisibility(View.VISIBLE);
                }
                _friendsAdapter.notifyDataSetChanged();
                _progressBar.setVisibility(View.GONE);
                _submitButton.setEnabled(true);
            }
        });

        final List<VidTrain> vidtrains = new ArrayList<>();
        _vidtrainsAdapter = new VidtrainAdapter(this, vidtrains);
        _vidtrainsRecyclerView.setAdapter(_vidtrainsAdapter);
        _vidtrainsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        _vidtrainsRecyclerView.setHasFixedSize(true);
        ParseQuery<VidTrain> query = VidTrain.getQuery();
        List<User> usersToMatch = new ArrayList<>();
        usersToMatch.add(User.getCurrentUser());
        query.whereContainedIn(VidTrain.COLLABORATORS, usersToMatch)
                .orderByDescending("updatedAt")
                .include(VidTrain.COLLABORATORS + "." + VidTrain.USER_KEY)
                .include(VidTrain.VIDEOS_KEY)
                .setLimit(10);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e != null) {
                    Log.e(VidtrainApplication.TAG, e.toString());
                    return;
                }
                vidtrains.addAll(objects);
                _vidtrainsAdapter.notifyDataSetChanged();
                if (!vidtrains.isEmpty()) {
                    _existingGroupsInstructions.setVisibility(View.VISIBLE);
                    _vidtrainsRecyclerView.setVisibility(View.VISIBLE);
                }

            }
        });
        _videoPath = getIntent().getStringExtra(MainActivity.VIDEO_PATH);
        _message = getIntent().getStringExtra(MainActivity.VIDEO_MESSAGE);
        _context = this;
    }

    @OnClick(R.id.btnSubmit)
    public void submitVidTrain(View view) {
        final ParseFile parseFile = Utility.createParseFile(_videoPath);
        if (parseFile == null) {
            Toast.makeText(this, R.string.video_file_fail, Toast.LENGTH_LONG).show();
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

        final List<User> collaborators = new ArrayList<>();
        // collaborators is never empty since it always contains the current user
        collaborators.add(user);
        collaborators.addAll(_friendsAdapter.getCollaborators());
        Bitmap thumbnailBitmap = Utility.getImageBitmap(_videoPath);
        final ParseFile thumbnail = Utility.createParseFileFromBitmap(thumbnailBitmap);

        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final Video video = new Video();
                video.setUser(user);
                video.setVideoFile(parseFile);
                video.setThumbnail(thumbnail);
                video.setMessage(_message);
                video.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        List<VidTrain> selectedVidtrains = _vidtrainsAdapter.getSelectedVidtrains();
                        if (!selectedVidtrains.isEmpty()) {
                            // Send to the selected vidtrains, do not create new train
                            for (final VidTrain vidtrain : selectedVidtrains) {
                                vidtrain.setVideos(vidtrain.maybeInitAndAdd(video));
                                vidtrain.saveEventually();
                                Utility.sendNotification(vidtrain, _context);
                                Unseen.addUnseen(vidtrain);
                            }
                            _progressDialog.dismiss();
                            String updatedMessage = _context.getResources().getQuantityString(
                                    R.plurals.updated_vidtrains_success,
                                    selectedVidtrains.size(),
                                    selectedVidtrains.size());
                            Toast.makeText(_context, updatedMessage, Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // Check to see if there is already a vidtrain with the same collaborators
                        ParseQuery<VidTrain> query = VidTrain.getQuery();
                        query.whereContainsAll(VidTrain.COLLABORATORS, collaborators);
                        query.whereEqualTo(VidTrain.COLLABORATOR_COUNT, collaborators.size());
                        query.getFirstInBackground(new GetCallback<VidTrain>() {
                            @Override
                            public void done(VidTrain vidtrain, ParseException e) {
                                if (e != null) {
                                    Log.d(VidtrainApplication.TAG,
                                            "Could not find existing convo: " + e.toString());
                                    final VidTrain vidTrain = new VidTrain();
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
                                    vidTrain.setCollaboratorCount(collaborators.size());
                                    vidTrain.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            user.put(User.VIDTRAINS_KEY, user.maybeInitAndAdd(vidTrain));
                                            user.put(User.VIDEOS_KEY, user.maybeInitAndAdd(video));
                                            user.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    videoSaved(vidTrain, R.string.save_success);
                                                }
                                            });
                                        }
                                    });
                                    return;
                                }
                                updateVidtrain(vidtrain, video);
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateVidtrain(final VidTrain vidtrain, Video video) {
        Log.d(VidtrainApplication.TAG, "Found existing Train: " + vidtrain.getObjectId());
        video.setVidTrain(vidtrain);
        video.saveInBackground();
        vidtrain.setVideos(vidtrain.maybeInitAndAdd(video));
        vidtrain.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                videoSaved(vidtrain, R.string.update_success);
                Utility.goVidtrainDetail(_context, vidtrain.getObjectId());
            }
        });
    }

    private void videoSaved(VidTrain vidtrain, int toastMessage) {
        Utility.sendNotification(vidtrain, _context);
        Unseen.addUnseen(vidtrain);
        _progressDialog.dismiss();
        Toast.makeText(_context, toastMessage, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onStop() {
        Utility.deleteFile(_videoPath);
        super.onStop();
    }
}
