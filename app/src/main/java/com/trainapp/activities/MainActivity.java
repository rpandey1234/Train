package com.trainapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.trainapp.R;
import com.trainapp.fragments.ConversationsFragment;
import com.trainapp.utilities.PermissionHelper;
import com.trainapp.utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int VIDEO_CAPTURE = 101;
    public static final String VIDEO_PATH = "VIDEO_PATH";

    @Bind(R.id.toolbar) Toolbar _toolbar;
    @Bind(R.id.conversations_fragment) FrameLayout _conversationsFragment;
    @Bind(R.id.rootLayout) CoordinatorLayout _rootLayout;

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
        } else if (id == R.id.actionFriendList) {
            startActivity(new Intent(getApplicationContext(), FriendListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab_create)
    public void startCreateFlow(View view) {
        if (PermissionHelper.allPermissionsAlreadyGranted(this)) {
            goVideoCapture();
        } else {
            PermissionHelper.requestVideoPermission(this, _rootLayout);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQUEST_VIDEO) {
            if (PermissionHelper.allPermissionsGranted(permissions, grantResults)) {
                goVideoCapture();
            }
        }
    }

    private void goVideoCapture() {
        Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
        intent.putExtra(Utility.UNIQUE_ID_INTENT, Long.toString(System.currentTimeMillis()));
        startActivityForResult(intent, Utility.VIDEO_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Utility.VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                String uid = data.getStringExtra(Utility.UNIQUE_ID_INTENT);
                Intent i = new Intent(this, CreationDetailActivity.class);
                i.putExtra(VIDEO_PATH, Utility.getOutputMediaFile(uid).getPath());
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
    }
}
