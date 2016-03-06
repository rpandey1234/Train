package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.franklinho.vidtrain_android.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogInActivity extends AppCompatActivity {

    CallbackManager callbackManager;

    @Bind(R.id.login_button) Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            sendToHomeActivity();
//            sendToDiscoveryActivity();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void logInWithFaceBook(View view) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
                Arrays.asList("user_friends", "email", "public_profile"),
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {
                            Log.d("Vidtrain", "Uh oh. The user cancelled the Facebook login.");
                        } else {
//                            System.out.println("user found!!!");
                            sendToDiscoveryActivity();
                        }

                    }
                });
    }

    public void sendToDiscoveryActivity() {
        Intent i = new Intent(getBaseContext(), DiscoveryActivity.class);
        startActivity(i);
        Log.d("Vidtrain", "Logged in with Facebook");
        Toast.makeText(getBaseContext(), "Successfully logged in with Facebook", Toast.LENGTH_SHORT).show();
    }

    public void sendToHomeActivity() {
        Intent i = new Intent(getBaseContext(), HomeActivity.class);
        startActivity(i);
        Log.d("Vidtrain", "Logged in with Facebook");
        Toast.makeText(getBaseContext(), "Successfully logged in with Facebook", Toast.LENGTH_SHORT).show();
    }



}