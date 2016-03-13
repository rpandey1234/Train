package com.franklinho.vidtrain_android.activities;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.io.Files;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.UsersAdapter;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreationDetailActivity extends AppCompatActivity {
    @Bind(R.id.vvPreview) DynamicHeightVideoPlayerManagerView vvPreview;
    @Bind(R.id.btnSubmit) Button btnSubmit;
    @Bind(R.id.etTitle) EditText etTitle;
    @Bind(R.id.toggleBtn) Switch toggleBtn;
    @Bind(R.id.etCollaborators) AutoCompleteTextView etCollaborators;
    @Bind(R.id.tvCollaboratorsAdded) TextView tvCollaboratorsAdded;

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
        toggleBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCollaborators.setVisibility(View.VISIBLE);
                    tvCollaboratorsAdded.setVisibility(View.VISIBLE);
                } else {
                    etCollaborators.setVisibility(View.GONE);
                    tvCollaboratorsAdded.setVisibility(View.GONE);
                }
            }
        });

        vvPreview.setHeightRatio(1);
        if (videoPath != null) {
            vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                @Override
                public void onVideoCompletionMainThread() {
                    vvPreview.start();
                }
            });
            VidtrainApplication.getVideoPlayer().playNewVideo(null, vvPreview, videoPath);
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
                // TODO: should not allow duplicate names to be added
                ParseUser parseUser = usersFromAutocomplete.get(position);
                // 1. Add this user to the textview
                String name = User.getName(parseUser);
                String currentText = tvCollaboratorsAdded.getText().toString();
                if (currentText.isEmpty()) {
                    tvCollaboratorsAdded.setText(name);
                } else {
                    tvCollaboratorsAdded.setText(currentText + ", " + name);
                }

                // 2. Clear the text field
                etCollaborators.clearListSelection();
                etCollaborators.setText("");

                // 3. Add the user to the collaborators AL
                collaborators.add(parseUser);
            }
        });
    }

    public void submitVidTrain(View view) {
        File file = new File(videoPath);
        final Video video = new Video();
        final VidTrain vidTrain = new VidTrain();

        byte[] data;
        try {
            data = Files.toByteArray(file);
            final ParseFile parseFile = new ParseFile("video.mp4", data);

            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    video.setUser(ParseUser.getCurrentUser());
                    video.setVideoFile(parseFile);
                    video.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            vidTrain.setTitle(etTitle.getText().toString());
                            vidTrain.setUser(ParseUser.getCurrentUser());
                            ArrayList<Video> videos = new ArrayList<>();
                            videos.add(video);
                            vidTrain.setVideos(videos);
                            if (toggleBtn.isChecked() && !collaborators.isEmpty()) {
                                vidTrain.setWritePrivacy(true);
                                vidTrain.setCollaborators(collaborators);
                            }
                            vidTrain.setReadPrivacy(false);
                            vidTrain.setThumbnailFile(parseFile);

                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();

                            vidTrain.setLL(new ParseGeoPoint(latitude, longitude));


                            vidTrain.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    video.setVidTrain(vidTrain);
                                    video.saveInBackground();

                                    ArrayList<VidTrain> vidTrains;
                                    if (ParseUser.getCurrentUser().get("vidtrains") == null) {
                                        vidTrains = new ArrayList<>();

                                    } else {
                                        vidTrains = (ArrayList<VidTrain>) ParseUser.getCurrentUser().get("vidtrains");
                                    }
                                    vidTrains.add(vidTrain);
                                    ParseUser.getCurrentUser().put("vidtrains", vidTrains);


                                    ArrayList<Video> videos;
                                    if (ParseUser.getCurrentUser().get("videos") == null) {
                                        videos = new ArrayList<>();
                                    } else {
                                        videos = (ArrayList<Video>) ParseUser.getCurrentUser().get("vidtrains");
                                    }
                                    videos.add(video);
                                    ParseUser.getCurrentUser().put("videos", vidTrains);

                                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            successfullySavedVidTrain();
                                        }
                                    });


                                }
                            });

                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void successfullySavedVidTrain() {
        Toast.makeText(getBaseContext(), "Successfully saved vidtrain", Toast.LENGTH_SHORT).show();
        this.finish();
    }
}
