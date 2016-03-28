package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.UsersAdapter;
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreationDetailActivity extends AppCompatActivity {
    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
    @Bind(R.id.vvThumbnail) ImageView vvThumbnail;
    @Bind(R.id.btnSubmit) Button btnSubmit;
    @Bind(R.id.etTitle) EditText etTitle;
    @Bind(R.id.toggleBtn) SwitchCompat toggleBtn;
    @Bind(R.id.etCollaborators) AutoCompleteTextView etCollaborators;
    @Bind(R.id.containerCollab) LinearLayout containerCollab;

    ProgressDialog progress;
    String videoPath;
    List<ParseUser> collaborators;
    List<ParseUser> usersFromAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_detail);
        ButterKnife.bind(this);
        videoPath = getIntent().getExtras().getString("videoPath");
        collaborators = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            collaborators.add(currentUser);
        }
        toggleBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCollaborators.setVisibility(View.VISIBLE);
                    containerCollab.setVisibility(View.VISIBLE);
                } else {
                    etCollaborators.setVisibility(View.GONE);
                    containerCollab.setVisibility(View.GONE);
                }
            }
        });

        vvPreview.setHeightRatio(1);
        if (videoPath != null) {
            vvThumbnail.setImageBitmap(Utility.getImageBitmap(videoPath));
            vvThumbnail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    vvThumbnail.setVisibility(View.GONE);
                    VideoPlayer.playVideo(vvPreview, videoPath);
                }
            });
            vvPreview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoPlayer.playVideo(vvPreview, videoPath);
                }
            });
        }

        etCollaborators.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereMatches("name", "^" + s.toString(), "i");
                query.setLimit(4);
                query.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> objects, ParseException e) {
                        if (e == null) {
                            usersFromAutocomplete = objects;
                            // Create the adapter and set it to the AutoCompleteTextView
                            ArrayAdapter<ParseUser> adapter = new UsersAdapter(getApplicationContext(), usersFromAutocomplete);
                            etCollaborators.setAdapter(adapter);
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

        etCollaborators.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                ParseUser user = usersFromAutocomplete.get(position);

                // Clear the text field
                etCollaborators.clearListSelection();
                etCollaborators.setText("");
                if (Utility.contains(collaborators, user)) {
                    Toast.makeText(getApplicationContext(), "You already added this user!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add this user in the UI
                View profileImage = getLayoutInflater().inflate(R.layout.profile_image, null);
                RoundedImageView ivProfileCollaborator = (RoundedImageView) profileImage
                        .findViewById(R.id.ivProfileCollaborator);
                Glide.with(getApplicationContext()).load(User.getProfileImageUrl(user)).placeholder(
                        R.drawable.profile_icon).into(
                        ivProfileCollaborator);
                containerCollab.addView(profileImage);

                // Add the user to the collaborators ArrayList
                collaborators.add(user);
            }
        });
    }

    public void submitVidTrain(View view) {
        if (etTitle.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        progress = ProgressDialog.show(this, "Saving", "Just a moment please!", true);
        final Video video = new Video();
        final VidTrain vidTrain = new VidTrain();

        final ParseFile parseFile = Utility.createParseFile(videoPath);
        if (parseFile == null) {
            return;
        }
        Bitmap thumbnailBitmap = Utility.getImageBitmap(videoPath);
        final ParseFile parseThumbnail = Utility.createParseFileFromBitmap(thumbnailBitmap);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final ParseUser user = ParseUser.getCurrentUser();
                video.setUser(user);
                video.setVideoFile(parseFile);
                video.setThumbnail(parseThumbnail);
                video.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        vidTrain.setTitle(etTitle.getText().toString());
                        vidTrain.setUser(user);
                        ArrayList<Video> videos = new ArrayList<>();
                        videos.add(video);
                        vidTrain.setVideos(videos);
                        if (toggleBtn.isChecked() && !collaborators.isEmpty()) {
                            vidTrain.setWritePrivacy(true);
                            vidTrain.setCollaborators(collaborators);
                        } else {
                            vidTrain.setWritePrivacy(false);
                        }
                        vidTrain.setReadPrivacy(false);
                        vidTrain.setLatestVideo(parseFile);
                        LocationManager lm = (LocationManager) getSystemService(
                                Context.LOCATION_SERVICE);
                        Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        vidTrain.setLL(new ParseGeoPoint(lc.getLatitude(), lc.getLongitude()));
                        vidTrain.setRankingValue(((float) System.currentTimeMillis() / (float) 1000 - (float) 1134028003) / (float) 45000 + 1);

                        vidTrain.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                video.setVidTrain(vidTrain);
                                video.saveInBackground();
                                user.put("vidtrains", User.maybeInitAndAdd(user, vidTrain));
                                user.put("videos", User.maybeInitAndAdd(user, video));
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        successfullySavedVidtrain();

                                        for (ParseUser collaborator : collaborators) {
                                            sendCollaboratorNotification(collaborator, vidTrain);
                                        };

                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void successfullySavedVidtrain() {
        progress.dismiss();
        Toast.makeText(getBaseContext(), "Successfully saved Vidtrain!", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void sendCollaboratorNotification(ParseUser user, VidTrain vidtrain) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", user.getObjectId());
        String currentUserName = ParseUser.getCurrentUser().getString("name");
        String alertString = currentUserName + " has added you to the vidtrain: " + vidtrain.getTitle();
        JSONObject data = new JSONObject();

        try {
            data.put("alert", alertString);
            data.put("title", "VidTrain");
            data.put("vidTrain", vidtrain.getObjectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();

    }
}
