package com.franklinho.vidtrain_android.actitivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void logInWithFaceBook(View view) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, null, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user == null) {
                    Log.d("Vidtrain", "Uh oh. The user cancelled the Facebook login.");
                } else {
                    Intent i = new Intent(getBaseContext(), DiscoveryActivity.class);
                    startActivity(i);
                    Log.d("Vidtrain", "Logged in with Facebook");
                    Toast.makeText(getBaseContext(), "Successfully logged in with Facebook", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
