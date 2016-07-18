package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.UsersAdapter;
import com.franklinho.vidtrain_android.models.DynamicVideoView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreationDetailActivity extends AppCompatActivity {
    @Bind(R.id.vvPreview) DynamicVideoView _vvPreview;
    @Bind(R.id.vvThumbnail) ImageView _vvThumbnail;
    @Bind(R.id.btnSubmit) Button _btnSubmit;
    @Bind(R.id.etTitle) EditText _etTitle;
    @Bind(R.id.etCollaborators) AutoCompleteTextView _etCollaborators;
    @Bind(R.id.containerCollab) LinearLayout _containerCollab;

    ProgressDialog _progressDialog;
    String _videoPath;
    List<String> _friendsUsingApp;
    List<User> _collaborators;
    List<User> _usersFromAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_detail);
        ButterKnife.bind(this);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        _friendsUsingApp = Utility.getFacebookFriends(response, "name");
                    }
                }
        ).executeAsync();
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            _videoPath = null;
        } else {
            _videoPath = extras.getString("videoPath");
        }
        _collaborators = new ArrayList<>();
        final User currentUser = User.getCurrentUser();
        if (currentUser != null) {
            _collaborators.add(currentUser);
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

        _etCollaborators.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (_friendsUsingApp == null) {
                    // Add message asking user to manually invite friends
                    return;
                }
                String query = s.toString();
                List<String> candidateUsers = Utility.getCandidateUsers(_friendsUsingApp, query);
                ParseQuery<User> userQuery = ParseQuery.getQuery("_User");
                userQuery.whereContainedIn("name", candidateUsers)
                        .setLimit(4)
                        .findInBackground(new FindCallback<User>() {
                            public void done(List<User> objects, ParseException e) {
                                if (e == null) {
                                    _usersFromAutocomplete = objects;
                                    // Create the adapter and set it to the AutoCompleteTextView
                                    ArrayAdapter<User> adapter = new UsersAdapter(
                                            getApplicationContext(), _usersFromAutocomplete);
                                    _etCollaborators.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        _etCollaborators.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                User user = _usersFromAutocomplete.get(position);

                // Clear the text field
                _etCollaborators.clearListSelection();
                _etCollaborators.setText("");
                if (Utility.indexOf(_collaborators, user) == -1) {
                    Toast.makeText(getApplicationContext(), "You already added this user!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add this user in the UI
                View profileImage = getLayoutInflater().inflate(R.layout.profile_image, null);
                RoundedImageView ivProfileCollaborator = (RoundedImageView) profileImage
                        .findViewById(R.id.ivProfileCollaborator);
                Glide.with(getApplicationContext()).load(user.getProfileImageUrl()).placeholder(
                        R.drawable.profile_icon).into(
                        ivProfileCollaborator);
                _containerCollab.addView(profileImage);

                // Add the user to the collaborators ArrayList
                _collaborators.add(user);
            }
        });
    }

    public void submitVidTrain(View view) {
        final ParseFile parseFile = Utility.createParseFile(_videoPath);
        if (parseFile == null) {
            Toast.makeText(this, "Was unable to create file for video.",  Toast.LENGTH_LONG).show();
            return;
        }
        _progressDialog = ProgressDialog.show(this, "Saving", "Just a moment please!", true);
        final Video video = new Video();
        final VidTrain vidTrain = new VidTrain();

        Bitmap thumbnailBitmap = Utility.getImageBitmap(_videoPath);
        final ParseFile parseThumbnail = Utility.createParseFileFromBitmap(thumbnailBitmap);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final User user = User.getCurrentUser();
                video.setUser(user);
                video.setVideoFile(parseFile);
                video.setThumbnail(parseThumbnail);
                video.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        vidTrain.setTitle(_etTitle.getText().toString());
                        vidTrain.setUser(user);
                        ArrayList<Video> videos = new ArrayList<>();
                        videos.add(video);
                        vidTrain.setVideos(videos);
                        if (!_collaborators.isEmpty()) {
                            vidTrain.setCollaborators(_collaborators);
                        }
                        vidTrain.setThumbnail(parseFile);
                        vidTrain.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                video.setVidTrain(vidTrain);
                                video.saveInBackground();
                                assert user != null;
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
