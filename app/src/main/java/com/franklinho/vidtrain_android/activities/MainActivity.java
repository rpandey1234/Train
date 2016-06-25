package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.fragments.ConversationsFragment;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int VIDEO_CAPTURE = 101;
    public static final String UNIQUE_ID_INTENT = "UNIQUE_ID";
    public static final String SHOW_CONFIRM = "SHOW_CONFIRM";

    @Bind(R.id.toolbar) Toolbar _toolbar;
    @Bind(R.id.conversations_fragment) FrameLayout _conversationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.conversations_fragment, ConversationsFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab_create)
    public void startCreateFlow(View view) {
        Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
        intent.putExtra(UNIQUE_ID_INTENT, Long.toString(System.currentTimeMillis()));
        intent.putExtra(SHOW_CONFIRM, false);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            Log.d(VidtrainApplication.TAG, "intent data is null");
            Toast.makeText(this, "Intent data is null.",  Toast.LENGTH_LONG).show();
            return;
        }
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                String uid = data.getStringExtra(UNIQUE_ID_INTENT);
                Intent i = new Intent(this, CreationDetailActivity.class);
                i.putExtra("videoPath", Utility.getOutputMediaFile(uid).getPath());
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
    }
}
