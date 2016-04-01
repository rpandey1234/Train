package com.franklinho.vidtrain_android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogInActivity extends AppCompatActivity {

    CallbackManager callbackManager;

    @Bind(R.id.login_button) Button loginButton;
    @Bind(R.id.pbProgessAction) View pbProgessAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            if (User.getName(currentUser) == null) {
                updateUserInfo(currentUser);
            }
            sendToHomeActivity();
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(parent, name, context, attrs);


        return view;

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

    public void logInWithFaceBook(View view) {
        if (ParseUser.getCurrentUser() != null) {
            Log.d("Vidtrain", ParseUser.getCurrentUser().toString());
        }
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
                Arrays.asList("user_friends", "email", "public_profile"),
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {
                            Log.d(VidtrainApplication.TAG, "User cancelled the Facebook login.");
                            Toast.makeText(LogInActivity.this, "Failed to log into Facebook", Toast.LENGTH_SHORT).show();
                        } else {
                            showProgressBar();
                            updateUserInfo(user);
                            sendToHomeActivity();
                        }
                    }
                });
    }

    public void sendToHomeActivity() {
        Intent i = new Intent(getBaseContext(), HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
        Log.d(VidtrainApplication.TAG, "Logged in with Facebook");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showProgressBar() {
        // Show progress item
        pbProgessAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        pbProgessAction.setVisibility(View.GONE);
    }
}
