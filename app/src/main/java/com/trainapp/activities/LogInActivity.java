package com.trainapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.trainapp.R;
import com.trainapp.models.User;
import com.trainapp.networking.VidtrainApplication;

import org.json.JSONObject;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

public class LogInActivity extends AppCompatActivity {

    @Bind(R.id.login_button) Button _loginButton;
    @Bind(R.id.progressBar) View _progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);
        User currentUser = User.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getName() == null) {
                updateUserInfo(currentUser);
            }
            goMainActivity();
        }
    }

    private void updateUserInfo(final ParseUser user) {
        if (!ParseFacebookUtils.isLinked(user)) {
            return;
        }
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        User.updateFacebookData(user, response);
                    }
                });
        Bundle parameters = new Bundle();

        parameters.putString("fields", "id,name,link,location,email,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.login_button)
    public void logInWithFaceBook(View view) {
        if (ParseUser.getCurrentUser() != null) {
            Log.d(VidtrainApplication.TAG, ParseUser.getCurrentUser().toString());
        }
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
                Arrays.asList("user_friends", "email", "public_profile"),
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {
                            Log.d(VidtrainApplication.TAG, "User cancelled the Facebook login.");
                            Toast.makeText(LogInActivity.this, R.string.fb_login_fail,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Save user to this installation (device)
                            ParseInstallation install = ParseInstallation.getCurrentInstallation();
                            install.put("user", user.getObjectId());
                            install.saveInBackground();
                            showProgressBar();
                            updateUserInfo(user);
                            goMainActivity();
                        }
                    }
                });
    }

    private void goMainActivity() {
        logUser();
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void logUser() {
        User currentUser = User.getCurrentUser();
        assert currentUser != null;
        Crashlytics.setUserName(currentUser.getName());
    }

    public void showProgressBar() {
        // Show progress item
        _progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        _progressBar.setVisibility(View.GONE);
    }
}
